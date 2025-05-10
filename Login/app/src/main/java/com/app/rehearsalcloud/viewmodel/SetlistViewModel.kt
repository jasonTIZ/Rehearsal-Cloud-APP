package com.app.rehearsalcloud.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.model.Setlist
import com.app.rehearsalcloud.repository.SetlistRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SetlistViewModel(private val repository: SetlistRepository = SetlistRepository()) : ViewModel() {

    var setlists by mutableStateOf<List<Setlist>>(emptyList())
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
                setlists = repository.getSetlists()
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
        // Parse from MM/dd/yyyy
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val parsedDate = inputFormat.parse(inputDate)
        val isoDate = outputFormat.format(parsedDate ?: Date())

        val newSetlist = Setlist(
            name = name,
            date = isoDate,
            setlistSongs = emptyList()
        )

        Log.d("API", "Creating setlist: $newSetlist")

        viewModelScope.launch {
            try {
                repository.createSetlist(newSetlist)
                Log.d("API", "Setlist created successfully")
                loadSetlists()
            } catch (e: Exception) {
                Log.e("API", "Failed to create setlist: ${e.localizedMessage}", e)
            }
        }
    }

    fun updateSetlist(id: Int, setlist: Setlist) {
        viewModelScope.launch {
            try {
                val formattedDate = convertToIsoFormat(setlist.date)
                val updated = Setlist(
                    name = setlist.name,
                    date = formattedDate,
                    setlistSongs = setlist.setlistSongs
                )
                repository.updateSetlist(id, updated)

                // Update local list if needed
                setlists = setlists.map { if (it.id == id) updated.copy(id = id) else it }

            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    var selectedSetlist by mutableStateOf<Setlist?>(null)
        private set

    fun getSetlistById(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                selectedSetlist = repository.getSetlistById(id) // Use selectedSetlist for single setlist
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete a setlist
    fun deleteSetlist(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteSetlist(id)
                setlists = setlists.filter { it.id != id }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    private fun convertToIsoFormat(dateString: String): String {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val parsedDate = inputFormat.parse(dateString)
        return outputFormat.format(parsedDate ?: Date())
    }
}
