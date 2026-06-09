package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifybiz.ops.presentation.theme.OpsColors

/**
 * Soft pill showing a task's workflow stage. Colors come from OpsColors
 * and are chosen by matching the stage label text (which carries the
 * "0 Accept", "1 To do" ... prefix from field 70).
 */
@Composable
fun StageBadge(stage: String, modifier: Modifier = Modifier) {
    if (stage.isBlank()) return   // no empty pill
    val s = stage.lowercase()
    val (bg, fg) = when {
        s.contains("accept")  -> OpsColors.acceptBg to OpsColors.acceptFg
        s.contains("to do") || s.contains("todo") -> OpsColors.todoBg to OpsColors.todoFg
        s.contains("doing")   -> OpsColors.doingBg to OpsColors.doingFg
        s.contains("approve") -> OpsColors.approveBg to OpsColors.approveFg
        s.contains("done")    -> OpsColors.doneBg to OpsColors.doneFg
        else                  -> OpsColors.doneBg to OpsColors.doneFg
    }
    Text(
        text = stage,
        color = fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.2.sp,
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/** Priority colors for the small dot on task rows. */
fun priorityColor(priority: String): Color {
    val p = priority.lowercase()
    return when {
        p.contains("high") || p.startsWith("3") -> OpsColors.prioHigh
        p.contains("low") || p.startsWith("1")  -> OpsColors.prioLow
        else -> OpsColors.prioNormal
    }
}
