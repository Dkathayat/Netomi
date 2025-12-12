package com.kathayat.netomi.domain.repository

import com.kathayat.netomi.data.local.ChatEntity
import com.kathayat.netomi.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // socket
    fun incomingMessages(): Flow<String>
    val isSocketConnected: kotlinx.coroutines.flow.StateFlow<Boolean>
    fun connectSocket()
    fun closeSocket()
    fun setSocketOffline(simulateOffline: Boolean)

    // chats
    suspend fun createChat(chatName: String): Long
    fun observeChats(): Flow<List<ChatEntity>>
    suspend fun getAllChats(): List<ChatEntity>
    suspend fun clearChats()

    // messages
    suspend fun getMessages(chatId: Int): List<ChatMessage>
    suspend fun sendMessage(chatId: Int, sender: String, message: String, isConnected: Boolean): Boolean
    suspend fun saveIncoming(chatId: Int, sender: String, message: String)
    suspend fun markChatAsRead(chatId: Int)

    // pending
    suspend fun retryPending()
    suspend fun retrySingleMessage(chatId: Int, pendingId: Long, isConnected:Boolean)
    suspend fun deletePendingMessage(pendingId: Long)
}