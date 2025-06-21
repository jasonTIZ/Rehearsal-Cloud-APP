package com.app.rehearsalcloud.dtos

data class AudioFileDto(
    val id: Int,
    val fileName: String,
    val fileExtension: String,
    val fileSize: Long,
    val songId: Int,
    val fileUrl: String?
)