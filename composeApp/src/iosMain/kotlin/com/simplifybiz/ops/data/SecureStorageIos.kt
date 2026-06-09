package com.simplifybiz.ops.data

import com.russhwolf.settings.KeychainSettings

class SecureStorageIos(
    service: String = "com.simplifybiz.ops.secure"
) : SecureStorage {

    private val keychain = KeychainSettings(service = service)

    override fun put(key: String, value: String) {
        keychain.putString(key, value)
    }

    override fun get(key: String): String? =
        keychain.getStringOrNull(key)

    override fun remove(key: String) {
        keychain.remove(key)
    }
}
