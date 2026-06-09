package com.simplifybiz.ops.data.tasks

import com.simplifybiz.ops.data.ApiConstants
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.ApiResponse
import com.simplifybiz.ops.data.SessionManager
import com.simplifybiz.ops.data.cache.CacheKeys
import com.simplifybiz.ops.data.cache.JsonCache
import com.simplifybiz.ops.data.cache.PendingOperation
import com.simplifybiz.ops.data.cache.PendingQueue
import com.simplifybiz.ops.data.cache.SyncState
import com.simplifybiz.ops.util.generateUuid
import com.simplifybiz.ops.util.nowMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Per-submission outcome from the most recent sync attempt.
 * Used by SubmitTaskViewModel to show success or surface server errors.
 */
data class SubmitOutcome(
    val localId: String,
    val success: Boolean,
    val errorCode: String = "",
    val errorMessage: String = ""
)

class TaskRepository(
    private val httpClient: HttpClient,
    private val session: SessionManager,
    private val cache: JsonCache,
    private val queue: PendingQueue,
    private val json: Json
) {
    private val _tasks = MutableStateFlow(loadCached())
    val tasks: StateFlow<List<CachedTask>> = _tasks

    private val _lastSubmitOutcome = MutableStateFlow<SubmitOutcome?>(null)
    val lastSubmitOutcome: StateFlow<SubmitOutcome?> = _lastSubmitOutcome

    private val syncMutex = Mutex()
    private val inFlight = mutableSetOf<String>()

    private fun loadCached(): List<CachedTask> =
        cache.load(CacheKeys.TASKS, ListSerializer(CachedTask.serializer())) ?: emptyList()

    private fun saveCache(list: List<CachedTask>) {
        cache.save(CacheKeys.TASKS, ListSerializer(CachedTask.serializer()), list)
        _tasks.value = list
    }

    suspend fun refresh(): Result<Unit> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")

        val response: ApiResponse<List<Task>> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/tasks"
        ).body()

        if (!response.success) throw ApiException(
            response.error?.code ?: "fetch_failed",
            response.error?.message ?: "Could not load tasks"
        )

        val remoteTasks = (response.data ?: emptyList()).map { CachedTask(it, SyncState.SYNCED) }

        val pendingLocal = _tasks.value.filter { it.syncState != SyncState.SYNCED }
        val deduplicated = pendingLocal.filter { local ->
            val matchOnServer = remoteTasks.any { server ->
                server.task.task.trim() == local.task.task.trim() &&
                server.task.dateDue.trim() == local.task.dateDue.trim() &&
                local.task.task.isNotBlank()
            }
            if (matchOnServer && local.localId != null) {
                queue.remove(local.localId)
                false
            } else {
                true
            }
        }

        saveCache(remoteTasks + deduplicated)
    }

    suspend fun get(id: Int): Result<Task> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")
        val cached = _tasks.value.firstOrNull { it.task.id == id }?.task

        val response: ApiResponse<Task> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/tasks/$id"
        ).body()

        if (!response.success) {
            cached?.let { return@runCatching it }
            throw ApiException(
                response.error?.code ?: "fetch_failed",
                response.error?.message ?: "Could not load task"
            )
        }
        response.data ?: cached ?: throw ApiException("empty_response", "Task not found")
    }

    suspend fun submit(payload: SubmitTaskPayload): String {
        val localId = generateUuid()
        val optimisticTask = Task(
            id = -nowMillis().toInt(),
            task = payload.task,
            description = payload.description ?: "",
            project = payload.project,
            priority = payload.priority,
            dateDue = payload.dateDue,
            status = "Received",
            stage = TaskStages.ACCEPT,
            assignTo = payload.assignedTo
        )
        val cached = CachedTask(optimisticTask, SyncState.PENDING, localId = localId)
        saveCache(listOf(cached) + _tasks.value)

        queue.add(PendingOperation(
            id = localId,
            kind = PendingOperation.Kind.SUBMIT_TASK,
            payload = json.encodeToString(SubmitTaskPayload.serializer(), payload),
            createdAt = nowMillis()
        ))
        // reset previous outcome so observers don't see stale state
        _lastSubmitOutcome.value = null
        return localId
    }

    suspend fun trySync() = syncMutex.withLock {
        if (!session.isLoggedIn()) return@withLock

        val pending = queue.byKind(PendingOperation.Kind.SUBMIT_TASK)
            .filter { it.id !in inFlight }

        pending.forEach { op ->
            inFlight.add(op.id)
            markState(op.id, SyncState.SENDING)
            try {
                val payload = json.decodeFromString(SubmitTaskPayload.serializer(), op.payload)
                val response: ApiResponse<Task> = httpClient.post(
                    "${ApiConstants.API_BASE_URL}/tasks"
                ) {
                    header("X-Idempotency-Key", op.id)
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }.body()

                if (response.success && response.data != null) {
                    markSubmitted(op.id, response.data)
                    queue.remove(op.id)
                    _lastSubmitOutcome.value = SubmitOutcome(localId = op.id, success = true)
                } else {
                    val code = response.error?.code ?: "create_failed"
                    val msg = response.error?.message ?: ""
                    markState(op.id, SyncState.FAILED)
                    queue.update(op.copy(attempts = op.attempts + 1, lastError = msg))
                    _lastSubmitOutcome.value = SubmitOutcome(
                        localId = op.id, success = false, errorCode = code, errorMessage = msg
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Network error"
                markState(op.id, SyncState.FAILED)
                queue.update(op.copy(attempts = op.attempts + 1, lastError = msg))
                _lastSubmitOutcome.value = SubmitOutcome(
                    localId = op.id, success = false, errorCode = "network_error", errorMessage = msg
                )
            } finally {
                inFlight.remove(op.id)
            }
        }
    }

    private fun markState(localId: String, state: SyncState) {
        saveCache(_tasks.value.map { if (it.localId == localId) it.copy(syncState = state) else it })
    }

    private fun markSubmitted(localId: String, serverTask: Task?) {
        saveCache(_tasks.value.map { item ->
            if (item.localId == localId) {
                CachedTask(
                    task = serverTask ?: item.task,
                    syncState = SyncState.SYNCED,
                    localId = null
                )
            } else item
        })
    }

    suspend fun retry(localId: String) {
        markState(localId, SyncState.PENDING)
        val op = queue.all().firstOrNull { it.id == localId } ?: return
        queue.update(op.copy(attempts = 0, lastError = null))
    }

    suspend fun discardLocal(localId: String) {
        saveCache(_tasks.value.filter { it.localId != localId })
        queue.remove(localId)
    }

    fun clearOutcome() {
        _lastSubmitOutcome.value = null
    }
}
