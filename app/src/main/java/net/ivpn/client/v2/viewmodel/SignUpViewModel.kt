package net.ivpn.client.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.android.billingclient.api.SkuDetails
import net.ivpn.client.common.billing.BillingListener
import net.ivpn.client.common.billing.BillingManagerWrapper
import net.ivpn.client.common.billing.addfunds.Period
import net.ivpn.client.common.billing.addfunds.Plan
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.common.utils.DateUtil
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.Responses
import net.ivpn.client.rest.data.addfunds.NewAccountRequestBody
import net.ivpn.client.rest.data.addfunds.NewAccountResponse
import net.ivpn.client.rest.requests.common.Request
import org.slf4j.LoggerFactory
import javax.inject.Inject
import kotlin.math.floor

@ApplicationScope
class SignUpViewModel @Inject constructor(
        private val billingManager: BillingManagerWrapper,
        private val userPreference: UserPreference,
        private val settings: Settings,
        private val serversRepository: ServersRepository,
        private val httpClientFactory: HttpClientFactory
) : BillingListener {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BillingListener::class.java)
    }

    val selectedPeriod = ObservableField<Period?>()
    val selectedPlan = ObservableField<Plan>()
    val dataLoading = ObservableBoolean()

    val oneWeek = ObservableField<SkuDetails>()
    val oneMonth = ObservableField<SkuDetails>()
    val oneYear = ObservableField<SkuDetails>()
    val twoYear = ObservableField<SkuDetails>()
    val threeYear = ObservableField<SkuDetails>()
    val oneYearDiscount = ObservableField<String>()
    val twoYearDiscount = ObservableField<String>()
    val threeYearDiscount = ObservableField<String>()

    val blankAccountID = ObservableField<String>()
    var blankAccountGeneratedDate = 0L

    var navigator: SignUpNavigator? = null
    var creationNavigator: CreateAccountNavigator? = null

    init {
        dataLoading.set(false)
        blankAccountID.set(userPreference.blankUsername)
        blankAccountGeneratedDate = userPreference.blankUsernameGeneratedDate
    }

    fun selectPeriod(period: Period) {
        selectedPeriod.set(period)
    }

    fun initOffers() {
        dataLoading.set(true)
        selectedPeriod.set(null)
        oneWeek.set(null)
        oneMonth.set(null)
        oneYear.set(null)
        twoYear.set(null)
        threeYear.set(null)

        billingManager.setBillingListener(this)
    }

    fun purchase(activity: Activity) {
        getProperSkuDetail()?.let {
            billingManager.setSkuDetails(it)
            billingManager.setProductName(getProperProductName())
            billingManager.startPurchase(activity)
        }
    }

    fun getPrice(skuDetails: SkuDetails?): String? {
        return skuDetails?.price
    }

    fun isBlankAccountFresh(): Boolean {
        return System.currentTimeMillis() - blankAccountGeneratedDate < DateUtil.WEEK
    }

    fun reset() {
        dataLoading.set(false)
        selectedPeriod.set(null)
        oneWeek.set(null)
        oneMonth.set(null)
        oneYear.set(null)
        twoYear.set(null)
        threeYear.set(null)
        blankAccountID.set(null)
        blankAccountGeneratedDate = 0L
    }

    fun createNewAccount() {
        dataLoading.set(true)
        val requestBody = NewAccountRequestBody("IVPN Standard")
        val request = Request<NewAccountResponse>(settings, httpClientFactory, serversRepository, Request.Duration.LONG)
        request.start({ api: IVPNApi -> api.newAccount(requestBody) }, object : RequestListener<NewAccountResponse> {
            override fun onSuccess(response: NewAccountResponse) {
                LOGGER.info("SUCCESS, response = $response")
                dataLoading.set(false)
                if (response.status == Responses.SUCCESS) {
                    userPreference.putBlankUsername(response.accountId)
                    userPreference.putBlankUsernameGenerationDate(System.currentTimeMillis())
                    blankAccountGeneratedDate = System.currentTimeMillis()
                    blankAccountID.set(response.accountId)
                    creationNavigator?.onAccountCreationSuccess()
                } else {
                    creationNavigator?.onAccountCreationError()
                }
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("ERROR, throwable = $throwable")
                dataLoading.set(false)
                creationNavigator?.onAccountCreationError()
            }

            override fun onError(error: String) {
                LOGGER.error("ERROR, error = $error")
                dataLoading.set(false)
                creationNavigator?.onAccountCreationError()
            }
        })
    }

    private fun getProperSkuDetail(): SkuDetails? {
        return when(selectedPeriod.get()) {
            Period.ONE_WEEK -> oneWeek.get()
            Period.ONE_MONTH -> oneMonth.get()
            Period.ONE_YEAR -> oneYear.get()
            Period.TWO_YEARS -> twoYear.get()
            Period.THREE_YEARS -> threeYear.get()
            null -> null
        }
    }

    private fun getProperProductName(): String? {
        return selectedPlan.get()?.productName
    }

    private fun checkSkuDetails() {
        selectedPlan.get()?.let { plan ->
            val skuList = ArrayList<String>()
            for (period in Period.values()) {
                skuList.add(plan.skuPath + period.skuPath)
            }
            billingManager.checkSkuDetails(skuList)
        }
    }

    override fun onInitStateChanged(isInit: Boolean, errorCode: Int) {
        LOGGER.info("Is billing manager init? - $isInit, errorCode = $errorCode")
        if (isInit) {
            checkSkuDetails()
        } else {
            handleError(errorCode)
        }
    }

    override fun onCheckingSkuDetailsSuccess(skuDetailsList: MutableList<SkuDetails>?) {
        LOGGER.info("processSkuDetails")
        dataLoading.set(false)

        skuDetailsList?.let { details ->
            selectedPlan.get()?.let { plan ->
                for (skuDetails in details) {
                    LOGGER.info("Check ${skuDetails.sku}")
                    when (skuDetails.sku) {
                        plan.skuPath + Period.ONE_WEEK.skuPath -> {
                            oneWeek.set(skuDetails)
                        }
                        plan.skuPath + Period.ONE_MONTH.skuPath -> {
                            oneMonth.set(skuDetails)
                        }
                        plan.skuPath + Period.ONE_YEAR.skuPath -> {
                            oneYear.set(skuDetails)
                        }
                        plan.skuPath + Period.TWO_YEARS.skuPath -> {
                            twoYear.set(skuDetails)
                        }
                        plan.skuPath + Period.THREE_YEARS.skuPath -> {
                            threeYear.set(skuDetails)
                        }
                    }
                }

                calculateYearDiscount()
                calculateTwoYearDiscount()
                calculateThreeYearDiscount()

                selectedPeriod.set(Period.ONE_YEAR)
            }
        }
    }

    override fun onPurchaseError(errorStatus: Int, errorMessage: String?) {
    }

    override fun onPurchaseAlreadyDone() {
    }

    override fun onCreateAccountFinish() {
        blankAccountID.set(null)
        userPreference.putBlankUsername(null)
        navigator?.onCreateAccountFinish()
    }

    override fun onAddFundsFinish() {
        navigator?.onAddFundsFinish()
    }

    override fun onPurchaseStateChanged(state: BillingManagerWrapper.PurchaseState?) {
    }

    private fun handleError(error: Int) {
        dataLoading.set(false)
        navigator?.onGoogleConnectFailure()
    }

    private fun calculateYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var yearPrice: Long = 0
        oneYear.get()?.let {
            yearPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - yearPrice / (monthPrice * 12.0)))
        oneYearDiscount.set("-${discount.toInt()}%")
    }

    private fun calculateTwoYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var twoYearsPrice: Long = 0
        twoYear.get()?.let {
            twoYearsPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - twoYearsPrice / (monthPrice * 24.0)))
        twoYearDiscount.set("-${discount.toInt()}%")
    }

    private fun calculateThreeYearDiscount() {
        var monthPrice: Long = 0
        oneMonth.get()?.let {
            monthPrice = it.priceAmountMicros
        } ?: return
        var threeYearsPrice: Long = 0
        threeYear.get()?.let {
            threeYearsPrice = it.priceAmountMicros
        } ?: return
        if (monthPrice == 0L) {
            return
        }
        val discount = floor(100 * (1 - threeYearsPrice / (monthPrice * 36.0)))
        threeYearDiscount.set("-${discount.toInt()}%")
    }

    interface SignUpNavigator {
        fun onCreateAccountFinish()

        fun onAddFundsFinish()

        fun onGoogleConnectFailure()
    }

    interface CreateAccountNavigator {
        fun onAccountCreationSuccess()

        fun onAccountCreationError()
    }
}