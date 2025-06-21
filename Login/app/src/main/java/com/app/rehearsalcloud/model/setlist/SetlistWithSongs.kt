package com.app.rehearsalcloud.model.setlist

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.app.rehearsalcloud.model.song.Song

// Data class for Setlist with its Songs (for querying)
data class SetlistWithSongs(
    @Embedded val setlist: Setlist,
    @Relation(
        parentColumn = "id", // setlistId
        entityColumn = "id", // songId
        associateBy = Junction(
            value = SetlistSongCrossRef::class,
            parentColumn = "setlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)
