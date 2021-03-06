package com.johnkuper.currenciesconverter.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.BASE_CURRENCY_RATE
import com.johnkuper.currenciesconverter.DEFAULT_AMOUNT
import com.johnkuper.currenciesconverter.DEFAULT_BASE_CURRENCY
import com.johnkuper.currenciesconverter.network.GetRatesUseCase
import com.johnkuper.currenciesconverter.network.ResponseResult
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

class ConverterViewModel @Inject constructor(
    private val currenciesResources: CurrenciesResources,
    private val getRatesUseCase: GetRatesUseCase
) : ViewModel() {

    private val currenciesRates = MutableLiveData<ResponseResult<LinkedHashMap<String, Double>>>()
    val converterItemsLiveData = MutableLiveData<List<ConverterItem>>()

    private var baseCurrency: String = DEFAULT_BASE_CURRENCY
    private var converterAmount: Double = DEFAULT_AMOUNT
    private var converterItems = mutableListOf<ConverterItem>()
    private lateinit var lastRates: LinkedHashMap<String, Double>

    private var ratesDisposable: Disposable? = null

    private val currenciesRatesObserver: ((ResponseResult<LinkedHashMap<String, Double>>) -> Unit) = { result ->
        if (result is ResponseResult.Success) {
            lastRates = result.data
            converterItemsLiveData.value = toConverterItems(result.data)
        }
    }

    init {
        currenciesRates.observeForever(currenciesRatesObserver)
    }

    override fun onCleared() {
        currenciesRates.removeObserver(currenciesRatesObserver)
    }

    /**
     * Transforms currencies rates to converter list items.
     * If converter list items are not empty than updates them keeping order.
     */
    private fun toConverterItems(rates: Map<String, Double>): List<ConverterItem> {
        return if (converterItems.isEmpty()) {
            rates.map {
                ConverterItem(
                    it.key,
                    currenciesResources.getCurrencyName(it.key),
                    currenciesResources.getFlagUri(it.key),
                    it.value * converterAmount
                )
            }
        } else {
            converterItems.mapNotNull { item ->
                rates[item.currencyCode]?.let { item.copy(amount = it * converterAmount) }
            }
        }.also { converterItems = it.toMutableList() }
    }

    /**
     * Calculates rates for [baseCurrency] using the latest known currency ratios.
     * It helps to receive new rates after changing the base currency immediately without waiting for server response.
     */
    private fun getRecalculatedRates(baseCurrency: String): LinkedHashMap<String, Double> {
        val baseCurrencyOldRate = requireNotNull(lastRates.remove(baseCurrency))
        return linkedMapOf(baseCurrency to BASE_CURRENCY_RATE).apply {
            putAll(lastRates.mapValues { (_, rate) ->
                baseCurrencyOldRate.takeIf { it > 0.0 }?.let { rate / it } ?: 0.0
            })
        }
    }

    fun startRatesPolling() {
        ratesDisposable?.dispose()
        ratesDisposable = getRatesUseCase(baseCurrency, currenciesRates)
    }

    fun stopRatesPolling() {
        ratesDisposable?.dispose()
        ratesDisposable = null
    }

    fun onItemsChanged(items: List<ConverterItem>) {
        baseCurrency = items.first().currencyCode
        converterAmount = items.first().amount
        converterItems.clear()
        converterItems.addAll(items)
        currenciesRates.value = ResponseResult.Success(getRecalculatedRates(baseCurrency))
        startRatesPolling()
    }

    fun onAmountChanged(amount: Double) {
        converterAmount = amount
        converterItemsLiveData.value = toConverterItems(lastRates)
    }
}
