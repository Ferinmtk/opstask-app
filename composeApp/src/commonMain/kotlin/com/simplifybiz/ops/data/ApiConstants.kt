package com.simplifybiz.ops.data

/**
 * Mobile clients submit directly to ops.simplifybiz.com.
 *
 * Auth is JWT: POST email+password to /wp-json/jwt-auth/v1/token, get back a
 * Bearer token good for 7 days, then attach it to all API calls.
 */
object ApiConstants {
    const val OPS_BASE_URL = "https://ops.simplifybiz.com"
    const val API_PATH = "/wp-json/simplify-ops/v1"
    const val JWT_TOKEN_PATH = "/wp-json/jwt-auth/v1/token"
    const val JWT_VALIDATE_PATH = "/wp-json/jwt-auth/v1/token/validate"

    val API_BASE_URL: String = "$OPS_BASE_URL$API_PATH"
    val JWT_TOKEN_URL: String = "$OPS_BASE_URL$JWT_TOKEN_PATH"
    val JWT_VALIDATE_URL: String = "$OPS_BASE_URL$JWT_VALIDATE_PATH"
}
