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

class ConverterViewModel @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase
) : ViewModel() {

    private val _currenciesRates = MutableLiveData<ResponseResult<LinkedHashMap<String, Double>>>()
    val converterItemsLiveData: LiveData<List<ConverterItem>>

    private var baseCurrency: String = "EUR"
    private var converterAmount: Double = 100.0
    private var converterItems = mutableListOf<ConverterItem>()

    private var ratesDisposable: Disposable? = null

    init {
        converterItemsLiveData = _currenciesRates.map { result ->
            val currencyToRate = (result as? ResponseResult.Success)?.data.orEmpty()
            if (converterItems.isEmpty()) {
                currencyToRate.mapTo(converterItems) { ConverterItem(it.key, it.value * converterAmount) }
            } else {
                converterItems = converterItems.mapNotNull { item ->
                    currencyToRate[item.code]?.let { item.copy(amount = it * converterAmount) }
                }.toMutableList()
            }
            converterItems
        }
    }

    override fun onCleared() {
        stopRatesUpdates()
    }

    fun startRatesUpdates(isCurrencyChanged: Boolean = false) {
        ratesDisposable?.dispose()
        val params = GetRatesParams(
            baseCurrency,
            isCurrencyChanged,
            (_currenciesRates.value as? ResponseResult.Success)?.data ?: linkedMapOf()
        )
        ratesDisposable = getRatesUseCase(params, _currenciesRates)
    }

    fun stopRatesUpdates() {
        ratesDisposable?.dispose()
        ratesDisposable = null
    }

    fun onItemsChanged(items: List<ConverterItem>) {
        baseCurrency = items.first().code
        converterAmount = items.first().amount
        converterItems.clear()
        converterItems.addAll(items)
        startRatesUpdates(true)
    }

    fun onAmountChanged(amount: Double) {
        kuperLog("ConverterViewModel.onAmountChanged(), amount=$amount")
        converterAmount = amount
        _currenciesRates.repeat()
    }
}