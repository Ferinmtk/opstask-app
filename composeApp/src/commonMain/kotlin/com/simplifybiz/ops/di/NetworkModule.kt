package com.simplifybiz.ops.di

import com.simplifybiz.ops.data.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    single {
        val session: SessionManager = get()
        HttpClient {
            install(ContentNegotiation) { json(get()) }
            install(Logging) { level = networkLogLevel() }
            installPlatformSecurity()
            defaultRequest {
                // Attach Bearer token from session on every request when present.
                // Login endpoint sets its own Authorization or calls /token
                // (which doesn't need auth) directly, so this is safe.
                session.getBearerHeader()?.let {
                    header(HttpHeaders.Authorization, it)
                }
                header(HttpHeaders.ContentType, "application/json")
            }
        }
    }
}
