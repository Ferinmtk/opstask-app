package com.simplifybiz.ops.util

/**
 * True when running on iOS. Used for platform-adaptive UI: centered nav
 * titles, chevron back, segmented controls, wheel date picker, no ripple.
 * Android keeps its existing Material behavior untouched.
 */
expect val isIosPlatform: Boolean
