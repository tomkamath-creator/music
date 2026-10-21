package com.vk.starscriptmusic.di

import android.content.Context
import androidx.room.Room
import com.vk.starscriptmusic.core.common.Constants
import com.vk.starscriptmusic.data.local.db.StarscriptDatabase
import com.vk.starscriptmusic.data.local.db.dao.PlayCountDao
import com.vk.starscriptmusic.data.local.db.dao.PlaylistDao
import com.vk.starscriptmusic.data.local.db.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStarscriptDatabase(
        @ApplicationContext context: Context
    ): StarscriptDatabase {
        return Room.databaseBuilder(
            context,
            StarscriptDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: StarscriptDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun providePlayCountDao(database: StarscriptDatabase): PlayCountDao {
        return database.playCountDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: StarscriptDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
