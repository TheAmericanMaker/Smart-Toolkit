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
import com.android.billingclient.api.queryPurchasesAsync
import com.smarttoolkit.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

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
    val isBillingAvailable: Boolean =
        BuildConfig.REMOVE_ADS_PURCHASE_ENABLED && BuildConfig.REMOVE_ADS_PRODUCT_ID.isNotBlank()

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState

    private val _removeAdsPrice = MutableStateFlow<String?>(null)
    val removeAdsPrice: StateFlow<String?> = _removeAdsPrice

    private var cachedProductDetails: ProductDetails? = null

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
                _billingState.value = BillingState.Error(
                    billingResult.debugMessage.ifBlank { "Purchase failed. Please try again." }
                )
            }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        if (isBillingAvailable) {
            connectBillingClient()
        }
    }

    private fun connectBillingClient() {
        if (!isBillingAvailable || billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                cachedProductDetails = null
            }
        })
    }

    private fun ensureConnected(onReady: (Boolean) -> Unit) {
        if (!isBillingAvailable) {
            onReady(false)
            return
        }

        if (billingClient.isReady) {
            onReady(true)
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    onReady(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    cachedProductDetails = null
                }
            })
        }
    }

    private suspend fun awaitConnection(): Boolean = suspendCancellableCoroutine { continuation ->
        ensureConnected { connected ->
            if (continuation.isActive) {
                continuation.resume(connected)
            }
        }
    }

    private fun refreshProductDetails() {
        ensureConnected { connected ->
            if (connected) {
                queryProductDetails(reportErrors = false)
            }
        }
    }

    private fun queryProductDetails(
        reportErrors: Boolean,
        onResult: (ProductDetails?) -> Unit = {}
    ) {
        if (!isBillingAvailable) {
            cachedProductDetails = null
            _removeAdsPrice.value = null
            onResult(null)
            return
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.REMOVE_ADS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            val productDetails = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.firstOrNull()
            } else {
                null
            }

            cachedProductDetails = productDetails
            _removeAdsPrice.value = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice

            if (productDetails == null && reportErrors) {
                _billingState.value = BillingState.Error(
                    "Remove Ads is unavailable in Google Play for this build."
                )
            }

            onResult(productDetails)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.products.contains(BuildConfig.REMOVE_ADS_PRODUCT_ID)) {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        if (!purchase.isAcknowledged) {
                            val ackParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(ackParams) { }
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
        if (!isBillingAvailable || !awaitConnection()) return false

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return false

        val removeAdsPurchase = result.purchasesList.firstOrNull { purchase ->
            purchase.products.contains(BuildConfig.REMOVE_ADS_PRODUCT_ID) &&
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
        if (!isBillingAvailable) {
            _billingState.value = BillingState.Error(
                "This build does not include Google Play purchases."
            )
            return
        }

        _billingState.value = BillingState.Idle

        cachedProductDetails?.let { productDetails ->
            launchBillingFlow(activity, productDetails)
            return
        }

        ensureConnected { connected ->
            if (!connected) {
                _billingState.value = BillingState.Error("Could not connect to Google Play.")
                return@ensureConnected
            }

            queryProductDetails(reportErrors = true) { productDetails ->
                if (productDetails != null) {
                    launchBillingFlow(activity, productDetails)
                }
            }
        }
    }

    private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}
