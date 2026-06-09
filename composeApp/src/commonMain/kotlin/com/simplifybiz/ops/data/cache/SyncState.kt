package com.simplifybiz.ops.data.cache

import kotlinx.serialization.Serializable

@Serializable
enum class SyncState {
    SYNCED,    // Server confirmed
    PENDING,   // Created locally not yet sent
    SENDING,   // Currently being sent
    FAILED     // Send failed will retry
}
