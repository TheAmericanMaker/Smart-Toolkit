package com.smarttoolkit.app.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class BillingState {
    data object Idle : BillingState()
    data object Pending : BillingState()
    data object Purchased : BillingState()
    data class Error(val message: String) : BillingState()
}

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val PRODUCT_ID_REMOVE_ADS = "remove_ads"
    }

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                handlePurchases(purchases)
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingState.value = BillingState.Purchased
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Idle
            }
            else -> {
                _billingState.value = BillingState.Error(billingResult.debugMessage)
            }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        connectBillingClient()
    }

    private fun connectBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // Connection established; ready for queries
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection on next operation
            }
        })
    }

    private fun ensureConnected(onReady: () -> Unit) {
        if (billingClient.isReady) {
            onReady()
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onReady()
                    }
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.products.contains(PRODUCT_ID_REMOVE_ADS)) {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(ackParams) { _ -> }
                        }
                        _billingState.value = BillingState.Purchased
                    }
                    Purchase.PurchaseState.PENDING -> {
                        _billingState.value = BillingState.Pending
                    }
                    else -> {}
                }
            }
        }
    }

    suspend fun queryAndAcknowledgePurchases(): Boolean {
        if (!billingClient.isReady) return false

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return false

        val removeAdsPurchase = result.purchasesList.firstOrNull { purchase ->
            purchase.products.contains(PRODUCT_ID_REMOVE_ADS) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        } ?: return false

        if (!removeAdsPurchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(removeAdsPurchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams)
        }

        return true
    }

    fun launchBillingFlow(activity: Activity) {
        _billingState.value = BillingState.Idle

        ensureConnected {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_REMOVE_ADS)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK ||
                    productDetailsList.isEmpty()
                ) {
                    _billingState.value = BillingState.Error("Product not found")
                    return@queryProductDetailsAsync
                }

                val productDetails: ProductDetails = productDetailsList.first()
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }
}
