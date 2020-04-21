package com.johnkuper.currenciesconverter.network

import android.net.ConnectivityManager
import com.johnkuper.currenciesconverter.utils.isNetworkConnected
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor(private val cm: ConnectivityManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!cm.isNetworkConnected()) {
            throw NoNetworkException()
        } else {
            chain.proceed(chain.request())
        }
    }
}