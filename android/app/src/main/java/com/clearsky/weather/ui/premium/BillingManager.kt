package com.clearsky.weather.ui.premium

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class BillingState(
    val isConnected: Boolean = false,
    val productName: String? = null,
    val productPrice: String? = null,
    val isPurchased: Boolean = false,
    val error: String? = null
)

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_ID = "clearsky_premium"
    }

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private var onPurchaseComplete: ((Purchase) -> Unit)? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun startConnection(onPurchaseComplete: (Purchase) -> Unit) {
        this.onPurchaseComplete = onPurchaseComplete

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _billingState.value = _billingState.value.copy(isConnected = true)
                    queryProductDetails()
                    queryExistingPurchases()
                } else {
                    _billingState.value = _billingState.value.copy(
                        error = "Billing setup failed: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = _billingState.value.copy(isConnected = false)
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val details = productDetailsList.productDetailsList?.firstOrNull()
                if (details != null) {
                    val offer = details.oneTimePurchaseOfferDetails
                    _billingState.value = _billingState.value.copy(
                        productName = details.name,
                        productPrice = offer?.formattedPrice
                    )
                }
            }
        }
    }

    private fun queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (premiumPurchase != null) {
                    _billingState.value = _billingState.value.copy(isPurchased = true)
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val details = productDetailsList.productDetailsList?.firstOrNull() ?: return@queryProductDetailsAsync

                val offerToken = details.oneTimePurchaseOfferDetails?.let { "" } ?: return@queryProductDetailsAsync

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build()
                        )
                    )
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _billingState.value = _billingState.value.copy(isPurchased = true)
                    acknowledgePurchase(purchase)
                    onPurchaseComplete?.invoke(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            _billingState.value = _billingState.value.copy(error = null)
        } else {
            _billingState.value = _billingState.value.copy(
                error = "Purchase failed: ${billingResult.debugMessage}"
            )
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode != BillingResponseCode.OK) {
                _billingState.value = _billingState.value.copy(
                    error = "Acknowledgment failed: ${billingResult.debugMessage}"
                )
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
