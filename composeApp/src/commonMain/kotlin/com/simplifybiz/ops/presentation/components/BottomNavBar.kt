package com.simplifybiz.ops.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route

private val Action = Color(0xFF0A84FF)
private val InkSoft = Color(0xFF6B7280)

/**
 * Floating glass-style tab bar. Rendered inside the Scaffold bottomBar
 * slot (so content insets still reserve its height and nothing overlaps),
 * but styled as a rounded white pill that floats with a soft shadow and
 * hairline — the iOS-on-white look.
 */
@Composable
fun BottomNavBar(navigator: AppNavigator, current: Route) {
    val onTasksTab = current is Route.TasksHome ||
        current is Route.ViewTasks ||
        current is Route.Inbox ||
        current is Route.Status ||
        current is Route.SubmitTask ||
        current is Route.TaskDetail

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0x12111827)),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab("Tasks", Icons.Outlined.List, onTasksTab) { navigator.goto(Route.TasksHome) }
                NavTab("Messages", Icons.Outlined.Email, current is Route.Messages) { navigator.goto(Route.Messages) }
                NavTab("Profile", Icons.Outlined.AccountCircle, current is Route.Profile) { navigator.goto(Route.Profile) }
            }
        }
    }
}

@Composable
private fun NavTab(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0x1F0A84FF) else Color.Transparent)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) Action else InkSoft,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            color = if (selected) Action else InkSoft,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
