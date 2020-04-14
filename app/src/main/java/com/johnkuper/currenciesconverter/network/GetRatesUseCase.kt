package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.domain.ConverterItem
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<GetRatesParameters, List<ConverterItem>>() {

    // TODO Kuper after receiving an error stop immediately
    override fun execute(params: GetRatesParameters): Observable<List<ConverterItem>> {
        return currenciesApi
            .getRates(params.baseCurrency)
            .map { response ->
                // TODO could be optimized?
                if (params.converterItems.isEmpty()) {
                    response.rates.mapTo(mutableListOf(ConverterItem(params.baseCurrency, params.amount))) { rate ->
                        ConverterItem(rate.key, rate.value * params.amount)
                    }
                } else {
                    params.converterItems.map { item ->
                        response.rates[item.code]?.let { item.copy(amount = it * params.amount) } ?: item
                    }
                }
            }
            .repeatWhen { it.delay(1000, TimeUnit.MILLISECONDS) }
    }
}

class GetRatesParameters(
    val baseCurrency: String,
    val amount: Double,
    val converterItems: List<ConverterItem>
)