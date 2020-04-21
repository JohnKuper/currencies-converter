package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BASE_CURRENCY_RATE = 1.0

class GetRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<String, LinkedHashMap<String, Double>>() {

    override fun execute(params: String): Observable<LinkedHashMap<String, Double>> {
        return currenciesApi
            .getRates(params)
            .map {
                linkedMapOf(it.baseCurrency to BASE_CURRENCY_RATE).apply { putAll(it.rates) }
            }.repeatWhen { it.delay(1000, TimeUnit.MILLISECONDS) }
    }
}