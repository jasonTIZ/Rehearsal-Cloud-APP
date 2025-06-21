package com.app.rehearsalcloud.model.setlist

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.model.song.SongWithAudioFiles

data class SetlistWithSongsWithAudioFile(
    @Embedded val setlist: Setlist,
    @Relation(
        entity = Song::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SetlistSongCrossRef::class,
            parentColumn = "setlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongWithAudioFiles>
)
