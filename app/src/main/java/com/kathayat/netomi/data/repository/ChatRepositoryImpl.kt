package com.kathayat.netomi.data.repository

import android.util.Log
import com.kathayat.netomi.data.local.ChatDao
import com.kathayat.netomi.data.local.ChatEntity
import com.kathayat.netomi.data.local.MessageEntity
import com.kathayat.netomi.data.local.PendingMessageEntity
import com.kathayat.netomi.data.remote.SocketManager
import com.kathayat.netomi.domain.model.ChatMessage
import com.kathayat.netomi.domain.repository.ChatRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatDao,
    private val socket: SocketManager
) : ChatRepository {

    override val isSocketConnected = socket.connected
    override fun incomingMessages(): Flow<String> = socket.incomingFlow

    // CREATE / GET / DELETE CHATS
    override suspend fun createChat(chatName: String): Long {
        val id = dao.insertChat(ChatEntity(chatName = chatName))
        return id
    }

    override fun observeChats(): Flow<List<ChatEntity>> = dao.observeChats()

    override suspend fun getAllChats(): List<ChatEntity> = withContext(Dispatchers.IO) {
        dao.getAllChats()
    }

    override suspend fun clearChats() = withContext(Dispatchers.IO) {
        dao.clearChats()
        dao.clearmessages()
        socket.connect()
    }

    // SEND MESSAGES
    override suspend fun sendMessage(chatId: Int, sender: String, message: String, isConnected: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            Log.e("RoomDebug", "sendMessage() chatId = $chatId")
            val now = System.currentTimeMillis()
            val sent = socket.send(message)
            if (isConnected) {
                dao.insertMessage(
                    MessageEntity(
                        chatOwnerId = chatId,
                        sender = sender,
                        message = message,
                        timestamp = now,
                        isPending = false,
                        isUnread = false
                    )
                )
                dao.updateChatPreview(chatId, message, now)
            } else {
                dao.addPending(
                    PendingMessageEntity(
                        chatOwnerId = chatId,
                        sender = sender,
                        message = message,
                        timestamp = now
                    )
                )
            }
            sent
        }

    override suspend fun saveIncoming(chatId: Int, sender: String, message: String) =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            Log.e("RoomDebug", "saveIncoming() chatId = $chatId")
            dao.insertMessage(
                MessageEntity(
                    chatOwnerId = chatId,
                    sender = sender,
                    message = message,
                    timestamp = now,
                    isPending = false,
                    isUnread = true
                )
            )
            dao.updateChatPreview(chatId, message, now)
            dao.incrementUnread(chatId)
        }

    override suspend fun getMessages(chatId: Int): List<ChatMessage> = withContext(Dispatchers.IO) {
        val normal = dao.getMessages(chatId)
        val pending = dao.getPendingForChat(chatId)

        val normalMapped = normal.map {
            ChatMessage(it.id, it.sender, it.message, it.timestamp, isPending = false, isUnread = it.isUnread)
        }
        val pendingMapped = pending.map {
            // negative id to avoid colliding with real messages in UI
            ChatMessage(
                id = it.id,               // IMPORTANT: use real id
                sender = it.sender,
                message = it.message,
                timestamp = it.timestamp,
                isPending = true,
                isUnread = false
            )
        }
        (normalMapped + pendingMapped).sortedBy { it.timestamp }
    }

    override suspend fun markChatAsRead(chatId: Int) = withContext(Dispatchers.IO) {
        dao.markChatMessagesRead(chatId)
        dao.resetUnreadCount(chatId)
    }

    // PENDING
    override suspend fun retryPending() = withContext(Dispatchers.IO) {
        val pending = dao.getPendingMessages()
        pending.forEach { p ->
            val ok = socket.send(p.message)
            if (ok) {
                dao.deletePending(p.id)
                dao.insertMessage(
                    MessageEntity(
                        chatOwnerId = p.chatOwnerId,
                        sender = p.sender,
                        message = p.message,
                        timestamp = p.timestamp,
                        isPending = false,
                        isUnread = false
                    )
                )
                dao.updateChatPreview(p.chatOwnerId, p.message, p.timestamp)
            }
        }
    }

    override suspend fun retrySingleMessage(chatId: Int, pendingId: Long, isConnected: Boolean) = withContext(Dispatchers.IO) {
        val pending = dao.getPendingById(pendingId) ?: return@withContext
        if (isConnected) {
            dao.deletePending(pendingId)
            dao.insertMessage(
                MessageEntity(
                    chatOwnerId = pending.chatOwnerId,
                    sender = pending.sender,
                    message = pending.message,
                    timestamp = pending.timestamp,
                    isPending = false,
                    isUnread = false
                )
            )

            dao.updateChatPreview(pending.chatOwnerId, pending.message, pending.timestamp)
        }

    }
    override suspend fun deletePendingMessage(pendingId: Long) = withContext(Dispatchers.IO) {
        dao.deletePending(pendingId)
    }

    // SOCKET CONTROL
    override fun connectSocket() = socket.connect()
    override fun closeSocket() = socket.close()
    override fun setSocketOffline(simulateOffline: Boolean) {
        socket.simulateOffline = simulateOffline
    }
}