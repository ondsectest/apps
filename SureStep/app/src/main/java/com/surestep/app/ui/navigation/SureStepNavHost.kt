package com.surestep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.surestep.app.camera.CaptureScreen
import com.surestep.app.ui.calendar.CalendarScreen
import com.surestep.app.ui.calendar.DayDetailScreen
import com.surestep.app.ui.history.HistoryScreen
import com.surestep.app.ui.history.LogDetailScreen
import com.surestep.app.ui.home.HomeScreen
import com.surestep.app.ui.settings.RemindersScreen
import com.surestep.app.ui.settings.SettingsScreen
import com.surestep.app.ui.tasks.ManageTasksScreen
import java.time.LocalDate

@Composable
fun SureStepNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenCapture = {
                    navController.navigate(Routes.CAPTURE) { launchSingleTop = true }
                },
                onManageTasks = { navController.navigate(Routes.MANAGE_TASKS) },
                onOpenLog = { logId -> navController.navigate(Routes.logDetail(logId)) },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onOpenLog = { logId -> navController.navigate(Routes.logDetail(logId)) },
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                onOpenDay = { date -> navController.navigate(Routes.dayDetail(date)) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onManageTasks = { navController.navigate(Routes.MANAGE_TASKS) },
                onManageReminders = { navController.navigate(Routes.REMINDERS) },
            )
        }

        composable(Routes.CAPTURE) {
            CaptureScreen(onFinished = { navController.popBackStack() })
        }

        composable(Routes.MANAGE_TASKS) {
            ManageTasksScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.REMINDERS) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.LOG_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_LOG_ID) { type = NavType.LongType }),
        ) {
            LogDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_DATE) { type = NavType.StringType }),
        ) { entry ->
            val date = entry.arguments?.getString(Routes.ARG_DATE)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now()
            DayDetailScreen(
                date = date,
                onBack = { navController.popBackStack() },
                onOpenLog = { logId -> navController.navigate(Routes.logDetail(logId)) },
            )
        }
    }
}
