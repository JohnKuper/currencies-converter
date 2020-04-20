package com.johnkuper.currenciesconverter.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

val decimalFormat = DecimalFormat("#0.00", DecimalFormatSymbols().apply { decimalSeparator = ',' })

fun Double.round(places: Int): Double {
    return BigDecimal.valueOf(this).setScale(places, RoundingMode.HALF_UP).toDouble()
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun parseDouble(text: CharSequence): Double {
    return try {
        decimalFormat.parse(text.toString()).toDouble()
    } catch (e: Exception) {
        0.0
    }
}
