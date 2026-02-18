package com.clearsky.weather.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearsky.weather.domain.model.PremiumStatus
import com.clearsky.weather.domain.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val productName: String? = null,
    val productPrice: String? = null,
    val error: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        checkPremiumStatus()
        observeBilling()
    }

    private fun checkPremiumStatus() {
        viewModelScope.launch {
            val status = premiumRepository.checkPremiumStatus()
            _uiState.update {
                it.copy(isLoading = false, isPremium = status.isPremium)
            }
        }
    }

    fun initBilling() {
        billingManager.startConnection { purchase ->
            viewModelScope.launch {
                premiumRepository.verifyPurchase(
                    purchaseToken = purchase.purchaseToken,
                    productId = BillingManager.PRODUCT_ID
                )
            }
        }
    }

    private fun observeBilling() {
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                _uiState.update {
                    it.copy(
                        isPremium = state.isPurchased || it.isPremium,
                        productName = state.productName ?: it.productName,
                        productPrice = state.productPrice ?: it.productPrice,
                        error = state.error
                    )
                }
            }
        }
    }

    fun purchasePremium(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
