package com.app.rehearsalcloud.model.song

import androidx.room.Entity
import androidx.room.PrimaryKey

// Song Entity
@Entity()
data class Song(
    @PrimaryKey
    val id: Int,
    val songName: String,
    val artist: String,
    val bpm: Int,
    val tone: String,
    val coverImage: String?, // Nullable to handle cases where no cover image is provided
    val createdAt: String
)
