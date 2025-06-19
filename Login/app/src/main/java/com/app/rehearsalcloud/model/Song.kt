package com.app.rehearsalcloud.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Song(
    val id: Int,
    val songName: String,
    val artist: String,
    val bpm: Int,
    val tone: String,
    val coverImage: String,
    val createdAt: String,
    val audioFiles: List<AudioFile>
)