package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.extensions.map
import com.johnkuper.currenciesconverter.extensions.repeat
import com.johnkuper.currenciesconverter.network.GetRatesParams
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
    val converterItemsLiveData: LiveData<List<ConverterItem>>

    private var baseCurrency: String = DEFAULT_BASE_CURRENCY
    private var converterAmount: Double = DEFAULT_AMOUNT
    private var converterItems = mutableListOf<ConverterItem>()
    private var ratesDisposable: Disposable? = null

    init {
        converterItemsLiveData = _currenciesRates.map { result ->
            toConverterItems((result as? ResponseResult.Success)?.data.orEmpty())
        }
    }

    override fun onCleared() {
        stopRatesPolling()
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

    fun startRatesPolling(isCurrencyChanged: Boolean = false) {
        ratesDisposable?.dispose()
        val params = GetRatesParams(
            baseCurrency,
            isCurrencyChanged,
            (_currenciesRates.value as? ResponseResult.Success)?.data ?: linkedMapOf()
        )
        ratesDisposable = getRatesUseCase(params, _currenciesRates)
    }

    fun stopRatesPolling() {
        ratesDisposable?.dispose()
        ratesDisposable = null
    }

    fun onItemsChanged(items: List<ConverterItem>) {
        baseCurrency = items.first().code
        converterAmount = items.first().amount
        converterItems.clear()
        converterItems.addAll(items)
        startRatesPolling(true)
    }

    fun onAmountChanged(amount: Double) {
        kuperLog("ConverterViewModel.onAmountChanged(), amount=$amount")
        converterAmount = amount
        _currenciesRates.repeat()
    }
}
