package com.johnkuper.currenciesconverter.di

import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.network.GetCurrenciesRatesUseCase
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun providerOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hiring.revolut.codes/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideCurrenciesApi(retrofit: Retrofit): CurrenciesApi = retrofit.create(CurrenciesApi::class.java)

    @Provides
    @Singleton
    fun provideGetRatesUseCase(currenciesApi: CurrenciesApi) = GetCurrenciesRatesUseCase(currenciesApi)
}