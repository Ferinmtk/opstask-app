package com.simplifybiz.ops.presentation.messages

import com.simplifybiz.ops.data.ApiErrorMessages
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.messages.Message
import com.simplifybiz.ops.data.messages.MessageRepository
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

data class MessagesState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val messages: List<Message> = emptyList(),
    val lastSeenId: Int = 0,
    val error: String? = null
)

class MessagesViewModel(
    private val repository: MessageRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(MessagesState(lastSeenId = repository.lastSeenId()))
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private val pollIntervalMs = 30_000L

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        scope.launch {
            doFetch()
            _state.value = _state.value.copy(loading = false)
            startPolling()
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(refreshing = true, error = null)
        scope.launch {
            doFetch()
            _state.value = _state.value.copy(refreshing = false)
        }
    }

    private suspend fun doFetch() {
        val result = repository.list()
        if (result.isSuccess) {
            val list = result.getOrThrow()
            _state.value = _state.value.copy(messages = list, error = null)
        } else {
            val ex = result.exceptionOrNull()
            val code = (ex as? ApiException)?.code ?: ""
            _state.value = _state.value.copy(
                error = ApiErrorMessages.forFetch(code, ex?.message ?: "")
            )
        }
    }

    /** Call when the user is actively viewing the Messages screen so we
     * mark the most recent id as seen. */
    fun markAllSeen() {
        val newest = _state.value.messages.maxOfOrNull { it.id } ?: return
        repository.markSeen(newest)
        _state.value = _state.value.copy(lastSeenId = repository.lastSeenId())
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                delay(pollIntervalMs)
                if (!isActive) break
                doFetch()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
}
