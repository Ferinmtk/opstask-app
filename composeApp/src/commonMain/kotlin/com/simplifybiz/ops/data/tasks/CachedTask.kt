package com.simplifybiz.ops.data.tasks

import com.simplifybiz.ops.data.cache.SyncState
import kotlinx.serialization.Serializable

@Serializable
data class CachedTask(
    val task: Task,
    val syncState: SyncState = SyncState.SYNCED,
    val localId: String? = null
)
