package com.johnkuper.currenciesconverter

import com.google.gson.Gson
import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.api.CurrenciesRatesResponse
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.ui.ConverterViewModel
import com.johnkuper.currenciesconverter.ui.CurrenciesResources
import com.johnkuper.currenciesconverter.utils.fromJson
import com.johnkuper.currenciesconverter.utils.readTextResource
import com.jraska.livedata.TestObserver
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [23],
    application = TestConverterApplication::class
)
class ConverterViewModelTest {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    @Inject
    lateinit var converterViewModel: ConverterViewModel

    @Inject
    lateinit var currenciesApi: CurrenciesApi

    @Inject
    lateinit var resources: CurrenciesResources

    @Inject
    lateinit var gson: Gson

    private lateinit var itemsObserver: TestObserver<List<ConverterItem>>

    private val eurResponse by lazy { getRatesResponse("eur_rates_response.json") }
    private val audResponse by lazy { getRatesResponse("aud_rates_response.json") }

    @Before
    fun setUp() {
        TestConverterApplication.testAppComponent.inject(this)
        itemsObserver = converterViewModel.converterItemsLiveData.test()
    }

    private fun CurrenciesRatesResponse.toConverterItems(amount: Double): List<ConverterItem> {
        return toRates().map { getConverterItem(it.key, it.value, amount) }
    }

    private fun getConverterItem(currencyCode: String, rate: Double, amount: Double): ConverterItem {
        return ConverterItem(
            currencyCode,
            resources.getCurrencyName(currencyCode),
            resources.getFlagUri(currencyCode),
            rate * amount
        )
    }

    private fun CurrenciesRatesResponse.toRates(): LinkedHashMap<String, Double> {
        return linkedMapOf(baseCurrency to BASE_CURRENCY_RATE).apply { putAll(rates) }
    }

    private fun getRatesResponse(path: String) = gson.fromJson<CurrenciesRatesResponse>(path.readTextResource())

    private fun mockRatesResponse(currency: String, response: CurrenciesRatesResponse) {
        whenever(currenciesApi.getRates(currency)).thenReturn(Observable.just(response))
    }

    @Test
    fun initial_polling_leads_to_updated_converter_items() {
        mockRatesResponse(DEFAULT_BASE_CURRENCY, eurResponse)
        converterViewModel.startRatesPolling()

        itemsObserver.assertValue(eurResponse.toConverterItems(DEFAULT_AMOUNT))
        itemsObserver.assertHistorySize(1)
    }

    @Test
    fun on_items_changed_leads_to_recalculated_then_updated_converter_items() {
        mockRatesResponse(DEFAULT_BASE_CURRENCY, eurResponse)
        converterViewModel.startRatesPolling()

        val audMovedOnTopItems = itemsObserver.value().toMutableList().apply { add(0, removeAt(1)) }
        val audTopItem = audMovedOnTopItems[0]
        mockRatesResponse(audTopItem.currencyCode, audResponse)
        converterViewModel.onItemsChanged(audMovedOnTopItems)

        val eurExpectedItems = eurResponse.toConverterItems(DEFAULT_AMOUNT)

        val audOldRate = requireNotNull(eurResponse.rates[audTopItem.currencyCode])
        val audRecalculatedRates = eurResponse.toRates().mapValues { it.value / audOldRate }
        val audRecalculatedItems = audMovedOnTopItems.mapNotNull { item ->
            audRecalculatedRates[item.currencyCode]?.let { item.copy(amount = it * audTopItem.amount) }
        }

        val audRates = audResponse.toRates()
        val audExpectedItems = audMovedOnTopItems.mapNotNull { item ->
            audRates[item.currencyCode]?.let { item.copy(amount = it * audTopItem.amount) }
        }
        itemsObserver.assertValueHistory(eurExpectedItems, audRecalculatedItems, audExpectedItems)
        itemsObserver.assertHistorySize(3)
    }

    @Test
    fun on_amount_changed_leads_to_updated_items() {
        mockRatesResponse(DEFAULT_BASE_CURRENCY, eurResponse)
        converterViewModel.startRatesPolling()

        val eurRates = eurResponse.toRates()
        val amounts = arrayOf(10.0, 1.0, 12.0, 123.0, 1230.0)
        amounts.forEach { converterViewModel.onAmountChanged(it) }
        itemsObserver.assertValueHistory(
            eurRates.map { getConverterItem(it.key, it.value, DEFAULT_AMOUNT) },
            eurRates.map { getConverterItem(it.key, it.value, amounts[0]) },
            eurRates.map { getConverterItem(it.key, it.value, amounts[1]) },
            eurRates.map { getConverterItem(it.key, it.value, amounts[2]) },
            eurRates.map { getConverterItem(it.key, it.value, amounts[3]) },
            eurRates.map { getConverterItem(it.key, it.value, amounts[4]) }
        )
    }
}