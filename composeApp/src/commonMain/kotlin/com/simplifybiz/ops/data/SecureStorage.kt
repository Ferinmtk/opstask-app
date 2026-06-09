package com.simplifybiz.ops.data

/**
 * Platform-backed encrypted key-value storage for sensitive strings.
 *
 * Android: EncryptedSharedPreferences with a master key in Android Keystore.
 * iOS: Keychain Services (kSecClassGenericPassword).
 *
 * Use this for the auth token and all profile PII. Non-sensitive
 * preferences (display theme, last-seen message id, etc.) can continue
 * using the unencrypted Settings.
 */
interface SecureStorage {
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
    fun clear(keys: List<String>) { keys.forEach { remove(it) } }
}
