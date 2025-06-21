package com.app.rehearsalcloud.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.repository.SetlistRepository
import com.app.rehearsalcloud.repository.SongRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SongViewModel(
    val repository: SongRepository
) : ViewModel() {
    var songs by mutableStateOf<List<Song>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var selectedSong by mutableStateOf<Song?>(null)
        private set

    fun loadSongs() {
        viewModelScope.launch {
            isLoading = true
            Log.d("SongViewModel", "Loading songs...")
            try {
                repository.syncSongs()
                songs = repository.getSongs()
                Log.d("SongViewModel", "Songs loaded: ${songs.size} items")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SongViewModel", "Failed to load songs: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun getSongById(id: Int, fetchAudio: Boolean = false) {
        viewModelScope.launch {
            isLoading = true
            try {
                selectedSong = repository.getSongById(id, fetchAudio)
                Log.d("SongViewModel", "Song loaded: $selectedSong")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SongViewModel", "Failed to load song: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun createSong(song: Song, coverImageFile: File, zipFile: File) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.createSong(song, coverImageFile, zipFile)
                loadSongs()
                Log.d("SongViewModel", "Song created successfully")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SongViewModel", "Failed to create song: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSong(song: Song, coverImageFile: File?, zipFile: File?) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.updateSong(song, coverImageFile, zipFile)
                loadSongs()
                Log.d("SongViewModel", "Song updated successfully")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SongViewModel", "Failed to update song: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSong(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.deleteSong(id)
                songs = songs.filter { it.id != id }
                Log.d("SongViewModel", "Song deleted: $id")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SongViewModel", "Failed to delete song: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }
}