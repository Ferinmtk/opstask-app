package com.simplifybiz.ops.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: String = "",
    val message: String = ""
)

class ApiException(val code: String, message: String) : Exception(message)
