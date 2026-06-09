package com.simplifybiz.ops.presentation.tasks.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.tasks.CachedTask
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.presentation.tasks.TasksListViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(navigator: AppNavigator) {
    val viewModel: TasksListViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val inboxTasks = state.tasks.filter {
        it.task.stage.contains("To do", true) || it.task.stage.contains("Approve", true)
    }.sortedBy { it.task.dateDue }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inbox", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${inboxTasks.size} need your action",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goto(Route.TasksHome) }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navigator, current = Route.Inbox) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading && inboxTasks.isEmpty() -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null && inboxTasks.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load() }) { Text("Try again") }
                }
                inboxTasks.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("All caught up", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nothing waiting on you",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(inboxTasks) { cached ->
                        TaskRow(cached) { navigator.goto(Route.TaskDetail(cached.task.id)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(cached: CachedTask, onClick: () -> Unit) {
    val task = cached.task
    val isHigh = task.priority.contains("High", true)
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row {
            if (isHigh) Box(modifier = Modifier.width(3.dp).height(72.dp).background(Color(0xFFA32D2D)))
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(task.task, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    com.simplifybiz.ops.presentation.components.StageBadge(task.stage)
                }
                Spacer(Modifier.height(4.dp))
                if (task.project.isNotBlank() || task.dateDue.isNotBlank()) {
                    Text(
                        listOfNotNull(
                            task.project.takeIf { it.isNotBlank() },
                            task.dateDue.takeIf { it.isNotBlank() }?.let { "Due $it" }
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
