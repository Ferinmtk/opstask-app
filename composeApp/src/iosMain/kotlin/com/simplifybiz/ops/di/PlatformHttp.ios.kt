package com.simplifybiz.ops.di

import com.simplifybiz.ops.data.CertificatePinning
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinClientEngineConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionDelegateProtocol
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Security.SecKeyCopyExternalRepresentation
import platform.Security.SecTrustCopyKey
import platform.Security.SecTrustGetCertificateAtIndex
import platform.Security.SecTrustGetCertificateCount
import platform.darwin.NSObject

/**
 * iOS certificate pinning via Darwin engine handleChallenge.
 *
 * For each challenge against CertificatePinning.HOSTNAME:
 *   1. Extract the public key from the leaf certificate.
 *   2. SHA-256 it.
 *   3. Compare base64 to each entry in CertificatePinning.PINS
 *      (each prefixed with "sha256/").
 *   4. Match → trust. No match → cancel.
 *
 * Other hosts (auth, CDN, etc) fall through to default handling so we
 * don't accidentally block them.
 *
 * Only activates when CertificatePinning.ENABLED is true.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun HttpClientConfig<*>.installPlatformSecurity() {
    if (!CertificatePinning.ENABLED || CertificatePinning.PINS.isEmpty()) return

    engine {
        if (this is DarwinClientEngineConfig) {
            handleChallenge { _, _, challenge, completionHandler ->
                handlePinningChallenge(
                    challenge = challenge,
                    hostname = CertificatePinning.HOSTNAME,
                    pins = CertificatePinning.PINS,
                    complete = completionHandler
                )
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun handlePinningChallenge(
    challenge: NSURLAuthenticationChallenge,
    hostname: String,
    pins: List<String>,
    complete: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
) {
    val space = challenge.protectionSpace
    if (space.authenticationMethod != NSURLAuthenticationMethodServerTrust) {
        complete(NSURLSessionAuthChallengePerformDefaultHandling, null); return
    }
    if (space.host != hostname) {
        complete(NSURLSessionAuthChallengePerformDefaultHandling, null); return
    }

    val trust = space.serverTrust ?: run {
        complete(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null); return
    }

    val count = SecTrustGetCertificateCount(trust)
    for (i in 0 until count) {
        SecTrustGetCertificateAtIndex(trust, i) ?: continue
        val key = SecTrustCopyKey(trust) ?: continue
        val pubKeyData = SecKeyCopyExternalRepresentation(key, null) as? NSData ?: continue
        val pin = "sha256/" + sha256Base64(pubKeyData)
        if (pins.contains(pin)) {
            complete(
                NSURLSessionAuthChallengeUseCredential,
                NSURLCredential.credentialForTrust(trust)
            )
            return
        }
    }
    complete(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
}

@OptIn(ExperimentalForeignApi::class)
private fun sha256Base64(data: NSData): String {
    val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
    hash.usePinned { hashPin ->
        data.bytes?.let { bytes ->
            CC_SHA256(bytes, data.length.toUInt(), hashPin.addressOf(0).reinterpret())
        }
    }
    val hashData = NSData.create(bytes = hash.refTo(0), length = hash.size.toULong())
    return hashData.base64EncodedStringWithOptions(0u)
}
