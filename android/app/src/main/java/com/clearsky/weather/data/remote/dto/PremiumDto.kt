package com.clearsky.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PremiumVerifyRequest(
    val deviceId: String,
    val purchaseToken: String,
    val productId: String
)

@Serializable
data class PremiumVerifyResponseDto(
    val premium: Boolean,
    val productId: String
)

@Serializable
data class PremiumStatusResponseDto(
    val premium: Boolean,
    val productId: String? = null,
    val purchaseTime: Long? = null
)
