package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 7dp colored dot used inline before task meta to signal priority. */
@Composable
fun PriorityDot(priority: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(7.dp)
            .background(priorityColor(priority), CircleShape)
    )
}
