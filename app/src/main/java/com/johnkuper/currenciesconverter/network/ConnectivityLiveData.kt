package com.johnkuper.currenciesconverter.network

import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import androidx.lifecycle.LiveData
import com.johnkuper.currenciesconverter.utils.isNetworkConnected
import javax.inject.Inject

private const val ON_NETWORK_LOST_DELAY = 500L

class ConnectivityLiveData @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : LiveData<Boolean>() {

    private val handler = Handler()
    private val onLostNetworkRunnable = Runnable { postValue(false) }
    private var isNetworkConnected = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
//            kuperLog("ConnectivityLiveData.onLost()")
            if (isNetworkConnected) {
                isNetworkConnected = false
                handler.postDelayed(onLostNetworkRunnable, ON_NETWORK_LOST_DELAY)
            }
        }

        override fun onAvailable(network: Network) {
//            kuperLog("ConnectivityLiveData.onAvailable()")
            handler.removeCallbacks(onLostNetworkRunnable)
            if (!isNetworkConnected) {
                isNetworkConnected = true
                postValue(true)
            }
        }
    }

    override fun onActive() {
        isNetworkConnected = connectivityManager.isNetworkConnected()
//        kuperLog("ConnectivityLiveData.onActive(), isNetworkConnected=$isNetworkConnected")
        if (!isNetworkConnected) {
            postValue(false)
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onInactive() {
//        kuperLog("ConnectivityLiveData.onInactive()")
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
