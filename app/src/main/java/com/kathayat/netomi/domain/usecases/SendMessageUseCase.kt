package com.kathayat.netomi.domain.usecases

import com.kathayat.netomi.domain.repository.ChatRepository

class SendMessageUseCase (private val repo: ChatRepository) {
    suspend operator fun invoke(chatId:Int, sender: String, message: String) =
        repo.sendMessage( chatId,sender, message)
}