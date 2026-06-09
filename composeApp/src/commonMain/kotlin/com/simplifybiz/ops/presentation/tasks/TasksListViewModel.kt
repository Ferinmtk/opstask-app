package com.simplifybiz.ops.presentation.tasks

import com.simplifybiz.ops.data.ApiErrorMessages
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.tasks.CachedTask
import com.simplifybiz.ops.data.tasks.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TasksState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val tasks: List<CachedTask> = emptyList(),
    val error: String? = null
)

/**
 * Tasks list ViewModel. Refresh strategy:
 *  - load() called when screen first opens, runs a full refresh
 *  - refresh() called on pull-to-refresh
 *  - When polling is active (foreground), refreshes every 30 seconds
 *  - pausePolling() + resumePolling() called on lifecycle events
 */
class TasksListViewModel(private val repository: TaskRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(TasksState(tasks = repository.tasks.value))
    val state: StateFlow<TasksState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private val pollIntervalMs = 30_000L

    init {
        scope.launch {
            repository.tasks.collect { tasks ->
                _state.value = _state.value.copy(tasks = tasks)
            }
        }
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        scope.launch {
            doRefresh()
            _state.value = _state.value.copy(loading = false)
            repository.trySync()
            startPolling()
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(refreshing = true, error = null)
        scope.launch {
            doRefresh()
            _state.value = _state.value.copy(refreshing = false)
            repository.trySync()
        }
    }

    private suspend fun doRefresh() {
        val result = repository.refresh()
        if (result.isFailure) {
            val ex = result.exceptionOrNull()
            val code = (ex as? ApiException)?.code ?: ""
            val msg = ex?.message ?: ""
            _state.value = _state.value.copy(
                error = ApiErrorMessages.forFetch(code, msg)
            )
        } else {
            _state.value = _state.value.copy(error = null)
        }
    }

    fun retry(localId: String) {
        scope.launch {
            repository.retry(localId)
            repository.trySync()
        }
    }

    fun discard(localId: String) {
        scope.launch { repository.discardLocal(localId) }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                delay(pollIntervalMs)
                if (!isActive) break
                doRefresh()
                repository.trySync()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.let { job ->
            job.cancel()
            pollingJob = null
        }
    }
}
