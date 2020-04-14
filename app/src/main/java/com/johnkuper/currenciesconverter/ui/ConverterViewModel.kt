package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.domain.ConverterItem
import com.johnkuper.currenciesconverter.extensions.map
import com.johnkuper.currenciesconverter.network.GetRatesParameters
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

    private val _currenciesRates = MutableLiveData<ResponseResult<List<ConverterItem>>>()
    val converterItemsLiveData: LiveData<List<ConverterItem>>

    private var baseCurrency: String = "EUR"
    private var converterAmount: Double = 100.0
    private var converterItems = listOf<ConverterItem>()

    private var ratesDisposable: Disposable? = null

    init {
        converterItemsLiveData = _currenciesRates.map { result ->
            (result as? ResponseResult.Success)?.data.orEmpty().also {
                converterItems = it
            }
        }
    }

    override fun onCleared() {
        ratesDisposable?.dispose()
    }

    fun startRatesPolling() {
        ratesDisposable?.dispose()
        val params = GetRatesParameters(baseCurrency, converterAmount, converterItems)
        ratesDisposable = getRatesUseCase(params, _currenciesRates)
    }

    fun onItemsChanged(items: List<ConverterItem>) {
        baseCurrency = items.first().code
        converterAmount = items.first().amount
        converterItems = items
        startRatesPolling()
    }
}