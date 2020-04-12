package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.api.CurrenciesRatesResponse
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.extensions.map
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

    private val _currenciesRates = MutableLiveData<ResponseResult<CurrenciesRatesResponse>>()
    val converterItems: LiveData<List<ConverterItem>>

    var ratesDisposable: Disposable? = null
    private var converterAmount: Double = 100.0
    private var baseCurrency: String = "EUR"

    init {
        converterItems = _currenciesRates.map {
            val converterItems = mutableListOf(ConverterItem(baseCurrency, converterAmount, true))
            (it as? ResponseResult.Success)?.data?.rates?.mapTo(converterItems) { currencyRate ->
                ConverterItem(currencyRate.code, converterAmount * currencyRate.rate, false)
            }.orEmpty()
        }
    }

    override fun onCleared() {
        ratesDisposable?.dispose()
    }

    fun startRatesPolling() {
        ratesDisposable?.dispose()
        ratesDisposable = getRatesUseCase(baseCurrency, _currenciesRates)
    }

    fun onPrimaryItemChanged(item: ConverterItem) {
        baseCurrency = item.code
        converterAmount = item.amount
        startRatesPolling()
    }
}