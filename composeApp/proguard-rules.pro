# ====================================================================
# Simplify Ops Mobile — R8 / ProGuard rules
# ====================================================================

# --- kotlinx.serialization ---
# Keep all @Serializable classes and their companion/serializer descriptors.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep the runtime API used by the serializer
-keep,includedescriptorclasses class com.simplifybiz.ops.**$$serializer { *; }
-keepclassmembers class com.simplifybiz.ops.** {
    *** Companion;
}
-keepclasseswithmembers class com.simplifybiz.ops.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Koin ---
# Koin uses reflection on constructors registered via factoryOf / singleOf
-keep class com.simplifybiz.ops.presentation.**.*ViewModel { *; }
-keep class com.simplifybiz.ops.data.**.*Repository { *; }
-keep class com.simplifybiz.ops.data.SessionManager { *; }

# --- Ktor ---
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**

# --- Compose Multiplatform ---
-keep class org.jetbrains.compose.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn org.jetbrains.compose.**

# --- Kotlin reflection (used by Koin) ---
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# --- Our own data classes used over the network (Serializable) ---
-keep class com.simplifybiz.ops.data.tasks.** { *; }
-keep class com.simplifybiz.ops.data.comments.** { *; }
-keep class com.simplifybiz.ops.data.messages.** { *; }
-keep class com.simplifybiz.ops.data.auth.** { *; }
-keep class com.simplifybiz.ops.data.cache.** { *; }
-keep class com.simplifybiz.ops.data.Api* { *; }

# --- Multiplatform Settings ---
-keep class com.russhwolf.settings.** { *; }

# --- Tink (used by EncryptedSharedPreferences) ---
# These are compile-time-only annotations from Google's errorprone library.
# Safe to ignore — they don't exist at runtime.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# Keep all Tink crypto classes (used reflectively by EncryptedSharedPreferences)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# AndroidX Security uses reflection to load Tink keysets
-keep class androidx.security.crypto.** { *; }
