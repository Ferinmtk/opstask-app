package com.simplifybiz.ops.data.cache

import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonCache(
    private val settings: Settings,
    private val json: Json
) {
    fun <T> save(key: String, serializer: KSerializer<T>, value: T) {
        settings.putString(key, json.encodeToString(serializer, value))
    }

    fun <T> load(key: String, serializer: KSerializer<T>): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return try {
            json.decodeFromString(serializer, raw)
        } catch (e: Exception) {
            settings.remove(key)
            null
        }
    }

    fun remove(key: String) {
        settings.remove(key)
    }
}
