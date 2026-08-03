package com.surestep.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surestep.app.ui.MainViewModel
import com.surestep.app.ui.SureStepApp
import com.surestep.app.ui.theme.SureStepTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single activity. [FragmentActivity] rather than ComponentActivity because
 * BiometricPrompt needs a fragment host.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SureStepTheme(
                themeMode = state.settings.themeMode,
                highContrast = state.settings.highContrast,
            ) {
                SureStepApp(
                    state = state,
                    activity = this,
                    onUnlock = viewModel::unlock,
                    verifyPin = viewModel::verifyPin,
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Re-lock as soon as the app is no longer visible.
        viewModel.lock()
    }
}
