package com.simplifybiz.ops.data.messages

import com.russhwolf.settings.Settings
import com.simplifybiz.ops.data.ApiConstants
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.ApiResponse
import com.simplifybiz.ops.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Reads the cross-task /messages feed from the plugin.
 *
 * Unread tracking is local-only: the highest Message.id the user has
 * seen is stored as "last_seen_message_id" in Settings. Anything with a
 * higher id renders as unread.
 */
class MessageRepository(
    private val httpClient: HttpClient,
    private val session: SessionManager,
    private val settings: Settings
) {
    companion object {
        private const val KEY_LAST_SEEN_ID = "messages_last_seen_id"
    }

    suspend fun list(): Result<List<Message>> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")
        val response: ApiResponse<List<Message>> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/messages"
        ).body()
        if (!response.success) throw ApiException(
            response.error?.code ?: "fetch_failed",
            response.error?.message ?: "Could not load messages"
        )
        response.data ?: emptyList()
    }

    fun lastSeenId(): Int = settings.getInt(KEY_LAST_SEEN_ID, 0)

    /**
     * Marks all messages with id <= newestId as read. Call when the user
     * opens the Messages tab.
     */
    fun markSeen(newestId: Int) {
        if (newestId > lastSeenId()) {
            settings.putInt(KEY_LAST_SEEN_ID, newestId)
        }
    }

    fun unreadCount(messages: List<Message>): Int {
        val seen = lastSeenId()
        return messages.count { it.id > seen }
    }
}
