package com.simplifybiz.ops

import androidx.compose.ui.window.ComposeUIViewController
import com.simplifybiz.ops.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) {
    App()
}
