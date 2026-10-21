package com.vk.starscriptmusic.di

import com.vk.starscriptmusic.data.media.PlaybackRepositoryImpl
import com.vk.starscriptmusic.domain.repository.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(
        impl: PlaybackRepositoryImpl
    ): PlaybackRepository
}
