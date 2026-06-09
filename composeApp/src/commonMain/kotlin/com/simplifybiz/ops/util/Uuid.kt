package com.simplifybiz.ops.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Cryptographically-secure RFC 4122 v4 UUID (string form).
 *
 * Used as an idempotency key on POST /tasks so the server can dedupe
 * if the mobile retries a submission. Kotlin's kotlin.uuid.Uuid is
 * backed by a CSPRNG on each platform and emits the correct version
 * and variant bits, unlike the hand-rolled v0.7.5 implementation.
 */
@OptIn(ExperimentalUuidApi::class)
fun generateUuid(): String = Uuid.random().toString()

fun nowMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
