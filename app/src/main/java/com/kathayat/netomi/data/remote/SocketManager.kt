package com.kathayat.netomi.data.remote

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SocketManager {

    companion object {
        private const val TAG = "SocketManager"
        private const val WS_URL = "wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self"
    }

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private val incomingChannel = Channel<String>(Channel.BUFFERED)
    val incomingFlow: Flow<String> = incomingChannel.receiveAsFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    var simulateOffline = false

    fun connect() {
        if (simulateOffline) {
            _connected.value = false
            return
        }
        val request = Request.Builder().url(WS_URL).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connected.value = true
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                incomingChannel.trySend(text)
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connected.value = false
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connected.value = false
                webSocket.close(1000, null)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connected.value = false
            }
        })
    }

    fun send(text: String): Boolean {
        if (simulateOffline) return false
        return ws?.send(text) ?: false
    }

    fun close() {
        ws?.close(1000, "bye")
        ws = null
        _connected.value = false
    }
}



