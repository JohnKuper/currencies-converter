package com.johnkuper.currenciesconverter.di

import android.app.Application
import com.johnkuper.currenciesconverter.ConverterViewModelTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        TestNetworkModule::class,
        ViewModelsModule::class
    ]
)
interface TestAppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): TestAppComponent
    }

    fun inject(test: ConverterViewModelTest)
}