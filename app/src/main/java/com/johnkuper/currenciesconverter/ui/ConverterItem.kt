package com.johnkuper.currenciesconverter.ui

import android.net.Uri
import com.johnkuper.currenciesconverter.utils.decimalFormat
import com.johnkuper.currenciesconverter.utils.round

data class ConverterItem(
    val currencyCode: String,
    val currencyName: String,
    val flagUri: Uri,
    val amount: Double
) {

    private val roundedAmount: Double
        get() = amount.round(2)

    val formattedAmount: String
        get() = when {
            roundedAmount == 0.0 -> ""
            roundedAmount % 1.0 != 0.0 -> decimalFormat.format(roundedAmount)
            else -> String.format("%.0f", roundedAmount)
        }
}
