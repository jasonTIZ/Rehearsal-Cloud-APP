package com.app.rehearsalcloud.model.setlist

import androidx.room.Entity
import androidx.room.ForeignKey
import com.app.rehearsalcloud.model.song.Song

// SetlistSong Entity (Junction table for many-to-many relationship)
@Entity(
    primaryKeys = ["setlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Setlist::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetlistSongCrossRef(
    val setlistId: Int,
    val songId: Int,
    val order: Int
)
