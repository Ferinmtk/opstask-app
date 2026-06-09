package com.simplifybiz.ops.presentation

import androidx.compose.runtime.Composable
import com.simplifybiz.ops.presentation.login.LoginScreen
import com.simplifybiz.ops.presentation.messages.MessagesScreen
import com.simplifybiz.ops.presentation.profile.ProfileScreen
import com.simplifybiz.ops.presentation.tasks.SubmitTaskScreen
import com.simplifybiz.ops.presentation.tasks.TaskDetailScreen
import com.simplifybiz.ops.presentation.tasks.ViewTasksScreen
import com.simplifybiz.ops.presentation.tasks.dashboard.InboxScreen
import com.simplifybiz.ops.presentation.tasks.dashboard.StatusScreen
import com.simplifybiz.ops.presentation.tasks.dashboard.TasksHomeScreen

@Composable
fun AppNavHost(navigator: AppNavigator) {
    when (val route = navigator.current) {
        Route.Splash -> SplashScreen(navigator)
        Route.Login -> LoginScreen(navigator)
        Route.TasksHome -> TasksHomeScreen(navigator)
        Route.ViewTasks -> ViewTasksScreen(navigator)
        Route.Inbox -> InboxScreen(navigator)
        Route.Status -> StatusScreen(navigator)
        Route.SubmitTask -> SubmitTaskScreen(navigator)
        is Route.TaskDetail -> TaskDetailScreen(navigator, route.taskId)
        Route.Messages -> MessagesScreen(navigator)
        Route.Profile -> ProfileScreen(navigator)
    }
}
