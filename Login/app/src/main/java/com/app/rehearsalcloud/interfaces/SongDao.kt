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
    @Query("SELECT * FROM song")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getSongById(id: Int): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Query("DELETE FROM Song WHERE id = :id")
    suspend fun deleteSong(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFiles(audioFiles: List<AudioFile>)

    @Query("SELECT * FROM AudioFile WHERE songId = :songId")
    suspend fun getAudioFilesBySongId(songId: Int): List<AudioFile>

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongWithAudioFiles(songId: Int): SongWithAudioFiles?

    @Transaction
    @Query("SELECT * FROM song WHERE id IN (:songIds)")
    suspend fun getSongsWithAudioFiles(songIds: List<Int>): List<SongWithAudioFiles>

    @Query("DELETE FROM Song")
    suspend fun deleteAllSongs()
}