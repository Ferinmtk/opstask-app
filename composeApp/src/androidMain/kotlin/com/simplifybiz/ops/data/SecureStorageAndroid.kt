package com.simplifybiz.ops.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * EncryptedSharedPreferences backed by a master key in Android Keystore.
 *
 * File is stored at:
 *   /data/data/com.simplifybiz.ops/shared_prefs/smplfy_ops_secure.xml
 *
 * Both keys and values are encrypted, so a static dump from a rooted
 * device shows only ciphertext. The master key never leaves Keystore.
 */
class SecureStorageAndroid(context: Context) : SecureStorage {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "smplfy_ops_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun get(key: String): String? = prefs.getString(key, null)

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
