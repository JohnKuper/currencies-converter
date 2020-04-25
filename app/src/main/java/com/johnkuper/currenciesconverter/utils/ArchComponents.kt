package com.johnkuper.currenciesconverter.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> FragmentActivity.createViewModel(
    factory: ViewModelProvider.Factory, body: T.() -> Unit
) = ViewModelProvider(this, factory)[T::class.java].apply(body)