package com.simplifybiz.ops.presentation.login

import com.simplifybiz.ops.data.ApiErrorMessages
import com.simplifybiz.ops.data.ApiException
import com.simplifybiz.ops.data.auth.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val loading: Boolean = false,
    val errorTitle: String? = null,
    val errorBody: String? = null,
    val success: Boolean = false
)

class LoginViewModel(private val auth: AuthRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState(
                errorTitle = "Missing info",
                errorBody = "Enter both your email and password."
            )
            return
        }
        _state.value = LoginState(loading = true)
        scope.launch {
            val result = auth.login(email.trim(), password)
            _state.value = if (result.isSuccess) {
                LoginState(success = true)
            } else {
                val (code, msg) = when (val ex = result.exceptionOrNull()) {
                    is ApiException -> ex.code to (ex.message ?: "")
                    else -> "" to (result.exceptionOrNull()?.message ?: "")
                }
                val (title, body) = ApiErrorMessages.forLogin(code, msg)
                LoginState(errorTitle = title, errorBody = body)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorTitle = null, errorBody = null)
    }
}
