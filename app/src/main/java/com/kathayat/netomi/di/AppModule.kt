package com.kathayat.netomi.di

import com.kathayat.netomi.domain.repository.ChatRepository
import com.kathayat.netomi.domain.usecases.RetryPendingUseCase
import com.kathayat.netomi.domain.usecases.SendMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSendUseCase(repo: ChatRepository) = SendMessageUseCase(repo)

    @Provides
    @Singleton
    fun provideRetryUseCase(repo: ChatRepository) = RetryPendingUseCase(repo)
}