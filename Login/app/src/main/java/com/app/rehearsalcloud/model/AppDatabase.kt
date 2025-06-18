package com.app.rehearsalcloud.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.rehearsalcloud.interfaces.SongDao
import com.app.rehearsalcloud.model.audiofile.AudioFile
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistSongCrossRef
import com.app.rehearsalcloud.model.song.Song

// Room Database
@Database(entities = [Song::class, AudioFile::class, Setlist::class, SetlistSongCrossRef::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rehearsalcloud_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
