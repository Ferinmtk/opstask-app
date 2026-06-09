package com.simplifybiz.ops.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifybiz.ops.data.SessionManager
import com.simplifybiz.ops.data.auth.AuthRepository
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.BottomNavBar
import com.simplifybiz.ops.presentation.components.GlassCard
import com.simplifybiz.ops.presentation.theme.OpsBrand
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navigator: AppNavigator) {
    val session: SessionManager = koinInject()
    val auth: AuthRepository = koinInject()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Refresh profile fields each time the screen opens so changes Andre
    // makes in MemberPress show up without forcing a logout/login.
    LaunchedEffect(Unit) {
        scope.launch { auth.refreshProfile() }
    }

    val name = session.getDisplayName().orEmpty()
    val email = session.getEmail().orEmpty()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
        bottomBar = { BottomNavBar(navigator, current = Route.Profile) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient initials avatar with glow
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(24.dp, CircleShape, spotColor = OpsBrand.action)
                    .clip(CircleShape)
                    .background(OpsBrand.avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials(name, email),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Organization
            InfoCard {
                InfoRow("Organization", session.getOrganization())
                InfoRow("Website", session.getOrganizationWebsite())
                InfoRow("Bills to", session.getBillClientEmail())
            }

            // Contact (only if present)
            val phone = session.getPhone()
            val address = session.getAddress()
            if (phone.isNotBlank() || address.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                InfoCard {
                    InfoRow("Phone", phone)
                    InfoRow("Address", address)
                }
            }

            // Work (only if present)
            val hourly = session.getHourlyRate()
            val github = session.getGithubHandle()
            if (hourly.isNotBlank() || github.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                InfoCard {
                    InfoRow("Hourly rate", hourly)
                    InfoRow("GitHub", github)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    auth.logout()
                    navigator.goto(Route.Login)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = OpsBrand.action
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, OpsBrand.glassBorder),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 15.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign out", style = MaterialTheme.typography.labelLarge) }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    GlassCard {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(
            label.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** First letters of up to two name words; falls back to the email initial. */
private fun initials(name: String, email: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        email.isNotBlank() -> email.first().uppercase()
        else -> "?"
    }
}
