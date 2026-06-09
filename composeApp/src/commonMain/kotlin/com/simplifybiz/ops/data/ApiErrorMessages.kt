package com.simplifybiz.ops.data

object ApiErrorMessages {

    fun forLogin(code: String, fallback: String): Pair<String, String> = when (code) {
        "incorrect_password",
        "invalid_username",
        "invalid_email",
        "[jwt_auth] incorrect_password",
        "[jwt_auth] invalid_username",
        "[jwt_auth] invalid_email" ->
            "Wrong email or password" to "Check your credentials and try again."
        "not_provisioned" ->
            "Account not ready for mobile" to "Your account isn't set up for the mobile app yet. Please contact your administrator."
        "no_session", "rest_not_logged_in" ->
            "Not signed in" to "Sign in with your email and password."
        "rest_forbidden" ->
            "No mobile access" to "Your account exists but lacks the Submit Task Mobile App role. Contact your administrator."
        "jwt_auth_bad_config" ->
            "Server not ready" to "Authentication is not configured on the server. Contact your administrator."
        "jwt_auth_failed", "[jwt_auth] failed" ->
            "Sign in failed" to "Could not authenticate. Try again or contact support."
        else ->
            "Sign in failed" to fallback.ifBlank { "Something went wrong. Try again in a moment." }
    }

    fun forSubmit(code: String, fallback: String): Pair<String, String> = when (code) {
        "not_provisioned" ->
            "Account not ready" to "Your account isn't set up to submit tasks. Contact your administrator."
        "create_failed" ->
            "Couldn't save task" to (fallback.ifBlank { "The server rejected the task. Check required fields and try again." })
        "internal_error" ->
            "Server error" to (fallback.ifBlank { "Something went wrong on the server. Try again in a moment." })
        "rest_forbidden" ->
            "No mobile access" to "Your account lacks the Submit Task Mobile App role."
        "jwt_auth_invalid_token", "jwt_auth_expired_token" ->
            "Session expired" to "Please sign in again."
        else ->
            "Couldn't submit" to fallback.ifBlank { "Try again in a moment." }
    }

    fun forFetch(code: String, fallback: String): String = when (code) {
        "no_session" -> "Please sign in again"
        "rest_forbidden" -> "Your mobile access was removed. Contact admin."
        "internal_error" -> "Server error. Pull to refresh to try again."
        "jwt_auth_invalid_token", "jwt_auth_expired_token" -> "Session expired. Please sign in again."
        else -> fallback.ifBlank { "Couldn't load tasks. Check your connection." }
    }
}
