package com.johnkuper.currenciesconverter.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrenciesApi {

    @GET("api/android/latest")
    fun getRates(@Query("base") currency: String): Observable<CurrenciesRatesResponse>
}