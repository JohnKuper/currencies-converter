package com.johnkuper.currenciesconverter.di

import com.google.gson.Gson
import com.johnkuper.currenciesconverter.api.CurrenciesApi
import com.johnkuper.currenciesconverter.network.GetRatesUseCase
import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import javax.inject.Singleton

@Module
class TestNetworkModule {

    @Singleton
    @Provides
    fun providesCurrenciesApi(): CurrenciesApi = Mockito.mock(CurrenciesApi::class.java)

    @Provides
    @Singleton
    fun provideGetRatesUseCase(currenciesApi: CurrenciesApi) = GetRatesUseCase(currenciesApi)

    @Provides
    @Singleton
    fun providesGson() = Gson()
}