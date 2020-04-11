package com.johnkuper.currenciesconverter.network

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrenciesApi {

    @GET("api/android/latest")
    fun getRates(@Query("base") currency: String = "EUR"): Observable<RatesResponse>
}