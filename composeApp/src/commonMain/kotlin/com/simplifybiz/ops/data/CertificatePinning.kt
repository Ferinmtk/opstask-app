package com.simplifybiz.ops.data

/**
 * Certificate pinning configuration.
 *
 * When ENABLED is true, the HTTP client refuses to talk to
 * ops.simplifybiz.com unless the server's certificate chain matches one
 * of the SHA-256 fingerprints in PINS. This blocks MITM via rogue or
 * mis-issued CAs.
 *
 * To enable:
 *   1. Fetch the live cert fingerprint:
 *      openssl s_client -connect ops.simplifybiz.com:443 \\
 *        -servername ops.simplifybiz.com < /dev/null 2>/dev/null \\
 *        | openssl x509 -pubkey -noout \\
 *        | openssl pkey -pubin -outform DER \\
 *        | openssl dgst -sha256 -binary \\
 *        | openssl enc -base64
 *   2. Add the base64 string to PINS prefixed with "sha256/"
 *   3. Also add a backup pin (the next cert in the chain) so a routine
 *      rotation doesn't brick the app.
 *   4. Set ENABLED = true.
 *
 * KEEP THIS IN SYNC WITH CERT ROTATIONS or users will be locked out.
 */
object CertificatePinning {
    const val ENABLED: Boolean = false
    const val HOSTNAME: String = "ops.simplifybiz.com"
    val PINS: List<String> = listOf(
        // "sha256/REPLACE_ME_WITH_CURRENT_CERT_FINGERPRINT",
        // "sha256/REPLACE_ME_WITH_BACKUP_FINGERPRINT"
    )
}
