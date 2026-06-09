package com.simplifybiz.ops.data

import com.russhwolf.settings.Settings
import com.simplifybiz.ops.data.auth.AuthMeData

/**
 * Stores JWT auth state plus the user's MemberPress profile.
 *
 * Storage split (post v0.7.6 audit):
 *   - SecureStorage  → token, user id, email, organization, bill_client_email,
 *                       phone, hourly_rate, github_handle, address
 *   - Settings       → display name, token issued-at timestamp,
 *                       organization website (public-facing already)
 *
 * The split keeps anything that's PII or a credential out of unencrypted
 * SharedPreferences / NSUserDefaults so a static dump on a compromised
 * device shows only the user-visible display name and a timestamp.
 *
 * Token expiry is 7 days (JWT plugin default). We force re-login at 6
 * days via SplashScreen.
 */
class SessionManager(
    private val settings: Settings,
    private val secure: SecureStorage
) {
    companion object {
        // Non-sensitive Settings keys
        private const val KEY_TOKEN_ISSUED_AT = "jwt_token_issued_at"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_ORG_WEBSITE = "p_org_website"

        // Sensitive SecureStorage keys
        private const val SEC_EMAIL = "user_email"
        private const val SEC_TOKEN = "jwt_token"
        private const val SEC_USER_ID = "user_id"
        private const val SEC_ORGANIZATION = "p_organization"
        private const val SEC_BILL_CLIENT_EMAIL = "p_bill_client_email"
        private const val SEC_PHONE = "p_phone"
        private const val SEC_HOURLY_RATE = "p_hourly_rate"
        private const val SEC_GITHUB_HANDLE = "p_github_handle"
        private const val SEC_ADDRESS = "p_address"

        const val TOKEN_REFRESH_AGE_MS = 6L * 24 * 60 * 60 * 1000
    }

    fun saveSession(
        email: String,
        token: String,
        displayName: String,
        userId: Int,
        issuedAtMs: Long = currentTimeMillis()
    ) {
        secure.put(SEC_EMAIL, email)
        secure.put(SEC_TOKEN, token)
        secure.put(SEC_USER_ID, userId.toString())
        settings.putString(KEY_DISPLAY_NAME, displayName)
        settings.putLong(KEY_TOKEN_ISSUED_AT, issuedAtMs)
    }

    fun saveProfile(profile: AuthMeData) {
        secure.put(SEC_ORGANIZATION, profile.organization)
        secure.put(SEC_BILL_CLIENT_EMAIL, profile.billClientEmail)
        secure.put(SEC_PHONE, profile.phone)
        secure.put(SEC_HOURLY_RATE, profile.hourlyRate)
        secure.put(SEC_GITHUB_HANDLE, profile.githubHandle)
        secure.put(SEC_ADDRESS, profile.address)
        // Org website is public on the org's site anyway — keep in Settings
        settings.putString(KEY_ORG_WEBSITE, profile.organizationWebsite)
    }

    fun getEmail(): String? = secure.get(SEC_EMAIL)
    fun getToken(): String? = secure.get(SEC_TOKEN)
    fun getDisplayName(): String? = settings.getStringOrNull(KEY_DISPLAY_NAME)
    fun getUserId(): Int = secure.get(SEC_USER_ID)?.toIntOrNull() ?: 0
    fun getTokenIssuedAt(): Long = settings.getLong(KEY_TOKEN_ISSUED_AT, 0)

    fun getOrganization(): String = secure.get(SEC_ORGANIZATION) ?: ""
    fun getOrganizationWebsite(): String = settings.getString(KEY_ORG_WEBSITE, "")
    fun getBillClientEmail(): String = secure.get(SEC_BILL_CLIENT_EMAIL) ?: ""
    fun getPhone(): String = secure.get(SEC_PHONE) ?: ""
    fun getHourlyRate(): String = secure.get(SEC_HOURLY_RATE) ?: ""
    fun getGithubHandle(): String = secure.get(SEC_GITHUB_HANDLE) ?: ""
    fun getAddress(): String = secure.get(SEC_ADDRESS) ?: ""

    fun isLoggedIn(): Boolean =
        !getEmail().isNullOrBlank() && !getToken().isNullOrBlank()

    fun tokenAgeMs(): Long {
        val issued = getTokenIssuedAt()
        if (issued == 0L) return Long.MAX_VALUE
        return currentTimeMillis() - issued
    }

    fun tokenIsStale(): Boolean = tokenAgeMs() >= TOKEN_REFRESH_AGE_MS

    fun getBearerHeader(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }

    fun clear() {
        secure.clear(listOf(
            SEC_EMAIL, SEC_TOKEN, SEC_USER_ID,
            SEC_ORGANIZATION, SEC_BILL_CLIENT_EMAIL, SEC_PHONE,
            SEC_HOURLY_RATE, SEC_GITHUB_HANDLE, SEC_ADDRESS
        ))
        listOf(KEY_TOKEN_ISSUED_AT, KEY_DISPLAY_NAME, KEY_ORG_WEBSITE)
            .forEach { settings.remove(it) }
    }

    private fun currentTimeMillis(): Long =
        kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
