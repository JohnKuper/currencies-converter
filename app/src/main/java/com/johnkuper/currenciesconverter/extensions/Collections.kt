package com.johnkuper.currenciesconverter.extensions

// TODO Kuper remove it
fun <T> List<T>.copy() = mutableListOf<T>().apply { addAll(this@copy) }