package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.api.RatesResponse
import com.johnkuper.currenciesconverter.network.GetCurrenciesRatesUseCase
import com.johnkuper.currenciesconverter.network.ResponseResult
import io.reactivex.disposables.Disposable
import javax.inject.Inject

fun kuperLog(message: String) {
    Log.d("JohnKuper", message)
}

class ConverterViewModel @Inject constructor(
    private val getCurrenciesRatesUseCase: GetCurrenciesRatesUseCase
) : ViewModel() {

    val currenciesLiveData = MutableLiveData<ResponseResult<RatesResponse>>()

    var ratesDisposable: Disposable? = null

    override fun onCleared() {
        ratesDisposable?.dispose()
    }

    fun startCurrenciesRatesPolling() {
        ratesDisposable = getCurrenciesRatesUseCase(currenciesLiveData)
    }
}