package com.simplifybiz.ops.data

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

/**
 * iOS SecureStorage backed by a dedicated NSUserDefaults suite.
 *
 * Note: this is NOT Keychain-encrypted. The Keychain (KeychainSettings) throws
 * errSecMissingEntitlement (-34018) on unsigned simulator builds like the ones
 * run on Appetize. NSUserDefaults needs no entitlements and runs everywhere.
 * For a signed production build you can switch this back to KeychainSettings.
 */
class SecureStorageIos(
    suiteName: String = "com.simplifybiz.ops.secure"
) : SecureStorage {

    private val settings = NSUserDefaultsSettings(
        NSUserDefaults(suiteName = suiteName)
    )

    override fun put(key: String, value: String) {
        settings.putString(key, value)
    }

    override fun get(key: String): String? =
        settings.getStringOrNull(key)

    override fun remove(key: String) {
        settings.remove(key)
    }
}
