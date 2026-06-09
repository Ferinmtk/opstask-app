package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.presentation.theme.OpsBrand

/**
 * Primary action button with the iOS-blue vertical gradient and a soft
 * blue glow. Falls back to a flat disabled fill while loading/disabled so
 * the gradient never looks "stuck".
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    val interactive = enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (interactive) 12.dp else 0.dp, shape, spotColor = OpsBrand.action)
            .clip(shape)
            .then(
                if (interactive) Modifier.background(OpsBrand.actionGradient)
                else Modifier.background(OpsBrand.action).alpha(0.5f)
            )
            .clickable(enabled = interactive) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}
