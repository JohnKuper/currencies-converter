package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.BASE_CURRENCY_RATE
import com.johnkuper.currenciesconverter.RATES_POLLING_DELAY
import com.johnkuper.currenciesconverter.api.CurrenciesApi
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Receives currency rates every [RATES_POLLING_DELAY] and returns rates as LinkedHashMap having
 * base currency with the [BASE_CURRENCY_RATE] as a first value.
 */
class GetRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<String, LinkedHashMap<String, Double>>() {

    override fun execute(params: String): Observable<LinkedHashMap<String, Double>> {
        return currenciesApi
            .getRates(params)
            .map {
                linkedMapOf(it.baseCurrency to BASE_CURRENCY_RATE).apply { putAll(it.rates) }
            }.repeatWhen { it.delay(RATES_POLLING_DELAY, TimeUnit.MILLISECONDS) }
    }
}