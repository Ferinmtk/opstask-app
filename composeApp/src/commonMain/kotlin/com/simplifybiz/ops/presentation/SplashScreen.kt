package com.simplifybiz.ops.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.SessionManager
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun SplashScreen(navigator: AppNavigator) {
    val session: SessionManager = koinInject()

    LaunchedEffect(Unit) {
        delay(600)
        // If we have a session but it's older than 6 days, force re-login.
        // The JWT plugin issues 7-day tokens; refreshing on day 6 avoids a
        // confusing mid-action 403.
        if (session.isLoggedIn() && session.tokenIsStale()) {
            session.clear()
        }
        navigator.goto(if (session.isLoggedIn()) Route.TasksHome else Route.Login)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Simplify Ops", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
