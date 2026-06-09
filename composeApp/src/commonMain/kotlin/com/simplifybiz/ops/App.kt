package com.simplifybiz.ops

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.simplifybiz.ops.presentation.AppNavHost
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.theme.OpsTheme

@Composable
fun App() {
    OpsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navigator = remember { AppNavigator() }
            AppNavHost(navigator)
        }
    }
}
