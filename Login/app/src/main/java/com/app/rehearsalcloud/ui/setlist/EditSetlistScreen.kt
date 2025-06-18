package com.app.rehearsalcloud.ui.setlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.model.setlist.SetlistWithSongs
import com.app.rehearsalcloud.model.song.Song
import com.app.rehearsalcloud.viewmodel.SetlistViewModel

@Composable
fun EditSetlistScreen(
    setlistId: Int
) {
    val navController = rememberNavController()
    val viewModel: SetlistViewModel = viewModel()

    // Fetch setlist from the viewModel (ideally should be managed in ViewModel)
    val setlist = viewModel.selectedSetlist

    // State to control dialog visibility
    var showDialog by remember { mutableStateOf(false) }

    // State for editing setlist name and date
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    LaunchedEffect(setlistId, setlist) {
        viewModel.getSetlistById(setlistId)

        if (setlist != null) {
            name = setlist.setlist.name
            date = setlist.setlist.date
        }
    }

    // If the setlist is null or the data is still loading, show a loading indicator
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Gray)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header with close and edit actions
            SetlistHeader(
                setlistName = name,
                onClose = {
                    // Navigate back on close
                    navController.popBackStack()
                },
                onEdit = {
                    // Open the dialog to edit setlist details
                    showDialog = true
                }
            )
            Spacer(Modifier.height(20.dp))

            // Service info section (setlist name, date)
            ServiceInfo(serviceDate = date, onRemoveSetlist = {
                // Handle setlist deletion
                viewModel.deleteSetlist(setlistId)
                navController.popBackStack() // Navigate back after deletion
            })
            Spacer(Modifier.height(20.dp))

            // Song table header and songs list
            SongTableHeader()
            HorizontalDivider()
            // SongList(setlist.setlistSongs ?: emptyList()) // You can replace with actual song data
        }
    }

    // Show the EditSetlistDialog when dialog visibility is true
    if (showDialog) {
        EditSetlistDialog(
            setlistId = setlistId,
            initialName = name,
            initialDate = date,
            onDismiss = { showDialog = false },
            onEdit = { id, updatedName, updatedDate ->

                // When edit is confirmed, create a new updated setlist
                val updatedSetlist = setlist.let {
                    it?.let { it1 ->
                        SetlistWithSongs(
                            Setlist(id, name, date),
                            it1.songs
                        )
                    }
                }

                // Update the setlist using the viewModel
                if (updatedSetlist != null) {
                    viewModel.updateSetlist(id, updatedSetlist)
                }
                viewModel.getSetlistById(id) // Re-fetch the updated data

                // Close the dialog after editing
                showDialog = false
            },
            onDateChange = { updatedDate -> date = updatedDate },
            onNameChange = { updatedName -> name = updatedName }
        )
    }
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
fun SongList(songs: List<Int>) { // This list should conform for Song instead of Int
    LazyColumn {
        itemsIndexed(songs) { index, song ->
            SongRow(index = index, song = song)
            HorizontalDivider()
        }
    }
}

@Composable
fun SongRow(index: Int, song: Int) { // Here should be a Song type
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
            Image(
                painter = rememberAsyncImagePainter(song),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("song.name", fontWeight = FontWeight.Medium)
                Text("song.subtitle", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Text("song.key", modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
        Text("song.bpm.toString()", modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
    }
}