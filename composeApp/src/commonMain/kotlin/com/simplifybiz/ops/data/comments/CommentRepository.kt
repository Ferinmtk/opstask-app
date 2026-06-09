package com.simplifybiz.ops.data.comments

import com.simplifybiz.ops.data.ApiConstants
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.ApiResponse
import com.simplifybiz.ops.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class PostCommentBody(val note: String)

/**
 * Repository for Shared Notes comments on a task.
 *
 * Reads and writes happen against the plugin's /tasks/{id}/comments
 * endpoints. The plugin enforces task ownership (only the task submitter
 * can view or post). Comments within a task show all participants.
 */
class CommentRepository(
    private val httpClient: HttpClient,
    private val session: SessionManager
) {

    suspend fun list(taskId: Int): Result<List<Comment>> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")
        val response: ApiResponse<List<Comment>> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/tasks/$taskId/comments"
        ).body()
        if (!response.success) throw ApiException(
            response.error?.code ?: "fetch_failed",
            response.error?.message ?: "Could not load comments"
        )
        response.data ?: emptyList()
    }

    suspend fun add(taskId: Int, note: String): Result<Comment> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")
        val trimmed = note.trim()
        if (trimmed.isBlank()) throw ApiException("validation_error", "Note required")

        val response: ApiResponse<Comment> = httpClient.post(
            "${ApiConstants.API_BASE_URL}/tasks/$taskId/comments"
        ) {
            contentType(ContentType.Application.Json)
            setBody(PostCommentBody(trimmed))
        }.body()

        if (!response.success || response.data == null) throw ApiException(
            response.error?.code ?: "create_failed",
            response.error?.message ?: "Could not post comment"
        )
        response.data
    }
}
