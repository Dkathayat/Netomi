package com.kathayat.netomi.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val chatId: Int = 0,
    val chatName: String,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatOwnerId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatOwnerId: Int,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val isPending: Boolean = false,
    val isUnread: Boolean = true
)


