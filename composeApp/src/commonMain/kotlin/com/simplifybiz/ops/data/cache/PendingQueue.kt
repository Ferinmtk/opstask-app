package com.simplifybiz.ops.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer

/**
 * Persisted FIFO queue of operations waiting to sync to the server
 * (offline-first submission).
 *
 * Concurrency: every mutating method is suspend and runs inside a Mutex so
 * a sync flush running in parallel with a fresh submit cannot clobber
 * each other's writes. all() is also locked to give a consistent snapshot.
 */
class PendingQueue(private val cache: JsonCache) {
    companion object {
        private const val KEY = "pending_queue_v1"
    }

    private val mutex = Mutex()
    private val serializer = ListSerializer(PendingOperation.serializer())

    suspend fun all(): List<PendingOperation> = mutex.withLock {
        cache.load(KEY, serializer) ?: emptyList()
    }

    suspend fun add(op: PendingOperation): Unit = mutex.withLock {
        val current = (cache.load(KEY, serializer) ?: emptyList()).toMutableList()
        current.removeAll { it.id == op.id }
        current.add(op)
        cache.save(KEY, serializer, current)
    }

    suspend fun remove(id: String): Unit = mutex.withLock {
        val current = (cache.load(KEY, serializer) ?: emptyList()).filter { it.id != id }
        cache.save(KEY, serializer, current)
    }

    suspend fun update(op: PendingOperation): Unit = mutex.withLock {
        val current = (cache.load(KEY, serializer) ?: emptyList())
            .map { if (it.id == op.id) op else it }
        cache.save(KEY, serializer, current)
    }

    suspend fun byKind(kind: PendingOperation.Kind): List<PendingOperation> = mutex.withLock {
        (cache.load(KEY, serializer) ?: emptyList()).filter { it.kind == kind }
    }
}
