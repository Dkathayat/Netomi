package com.kathayat.netomi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_messages")
data class PendingMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatOwnerId: Int,
    val sender: String,
    val message: String,
    val timestamp: Long
)


