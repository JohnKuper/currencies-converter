package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BASE_CURRENCY_RATE = 1.0

class GetRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<GetRatesParams, LinkedHashMap<String, Double>>() {

    // TODO Kuper handle errors and stop after a several attempts
    //TODO Kuper be able to tune the repeat behavior
    override fun execute(params: GetRatesParams): Observable<LinkedHashMap<String, Double>> {
        var observable = currenciesApi.getRates(params.baseCurrency).map {
            linkedMapOf(it.baseCurrency to 1.0).apply { putAll(it.rates) }
        }.repeatWhen { it.delay(1000, TimeUnit.MILLISECONDS) }

        if (params.isCurrencyChanged && params.lastRates.isNotEmpty()) {
            observable = observable.startWith(recalculatedRates(params))
        }
        return observable
    }

    private fun recalculatedRates(params: GetRatesParams): LinkedHashMap<String, Double> {
        return params.run {
            val baseCurrencyOldRate = requireNotNull(lastRates.remove(baseCurrency))
            linkedMapOf(baseCurrency to BASE_CURRENCY_RATE).apply {
                putAll(lastRates.mapValues { it.value / baseCurrencyOldRate })
            }
        }
    }
}

class GetRatesParams(
    val baseCurrency: String,
    val isCurrencyChanged: Boolean,
    val lastRates: LinkedHashMap<String, Double>
)