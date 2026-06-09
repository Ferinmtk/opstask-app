package com.simplifybiz.ops.di

import io.ktor.client.plugins.logging.LogLevel

/**
 * Returns the Ktor log level to use for this build. Debug builds get
 * INFO (request URLs + status), release builds get NONE so no
 * sensitive headers (Bearer tokens, PII) leak to logcat.
 */
expect fun networkLogLevel(): LogLevel
