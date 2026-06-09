package com.simplifybiz.ops.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.numberWithBool
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.NSObjectProtocol

/**
 * Keychain Services backed SecureStorage for iOS.
 *
 * Items are stored as kSecClassGenericPassword with:
 *   - kSecAttrService = bundle-scoped service name
 *   - kSecAttrAccount = the storage key (one keychain entry per key)
 *   - kSecAttrAccessible = AfterFirstUnlockThisDeviceOnly
 *       Token is readable after the user unlocks the device once
 *       post-reboot, and is NEVER copied to iCloud Keychain or
 *       device-transfer backups.
 *
 * Bridging: CFStringRef constants (kSecClass etc) are toll-free bridged
 * to NSString and conform to NSCopying/NSObjectProtocol, so we can use
 * them as keys in an NSMutableDictionary directly. NSDictionary itself
 * bridges to CFDictionaryRef on the receiving side.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class SecureStorageIos(
    private val serviceName: String = "com.simplifybiz.ops"
) : SecureStorage {

    override fun put(key: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        // Try update first — succeeds only if the key already exists
        val updateAttrs = dictOf(kSecValueData to data)
        val updateStatus = SecItemUpdate(baseQuery(key).asCFDictionary(), updateAttrs.asCFDictionary())
        if (updateStatus == errSecSuccess) return

        // No existing item — insert
        val add = baseQuery(key).also {
            it.setObject(data, forKey = kSecValueData as NSObjectProtocol)
            it.setObject(
                kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly!!,
                forKey = kSecAttrAccessible as NSObjectProtocol
            )
        }
        SecItemAdd(add.asCFDictionary(), null)
    }

    override fun get(key: String): String? = memScoped {
        val query = baseQuery(key).also {
            it.setObject(NSNumber.numberWithBool(true), forKey = kSecReturnData as NSObjectProtocol)
            it.setObject(kSecMatchLimitOne!!, forKey = kSecMatchLimit as NSObjectProtocol)
        }

        val resultVar = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query.asCFDictionary(), resultVar.ptr)
        if (status != errSecSuccess) return@memScoped null

        val nsData = CFBridgingRelease(resultVar.value) as? NSData ?: return@memScoped null
        NSString.create(nsData, NSUTF8StringEncoding) as String?
    }

    override fun remove(key: String) {
        SecItemDelete(baseQuery(key).asCFDictionary())
    }

    private fun baseQuery(key: String): NSMutableDictionary =
        dictOf(
            kSecClass to kSecClassGenericPassword!!,
            kSecAttrService to (serviceName as NSString),
            kSecAttrAccount to (key as NSString)
        )

    private fun dictOf(vararg pairs: Pair<Any?, Any?>): NSMutableDictionary {
        val dict = NSMutableDictionary()
        for ((k, v) in pairs) {
            if (k != null && v != null) {
                dict.setObject(v, forKey = k as NSObjectProtocol)
            }
        }
        return dict
    }

    @Suppress("UNCHECKED_CAST")
    private fun NSDictionary.asCFDictionary(): CFDictionaryRef =
        CFBridgingRetain(this) as CFDictionaryRef
}
