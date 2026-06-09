package com.simplifybiz.ops.data.cache

import kotlinx.serialization.Serializable

@Serializable
data class PendingOperation(
    val id: String,
    val kind: Kind,
    val payload: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null
) {
    @Serializable
    enum class Kind {
        SUBMIT_TASK,
        POST_COMMENT,
        SEND_MESSAGE
    }
}
