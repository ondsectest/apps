package com.surestep.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.surestep.app.ui.lock.LockScreen
import com.surestep.app.ui.navigation.Routes
import com.surestep.app.ui.navigation.SureStepNavHost
import com.surestep.app.ui.navigation.TopLevelDestination

@Composable
fun SureStepApp(
    state: MainUiState,
    activity: FragmentActivity,
    onUnlock: () -> Unit,
    verifyPin: suspend (String) -> Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            !state.ready -> Unit // First frame only; settings resolve immediately after.

            // Nothing behind the lock screen is composed, so records are never
            // rendered — not even for a frame — before the PIN is accepted.
            state.showLockScreen -> LockScreen(
                biometricEnabled = state.settings.biometricEnabled,
                activity = activity,
                verifyPin = verifyPin,
                onUnlocked = onUnlock,
            )

            else -> MainScaffold()
        }
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevel = TopLevelDestination.entries
    val showBottomBar = topLevel.any { it.route == currentRoute }

    // From any top-level tab other than Home, back returns to Home rather than
    // closing the app.
    BackHandler(enabled = showBottomBar && currentRoute != Routes.HOME) {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        // Screens handle their own status-bar insets so the camera route can run
        // truly edge to edge; the scaffold only contributes the bottom bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                NavigationBar {
                    topLevel.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SureStepNavHost(navController = navController)
        }
    }
}
