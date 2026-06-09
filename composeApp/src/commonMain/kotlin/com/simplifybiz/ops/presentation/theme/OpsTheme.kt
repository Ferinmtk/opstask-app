package com.simplifybiz.ops.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Simplify Ops design tokens, ported from the approved HTML prototype.
 *
 * Material 3 slots (primary, surface, etc) are set here so every standard
 * component picks up the brand automatically. Colors that don't map to a
 * Material slot — stage badges, priority dots, dashboard cards — live in
 * the OpsColors object and are referenced directly by the relevant
 * composables.
 */

// --- Brand / action ---
private val ActionBlue       = Color(0xFF0A84FF)  // iOS system blue (prototype)
private val ActionBluePressed = Color(0xFF0066CC)
private val AccentBlue       = Color(0xFF3CA0FF)

// --- Neutrals (light) ---
private val TextPrimary   = Color(0xFF111827)
private val TextSecondary = Color(0xFF4B5563)
private val BorderColor   = Color(0xFFE5E7EB)
private val BgMain        = Color(0xFFFFFFFF)
private val BgSurface     = Color(0xFFF9FAFB)

// --- Neutrals (dark pairing) ---
private val DarkBg        = Color(0xFF0F1623)
private val DarkSurface   = Color(0xFF161F2E)
private val DarkText      = Color(0xFFF3F4F6)
private val DarkTextSec   = Color(0xFF9CA3AF)
private val DarkBorder    = Color(0xFF2A3546)

private val LightColors = lightColorScheme(
    primary = ActionBlue,
    onPrimary = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    background = BgMain,
    onBackground = TextPrimary,
    surface = BgMain,
    onSurface = TextPrimary,
    surfaceVariant = BgSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = BorderColor,
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSec,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = Color(0xFFF87171),
    onError = Color.Black
)

/**
 * Non-Material palette: stage badges, priority dots, dashboard cards.
 * Each pairs a soft background with a readable foreground, matching the
 * prototype exactly.
 */
object OpsColors {
    // Stage badges (field 70: 0 Accept -> 4 Done)
    val acceptBg   = Color(0xFFFEE2E2); val acceptFg   = Color(0xFF991B1B)
    val todoBg     = Color(0xFFE0E7FF); val todoFg     = Color(0xFF3730A3)
    val doingBg    = Color(0xFFFEF3C7); val doingFg    = Color(0xFF92400E)
    val approveBg  = Color(0xFFD1FAE5); val approveFg  = Color(0xFF065F46)
    val doneBg     = Color(0xFFF3F4F6); val doneFg     = Color(0xFF374151)

    // Priority dots
    val prioHigh   = Color(0xFFDC2626)
    val prioNormal = Color(0xFF2563EB)
    val prioLow    = Color(0xFF6B7280)

    // Dashboard cards (bg, fg)
    val cardAddBg     = Color(0xFFF0F7FF); val cardAddFg     = Color(0xFF0250BC)
    val cardInboxBg   = Color(0xFFFFF8E6); val cardInboxFg   = Color(0xFFB45309)
    val cardStatusBg  = Color(0xFFF0FDF4); val cardStatusFg  = Color(0xFF166534)
    val cardViewBg    = Color(0xFFF5F3FF); val cardViewFg    = Color(0xFF5B21B6)
}

/**
 * Brand brushes and fills that have no Material slot. The iOS-glass look
 * leans on subtle gradients for the logo mark, avatar, and primary action;
 * these are referenced directly by Login, Profile, and GradientButton.
 */
object OpsBrand {
    // Top-to-bottom action gradient for primary buttons (180deg in the prototype)
    val actionGradient = Brush.verticalGradient(listOf(Color(0xFF3CA0FF), Color(0xFF0A84FF)))

    // Diagonal logo mark: blue -> sky -> mint (135deg, midpoint at 45%)
    val logoGradient = Brush.linearGradient(
        0f to Color(0xFF0A84FF), 0.45f to Color(0xFF5EC8FF), 1f to Color(0xFF30D1A2),
        start = Offset(0f, 0f), end = Offset.Infinite
    )

    // Avatar: blue -> sky (135deg)
    val avatarGradient = Brush.linearGradient(
        listOf(Color(0xFF0A84FF), Color(0xFF5EC8FF)),
        start = Offset(0f, 0f), end = Offset.Infinite
    )

    val action      = Color(0xFF0A84FF)
    val fieldFill    = Color(0xFFF6F7F9)
    val glassBorder  = Color(0x12111827)   // ~7% ink hairline
}

/**
 * Type scale tuned to the prototype: tight headings, comfortable body.
 * Uses the platform default sans (Roboto on Android, San Francisco on
 * iOS). To swap in Inter, bundle the TTFs under composeResources/font
 * and set fontFamily on these styles — see the design follow-up notes.
 */
private val OpsTypography = Typography(
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, lineHeight = 30.sp),
    titleLarge    = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleSmall    = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp),
    labelMedium   = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp),
    labelSmall    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
)

@Composable
fun OpsTheme(content: @Composable () -> Unit) {
    // Always use the light (white) scheme. The app is a bright, document-style
    // surface; we don't follow the system dark setting. To re-enable dark mode
    // later, swap this back to: if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = LightColors,
        typography = OpsTypography,
        content = content
    )
}
