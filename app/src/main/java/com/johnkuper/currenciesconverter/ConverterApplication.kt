package com.johnkuper.currenciesconverter

import android.app.Application
import com.johnkuper.currenciesconverter.di.AppComponent
import com.johnkuper.currenciesconverter.di.DaggerAppComponent

class ConverterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    companion object {

        @JvmStatic
        lateinit var appComponent: AppComponent
    }
}