package com.app.rehearsalcloud.ui.song

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.AppDatabase
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.repository.SongRepository
import com.app.rehearsalcloud.viewmodel.SongViewModel
import com.app.rehearsalcloud.viewmodel.SongViewModelFactory
import java.io.File
import java.io.FileOutputStream

@Composable
fun SongManagerView(navController: NavController) {
    val viewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(
            SongRepository(
                AppDatabase.getDatabase(LocalContext.current).songDao(),
                RetrofitClient.songApiService
            )
        )
    )

    var selectedTab by remember { mutableStateOf("On my device") }
    var searchQuery by remember { mutableStateOf("") }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var songToEdit by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var songName by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var bpm by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("") }
    var coverImageFile by remember { mutableStateOf<File?>(null) }
    var zipFile by remember { mutableStateOf<File?>(null) }

    val filteredSongs by remember {
        derivedStateOf {
            viewModel.songs.filter {
                it.songName.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSongs()
    }

    if (isCreateDialogOpen) {
        SongDialog(
            isEditMode = false,
            song = null,
            onDismiss = {
                isCreateDialogOpen = false
                songName = ""
                artist = ""
                bpm = ""
                tone = ""
                coverImageFile = null
                zipFile = null
            },
            onSave = { name, artistValue, bpmValue, toneValue, coverImage, zip ->
                val song = Song(
                    id = 0,
                    songName = name,
                    artist = artistValue,
                    bpm = bpmValue.toIntOrNull() ?: 0,
                    tone = toneValue,
                    coverImage = null,
                    createdAt = System.currentTimeMillis()
                )
                if (coverImage != null && zip != null) {
                    viewModel.createSong(song, coverImage, zip)
                }
                isCreateDialogOpen = false
                songName = ""
                artist = ""
                bpm = ""
                tone = ""
                coverImageFile = null
                zipFile = null
            },
            songName = songName,
            artist = artist,
            bpm = bpm,
            tone = tone,
            coverImageFile = coverImageFile,
            zipFile = zipFile,
            onNameChange = { songName = it },
            onArtistChange = { artist = it },
            onBpmChange = { bpm = it },
            onToneChange = { tone = it },
            onCoverImageChange = { coverImageFile = it },
            onZipFileChange = { zipFile = it }
        )
    }

    if (isEditDialogOpen && songToEdit != null) {
        SongDialog(
            isEditMode = true,
            song = songToEdit,
            onDismiss = {
                isEditDialogOpen = false
                songToEdit = null
                songName = ""
                artist = ""
                bpm = ""
                tone = ""
                coverImageFile = null
                zipFile = null
            },
            onSave = { name, artistValue, bpmValue, toneValue, coverImage, zip ->
                val song = songToEdit!!.copy(
                    songName = name,
                    artist = artistValue,
                    bpm = bpmValue.toIntOrNull() ?: songToEdit!!.bpm,
                    tone = toneValue
                )
                viewModel.updateSong(song, coverImage, zip)
                isEditDialogOpen = false
                songToEdit = null
                songName = ""
                artist = ""
                bpm = ""
                tone = ""
                coverImageFile = null
                zipFile = null
            },
            songName = songName,
            artist = artist,
            bpm = bpm,
            tone = tone,
            coverImageFile = coverImageFile,
            zipFile = zipFile,
            onNameChange = { songName = it },
            onArtistChange = { artist = it },
            onBpmChange = { bpm = it },
            onToneChange = { tone = it },
            onCoverImageChange = { coverImageFile = it },
            onZipFileChange = { zipFile = it }
        )
    }

    songToDelete?.let { song ->
        ConfirmDeleteDialog(
            onDismiss = { songToDelete = null },
            onConfirmDelete = {
                viewModel.deleteSong(song.id)
                songToDelete = null
            }
        )
    }

    if (viewModel.isLoading) {
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
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Songs", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { isCreateDialogOpen = true },
                    border = BorderStroke(2.dp, Color.Black),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+", fontSize = 24.sp, color = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("Create new", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Songs") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(10.dp)),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    listOf("On my device", "Shared with me").forEach { tab ->
                        val isActive = tab == selectedTab
                        Button(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(50),
                            border = if (!isActive) BorderStroke(1.dp, Color.Black) else null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) Color.Black else Color.Transparent,
                                contentColor = if (isActive) Color.White else Color.Black
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(tab)
                        }
                    }
                }
                TextButton(onClick = { /* Sorting logic TBD */ }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    Text("Sort")
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSongs) { song ->
                    SongItemRow(
                        song = song,
                        onEditClick = {
                            songToEdit = song
                            songName = song.songName
                            artist = song.artist
                            bpm = song.bpm.toString()
                            tone = song.tone
                            isEditDialogOpen = true
                        },
                        onDeleteClick = { songToDelete = song }
                    )
                }
            }
        }
    }
}

@Composable
fun SongItemRow(
    song: Song,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(song.songName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(song.artist, color = Color.Gray, fontSize = 14.sp)
                Text("BPM: ${song.bpm}, Tone: ${song.tone}", color = Color.Gray, fontSize = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { onEditClick() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDeleteClick() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun SongDialog(
    isEditMode: Boolean,
    song: Song?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, File?, File?) -> Unit,
    songName: String,
    artist: String,
    bpm: String,
    tone: String,
    coverImageFile: File?,
    zipFile: File?,
    onNameChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onBpmChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onCoverImageChange: (File?) -> Unit,
    onZipFileChange: (File?) -> Unit
) {
    var nameError by remember { mutableStateOf<String?>(null) }
    var artistError by remember { mutableStateOf<String?>(null) }
    var bpmError by remember { mutableStateOf<String?>(null) }
    var coverImageError by remember { mutableStateOf<String?>(null) }
    var zipFileError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = createTempFile(context, it, "image")
            onCoverImageChange(file)
            coverImageError = null
        }
    }
    val pickZipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = createTempFile(context, it, "zip")
            onZipFileChange(file)
            zipFileError = null
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(if (isEditMode) "Edit Song" else "Create Song") },
        text = {
            Column {
                OutlinedTextField(
                    value = songName,
                    onValueChange = {
                        onNameChange(it)
                        nameError = if (it.isBlank()) "Name cannot be empty" else null
                    },
                    label = { Text("Song Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null
                )
                if (nameError != null) {
                    Text(nameError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = artist,
                    onValueChange = {
                        onArtistChange(it)
                        artistError = if (it.isBlank()) "Artist cannot be empty" else null
                    },
                    label = { Text("Artist") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = artistError != null
                )
                if (artistError != null) {
                    Text(artistError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = bpm,
                    onValueChange = {
                        onBpmChange(it)
                        bpmError = when {
                            it.isBlank() -> null
                            it.toIntOrNull() == null -> "Invalid BPM"
                            it.toInt() < 40 || it.toInt() > 280 -> "BPM must be 40-280"
                            else -> null
                        }
                    },
                    label = { Text("BPM") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = bpmError != null
                )
                if (bpmError != null) {
                    Text(bpmError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tone,
                    onValueChange = { onToneChange(it) },
                    label = { Text("Tone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(coverImageFile?.name ?: "Select Cover Image")
                }
                if (coverImageError != null) {
                    Text(coverImageError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { pickZipLauncher.launch("application/zip") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(zipFile?.name ?: "Select Zip File")
                }
                if (zipFileError != null) {
                    Text(zipFileError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coverImageError = if (coverImageFile == null && !isEditMode) "Cover image required" else null
                    zipFileError = if (zipFile == null && !isEditMode) "Zip file required" else null
                    if (nameError == null && artistError == null && bpmError == null && coverImageError == null && zipFileError == null) {
                        onSave(songName, artist, bpm, tone, coverImageFile, zipFile)
                    }
                },
                enabled = songName.isNotBlank() && artist.isNotBlank() && bpmError == null &&
                        (isEditMode || (coverImageFile != null && zipFile != null))
            ) {
                Text(if (isEditMode) "Save" else "Create")
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
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Song") },
        text = { Text("Are you sure you want to delete this song?") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun createTempFile(context: Context, uri: Uri, prefix: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val extension = when (prefix) {
            "image" -> context.contentResolver.getType(uri)?.let { type ->
                when {
                    type.contains("jpeg") -> ".jpg"
                    type.contains("png") -> ".png"
                    else -> ".jpg"
                }
            } ?: ".jpg"
            "zip" -> ".zip"
            else -> return null
        }
        val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file
    } catch (e: Exception) {
        null
    }
}