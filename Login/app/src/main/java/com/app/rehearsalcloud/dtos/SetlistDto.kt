package com.app.rehearsalcloud.dtos

data class SetlistDto(
    val id: Int,
    val name: String,
    val date: String, // ISO 8601 date string, e.g., "2025-06-20T10:50:00"
    val setlistSongs: List<SetlistSongDto>?
)

data class SetlistSongDto(
    val setlistId: Int,
    val songId: Int,
    val song: SongDto?
)

data class SetlistWithoutSongsDto(
    val id: Int,
    val name: String,
    val date: String // Assuming the API sends ISO 8601 date string
)

data class UpdateSetlistRequestDto(
    val name: String,
    val date: String, // Send ISO 8601 date string
    val setlistSongs: List<Int>?
)