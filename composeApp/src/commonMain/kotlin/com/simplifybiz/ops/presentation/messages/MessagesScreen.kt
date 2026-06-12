package com.simplifybiz.ops.presentation.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.messages.Message
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.components.BackIcon
import com.simplifybiz.ops.presentation.components.OpsTopBar
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.util.formatDateTimeForDisplay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navigator: AppNavigator) {
    val viewModel: MessagesViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }
    // Mark all as seen once the messages have loaded
    LaunchedEffect(state.messages) {
        if (state.messages.isNotEmpty()) viewModel.markAllSeen()
    }

    Scaffold(
        topBar = {
            OpsTopBar(
                title = { Text("Messages") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        if (state.refreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navigator, current = Route.Messages) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> CenteredLoader()
                state.error != null && state.messages.isEmpty() -> ErrorCard(state.error!!)
                state.messages.isEmpty() -> EmptyHint()
                else -> {
                    // Collapse to one row per task: newest note is the preview,
                    // with a count when the thread has more than one note.
                    val threads = state.messages
                        .groupBy { it.taskId }
                        .map { (_, notes) ->
                            val latest = notes.maxByOrNull { it.id } ?: notes.last()
                            MessageThread(
                                latest = latest,
                                count = notes.size,
                                hasUnread = notes.any { it.id > state.lastSeenId }
                            )
                        }
                        .sortedByDescending { it.latest.id }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(threads, key = { it.latest.taskId }) { thread ->
                            MessageRow(
                                thread.latest,
                                count = thread.count,
                                isUnread = thread.hasUnread,
                                onClick = { navigator.goto(Route.TaskDetail(thread.latest.taskId)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class MessageThread(
    val latest: Message,
    val count: Int,
    val hasUnread: Boolean
)

@Composable
private fun MessageRow(msg: Message, count: Int, isUnread: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = if (isUnread)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .padding(end = 8.dp)
                ) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Box(modifier = Modifier.size(10.dp))
                    }
                }
                Spacer(Modifier.size(8.dp))
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        msg.taskTitle.ifBlank { "Task #${msg.taskId}" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = true)
                    )
                    Text(
                        formatDateTimeForDisplay(msg.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(msg.authorName.ifBlank { "Unknown" })
                        if (count > 1) append("  ·  $count notes")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    msg.note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CenteredLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorCard(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyHint() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No messages yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Notes posted on your tasks will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
