package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T,
    val cached: Boolean = false,
    val cachedAt: String? = null
)

@Serializable
data class ApiError(
    val success: Boolean,
    val error: ErrorBody
)

@Serializable
data class ErrorBody(
    val code: String,
    val message: String
)
