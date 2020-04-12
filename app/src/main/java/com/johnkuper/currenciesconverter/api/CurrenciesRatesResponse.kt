package com.johnkuper.currenciesconverter.api

import com.google.gson.annotations.SerializedName

data class CurrenciesRatesResponse(
    @SerializedName("baseCurrency") val baseCurrency: String,
    @SerializedName("rates") private val currenciesRates: CurrenciesRates
) {

    val rates: List<CurrencyRate>
        get() = currenciesRates.rates
}