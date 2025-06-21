package com.app.rehearsalcloud.dtos

data class SongDto(
    val id: Int,
    val songName: String,
    val artist: String,
    val bpm: Int,
    val tone: String,
    val coverImage: String?,
    val createdAt: String,
    val audioFiles: List<AudioFileDto>?
)