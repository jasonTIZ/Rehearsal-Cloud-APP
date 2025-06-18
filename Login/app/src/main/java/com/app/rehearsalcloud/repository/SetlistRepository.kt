package com.app.rehearsalcloud.repository

import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs

class SetlistRepository {

    private val apiService = RetrofitClient.setlistApiService

    suspend fun getSetlists(): List<Setlist> {
        val response = apiService.getSetlists()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load setlists")
        }
    }

    suspend fun getSetlistsWithSongs(): List<SetlistWithSongs> {
        val response = apiService.getSetlistsWithSongs()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load setlists")
        }
    }

    suspend fun createSetlist(setlist: Setlist): Setlist {
        val response = apiService.createSetlist(setlist)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to create setlist")
        }
    }

    suspend fun updateSetlist(id: Int, setlist: Setlist): Setlist {
        val response = apiService.updateSetlist(id, setlist)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Failed to update setlist")
        }
    }

    suspend fun getSetlistById(id: Int): Setlist {
        val response = apiService.getSetlistById(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Setlist not found")
        } else {
            throw Exception("Failed to fetch setlist")
        }
    }

    suspend fun getSetlistWithSongsById(id: Int): SetlistWithSongs {
        val response = apiService.getSetlistWithSongsById(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Setlist not found")
        } else {
            throw Exception("Failed to fetch setlist")
        }
    }

    suspend fun deleteSetlist(id: Int) {
        val response = apiService.deleteSetlist(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete setlist")
        }
    }
}
