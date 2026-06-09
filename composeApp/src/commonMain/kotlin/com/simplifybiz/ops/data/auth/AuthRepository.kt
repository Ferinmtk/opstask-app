package com.simplifybiz.ops.data.auth

import com.simplifybiz.ops.data.ApiConstants
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.ApiResponse
import com.simplifybiz.ops.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthRepository(
    private val httpClient: HttpClient,
    private val session: SessionManager,
    private val json: Json
) {

    suspend fun login(email: String, password: String): Result<AuthMeData> = runCatching {
        val tokenResponse: HttpResponse = httpClient.post(ApiConstants.JWT_TOKEN_URL) {
            contentType(ContentType.Application.Json)
            // Drop any stale Bearer attached by defaultRequest so /token
            // never sees two Authorization headers. Ktor's header() appends,
            // so we have to remove via the headers builder.
            headers.remove(HttpHeaders.Authorization)
            setBody(JwtTokenRequest(username = email, password = password))
        }

        val tokenBody = tokenResponse.bodyAsText()
        if (!tokenResponse.status.isSuccess()) {
            val err = parseJwtError(tokenBody)
            throw ApiException(err.first, err.second)
        }

        val token = json.decodeFromString(JwtTokenResponse.serializer(), tokenBody)
        if (token.token.isBlank()) {
            throw ApiException("login_failed", "No token returned from server")
        }

        session.saveSession(
            email = token.userEmail.ifBlank { email },
            token = token.token,
            displayName = token.userDisplayName.ifBlank { email.substringBefore("@") },
            userId = 0
        )

        val me: ApiResponse<AuthMeData> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/auth/me"
        ).body()

        if (!me.success || me.data == null) {
            session.clear()
            throw ApiException(
                me.error?.code ?: "login_failed",
                me.error?.message ?: "Could not load profile after sign in"
            )
        }

        session.saveSession(
            email = me.data.email,
            token = token.token,
            displayName = me.data.displayName.ifBlank {
                me.data.firstName.ifBlank { email.substringBefore("@") }
            },
            userId = me.data.id
        )
        session.saveProfile(me.data)
        me.data
    }

    suspend fun refreshProfile(): Result<AuthMeData> = runCatching {
        if (!session.isLoggedIn()) throw ApiException("no_session", "Not logged in")
        val me: ApiResponse<AuthMeData> = httpClient.get(
            "${ApiConstants.API_BASE_URL}/auth/me"
        ).body()
        if (!me.success || me.data == null) {
            throw ApiException(
                me.error?.code ?: "fetch_failed",
                me.error?.message ?: "Could not load profile"
            )
        }
        session.saveProfile(me.data)
        me.data
    }

    suspend fun isCurrentTokenValid(): Boolean {
        if (!session.isLoggedIn()) return false
        return try {
            val response: HttpResponse = httpClient.post(ApiConstants.JWT_VALIDATE_URL)
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    fun logout() { session.clear() }

    private fun parseJwtError(body: String): Pair<String, String> {
        return try {
            val parsed = json.decodeFromString(JwtErrorResponse.serializer(), body)
            parsed.code to (parsed.message.ifBlank { "Sign in failed" })
        } catch (e: Exception) {
            "login_failed" to "Sign in failed"
        }
    }
}

@Serializable
private data class JwtTokenRequest(
    val username: String,
    val password: String
)

@Serializable
private data class JwtTokenResponse(
    val token: String = "",
    @SerialName("user_email") val userEmail: String = "",
    @SerialName("user_nicename") val userNicename: String = "",
    @SerialName("user_display_name") val userDisplayName: String = ""
)

@Serializable
private data class JwtErrorResponse(
    val code: String = "",
    val message: String = ""
)

@Serializable
data class AuthMeData(
    val id: Int = 0,
    val email: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("display_name") val displayName: String = "",
    val roles: List<String> = emptyList(),
    @SerialName("site_url") val siteUrl: String = "",
    val organization: String = "",
    @SerialName("organization_website") val organizationWebsite: String = "",
    @SerialName("bill_client_email") val billClientEmail: String = "",
    val phone: String = "",
    @SerialName("hourly_rate") val hourlyRate: String = "",
    @SerialName("github_handle") val githubHandle: String = "",
    val address: String = ""
)
