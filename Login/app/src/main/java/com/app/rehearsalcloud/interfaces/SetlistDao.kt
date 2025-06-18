package com.app.rehearsalcloud.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistSongCrossRef
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs

@Dao
interface SetlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: Setlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSongCrossRef(setlistSongCrossRef: SetlistSongCrossRef)

    @Transaction
    @Query("SELECT * FROM Setlist WHERE id = :id")
    suspend fun getSetlistWithSongs(id: Int): SetlistWithSongs?

    @Query("SELECT * FROM Setlist")
    suspend fun getAllSetlists(): List<SetlistWithSongs>

    @Query("DELETE FROM Setlist WHERE id = :id")
    suspend fun deleteSetlist(id: Int)
}