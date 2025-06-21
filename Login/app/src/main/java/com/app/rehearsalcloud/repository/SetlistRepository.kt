package com.app.rehearsalcloud.repository

import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.api.SetlistApiService
import com.app.rehearsalcloud.dtos.SetlistDto
import com.app.rehearsalcloud.dtos.UpdateSetlistRequestDto
import com.app.rehearsalcloud.interfaces.SetlistDao
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistSongCrossRef
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.model.song.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SetlistRepository(
    private val setlistDao: SetlistDao,
    private val setlistApiService: SetlistApiService
    ) {
        // Fetch setlists from API and save to Room
        suspend fun syncSetlists() {
            try {
                val setlists = setlistApiService.getSetlistsWithoutSongs()
                val setlistEntities = setlists.map { dto ->
                    Setlist(
                        id = dto.id,
                        name = dto.name,
                        date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                            .parse(dto.date)?.time ?: 0L
                    )
                }
                setlistDao.insertSetlists(setlistEntities)
            } catch (e: Exception) {
                throw Exception("Failed to sync setlists: ${e.message}")
            }
        }

    // Get setlists without songs from Room
    suspend fun getSetlists(): List<Setlist> {
        return setlistDao.getAllSetlists()
    }

    // Get a specific setlist with songs by ID
    suspend fun getSetlistWithSongsById(id: Int): SetlistWithSongs? {
        return setlistDao.getSetlistWithSongsById(id)
    }

        // Get setlists with songs from Room
        suspend fun getSetlistsWithSongs(): List<SetlistWithSongs> {
            return setlistDao.getAllSetlistsWithSongs()
        }

        // Update setlist locally and sync with server
        suspend fun updateSetlist(setlistId: Int, name: String, date: Long, songIds: List<Int>) {
            // Update local database
            val setlist = Setlist(setlistId, name, date)
            setlistDao.insertSetlists(listOf(setlist))

            // Update song associations
            setlistDao.deleteSetlistSongs(setlistId)
            val setlistSongs = songIds.map { songId ->
                SetlistSongCrossRef(setlistId, songId)
            }
            setlistDao.insertSetlistSongs(setlistSongs)

            // Sync with server
            try {
                val updateDto = UpdateSetlistRequestDto(
                    name = name,
                    date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(date)),
                    setlistSongs = songIds
                )
                val updatedSetlistDto = setlistApiService.updateSetlist(setlistId, updateDto)
                // Update local database with the server response
                updateLocalDatabaseFromSetlistDto(updatedSetlistDto)
            } catch (e: Exception) {
                throw Exception("Failed to update setlist on server: ${e.message}")
            }
        }

        // Update local database with SetlistDto from server
        suspend fun updateLocalDatabaseFromSetlistDto(setlistDto: SetlistDto) {
            val setlistEntity = Setlist(
                id = setlistDto.id,
                name = setlistDto.name,
                date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .parse(setlistDto.date)?.time ?: 0L
            )
            setlistDao.insertSetlists(listOf(setlistEntity))

            setlistDto.setlistSongs?.let { setlistSongs ->
                val setlistSongEntities = setlistSongs.map { ss ->
                    SetlistSongCrossRef(
                        setlistId = ss.setlistId,
                        songId = ss.songId
                    )
                }
                setlistDao.deleteSetlistSongs(setlistDto.id)
                setlistDao.insertSetlistSongs(setlistSongEntities)

                // Optionally update songs
                val songEntities = setlistSongs.mapNotNull { ss ->
                    ss.song?.let { songDto ->
                        Song(
                            id = songDto.id,
                            songName = songDto.songName,
                            artist = songDto.artist,
                            bpm = songDto.bpm,
                            tone = songDto.tone,
                            coverImage = songDto.coverImage,
                            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                                .parse(songDto.createdAt)?.time ?: 0L
                        )
                    }
                }
                setlistDao.insertSongs(songEntities)
            }
        }

    // Create a new setlist
    suspend fun createSetlist(setlist: Setlist) {
        try {
            val createDto = UpdateSetlistRequestDto(
                name = setlist.name,
                date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    .format(Date(setlist.date)),
                setlistSongs = emptyList() // Always empty for new setlists
            )
            val createdSetlistDto = setlistApiService.createSetlist(createDto)
            updateLocalDatabaseFromSetlistDto(createdSetlistDto)
        } catch (e: Exception) {
            throw Exception("Failed to create setlist: ${e.message}")
        }
    }

        suspend fun deleteSetlist(id: Int) {
            val response = setlistApiService.deleteSetlist(id)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete setlist")
            }
        }
    }

