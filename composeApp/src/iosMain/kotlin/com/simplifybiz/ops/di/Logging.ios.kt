package com.simplifybiz.ops.di

import io.ktor.client.plugins.logging.LogLevel
import platform.Foundation.NSProcessInfo

/**
 * iOS has no BuildConfig.DEBUG. We approximate via DEBUG env var which
 * Xcode sets in debug schemes, and fall back to NONE otherwise (safer
 * to under-log than over-log).
 */
actual fun networkLogLevel(): LogLevel {
    val env = NSProcessInfo.processInfo.environment
    val debug = env["DEBUG"] as? String
    return if (debug == "1" || debug == "true") LogLevel.INFO else LogLevel.NONE
}
