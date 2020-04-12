package com.johnkuper.currenciesconverter.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class CurrenciesRatesDeserializer : JsonDeserializer<CurrenciesRates> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CurrenciesRates {
        val rates = mutableListOf<CurrencyRate>()
        json.asJsonObject.entrySet().forEach {
            rates.add(CurrencyRate(it.key, it.value.asDouble))
        }
        return CurrenciesRates(rates)
    }
}