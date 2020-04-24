package com.johnkuper.currenciesconverter.ui

import android.content.Context
import android.net.Uri
import java.util.*

private const val FLAG_URI_FORMAT = "file:///android_asset/flags/flag_%1\$s.png"
private const val CURRENCY_NAME_FORMAT = "%1\$s_currency_name"

class CurrenciesResources(private val context: Context) {

    fun getFlagUri(currencyCode: String): Uri {
        val path = FLAG_URI_FORMAT.format(currencyCode.toLowerCase(Locale.getDefault()))
        return Uri.parse(path)
    }

    fun getCurrencyName(currencyCode: String): String {
        val stringName = CURRENCY_NAME_FORMAT.format(currencyCode.toLowerCase(Locale.getDefault()))
        val resId = context.resources.getIdentifier(stringName, "string", context.packageName)
        return context.getString(resId)
    }
}