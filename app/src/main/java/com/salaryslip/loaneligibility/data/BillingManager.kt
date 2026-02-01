package com.salaryslip.loaneligibility.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val purchaseDao: PurchaseDao
) : PurchasesUpdatedListener {
    
    companion object {
        const val PRODUCT_ID_PRO = "loan_pro_unlock"
    }
    
    private var billingClient: BillingClient? = null
    
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Retry connection
            }
        })
    }
    
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }
    
    private fun handlePurchases(purchases: List<com.android.billingclient.api.Purchase>) {
        var hasPremium = false
        for (purchase in purchases) {
            if (purchase.products.contains(PRODUCT_ID_PRO) &&
                purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
            ) {
                hasPremium = true
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        }
        _isPremium.value = hasPremium
    }
    
    private fun acknowledgePurchase(purchase: com.android.billingclient.api.Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Save to database
                savePurchaseToDatabase(purchase)
            }
        }
    }
    
    private fun savePurchaseToDatabase(purchase: com.android.billingclient.api.Purchase) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            purchaseDao.insertPurchase(
                Purchase(
                    productId = PRODUCT_ID_PRO,
                    purchaseToken = purchase.purchaseToken,
                    purchaseTime = purchase.purchaseTime,
                    acknowledged = true
                )
            )
        }
    }
    
    fun launchPurchaseFlow(activity: Activity) {
        _purchaseState.value = PurchaseState.Loading
        
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PRO)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                billingClient?.launchBillingFlow(activity, billingFlowParams)
            } else {
                _purchaseState.value = PurchaseState.Error("Failed to load product details")
            }
        }
    }
    
    fun restorePurchases() {
        _purchaseState.value = PurchaseState.Loading
        queryPurchases()
        
        // After a delay, set state based on premium status
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(1000)
            if (_isPremium.value) {
                _purchaseState.value = PurchaseState.Success
            } else {
                _purchaseState.value = PurchaseState.Error("No purchases found")
            }
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<com.android.billingclient.api.Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    handlePurchases(purchases)
                    _purchaseState.value = PurchaseState.Success
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }
    
    fun destroy() {
        billingClient?.endConnection()
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
