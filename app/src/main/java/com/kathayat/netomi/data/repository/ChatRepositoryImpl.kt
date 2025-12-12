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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatDao,
    private val socket: SocketManager
) : ChatRepository {

    val isSocketConnected = socket.connected
    override fun incomingMessages(): Flow<String> = socket.incomingFlow

    // CREATE / GET / DELETE CHATS
    override suspend fun createChat(chatName: String): Long {
        val id = dao.insertChat(
            ChatEntity(chatName = chatName)
        )
        return id
    }

    override suspend fun getAllChats(): List<ChatEntity> = withContext(Dispatchers.IO) {
        dao.getAllChats().map { chat ->
            ChatEntity(
                chatId = chat.chatId,
                chatName = chat.chatName,
                lastMessage = chat.lastMessage,
                lastMessageTime = chat.lastMessageTime,
                unreadCount = chat.unreadCount
            )
        }
    }

    override suspend fun clearChats() {
        dao.clearChats()
    }

    // SEND MESSAGES
    override suspend fun sendMessage(
        chatId: Int,
        sender: String,
        message: String
    ): Boolean = withContext(Dispatchers.IO) {
        Log.e("RoomDebug", "sendMessage() chatId = $chatId")
        val now = System.currentTimeMillis()

        val sent = socket.send(message)

        if (sent) {
            // Save message to DB
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

            // Update chat preview
            dao.updateChatPreview(chatId, message, now)

        } else {
            // Queue message for retry
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

    override suspend fun saveIncoming(
        chatId: Int,
        sender: String,
        message: String
    ) = withContext(Dispatchers.IO) {

        val now = System.currentTimeMillis()

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

    // LOAD MESSAGES FOR A CHAT

    override suspend fun getMessages(chatId: Int): List<ChatMessage> =
        withContext(Dispatchers.IO) {
            dao.getMessages(chatId).map {
                ChatMessage(
                    id = it.id,
                    sender = it.sender,
                    message = it.message,
                    timestamp = it.timestamp,
                    isUnread = it.isUnread
                )
            }
        }

    // MARK MESSAGES READ --
    override suspend fun markChatAsRead(chatId: Int) {
        withContext(Dispatchers.IO) {
            dao.markChatMessagesRead(chatId)
            dao.resetUnreadCount(chatId)
        }
    }

    // PENDING MESSAGES --

    override suspend fun retryPending() = withContext(Dispatchers.IO) {
        val pending = dao.getPendingMessages()

        pending.forEach { pendingMsg ->

            val ok = socket.send(pendingMsg.message)

            if (ok) {
                dao.deletePending(pendingMsg.id)
                dao.insertMessage(
                    MessageEntity(
                        chatOwnerId = pendingMsg.chatOwnerId,
                        sender = pendingMsg.sender,
                        message = pendingMsg.message,
                        timestamp = pendingMsg.timestamp,
                        isPending = false,
                        isUnread = false
                    )
                )

                dao.updateChatPreview(
                    pendingMsg.chatOwnerId,
                    pendingMsg.message,
                    pendingMsg.timestamp
                )
            }
        }
    }
    // SOCKET CONTROL
    override fun connectSocket() = socket.connect()

    override fun closeSocket() = socket.close()

    override fun setSocketOffline(simulateOffline: Boolean) {
        socket.simulateOffline = simulateOffline
    }
}