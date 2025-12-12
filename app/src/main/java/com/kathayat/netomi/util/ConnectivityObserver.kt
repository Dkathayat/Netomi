package com.kathayat.netomi.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectivityObserver(context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isConnected = MutableStateFlow<Boolean?>(false)
    val isConnected: StateFlow<Boolean?> = _isConnected.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }
    }

    fun start() {
        val request = NetworkRequest.Builder().build()
        cm.registerNetworkCallback(request, callback)
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        val active = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        _isConnected.value = active
    }

    fun stop() {
        try {
            cm.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
    }
}