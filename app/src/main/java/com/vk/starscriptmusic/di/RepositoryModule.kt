package com.vk.starscriptmusic.di

import com.vk.starscriptmusic.data.local.repository.MusicRepositoryImpl
import com.vk.starscriptmusic.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        impl: MusicRepositoryImpl
    ): MusicRepository
}
