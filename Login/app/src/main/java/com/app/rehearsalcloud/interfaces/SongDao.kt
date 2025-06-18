package com.app.rehearsalcloud.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.rehearsalcloud.model.audiofile.AudioFile
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistSongCrossRef
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.model.song.SongWithAudioFiles

// DAO for Song-related operations
@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFile(audioFile: AudioFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: Setlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSong(setlistSongCrossRef: SetlistSongCrossRef)

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongWithAudioFiles(songId: Int): List<SongWithAudioFiles>

    @Query("SELECT * FROM Song")
    suspend fun getAllSongs(): List<Song>
}