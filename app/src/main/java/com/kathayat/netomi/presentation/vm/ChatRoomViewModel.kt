package com.kathayat.netomi.presentation.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathayat.netomi.domain.model.ChatMessage
import com.kathayat.netomi.domain.repository.ChatRepository
import com.kathayat.netomi.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val repo: ChatRepository,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val connectivity = ConnectivityObserver(ctx)

    init {
        Log.d("SoketConnection", "is soket conneted ${repo.isSocketConnected.value}")
        connectivity.start()
        observeConnectivity()
        observeIncoming()
    }

    fun loadMessages(chatId: Int) {
        viewModelScope.launch {
            _messages.value = repo.getMessages(chatId)
        }
    }

    fun sendMessage(chatId: Int, text: String) {
        viewModelScope.launch {
            try {
                val ok = repo.sendMessage(chatId, "You", text,isOnline.value)
                if (!ok) {
                    _error.emit("Message queued (offline).")
                }
                loadMessages(chatId)
            } catch (e: Exception) {
                _error.emit("Send failed: ${e.localizedMessage}")
            }
        }
    }

    fun retryPending(chatId: Int) {
        viewModelScope.launch {
            try {
                repo.retryPending()
                loadMessages(chatId)
            } catch (e: Exception) {
                _error.emit("Retry failed: ${e.localizedMessage}")
            }
        }
    }

    fun retrySingle(chatId: Int, pendingId: Long) {
        viewModelScope.launch {
            try {
                repo.retrySingleMessage(chatId,pendingId,isOnline.value)
                loadMessages(chatId)   // <-- refresh UI instantly
            } catch (e: Exception) {
                _error.emit("Retry failed: ${e.localizedMessage}")
            }
        }
    }

    fun markAsRead(chatId: Int) {
        viewModelScope.launch {
            repo.markChatAsRead(chatId)
            loadMessages(chatId)
        }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            repo.incomingMessages().collect { raw ->
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivity.isConnected.collect { connected ->
                _isOnline.value = connected == true
                if (connected == true) {
                    try {
                        repo.connectSocket()
                        repo.retryPending()
                    } catch (e: Exception) {
                        _error.emit("Retry failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivity.stop()
    }
}