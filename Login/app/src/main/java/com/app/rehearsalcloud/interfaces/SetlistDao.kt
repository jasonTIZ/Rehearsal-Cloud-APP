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
import com.app.rehearsalcloud.model.setlist.SetlistWithSongsWithAudioFile
import com.app.rehearsalcloud.model.song.Song

@Dao
interface SetlistDao {

    // Fetch all setlists without songs
    @Query("SELECT * FROM setlist")
    suspend fun getAllSetlists(): List<Setlist>

    // Fetch a specific setlist with songs by ID
    @Transaction
    @Query("SELECT * FROM setlist WHERE id = :id")
    suspend fun getSetlistWithSongsById(id: Int): SetlistWithSongs?


    // Fetch a specific setlist with songs by ID
    @Transaction
    @Query("SELECT * FROM setlist WHERE id = :id")
    suspend fun getSetlistWithSongsWithAudioFiles(id: Int): SetlistWithSongsWithAudioFile?

    // Fetch all setlists with their associated songs
    @Transaction
    @Query("SELECT * FROM setlist")
    suspend fun getAllSetlistsWithSongs(): List<SetlistWithSongs>

    // Insert or update a list of setlists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlists(setlists: List<Setlist>)

    // Insert or update song associations for a setlist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongCrossRef>)

    // Delete all song associations for a specific setlist
    @Query("DELETE FROM SetlistSongCrossRef WHERE setlistId = :setlistId")

    suspend fun deleteSetlistSongs(setlistId: Int)

    // Insert or update a list of songs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFiles(songs: List<AudioFile>)
}