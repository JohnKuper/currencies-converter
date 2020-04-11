package com.johnkuper.currenciesconverter.api

import com.google.gson.annotations.SerializedName

data class RatesResponse(
    @SerializedName("baseCurrency") val baseCurrency: String
)