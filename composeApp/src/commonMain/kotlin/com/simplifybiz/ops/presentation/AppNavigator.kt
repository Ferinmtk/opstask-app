package com.simplifybiz.ops.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Route {
    object Splash : Route()
    object Login : Route()
    object TasksHome : Route()
    object ViewTasks : Route()
    object Inbox : Route()
    object Status : Route()
    object SubmitTask : Route()
    data class TaskDetail(val taskId: Int) : Route()
    object Messages : Route()
    object Profile : Route()
}

class AppNavigator {
    var current by mutableStateOf<Route>(Route.Splash)
        private set

    fun goto(route: Route) {
        current = route
    }
}
