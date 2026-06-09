package com.simplifybiz.ops.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.simplifybiz.ops.presentation.AppNavigator
import com.simplifybiz.ops.presentation.Route
import com.simplifybiz.ops.presentation.components.GradientButton
import com.simplifybiz.ops.presentation.theme.OpsBrand
import com.simplifybiz.ops.resources.Res
import com.simplifybiz.ops.resources.icon_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navigator: AppNavigator) {
    val viewModel: LoginViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) navigator.goto(Route.TasksHome)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Brand logo
            Image(
                painter = painterResource(Res.drawable.icon_logo),
                contentDescription = "Simplify Ops logo",
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(18.dp))

            Text("Simplify Ops", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign in with your ops account.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; if (state.errorTitle != null) viewModel.clearError() },
                placeholder = { Text("Email") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; if (state.errorTitle != null) viewModel.clearError() },
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            state.errorTitle?.let { title ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleSmall
                        )
                        state.errorBody?.let { body ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                body,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = "Sign in",
                onClick = { viewModel.login(email, password) },
                enabled = !state.loading,
                loading = state.loading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = OpsBrand.fieldFill,
    unfocusedContainerColor = OpsBrand.fieldFill,
    focusedBorderColor = OpsBrand.action,
    unfocusedBorderColor = OpsBrand.glassBorder,
    cursorColor = OpsBrand.action
)
