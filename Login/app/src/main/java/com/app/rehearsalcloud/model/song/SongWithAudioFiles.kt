package com.app.rehearsalcloud.model.song

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.app.rehearsalcloud.model.audiofile.AudioFile

// Data class for Song with its AudioFiles (for querying)
data class SongWithAudioFiles(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "id", // songId
        entityColumn = "songId" // audioFileId
    )
    val audioFiles: List<AudioFile>
)
