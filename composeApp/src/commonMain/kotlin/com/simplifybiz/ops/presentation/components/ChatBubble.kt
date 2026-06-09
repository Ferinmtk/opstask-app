package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OutBlue = Color(0xFF0A84FF)
private val InGrey = Color(0xFFEEF0F3)
private val InkSoft = Color(0xFF6B7280)

/**
 * One message in the task notes thread. Outgoing (the current user's own
 * notes) sit on the right in a blue bubble with white text. Incoming
 * (staff or anyone else) sit on the left in a light grey bubble with the
 * author name shown above the body.
 */
@Composable
fun ChatBubble(
    body: String,
    timeLabel: String,
    isOutgoing: Boolean,
    authorName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!isOutgoing && authorName.isNotBlank()) {
            Text(
                authorName,
                color = OutBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (isOutgoing) OutBlue else InGrey,
                        shape = if (isOutgoing)
                            RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
                        else
                            RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    body,
                    color = if (isOutgoing) Color.White else Color(0xFF0B1220),
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp
                )
                if (timeLabel.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        timeLabel,
                        color = if (isOutgoing) Color(0xD9FFFFFF) else InkSoft,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
