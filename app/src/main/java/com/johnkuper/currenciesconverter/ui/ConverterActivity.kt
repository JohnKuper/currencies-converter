package com.johnkuper.currenciesconverter.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.johnkuper.currenciesconverter.ConverterApplication
import com.johnkuper.currenciesconverter.R
import com.johnkuper.currenciesconverter.di.ViewModelFactory
import com.johnkuper.currenciesconverter.extensions.createViewModel
import javax.inject.Inject

class ConverterActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var converterViewModel: ConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConverterApplication.appComponent.inject(this)
        converterViewModel = createViewModel(viewModelFactory) {
            getCurrenciesRates()
        }
    }
}