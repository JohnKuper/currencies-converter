package com.johnkuper.currenciesconverter.network

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.api.RatesResponse
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetCurrenciesRatesUseCase @Inject constructor(
    private val currenciesApi: CurrenciesApi
) : UseCase<RatesResponse>() {

    // TODO Kuper after receiving an error stop immediately
    override fun execute(): Observable<RatesResponse> {
        return currenciesApi
            .getRates()
            .repeatWhen { it.delay(1000, TimeUnit.MILLISECONDS) }
    }
}