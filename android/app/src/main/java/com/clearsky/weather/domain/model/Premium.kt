package com.clearsky.weather.domain.model

data class PremiumStatus(
    val isPremium: Boolean,
    val productId: String? = null,
    val purchaseTime: Long? = null
)
