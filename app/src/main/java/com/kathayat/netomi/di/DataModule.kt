package com.kathayat.netomi.di

import android.content.Context
import com.kathayat.netomi.data.local.ChatDao
import com.kathayat.netomi.data.local.ChatDatabase
import com.kathayat.netomi.data.remote.SocketManager
import com.kathayat.netomi.data.repository.ChatRepositoryImpl
import com.kathayat.netomi.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context) =
        ChatDatabase.getInstance(ctx)

    @Provides
    fun provideDao(db: ChatDatabase) = db.chatDao()

    @Provides
    @Singleton
    fun provideSocket() = SocketManager()

    @Provides @Singleton
    fun provideRepository(dao: ChatDao, socket: SocketManager): ChatRepository =
        ChatRepositoryImpl(dao, socket)
}
