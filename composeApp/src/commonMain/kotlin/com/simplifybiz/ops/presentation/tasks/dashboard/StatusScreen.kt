package com.simplifybiz.ops.presentation.tasks.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.presentation.components.GlassCard
import com.simplifybiz.ops.presentation.tasks.TasksListViewModel
import org.koin.compose.koinInject

// Readable accents on white, consistent with the stage-badge palette.
private val TodoAccent    = Color(0xFF2563EB)  // blue
private val DoingAccent   = Color(0xFFD97706)  // amber
private val ApproveAccent = Color(0xFF059669)  // emerald
private val DoneAccent    = Color(0xFF6B7280)  // gray
private val PriorityRed    = Color(0xFFDC2626)  // high-priority emphasis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(navigator: AppNavigator) {
    val viewModel: TasksListViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val tasksList = state.tasks.map { it.task }
    val counts = tasksList.groupingBy { it.stage }.eachCount()
    val toDo = counts.filterKeys { it.contains("To do", true) }.values.sum()
    val doing = counts.filterKeys { it.contains("Doing", true) }.values.sum()
    val approve = counts.filterKeys { it.contains("Approve", true) }.values.sum()
    val done = counts.filterKeys { it.contains("Done", true) }.values.sum()
    val total = tasksList.size
    val highPriority = tasksList.count { it.priority.contains("High", true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status") },
                navigationIcon = {
                    IconButton(onClick = { navigator.goto(Route.TasksHome) }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navigator, current = Route.Status) }
    ) { padding ->
        if (state.loading && tasksList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    "Workflow summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                // Hero: active sprint metric with red accent bar
                GlassCard {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(Modifier.width(4.dp).fillMaxHeight().background(PriorityRed))
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                            Text(
                                "ACTIVE SPRINT",
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "$total Tasks",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "• $highPriority high priority",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PriorityRed
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                StatusRow("To do", toDo, total, TodoAccent)
                Spacer(Modifier.height(12.dp))
                StatusRow("Doing", doing, total, DoingAccent)
                Spacer(Modifier.height(12.dp))
                StatusRow("Approve", approve, total, ApproveAccent)
                Spacer(Modifier.height(12.dp))
                StatusRow("Done", done, total, DoneAccent)
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, count: Int, total: Int, accent: Color) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    val dim = if (count == 0) 0.45f else 1f
    GlassCard(modifier = Modifier.alpha(dim)) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label.uppercase(),
                modifier = Modifier.width(76.dp),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 1.sp,
                color = accent
            )
            // Progress track + proportional fill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(accent.copy(alpha = 0.14f), RoundedCornerShape(3.dp))
            ) {
                if (pct > 0f) {
                    Box(
                        Modifier.fillMaxWidth(pct.coerceIn(0f, 1f))
                            .height(6.dp)
                            .background(accent, RoundedCornerShape(3.dp))
                    )
                }
            }
            Text(
                count.toString(),
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.End,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                color = accent
            )
        }
    }
}
