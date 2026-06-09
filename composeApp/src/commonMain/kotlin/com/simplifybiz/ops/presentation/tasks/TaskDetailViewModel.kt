package com.simplifybiz.ops.presentation.tasks

import com.simplifybiz.ops.data.ApiErrorMessages
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.comments.Comment
import com.simplifybiz.ops.data.comments.CommentRepository
import com.simplifybiz.ops.data.tasks.Task
import com.simplifybiz.ops.data.tasks.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskDetailState(
    val loading: Boolean = false,
    val task: Task? = null,
    val error: String? = null,

    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = false,
    val commentsError: String? = null,

    val draft: String = "",
    val posting: Boolean = false,
    val postError: String? = null
)

class TaskDetailViewModel(
    private val taskRepo: TaskRepository,
    private val commentRepo: CommentRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    private var currentTaskId: Int = 0

    fun load(taskId: Int) {
        currentTaskId = taskId

        val cached = taskRepo.tasks.value.firstOrNull { it.task.id == taskId }?.task
        _state.value = _state.value.copy(loading = cached == null, task = cached, error = null)

        scope.launch {
            val taskResult = taskRepo.get(taskId)
            if (taskResult.isSuccess) {
                _state.value = _state.value.copy(loading = false, task = taskResult.getOrThrow())
            } else if (cached == null) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = taskResult.exceptionOrNull()?.message
                )
            } else {
                _state.value = _state.value.copy(loading = false)
            }

            refreshComments()
        }
    }

    fun refreshComments() {
        if (currentTaskId == 0) return
        _state.value = _state.value.copy(commentsLoading = true, commentsError = null)
        scope.launch {
            val result = commentRepo.list(currentTaskId)
            _state.value = if (result.isSuccess) {
                _state.value.copy(commentsLoading = false, comments = result.getOrThrow())
            } else {
                val ex = result.exceptionOrNull()
                val code = (ex as? ApiException)?.code ?: ""
                _state.value.copy(
                    commentsLoading = false,
                    commentsError = ApiErrorMessages.forFetch(code, ex?.message ?: "")
                )
            }
        }
    }

    fun setDraft(value: String) {
        _state.value = _state.value.copy(draft = value, postError = null)
    }

    fun post() {
        val text = _state.value.draft.trim()
        if (text.isBlank() || currentTaskId == 0) return

        _state.value = _state.value.copy(posting = true, postError = null)
        scope.launch {
            val result = commentRepo.add(currentTaskId, text)
            if (result.isSuccess) {
                val added = result.getOrThrow()
                _state.value = _state.value.copy(
                    posting = false,
                    draft = "",
                    comments = _state.value.comments + added
                )
            } else {
                val ex = result.exceptionOrNull()
                val code = (ex as? ApiException)?.code ?: ""
                val (_, body) = ApiErrorMessages.forSubmit(code, ex?.message ?: "")
                _state.value = _state.value.copy(posting = false, postError = body)
            }
        }
    }
}
