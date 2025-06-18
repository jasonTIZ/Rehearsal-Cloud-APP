package com.app.rehearsalcloud.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.repository.SetlistRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SetlistViewModel(private val repository: SetlistRepository = SetlistRepository()) : ViewModel() {

    var setlists by mutableStateOf<List<SetlistWithSongs>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Load setlists from API
    fun loadSetlists() {
        viewModelScope.launch {
            isLoading = true
            Log.d("API", "Loading setlists...")
            try {
                setlists = repository.getSetlistsWithSongs()
                Log.d("API", "Setlists loaded: ${setlists.size} items")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("API", "Failed to load setlists: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun createSetlist(name: String, inputDate: String) {
        // Parse from MM/dd/yyyy to ISO format
        val isoDate = convertToIsoFormat(inputDate)

        val newSetlist = Setlist(
            id = 0, // Room will auto-generate or API will assign
            name = name,
            date = isoDate
        )

        Log.d("API", "Creating setlist: $newSetlist")

        viewModelScope.launch {
            try {
                repository.createSetlist(newSetlist)
                Log.d("API", "Setlist created successfully")
                loadSetlists()
            } catch (e: Exception) {
                Log.e("API", "Failed to create setlist: ${e.localizedMessage}", e)
                errorMessage = e.message
            }
        }
    }

    fun updateSetlist(id: Int, setlist: SetlistWithSongs) {
        viewModelScope.launch {
            try {
                // Ensure date is in ISO format
                val formattedDate = convertToIsoFormat(setlist.setlist.date)
                val updated = setlist.setlist.copy(
                    id = id,
                    date = formattedDate
                )
                repository.updateSetlist(id, updated)
                // Update local list
                setlists = setlists.map { if (it.setlist.id == id) updated else it } as List<SetlistWithSongs>
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("API", "Failed to update setlist: ${e.localizedMessage}", e)
            }
        }
    }

    var selectedSetlist by mutableStateOf<SetlistWithSongs?>(null)
        private set

    fun getSetlistById(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                selectedSetlist = repository.getSetlistWithSongsById(id)
                Log.d("API", "Setlist loaded: $selectedSetlist")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("API", "Failed to load setlist: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSetlist(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteSetlist(id)
                setlists = setlists.filter { it.setlist.id != id }
                Log.d("API", "Setlist deleted: $id")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("API", "Failed to delete setlist: ${e.localizedMessage}", e)
            }
        }
    }

    private fun convertToIsoFormat(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = inputFormat.parse(dateString)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            Log.e("SetlistViewModel", "Date parsing failed: ${e.localizedMessage}", e)
            // Fallback to current date in ISO format
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(Date())
        }
    }
}
