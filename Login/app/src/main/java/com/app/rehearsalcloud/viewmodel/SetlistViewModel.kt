package com.app.rehearsalcloud.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.api.RetrofitClient.setlistApiService
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.model.setlist.SetlistWithSongsWithAudioFile
import com.app.rehearsalcloud.repository.SetlistRepository
import com.app.rehearsalcloud.ui.setlist.formatDateForDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SetlistViewModel(
    private val repository: SetlistRepository
) : ViewModel() {
    var setlists by mutableStateOf<List<Setlist>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    var selectedSetlist by mutableStateOf<SetlistWithSongs?>(null)
        private set

    private val _selectedSetlistWithSongs = MutableStateFlow<SetlistWithSongsWithAudioFile?>(null)
    val selectedSetlistWithSongs: StateFlow<SetlistWithSongsWithAudioFile?> = _selectedSetlistWithSongs

    fun validateDate(dateString: String): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            inputFormat.isLenient = false
            inputFormat.parse(dateString) != null
        } catch (e: Exception) {
            false
        }
    }

    fun loadSetlists() {
        viewModelScope.launch {
            isLoading = true
            Log.d("SetlistViewModel", "Loading setlists...")
            try {
                repository.syncSetlists()
                setlists = repository.getSetlists()
                Log.d("SetlistViewModel", "Setlists loaded: ${setlists.size} items")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to load setlists: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun createSetlist(name: String, inputDate: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                if (!validateDate(inputDate)) {
                    throw IllegalArgumentException("Invalid date format. Use MM/dd/yyyy")
                }
                val dateLong = convertToTimestamp(inputDate)
                val newSetlist = Setlist(id = 0, name = name, date = dateLong)
                Log.d("SetlistViewModel", "Creating setlist: $newSetlist")
                repository.createSetlist(newSetlist)
                loadSetlists()
                Log.d("SetlistViewModel", "Setlist created successfully")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to create setlist: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSetlist(id: Int, name: String, inputDate: String, songIds: List<Int>? = emptyList()) {
        viewModelScope.launch {
            isLoading = true
            try {
                if (!validateDate(inputDate)) {
                    throw IllegalArgumentException("Invalid date format. Use MM/dd/yyyy")
                }
                val dateLong = convertToTimestamp(inputDate)
                val safeSongIds = songIds ?: emptyList()
                Log.d("SetlistViewModel", "Updating setlist: id=$id, name=$name, date=$inputDate, songIds=$safeSongIds")
                repository.updateSetlist(id, name, dateLong, safeSongIds)
                setlists = repository.getSetlists()
                Log.d("SetlistViewModel", "Setlist updated successfully")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to update setlist: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // New method to update setlist with SetlistWithSongs
    fun updateSetlistWithSongs(setlistWithSongs: SetlistWithSongs) {
        viewModelScope.launch {
            isLoading = true
            try {
                val setlist = setlistWithSongs.setlist
                val songIds = setlistWithSongs.songs.map { it.id }
                if (!validateDate(formatDateForDisplay(setlist.date))) {
                    throw IllegalArgumentException("Invalid date format. Use MM/dd/yyyy")
                }
                Log.d("SetlistViewModel", "Updating setlist with songs: id=${setlist.id}, name=${setlist.name}, date=${setlist.date}, songIds=$songIds")
                repository.updateSetlist(setlist.id, setlist.name, setlist.date, songIds)
                setlists = repository.getSetlists()
                selectedSetlist = setlistWithSongs // Update selected setlist
                Log.d("SetlistViewModel", "Setlist with songs updated successfully")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to update setlist with songs: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun getSetlistById(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                selectedSetlist = repository.getSetlistWithSongsById(id)
                Log.d("SetlistViewModel", "Setlist loaded: $selectedSetlist")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to load setlist: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSetlist(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository.deleteSetlist(id)
                setlists = setlists.filter { it.id != id }
                Log.d("SetlistViewModel", "Setlist deleted: $id")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("SetlistViewModel", "Failed to delete setlist: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun convertToTimestamp(dateString: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            inputFormat.isLenient = false
            inputFormat.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("SetlistViewModel", "Date parsing failed: ${e.localizedMessage}", e)
            System.currentTimeMillis()
        }
    }

    suspend fun loadSetlistWithSongs(setlistId: Int) {
        try {
            // Llama a la API y actualiza Room
            val apiResponse = setlistApiService.getSetlistById(setlistId)
            repository.updateLocalDatabaseFromSetlistDto(apiResponse)

            // Ahora sí busca en Room y muestra
            val setlistWithSongs = repository.getSetlistWithSongsWithAudioFiles(setlistId)
            _selectedSetlistWithSongs.value = setlistWithSongs
        } catch (e: Exception) {
            errorMessage = ("Failed to load setlist: ${e.message}")
        }
    }
}