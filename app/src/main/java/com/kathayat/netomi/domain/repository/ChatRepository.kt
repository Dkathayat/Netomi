package com.kathayat.netomi.domain.repository

import com.kathayat.netomi.data.local.ChatEntity
import com.kathayat.netomi.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // SOCKET
    fun incomingMessages(): Flow<String>
    fun connectSocket()
    fun closeSocket()
    fun setSocketOffline(simulateOffline: Boolean)

    suspend fun createChat(chatName: String): Long
    suspend fun getAllChats(): List<ChatEntity>
    suspend fun clearChats()
    suspend fun markChatAsRead(chatId: Int)

    // MESSAGES
    suspend fun getMessages(chatId: Int): List<ChatMessage>

    suspend fun sendMessage(
        chatId: Int,
        sender: String,
        message: String
    ): Boolean

    suspend fun saveIncoming(
        chatId: Int,
        sender: String,
        message: String
    )
    // OFFLINE QUEUE
    suspend fun retryPending()
}