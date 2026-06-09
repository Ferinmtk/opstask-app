package com.simplifybiz.ops.di

import com.simplifybiz.ops.BuildConfig
import io.ktor.client.plugins.logging.LogLevel

actual fun networkLogLevel(): LogLevel =
    if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
