package com.johnkuper.currenciesconverter.extensions

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

inline fun <reified T : ViewModel> FragmentActivity.createViewModel(
    factory: ViewModelProvider.Factory, body: T.() -> Unit
) = ViewModelProvider(this, factory)[T::class.java].apply(body)

fun <X, Y> LiveData<X>.map(body: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, body)
}

fun <T> MutableLiveData<T>.repeat() {
    value = value
}
