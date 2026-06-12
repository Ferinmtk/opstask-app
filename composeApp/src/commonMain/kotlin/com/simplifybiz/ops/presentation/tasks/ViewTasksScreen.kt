package com.simplifybiz.ops.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.cache.SyncState
import com.simplifybiz.ops.data.tasks.CachedTask
import com.simplifybiz.ops.data.tasks.TaskStages
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.components.BackIcon
import com.simplifybiz.ops.presentation.components.OpsTopBar
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.presentation.components.GlassCard
import com.simplifybiz.ops.presentation.components.PriorityDot
import com.simplifybiz.ops.presentation.components.StageBadge
import com.simplifybiz.ops.presentation.theme.OpsBrand
import com.simplifybiz.ops.util.formatDateForDisplay
import org.koin.compose.koinInject

private enum class TaskFilter(val label: String, val match: (String) -> Boolean) {
    ALL("All", { true }),
    TO_DO("To do", { it == TaskStages.TO_DO || it == TaskStages.ACCEPT || it == TaskStages.WAITING || it.isBlank() }),
    DOING("Doing", { it == TaskStages.DOING }),
    APPROVE("Approve", { it == TaskStages.APPROVE || it == TaskStages.FINAL_APPROVAL }),
    DONE("Done", { it == TaskStages.DONE })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTasksScreen(navigator: AppNavigator) {
    val viewModel: TasksListViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    var filter by remember { mutableStateOf(TaskFilter.ALL) }

    LaunchedEffect(Unit) { viewModel.load() }
    DisposableEffect(Unit) { onDispose { viewModel.stopPolling() } }

    val filtered = state.tasks.filter { filter.match(it.task.stage) }

    Scaffold(
        topBar = {
            OpsTopBar(
                title = {
                    Column {
                        Text("View tasks", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${filter.label}  ·  ${filtered.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goto(Route.TasksHome) }) {
                        BackIcon()
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        if (state.loading || state.refreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navigator, current = Route.ViewTasks) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.goto(Route.SubmitTask) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "New task")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                TaskFilter.entries.forEach { f ->
                    val count = state.tasks.count { f.match(it.task.stage) }
                    FilterPill(
                        label = "${f.label}  $count",
                        selected = filter == f,
                        onClick = { filter = f }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.loading && filtered.isEmpty() ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    state.error != null && filtered.isEmpty() -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error ?: "", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Try again") }
                    }
                    filtered.isEmpty() -> Text(
                        if (filter == TaskFilter.ALL) "No tasks yet" else "No ${filter.label.lowercase()} tasks",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { cached ->
                            TaskCard(cached, onRetry = { cached.localId?.let { viewModel.retry(it) } }) {
                                navigator.goto(Route.TaskDetail(cached.task.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    if (selected) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(OpsBrand.actionGradient)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Color(0xFFF4F5F7))
                .border(1.dp, OpsBrand.glassBorder, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun TaskCard(cached: CachedTask, onRetry: () -> Unit, onClick: () -> Unit) {
    val task = cached.task
    GlassCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    task.task,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(10.dp))
                StageBadge(task.stage.ifBlank { task.status })
            }
            if (task.project.isNotBlank() || task.dateDue.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.priority.isNotBlank()) {
                        PriorityDot(task.priority)
                        Spacer(Modifier.width(7.dp))
                    }
                    Text(
                        listOfNotNull(
                            task.project.takeIf { it.isNotBlank() },
                            task.dateDue.takeIf { it.isNotBlank() }?.let { "Due ${formatDateForDisplay(it)}" }
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (cached.syncState != SyncState.SYNCED) {
                Spacer(Modifier.height(6.dp))
                SyncStateChip(cached.syncState, onRetry)
            }
        }
    }
}

@Composable
private fun SyncStateChip(syncState: SyncState, onRetry: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (syncState) {
            SyncState.PENDING -> Text("Queued for sync", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SyncState.SENDING -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.dp)
                Spacer(Modifier.width(4.dp))
                Text("Syncing...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SyncState.FAILED -> Row(
                modifier = Modifier.clickable(onClick = onRetry),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sync failed tap to retry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            SyncState.SYNCED -> {}
        }
    }
}
