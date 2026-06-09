package com.simplifybiz.ops.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.simplifybiz.ops.data.SecureStorage
import com.simplifybiz.ops.data.SecureStorageAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        val ctx = androidContext()
        val prefs = ctx.getSharedPreferences("smplfy_ops_prefs", android.content.Context.MODE_PRIVATE)
        SharedPreferencesSettings(prefs) as Settings
    }
    single<SecureStorage> { SecureStorageAndroid(androidContext()) }
}
