package com.johnkuper.currenciesconverter.network

import com.google.gson.annotations.SerializedName

data class RatesResponse(
    @SerializedName("baseCurrency") val baseCurrency: String
)