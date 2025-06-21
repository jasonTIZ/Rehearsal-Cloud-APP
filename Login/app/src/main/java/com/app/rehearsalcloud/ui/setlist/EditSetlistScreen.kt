package com.app.rehearsalcloud.ui.setlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.AppDatabase
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.repository.SetlistRepository
import com.app.rehearsalcloud.repository.SongRepository
import com.app.rehearsalcloud.viewmodel.SetlistViewModel
import com.app.rehearsalcloud.viewmodel.SetlistViewModelFactory
import com.app.rehearsalcloud.viewmodel.SongViewModel
import com.app.rehearsalcloud.viewmodel.SongViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditSetlistScreen(
    navController: NavHostController,
    setlistId: Int
) {
    val viewModel: SetlistViewModel = viewModel(
        factory = SetlistViewModelFactory(
            SetlistRepository(
                AppDatabase.getDatabase(LocalContext.current).setlistDao(),
                RetrofitClient.setlistApiService
            )
        )
    )
    val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(
            SongRepository(
                AppDatabase.getDatabase(LocalContext.current).songDao(),
                RetrofitClient.songApiService
            )
        )
    )

    val setlist = viewModel.selectedSetlist
    var showEditDialog by remember { mutableStateOf(false) }
    var showSongSelectionDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("MM/dd/yyyy") }
    var dateError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioDir = File(context.filesDir, "audio_files").apply { mkdirs() }

    LaunchedEffect(setlistId) {
        viewModel.getSetlistById(setlistId)
        songViewModel.loadSongs()
    }

    LaunchedEffect(setlist) {
        setlist?.let {
            name = it.setlist.name
            date = formatDateForDisplay(it.setlist.date)
        }
    }

    if (viewModel.isLoading || songViewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Gray)
        }
    } else if (viewModel.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Error: ${viewModel.errorMessage}",
                color = Color.Red,
                fontSize = 16.sp
            )
        }
    } else if (setlist != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            SetlistHeader(
                setlistName = name,
                onClose = { navController.popBackStack() },
                onEdit = { showEditDialog = true }
            )
            Spacer(Modifier.height(20.dp))

            ServiceInfo(
                serviceDate = date,
                onRemoveSetlist = {
                    viewModel.deleteSetlist(setlistId)
                    navController.popBackStack()
                }
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Songs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showSongSelectionDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Song")
                    Text("Add Songs")
                }
            }
            Spacer(Modifier.height(8.dp))

            SongTableHeader()
            HorizontalDivider()
            SongList(
                songs = setlist.songs,
                onSongSelectionChanged = { selectedSongIds ->
                    if (viewModel.validateDate(date)) {
                        val updatedSongs = setlist.songs.filter { it.id in selectedSongIds }
                        val updatedSetlist = SetlistWithSongs(
                            setlist = Setlist(
                                id = setlistId,
                                name = name,
                                date = convertToTimestamp(date)
                            ),
                            songs = updatedSongs
                        )
                        viewModel.updateSetlistWithSongs(updatedSetlist)
                    } else {
                        dateError = "Invalid date format. Use MM/dd/yyyy"
                    }
                }
            )
        }
    }

    if (showEditDialog) {
        EditSetlistDialog(
            setlistId = setlistId,
            initialName = name,
            initialDate = date,
            onDismiss = { showEditDialog = false },
            onEdit = { id, updatedName, updatedDate ->
                if (viewModel.validateDate(updatedDate)) {
                    val updatedSongs = setlist?.songs ?: emptyList()
                    val updatedSetlist = SetlistWithSongs(
                        setlist = Setlist(
                            id = id,
                            name = updatedName,
                            date = convertToTimestamp(updatedDate)
                        ),
                        songs = updatedSongs
                    )
                    viewModel.updateSetlistWithSongs(updatedSetlist)
                    name = updatedName
                    date = updatedDate
                    dateError = null
                    showEditDialog = false
                } else {
                    dateError = "Invalid date format. Use MM/dd/yyyy"
                }
            },
            onDateChange = { updatedDate ->
                date = updatedDate
                dateError = if (viewModel.validateDate(updatedDate)) null else "Invalid date format. Use MM/dd/yyyy"
            },
            onNameChange = { updatedName -> name = updatedName }
        )
    }

    if (showSongSelectionDialog) {
        SongSelectionDialog(
            songs = songViewModel.songs,
            onDismiss = { showSongSelectionDialog = false },
            onSongsSelected = { songIds ->
                coroutineScope.launch {
                    val newSongs = mutableListOf<Song>()
                    for (songId in songIds) {
                        try {
                            val song = songViewModel.repository.getSongById(songId, fetchAudio = true)
                            newSongs.add(song)
                            val audioFiles = songViewModel.repository.getAudioFilesBySongId(songId)
                            for (audioFile in audioFiles) {
                                if (audioFile.localPath == null) {
                                    songViewModel.repository.downloadAudioFile(songId, audioFile.id, audioDir)
                                }
                            }
                        } catch (e: Exception) {
                            viewModel.errorMessage = "Failed to add song: ${e.message}"
                        }
                    }
                    if (viewModel.validateDate(date)) {
                        val updatedSongs = (setlist?.songs ?: emptyList()) + newSongs
                        val updatedSetlist = SetlistWithSongs(
                            setlist = Setlist(
                                id = setlistId,
                                name = name,
                                date = convertToTimestamp(date)
                            ),
                            songs = updatedSongs
                        )
                        viewModel.updateSetlistWithSongs(updatedSetlist)
                    } else {
                        dateError = "Invalid date format. Use MM/dd/yyyy"
                    }
                    showSongSelectionDialog = false
                }
            }
        )
    }
}

@Composable
fun SongSelectionDialog(
    songs: List<Song>,
    onDismiss: () -> Unit,
    onSongsSelected: (List<Int>) -> Unit
) {
    var selectedSongIds by remember { mutableStateOf(setOf<Int>()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Songs") },
        text = {
            LazyColumn {
                itemsIndexed(songs) { _, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSongIds = if (song.id in selectedSongIds) {
                                    selectedSongIds - song.id
                                } else {
                                    selectedSongIds + song.id
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = song.id in selectedSongIds,
                            onCheckedChange = {
                                selectedSongIds = if (it) {
                                    selectedSongIds + song.id
                                } else {
                                    selectedSongIds - song.id
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(song.songName, fontWeight = FontWeight.Medium)
                            Text(song.artist, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSongsSelected(selectedSongIds.toList()) },
                enabled = selectedSongIds.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetlistHeader(setlistName: String, onClose: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFEE82EE), shape = RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("E", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(15.dp))
            Text(
                text = setlistName,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(24.dp))
            }
        }

        IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
        }
    }
}

@Composable
fun ServiceInfo(serviceDate: String, onRemoveSetlist: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Service date: $serviceDate", fontSize = 18.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = "Downloaded", tint = Color(0xFF2ECC71))
            Spacer(Modifier.width(6.dp))
            Text("Downloaded", color = Color(0xFF2ECC71))

            Spacer(Modifier.width(20.dp))

            IconButton(onClick = onRemoveSetlist) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFE74C3C))
            }
            Text("Remove Setlist", color = Color(0xFFE74C3C))
        }
    }
}

@Composable
fun SongTableHeader() {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("#", modifier = Modifier.width(30.dp), color = Color.Gray)
        Text("Name", modifier = Modifier.weight(1f), color = Color.Gray)
        Text("Key", modifier = Modifier.width(60.dp), color = Color.Gray)
        Text("BPM", modifier = Modifier.width(60.dp), color = Color.Gray)
    }
}

@Composable
fun SongList(
    songs: List<Song>,
    onSongSelectionChanged: (List<Int>) -> Unit
) {
    var selectedSongIds by remember { mutableStateOf(songs.map { it.id }.toSet()) }

    LazyColumn {
        itemsIndexed(songs) { index, song ->
            SongRow(
                index = index,
                song = song,
                isSelected = song.id in selectedSongIds,
                onSelectionChanged = { isSelected ->
                    selectedSongIds = if (isSelected) {
                        selectedSongIds + song.id
                    } else {
                        selectedSongIds - song.id
                    }
                    onSongSelectionChanged(selectedSongIds.toList())
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun SongRow(
    index: Int,
    song: Song,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${index + 1}", modifier = Modifier.width(30.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged(it) }
            )
            Spacer(Modifier.width(12.dp))
            Image(
                painter = rememberAsyncImagePainter(song.coverImage ?: ""),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(song.songName, fontWeight = FontWeight.Medium)
                Text(song.artist, color = Color.Gray, fontSize = 14.sp)
            }
        }

        Text(song.tone, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
        Text(song.bpm.toString(), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
    }
}

fun convertToTimestamp(dateString: String): Long {
    return try {
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        inputFormat.isLenient = false
        inputFormat.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
