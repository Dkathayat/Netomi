package com.kathayat.netomi.domain.model

data class ChatMessage(
    val id: Long,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val isPending: Boolean = false,
    val isUnread: Boolean = false,
    val pendingId: Long? = null
)

data class Chat(
    val chatId: Int,
    val chatName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int
)

