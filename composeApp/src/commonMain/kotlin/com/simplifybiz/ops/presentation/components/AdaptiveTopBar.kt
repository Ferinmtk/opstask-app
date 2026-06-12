package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplifybiz.ops.util.isIosPlatform

/**
 * Platform-adaptive top bar:
 *   iOS     -> CenterAlignedTopAppBar (titles centered, like UINavigationBar)
 *   Android -> standard start-aligned TopAppBar (unchanged behavior)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpsTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    if (isIosPlatform) {
        CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions
        )
    } else {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}

/** Back glyph: thin chevron on iOS, Material arrow on Android. */
@Composable
fun BackIcon() {
    if (isIosPlatform) {
        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "Back")
    } else {
        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
    }
}
