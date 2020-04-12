package com.johnkuper.currenciesconverter.domain

data class ConverterItem(
    val code: String,
    val amount: Double,
    val isActive: Boolean
)