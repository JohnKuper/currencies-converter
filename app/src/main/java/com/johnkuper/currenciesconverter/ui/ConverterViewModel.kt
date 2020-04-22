package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.network.BASE_CURRENCY_RATE
import com.johnkuper.currenciesconverter.network.GetRatesUseCase
import com.johnkuper.currenciesconverter.network.ResponseResult
import io.reactivex.disposables.Disposable
import javax.inject.Inject

fun kuperLog(message: String) {
    Log.d("JohnKuper", message)
}

const val DEFAULT_AMOUNT = 100.0
const val DEFAULT_BASE_CURRENCY = "EUR"

class ConverterViewModel @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase
) : ViewModel() {

    private val _currenciesRates = MutableLiveData<ResponseResult<LinkedHashMap<String, Double>>>()
    val converterItemsLiveData = MutableLiveData<List<ConverterItem>>()

    private var baseCurrency: String = DEFAULT_BASE_CURRENCY
    private var converterAmount: Double = DEFAULT_AMOUNT
    private var converterItems = mutableListOf<ConverterItem>()
    lateinit var lastRates: LinkedHashMap<String, Double>

    private var ratesDisposable: Disposable? = null

    private val currenciesRatesObserver: ((ResponseResult<LinkedHashMap<String, Double>>) -> Unit) = { result ->
        if (result is ResponseResult.Success) {
            lastRates = result.data
            converterItemsLiveData.value = toConverterItems(result.data)
        }
    }

    init {
        _currenciesRates.observeForever(currenciesRatesObserver)
    }

    override fun onCleared() {
        _currenciesRates.removeObserver(currenciesRatesObserver)
    }

    private fun toConverterItems(rates: Map<String, Double>): List<ConverterItem> {
        return if (converterItems.isEmpty()) {
            rates.map { ConverterItem(it.key, it.value * converterAmount) }
        } else {
            converterItems.mapNotNull { item ->
                rates[item.code]?.let { item.copy(amount = it * converterAmount) }
            }
        }.also { converterItems = it.toMutableList() }
    }

    private fun getRecalculatedRates(): LinkedHashMap<String, Double> {
        val baseCurrencyOldRate = requireNotNull(lastRates.remove(baseCurrency))
        return linkedMapOf(baseCurrency to BASE_CURRENCY_RATE).apply {
            putAll(lastRates.mapValues { (_, rate) ->
                baseCurrencyOldRate.takeIf { it > 0.0 }?.let { rate / it } ?: 0.0
            })
        }
    }

    fun startRatesPolling() {
        kuperLog("startRatesPolling()")
        ratesDisposable?.dispose()
        ratesDisposable = getRatesUseCase(baseCurrency, _currenciesRates)
    }

    fun stopRatesPolling() {
        kuperLog("stopRatesPolling()")
        ratesDisposable?.dispose()
        ratesDisposable = null
    }

    fun onItemsChanged(items: List<ConverterItem>) {
        baseCurrency = items.first().code
        converterAmount = items.first().amount
        converterItems.clear()
        converterItems.addAll(items)
        _currenciesRates.value = ResponseResult.Success(getRecalculatedRates())
        startRatesPolling()
    }

    fun onAmountChanged(amount: Double) {
        kuperLog("ConverterViewModel.onAmountChanged(), amount=$amount")
        converterAmount = amount
        converterItemsLiveData.value = toConverterItems(lastRates)
    }
}
