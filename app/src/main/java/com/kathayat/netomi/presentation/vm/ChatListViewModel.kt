package com.kathayat.netomi.presentation.vm

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repo: ChatRepository,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    // Expose a Flow of ChatEntity from DB so UI is reactive
    val chatsFlow: StateFlow<List<ChatEntity>> = repo.observeChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigateToChat = MutableSharedFlow<Int>()
    val navigateToChat = _navigateToChat.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val connectivity = ConnectivityObserver(ctx)
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    init {
        repo.connectSocket()
        observeIncomingSafe()
        observeConnectivity()
        Log.d("SoketConnection", "is soket conneted ${repo.isSocketConnected.value}")
    }

    fun createNewChat() {
        viewModelScope.launch {
            val id = repo.createChat("Chat ${System.currentTimeMillis()}")
            _navigateToChat.emit(id.toInt())
        }
    }

    private fun observeIncomingSafe() {
        // Listen to incoming socket messages and route them to the latest chat (or create one)
        viewModelScope.launch {
            repo.incomingMessages().collect { raw ->
                try {
                    val currentChats = repo.getAllChats()
                    val chatId = currentChats.lastOrNull()?.chatId ?: repo.createChat("Auto Chat").toInt()
                    repo.saveIncoming(chatId, "Bot", raw)
                } catch (e: Exception) {
                    _error.emit("Incoming save error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun observeConnectivity() {
        connectivity.start()
        viewModelScope.launch {
            connectivity.isConnected.collect { connected ->
                _isOnline.value = connected == true
                if (connected == true) {
                    try {
                        repo.retryPending()
                    } catch (e: Exception) {
                        _error.emit("Retry failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    fun clearAllChatsNow() {
        viewModelScope.launch { repo.clearChats() }
    }
}
