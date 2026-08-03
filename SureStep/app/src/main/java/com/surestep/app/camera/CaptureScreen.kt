package com.surestep.app.camera

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private enum class CaptureStatus { Starting, CountingDown, Capturing, Failed }

private const val CAMERA_INIT_TIMEOUT_MS = 12_000L

/**
 * Front camera, countdown, one automatic frame, then straight back to the
 * checklist. There is deliberately no shutter button — the user already pressed
 * their button, and asking them to press another would turn one confirmation
 * into two.
 *
 * On the shutter sound: CameraX exposes no API to silence it, and several
 * jurisdictions mandate the tone at the OS level. Where a device allows a silent
 * capture it stays silent on its own; where it does not, the system tone is left
 * alone rather than worked around.
 */
@Composable
fun CaptureScreen(
    onFinished: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val finish by rememberUpdatedState(onFinished)

    var status by remember { mutableStateOf(CaptureStatus.Starting) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    val previewUseCase = remember { Preview.Builder().build() }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val boundProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Backing out is allowed, and still writes the record — without the photo.
    BackHandler { viewModel.onPhotoSkipped(finish) }

    LaunchedEffect(state.ready, state.nothingToCapture) {
        if (state.nothingToCapture) {
            // Nothing pending — e.g. the screen was restored after process death.
            finish()
            return@LaunchedEffect
        }
        if (!state.ready) return@LaunchedEffect

        // CameraX initialisation is not instant — on some devices it retries for
        // several seconds before settling. The timeout means a camera that never
        // comes up costs the user a few seconds, not their record.
        val provider = withTimeoutOrNull(CAMERA_INIT_TIMEOUT_MS) {
            runCatching {
                context.awaitCameraProvider().also { provider ->
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        provider.preferredSelector(),
                        previewUseCase,
                        imageCapture,
                    )
                }
            }.getOrNull()
        }

        if (provider == null) {
            status = CaptureStatus.Failed
            delay(900) // Let the message be readable before the screen closes.
            viewModel.onPhotoSkipped(finish)
            return@LaunchedEffect
        }
        boundProvider.value = provider

        status = CaptureStatus.CountingDown
        for (tick in state.countdownSeconds downTo 1) {
            secondsLeft = tick
            delay(1_000)
        }
        secondsLeft = 0
        status = CaptureStatus.Capturing

        val target = viewModel.targetFile()
        if (target == null) {
            viewModel.onPhotoSkipped(finish)
            return@LaunchedEffect
        }

        val saved = runCatching { imageCapture.captureTo(context, target) }.getOrDefault(false)
        if (saved) viewModel.onPhotoCaptured(finish) else viewModel.onPhotoSkipped(finish)
    }

    DisposableEffect(Unit) {
        onDispose { boundProvider.value?.unbindAll() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewUseCase.setSurfaceProvider(surfaceProvider)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // A light scrim keeps the countdown legible against any scene.
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f)),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.taskTitle?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Saving your record",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (status) {
                CaptureStatus.CountingDown -> CountdownRing(secondsLeft)

                CaptureStatus.Capturing -> Text(
                    text = "Capturing…",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )

                CaptureStatus.Failed -> Text(
                    text = "Camera unavailable — saving the record without a photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )

                // Never leave a blank screen: camera start-up can take several
                // seconds, and silence there reads as the app having frozen.
                CaptureStatus.Starting -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Starting the camera…",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }

        TextButton(
            onClick = { viewModel.onPhotoSkipped(finish) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
        ) {
            Text("Skip the photo", color = Color.White)
        }
    }
}

@Composable
private fun CountdownRing(secondsLeft: Int) {
    val scale by animateFloatAsState(
        targetValue = if (secondsLeft > 0) 1f else 0.6f,
        label = "countdown",
    )
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        modifier = Modifier
            .size(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            // Announced to screen readers as the count changes, since the
            // number itself is purely visual feedback.
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Assertive
                contentDescription = "Photo in $secondsLeft"
            },
        ) {
            Text(
                text = secondsLeft.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
        }
    }
}

// --- CameraX plumbing ------------------------------------------------------

/**
 * The front camera is what this flow is designed around, but not every device
 * has one — and some report one that CameraX then cannot select. Falling back to
 * the rear lens keeps a photo on the record instead of silently dropping it.
 */
private fun ProcessCameraProvider.preferredSelector(): CameraSelector {
    val front = CameraSelector.DEFAULT_FRONT_CAMERA
    val back = CameraSelector.DEFAULT_BACK_CAMERA
    return when {
        runCatching { hasCamera(front) }.getOrDefault(false) -> front
        runCatching { hasCamera(back) }.getOrDefault(false) -> back
        else -> CameraSelector.Builder().build()
    }
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { provider -> if (continuation.isActive) continuation.resume(provider) }
                    .onFailure { error -> if (continuation.isActive) continuation.resumeWithException(error) }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

private suspend fun ImageCapture.captureTo(context: Context, target: File): Boolean =
    suspendCancellableCoroutine { continuation ->
        val options = ImageCapture.OutputFileOptions.Builder(target).build()
        takePicture(
            options,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onError(exception: ImageCaptureException) {
                    if (continuation.isActive) continuation.resume(false)
                }
            },
        )
    }
