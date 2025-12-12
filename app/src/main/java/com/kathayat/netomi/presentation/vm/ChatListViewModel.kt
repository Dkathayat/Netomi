package com.kathayat.netomi.presentation.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kathayat.netomi.data.local.ChatEntity
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
class ChatListViewModel @Inject constructor(
    private val repo: ChatRepository,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _navigateToChat = MutableSharedFlow<Int>()
    val navigateToChat = _navigateToChat.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val connectivity = ConnectivityObserver(ctx)

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    init {
        repo.connectSocket()
        observeIncomingMessages()
        observeConnectivity()
        loadChats()
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newChatId = repo.createChat("Chat Bot ${System.currentTimeMillis()}")
            loadChats()
            _navigateToChat.emit(newChatId.toInt())
        }
    }

    // LOAD CHATS LIST
    fun loadChats() {
        viewModelScope.launch {
            _chats.value = repo.getAllChats()
        }
    }

    // INCOMING SOCKET MESSAGES
    private fun observeIncomingMessages() {
        viewModelScope.launch {
            repo.incomingMessages().collect { rawMessage ->

                // 1. Try to get the latest chat
                val existingChat = repo.getAllChats().lastOrNull()

                val chatId = if (existingChat != null) {
                    existingChat.chatId
                } else {
                    // 2. If no chat exists → create one automatically
                    val newId = repo.createChat("New Chat")
                    newId.toInt()
                }

                repo.saveIncoming(
                    chatId = chatId,
                    sender = "Bot",
                    message = rawMessage
                )

                loadChats()
            }
        }
    }


    // CONNECTIVITY
    private fun observeConnectivity() {
        connectivity.start()
        viewModelScope.launch {
            connectivity.isConnected.collect { connected ->
                _isOnline.value = connected == true

                if (connected == true) {
                    try {
                        repo.retryPending()
                        loadChats()
                    } catch (e: Exception) {
                        _error.emit("Retry failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    // CLEAR ALL CHATS ON DESTROY
    fun clearAllChatsOnClose() {
        viewModelScope.launch { repo.clearChats() }
    }

    override fun onCleared() {
        super.onCleared()
        connectivity.stop()
        repo.closeSocket()
    }
}

