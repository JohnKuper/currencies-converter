package com.johnkuper.currenciesconverter.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object : TypeToken<T>() {}.type)

fun String.readTextResource(): String {
    return checkNotNull(
        javaClass::class.java.getResource("/$this"),
        { "Resource = $this is not found" }).readText()
}
