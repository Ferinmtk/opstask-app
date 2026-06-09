package com.simplifybiz.ops.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.simplifybiz.ops.data.SecureStorage
import com.simplifybiz.ops.data.SecureStorageIos
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule: Module = module {
    single { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) as Settings }
    single<SecureStorage> { SecureStorageIos() }
}
