package id.app.instaapp.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import id.app.instaapp.data.local.InstaDatabase
import id.app.instaapp.data.local.PostDao
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): InstaDatabase {
        return Room.databaseBuilder(application, InstaDatabase::class.java, "insta-db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePostDao(database: InstaDatabase): PostDao = database.postDao()
}