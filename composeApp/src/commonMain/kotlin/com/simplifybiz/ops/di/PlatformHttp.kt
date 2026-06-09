package com.simplifybiz.ops.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Hook for platform-specific HttpClient config — primarily certificate
 * pinning. Common code calls into this from the NetworkModule.
 */
expect fun HttpClientConfig<*>.installPlatformSecurity()
