package com.johnkuper.currenciesconverter.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.johnkuper.currenciesconverter.api.CurrenciesApi
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

fun kuperLog(message: String) {
    Log.d("JohnKuper", message)
}

class ConverterViewModel @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : ViewModel() {

    fun getCurrenciesRates() {
        currenciesApi.getRates()
            .subscribeOn(Schedulers.io())
            .subscribe { kuperLog(it.toString()) }
    }
}