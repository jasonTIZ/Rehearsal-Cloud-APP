package com.app.rehearsalcloud.repository

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.app.rehearsalcloud.api.SongApiService
import com.app.rehearsalcloud.interfaces.SongDao
import com.app.rehearsalcloud.model.audiofile.AudioFile
import com.app.rehearsalcloud.model.song.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class SongRepository(
    private val songDao: SongDao,
    private val songApiService: SongApiService
) {
    suspend fun syncSongs() {
        try {
            val apiSongs = songApiService.getSongs().map { dto ->
                Song(
                    id = dto.id,
                    songName = dto.songName,
                    artist = dto.artist,
                    bpm = dto.bpm,
                    tone = dto.tone,
                    coverImage = dto.coverImage,
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        .parse(dto.createdAt)?.time ?: 0L
                )
            }
            songDao.deleteAllSongs()      // Borra todas las canciones locales
            songDao.insertSongs(apiSongs) // Inserta las nuevas canciones del API
        } catch (e: Exception) {
            Log.e("SongRepository", "Sync failed: ${e.message}")
            throw Exception("Failed to sync songs: ${e.message}")
        }
    }

    suspend fun getSongs(): List<Song> {
        return songDao.getAllSongs()
    }

    suspend fun getSongById(id: Int, fetchAudio: Boolean = false): Song {
        val localSong = songDao.getSongById(id)
        if (localSong != null && !fetchAudio) {
            return localSong
        }
        try {
            val songDto = songApiService.getSongById(id)
            val songEntity = Song(
                id = songDto.id,
                songName = songDto.songName,
                artist = songDto.artist,
                bpm = songDto.bpm,
                tone = songDto.tone,
                coverImage = songDto.coverImage,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .parse(songDto.createdAt)?.time ?: 0L
            )
            songDao.insertSong(songEntity)

            if (fetchAudio && songDto.audioFiles != null) {
                val audioFileEntities = songDto.audioFiles.map { af ->
                    AudioFile(
                        id = af.id,
                        fileName = af.fileName,
                        fileExtension = af.fileExtension,
                        fileSize = af.fileSize,
                        songId = af.songId,
                        fileUrl = af.fileUrl,
                        localPath = null
                    )
                }
                songDao.insertAudioFiles(audioFileEntities)
            }
            return songEntity
        } catch (e: Exception) {
            Log.e("SongRepository", "Fetch failed: ${e.message}")
            throw Exception("Failed to fetch song: ${e.message}")
        }
    }

    suspend fun getAudioFilesBySongId(songId: Int): List<AudioFile> {
        return songDao.getAudioFilesBySongId(songId)
    }

    suspend fun downloadAudioFile(songId: Int, audioId: Int, outputDir: File): File {
        try {
            val audioFile = songDao.getAudioFilesBySongId(songId).find { it.id == audioId }
                ?: run {
                    val songDto = songApiService.getSongById(songId)
                    songDto.audioFiles?.find { it.id == audioId }?.let { af ->
                        if (af.fileUrl == null) {
                            throw Exception("Audio file URL is null for audioId=$audioId")
                        }
                        val audioFileEntity = AudioFile(
                            id = af.id,
                            fileName = af.fileName,
                            fileExtension = af.fileExtension,
                            fileSize = af.fileSize,
                            songId = af.songId,
                            fileUrl = af.fileUrl,
                            localPath = null
                        )
                        songDao.insertAudioFiles(listOf(audioFileEntity))
                        audioFileEntity
                    } ?: throw Exception("Audio file metadata not found for ID $audioId")
                }

            if (audioFile.localPath != null && File(audioFile.localPath).exists()) {
                return File(audioFile.localPath)
            }

            val response = songApiService.downloadAudioFile(songId, audioId)
            if (!response.isSuccessful) {
                throw Exception("Failed to download audio file: ${response.code()}")
            }
            val responseBody = response.body() ?: throw Exception("Response body is null")

            val file = File(outputDir, "${audioFile.id}_${audioFile.fileName}${audioFile.fileExtension}")
            withContext(Dispatchers.IO) {
                responseBody.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            songDao.insertAudioFiles(
                listOf(
                    audioFile.copy(localPath = file.absolutePath)
                )
            )
            return file
        } catch (e: Exception) {
            Log.e("SongRepository", "Download failed: songId=$songId, audioId=$audioId, error=${e.message}")
            throw e
        }
    }

    suspend fun createSong(song: Song, coverImageFile: File, zipFile: File) {
        try {
            val coverImagePart = MultipartBody.Part.createFormData(
                "CoverImage",
                coverImageFile.name,
                coverImageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val zipFilePart = MultipartBody.Part.createFormData(
                "ZipFile",
                zipFile.name,
                zipFile.asRequestBody("application/zip".toMediaTypeOrNull())
            )
            val createdSongDto = songApiService.createSong(
                songName = song.songName,
                artist = song.artist,
                bpm = song.bpm,
                tone = song.tone,
                coverImage = coverImagePart,
                zipFile = zipFilePart
            )
            val createdSong = Song(
                id = createdSongDto.id,
                songName = createdSongDto.songName,
                artist = createdSongDto.artist,
                bpm = createdSongDto.bpm,
                tone = createdSongDto.tone,
                coverImage = createdSongDto.coverImage,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .parse(createdSongDto.createdAt)?.time ?: 0L
            )
            songDao.insertSong(createdSong)
            createdSongDto.audioFiles?.let { audioFiles ->
                val audioFileEntities = audioFiles.map { af ->
                    AudioFile(
                        id = af.id,
                        fileName = af.fileName,
                        fileExtension = af.fileExtension,
                        fileSize = af.fileSize,
                        songId = af.songId,
                        fileUrl = af.fileUrl,
                        localPath = null
                    )
                }
                songDao.insertAudioFiles(audioFileEntities)
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Create failed: ${e.message}")
            throw Exception("Failed to create song: ${e.message}")
        }
    }

    suspend fun updateSong(song: Song, coverImageFile: File?, zipFile: File?) {
        try {
            val coverImagePart = coverImageFile?.let {
                MultipartBody.Part.createFormData(
                    "CoverImage",
                    it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }
            val zipFilePart = zipFile?.let {
                MultipartBody.Part.createFormData(
                    "ZipFile",
                    it.name,
                    it.asRequestBody("application/zip".toMediaTypeOrNull())
                )
            }
            val updatedSongDto = songApiService.updateSong(
                id = song.id,
                songName = song.songName,
                artist = song.artist,
                bpm = song.bpm,
                tone = song.tone,
                coverImage = coverImagePart,
                zipFile = zipFilePart
            )
            val updatedSong = Song(
                id = updatedSongDto.id,
                songName = updatedSongDto.songName,
                artist = updatedSongDto.artist,
                bpm = updatedSongDto.bpm,
                tone = updatedSongDto.tone,
                coverImage = updatedSongDto.coverImage,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .parse(updatedSongDto.createdAt)?.time ?: 0L
            )
            songDao.insertSong(updatedSong)
            updatedSongDto.audioFiles?.let { audioFiles ->
                val audioFileEntities = audioFiles.map { af ->
                    AudioFile(
                        id = af.id,
                        fileName = af.fileName,
                        fileExtension = af.fileExtension,
                        fileSize = af.fileSize,
                        songId = af.songId,
                        fileUrl = af.fileUrl,
                        localPath = null
                    )
                }
                songDao.insertAudioFiles(audioFileEntities)
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Update failed: ${e.message}")
            throw Exception("Failed to update song: ${e.message}")
        }
    }

    suspend fun deleteSong(id: Int) {
    try {
        songApiService.deleteSong(id) // No esperes ningún body, solo llama
        songDao.deleteSong(id)        // Borra localmente solo si no hubo excepción
    } catch (e: Exception) {
        Log.e("SongRepository", "Delete failed: ${e.message}")
        throw Exception("Failed to delete song: ${e.message}")
    }

    suspend fun playAudioFile(songId: Int, audioId: Int, context: Context): MediaPlayer {
        try {
            val audioFile = songDao.getAudioFilesBySongId(songId).find { it.id == audioId }
                ?: throw Exception("Audio file metadata not found for ID $audioId")

            if (audioFile.localPath == null || !File(audioFile.localPath).exists()) {
                downloadAudioFile(songId, audioId, File(context.filesDir, "audio_files"))
            }

            val updatedAudioFile = songDao.getAudioFilesBySongId(songId).find { it.id == audioId }
                ?: throw Exception("Audio file metadata not found after download for ID $audioId")

            if (updatedAudioFile.localPath == null) {
                throw Exception("Local path is null for audioId=$audioId")
            }

            return MediaPlayer().apply {
                setDataSource(updatedAudioFile.localPath)
                prepare()
                start()
                Log.d("SongRepository", "Playing audio file: ${updatedAudioFile.localPath}")
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Playback failed: songId=$songId, audioId=$audioId, error=${e.message}")
            throw e
        }
    }
}
}