package com.simplifybiz.ops.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.simplifybiz.ops.util.htmlToAnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.data.comments.Comment
import com.simplifybiz.ops.data.tasks.Task
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.util.formatDateForDisplay
import com.simplifybiz.ops.util.formatDateTimeForDisplay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navigator: AppNavigator, taskId: Int) {
    val viewModel: TaskDetailViewModel = koinInject()
    val session: com.simplifybiz.ops.data.SessionManager = koinInject()
    val currentUserId = session.getUserId()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(taskId) { viewModel.load(taskId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task") },
                navigationIcon = {
                    IconButton(onClick = { navigator.goto(Route.TasksHome) }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshComments() }) {
                        if (state.commentsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                state.loading -> CenteredLoader()
                state.task != null -> {
                    com.simplifybiz.ops.presentation.components.GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TaskBody(state.task!!)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    CommentsSection(
                        comments = state.comments,
                        loading = state.commentsLoading,
                        error = state.commentsError,
                        draft = state.draft,
                        posting = state.posting,
                        postError = state.postError,
                        currentUserId = currentUserId,
                        onDraftChange = viewModel::setDraft,
                        onPost = viewModel::post
                    )
                }
                state.error != null -> ErrorCard(state.error!!)
            }
        }
    }
}

@Composable
private fun CenteredLoader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun TaskBody(task: Task) {
    SectionLabel("Project")
    Text(task.project.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(12.dp))

    SectionLabel("Task")
    Text(task.task.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    if (task.description.isNotBlank()) {
        SectionLabel("Description")
        Text(htmlToAnnotatedString(task.description), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
    }

    if (task.expectedOutcomes.isNotBlank()) {
        SectionLabel("Expected outcomes")
        Text(task.expectedOutcomes, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
    }

    SectionLabel("Stage")
    Spacer(Modifier.height(4.dp))
    com.simplifybiz.ops.presentation.components.StageBadge(task.stage.ifBlank { "New" })
    Spacer(Modifier.height(8.dp))

    SectionLabel("Status")
    Text(task.status.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))

    SectionLabel("Priority")
    Text(task.priority.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))

    SectionLabel("Due")
    Text(formatDateForDisplay(task.dateDue).ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))

    if (task.assignTo.isNotBlank()) {
        SectionLabel("Assigned to")
        Text(task.assignTo, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
    }

    SectionLabel("Submitted by")
    Text(task.submitterEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(2.dp))
    if (task.organization.isNotBlank()) {
        Text(task.organization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CommentsSection(
    comments: List<Comment>,
    loading: Boolean,
    error: String?,
    draft: String,
    posting: Boolean,
    postError: String?,
    currentUserId: Int,
    onDraftChange: (String) -> Unit,
    onPost: () -> Unit
) {
    Text(
        "Notes",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(12.dp))

    when {
        loading && comments.isEmpty() -> CenteredLoader()
        error != null && comments.isEmpty() -> ErrorCard(error)
        comments.isEmpty() -> Text(
            "No notes yet. Start a conversation below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Column(modifier = Modifier.fillMaxWidth()) {
            comments.forEach { c ->
                val isMine = currentUserId != 0 && c.authorId == currentUserId
                val time = formatDateTimeForDisplay(c.createdAt).ifBlank { formatDateForDisplay(c.date) }
                com.simplifybiz.ops.presentation.components.ChatBubble(
                    body = c.note,
                    timeLabel = if (isMine) time else listOfNotNull(
                        c.authorName.takeIf { it.isNotBlank() }, time.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    isOutgoing = isMine,
                    authorName = c.authorName.ifBlank { "Unknown" }
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChange,
        placeholder = { Text("Write a note") },
        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
        minLines = 2,
        maxLines = 6,
        trailingIcon = {
            IconButton(
                onClick = onPost,
                enabled = !posting && draft.isNotBlank()
            ) {
                if (posting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Send, contentDescription = "Send")
                }
            }
        }
    )

    if (postError != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            postError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
