package com.kathayat.netomi.presentation.vm

import android.content.Context
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
            val ok = repo.sendMessage(chatId, "You", text)
            if (!ok) {
                _error.emit("Offline")
            }
            loadMessages(chatId)
        }
    }

    fun retryPending(chatId: Int) {
        viewModelScope.launch {
            connectivity.start()
            repo.retryPending()
            loadMessages(chatId)
        }
    }

    fun markAsRead(chatId: Int) {
        viewModelScope.launch {
            repo.markChatAsRead(chatId)
        }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            repo.incomingMessages().collect { text ->
                // TODO: You should parse real chatId from payload
                val chatId = 1

                repo.saveIncoming(chatId, "Bot", text)
                loadMessages(chatId)
            }
        }
    }

    private fun observeConnectivity() {
        connectivity.start()
        viewModelScope.launch {
            connectivity.isConnected.collect { connected ->
                _isOnline.value = connected == true

                if (connected == true) {
                    repo.retryPending()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivity.stop()
    }
}
