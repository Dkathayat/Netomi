package com.kathayat.netomi.domain.usecases

import com.kathayat.netomi.domain.repository.ChatRepository


class RetryPendingUseCase (private val repo: ChatRepository) {
    suspend operator fun invoke() = repo.retryPending()
}
