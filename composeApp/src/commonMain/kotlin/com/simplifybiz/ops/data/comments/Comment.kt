package com.simplifybiz.ops.data.comments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors CommentEntity::to_array() from the plugin.
 * One entry on form 197 linked to a form 172 task via Gravity Perks
 * Nested Forms.
 */
@Serializable
data class Comment(
    val id: Int = 0,
    @SerialName("task_id") val taskId: Int = 0,
    val note: String = "",
    val date: String = "",
    @SerialName("author_id") val authorId: Int = 0,
    @SerialName("author_name") val authorName: String = "",
    @SerialName("author_email") val authorEmail: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
