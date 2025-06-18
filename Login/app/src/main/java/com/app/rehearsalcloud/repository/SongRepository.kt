package com.app.rehearsalcloud.repository

import android.content.Context
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.interfaces.SongDao
import com.app.rehearsalcloud.model.song.SongWithAudioFiles
import com.app.rehearsalcloud.utils.ZipUtils
import java.io.File

// Example usage in a ViewModel or Repository
class SongRepository(private val songDao: SongDao, private val context: Context) {
    suspend fun saveSongFromZip(song: Song, zipFile: File) {
        // Insert the Song into the database
        songDao.insertSong(song)

        // Unzip and save audio files
        val audioFiles = ZipUtils.unzipSongFiles(zipFile, song.id, context)
        audioFiles.forEach { audioFile ->
            songDao.insertAudioFile(audioFile)
        }
    }

    suspend fun getSongWithAudioFiles(songId: Int): List<SongWithAudioFiles> {
        return songDao.getSongWithAudioFiles(songId)
    }
}