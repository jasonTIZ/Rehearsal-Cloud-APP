package com.app.rehearsalcloud.model.audiofile

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.rehearsalcloud.model.song.Song

// AudioFile Entity
@Entity(
    foreignKeys = [ForeignKey(
        entity = Song::class,
        parentColumns = ["id"],
        childColumns = ["songId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AudioFile(
    @PrimaryKey
    val id: Int,
    val fileName: String,
    val filePath: String,
    val fileExtension: String,
    val fileSize: Long,
    val songId: Int
)
