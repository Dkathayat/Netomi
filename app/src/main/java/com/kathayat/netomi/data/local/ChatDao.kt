package com.kathayat.netomi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert
    suspend fun insertChat(chat: ChatEntity): Long

    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    suspend fun getAllChats(): List<ChatEntity>

    @Query("DELETE FROM chats")
    suspend fun clearChats()

    @Query("DELETE FROM messages")
    suspend fun clearmessages()

    @Query("UPDATE chats SET lastMessage = :message, lastMessageTime = :time WHERE chatId = :chatId")
    suspend fun updateChatPreview(chatId: Int, message: String, time: Long)

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE chatId = :chatId")
    suspend fun incrementUnread(chatId: Int)

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun resetUnreadCount(chatId: Int)

    // MESSAGES table
    @Insert
    suspend fun insertMessage(msg: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE chatOwnerId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessages(chatId: Int): List<MessageEntity>

    @Query("UPDATE messages SET isUnread = 0 WHERE chatOwnerId = :chatId")
    suspend fun markChatMessagesRead(chatId: Int)

    @Query("DELETE FROM messages WHERE chatOwnerId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Int)

    // PENDING
    @Insert
    suspend fun addPending(message: PendingMessageEntity): Long

    @Query("SELECT * FROM pending_messages WHERE id = :id LIMIT 1")
    suspend fun getPendingById(id: Long): PendingMessageEntity?

    @Query("SELECT * FROM pending_messages")
    suspend fun getPendingMessages(): List<PendingMessageEntity>

    @Query("SELECT * FROM pending_messages WHERE chatOwnerId = :chatId ORDER BY timestamp ASC")
    suspend fun getPendingForChat(chatId: Int): List<PendingMessageEntity>

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun deletePending(id: Long)

    @Query("DELETE FROM pending_messages WHERE chatOwnerId = :chatId")
    suspend fun deletePendingForChat(chatId: Int)
}