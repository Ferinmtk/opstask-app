package com.simplifybiz.ops.data.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One entry in the cross-task Messages feed. Each Message is a single
 * Shared Note on a task with the task title joined in for display.
 *
 * Mirrors the /messages plugin endpoint response.
 */
@Serializable
data class Message(
    val id: Int = 0,
    @SerialName("task_id") val taskId: Int = 0,
    @SerialName("task_title") val taskTitle: String = "",
    val note: String = "",
    val date: String = "",
    @SerialName("author_id") val authorId: Int = 0,
    @SerialName("author_name") val authorName: String = "",
    @SerialName("author_email") val authorEmail: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
