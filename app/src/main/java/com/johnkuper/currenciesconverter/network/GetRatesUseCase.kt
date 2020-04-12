package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.api.CurrenciesRatesResponse
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<String, CurrenciesRatesResponse>() {

    // TODO Kuper after receiving an error stop immediately
    override fun execute(parameters: String): Observable<CurrenciesRatesResponse> {
        return currenciesApi
            .getRates(parameters)
            .repeatWhen { it.delay(1000, TimeUnit.MILLISECONDS) }
    }
}