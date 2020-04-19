package com.johnkuper.currenciesconverter

import android.app.Application
import com.johnkuper.currenciesconverter.di.DaggerTestAppComponent
import com.johnkuper.currenciesconverter.di.TestAppComponent

class TestConverterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        testAppComponent = DaggerTestAppComponent
            .builder()
            .application(this)
            .build()
    }

    companion object {

        @JvmStatic
        lateinit var testAppComponent: TestAppComponent
    }
}