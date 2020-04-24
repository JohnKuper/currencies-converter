package com.johnkuper.currenciesconverter.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.johnkuper.currenciesconverter.network.ConnectivityLiveData
import com.johnkuper.currenciesconverter.ui.CurrenciesResources
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    fun providesConnectivityManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun providesConnectivityLiveData(cm: ConnectivityManager) = ConnectivityLiveData(cm)

    @Provides
    @Singleton
    fun provideCurrenciesResources(context: Context) = CurrenciesResources(context)
}