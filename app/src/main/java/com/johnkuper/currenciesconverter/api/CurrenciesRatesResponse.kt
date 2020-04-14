package com.johnkuper.currenciesconverter.api

import com.google.gson.annotations.SerializedName

data class CurrenciesRatesResponse(
    @SerializedName("baseCurrency") val baseCurrency: String,
    @SerializedName("rates") val rates: Map<String, Double>
)