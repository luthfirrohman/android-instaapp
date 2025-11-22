package id.app.instaapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
@TypeConverters(PostConverters::class)
abstract class InstaDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}