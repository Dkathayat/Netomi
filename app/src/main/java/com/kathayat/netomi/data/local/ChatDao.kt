package com.kathayat.netomi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    // CHAT TABLE
    @Insert
    suspend fun insertChat(chat: ChatEntity): Long

    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    suspend fun getAllChats(): List<ChatEntity>

    @Query("DELETE FROM chats")
    suspend fun clearChats() // Will cascade delete messages if FK is set

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun resetUnreadCount(chatId: Int)

    @Query("UPDATE chats SET lastMessage = :message, lastMessageTime = :time WHERE chatId = :chatId")
    suspend fun updateChatPreview(chatId: Int, message: String, time: Long)

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE chatId = :chatId")
    suspend fun incrementUnread(chatId: Int)

    @Query("UPDATE chats SET unreadCount = :count WHERE chatId = :chatId")
    suspend fun updateUnreadCount(chatId: Int, count: Int)

    // MESSAGES TABLE
    @Insert
    suspend fun insertMessage(msg: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatOwnerId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessages(chatId: Int): List<MessageEntity>

    @Query("UPDATE messages SET isUnread = 0 WHERE chatOwnerId = :chatId")
    suspend fun markChatMessagesRead(chatId: Int)

    @Query("DELETE FROM messages WHERE chatOwnerId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Int)

    // PENDING MESSAGES TABLE
    @Insert
    suspend fun addPending(message: PendingMessageEntity)

    @Query("SELECT * FROM pending_messages")
    suspend fun getPendingMessages(): List<PendingMessageEntity>

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun deletePending(id: Int)

    @Query("DELETE FROM pending_messages WHERE chatOwnerId = :chatId")
    suspend fun deletePendingForChat(chatId: Int)
}