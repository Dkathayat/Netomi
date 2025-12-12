package com.kathayat.netomi.domain.model

data class ChatMessage(
    val id: Int = 0,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val isUnread: Boolean = false
)