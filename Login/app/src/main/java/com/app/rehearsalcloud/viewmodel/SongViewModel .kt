package com.app.rehearsalcloud.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.audio.AudioWaveformGenerator
import com.app.rehearsalcloud.audio.MultitrackSong
import kotlinx.coroutines.launch
import android.content.Context

class SongViewModel : ViewModel() {
    var multitrackSong by mutableStateOf<MultitrackSong?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadSong(context: Context, songId: Int, songName: String) {
        isLoading = true
        errorMessage = null
        val api = RetrofitClient.songApiService
        val generator = AudioWaveformGenerator(context)
        viewModelScope.launch {
            try {
                val response = api.getSong(songId)
                if (response.isSuccessful) {
                    val song = response.body()!!
                    val result = generator.processSongFromBackend(songName, song.audioFiles)
                    multitrackSong = result
                } else {
                    errorMessage = "Error loading song"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}