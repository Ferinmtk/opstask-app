package com.simplifybiz.ops.presentation.tasks.dashboard

import com.simplifybiz.ops.data.ApiConstants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.SessionManager
import com.simplifybiz.ops.data.tasks.CachedTask
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.components.BackIcon
import com.simplifybiz.ops.presentation.components.OpsTopBar
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.presentation.components.GlassCard
import com.simplifybiz.ops.presentation.components.PriorityDot
import com.simplifybiz.ops.presentation.components.StageBadge
import com.simplifybiz.ops.presentation.tasks.TasksListViewModel
import org.koin.compose.koinInject

// Diagonal (135deg) gradients for the tile icon squares — matches the prototype.
private fun diag(a: Color, b: Color) =
    Brush.linearGradient(listOf(a, b), start = Offset(0f, 0f), end = Offset.Infinite)

private val GradAdd    = diag(Color(0xFF0A84FF), Color(0xFF3CA0FF))
private val GradInbox  = diag(Color(0xFFFF9F0A), Color(0xFFFFB340))
private val GradStatus = diag(Color(0xFF30D1A2), Color(0xFF34C759))
private val GradView   = diag(Color(0xFF7863FF), Color(0xFF9A8CFF))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksHomeScreen(navigator: AppNavigator) {
    val viewModel: TasksListViewModel = koinInject()
    val session: SessionManager = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val tasksList = state.tasks.map { it.task }
    val counts = tasksList.groupingBy { it.stage }.eachCount()
    val total = tasksList.size
    val acceptCount = counts.filterKeys { it.contains("Accept", true) }.values.sum()
    val doingCount = counts.filterKeys { it.contains("Doing", true) }.values.sum()

    val upNext = state.tasks
        .filter { !it.task.stage.contains("Done", true) }
        .sortedBy { it.task.dateDue }
        .take(3)

    Scaffold(
        topBar = {
            OpsTopBar(title = {
                Column {
                    Text("Tasks", style = MaterialTheme.typography.titleMedium)
                    val subtitle = ApiConstants.OPS_BASE_URL
                        ?.removePrefix("https://")
                        ?.removePrefix("http://") ?: ""
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            })
        },
        bottomBar = { BottomNavBar(navigator, current = Route.TasksHome) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 2x2 tile grid
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Tile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Add, gradient = GradAdd,
                    title = "Add a task", subtitle = "Submit a new task",
                    onClick = { navigator.goto(Route.SubmitTask) }
                )
                Tile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Email, gradient = GradInbox,
                    title = "Inbox",
                    subtitle = if (acceptCount > 0) "$acceptCount awaiting accept" else "Nothing pending",
                    onClick = { navigator.goto(Route.Inbox) }
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Tile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart, gradient = GradStatus,
                    title = "Status",
                    subtitle = if (doingCount > 0) "$doingCount in progress" else "Workflow summary",
                    onClick = { navigator.goto(Route.Status) }
                )
                Tile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.List, gradient = GradView,
                    title = "View tasks",
                    subtitle = if (total > 0) "$total total" else "No tasks yet",
                    onClick = { navigator.goto(Route.ViewTasks) }
                )
            }

            Spacer(Modifier.height(22.dp))
            Text(
                "UP NEXT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            when {
                state.loading && upNext.isEmpty() -> HintText("Loading…")
                upNext.isEmpty() -> HintText("All caught up")
                else -> GlassCard {
                    upNext.forEachIndexed { index, cached ->
                        UpNextRow(cached) { navigator.goto(Route.TaskDetail(cached.task.id)) }
                        if (index < upNext.lastIndex) {
                            HorizontalDivider(color = Color(0x12111827))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tile(
    modifier: Modifier,
    icon: ImageVector,
    gradient: Brush,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier.fillMaxHeight().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpNextRow(cached: CachedTask, onClick: () -> Unit) {
    val task = cached.task
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
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
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PriorityDot(task.priority)
            Spacer(Modifier.width(7.dp))
            val meta = listOfNotNull(
                task.project.takeIf { it.isNotBlank() },
                task.dateDue.takeIf { it.isNotBlank() }?.let { "Due $it" }
            ).joinToString("  ·  ")
            Text(
                meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HintText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
