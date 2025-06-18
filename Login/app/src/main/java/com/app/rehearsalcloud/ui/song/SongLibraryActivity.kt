package com.app.rehearsalcloud.ui.song

import CreateSetlistDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.ui.setlist.ConfirmDeleteDialog
import com.app.rehearsalcloud.viewmodel.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongLibraryActivity (navController: NavHostController) {
//    val viewModel: SongViewModel = viewModel()
//
//    var selectedTab by remember { mutableStateOf("All") }
//    var searchQuery by remember { mutableStateOf("") }
//    var isDialogOpen by remember { mutableStateOf(false) }
//    var songToDelete by remember { mutableStateOf<Setlist?>(null) }
//
//    var selectedDate by remember { mutableStateOf("MM/dd/yyyy") } // Default date format
//    var name by remember { mutableStateOf("") }
//
//    val filteredSetlists = viewModel.setlists.filter {
//        it.name.contains(searchQuery, ignoreCase = true)
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.loadSetlists()
//    }
//
//    // Show CreateSetlistDialog when `isDialogOpen` is true
//    if (isDialogOpen) {
//        CreateSetlistDialog(
//            onDismiss = { isDialogOpen = false },
//            onCreate = { setlistName, date ->
//                // Create setlist logic
//                viewModel.createSetlist(setlistName, date)
//                isDialogOpen = false
//            },
//            selectedDate = selectedDate,
//            onDateChange = { newDate -> selectedDate = newDate },
//            onNameChange = { newName -> name = newName }
//        )
//    }
//
//    setlistToDelete?.let { setlist ->
//        ConfirmDeleteDialog(
//            onDismiss = { setlistToDelete = null },
//            onConfirmDelete = {
//                viewModel.deleteSetlist(setlist.id!!)
//                setlistToDelete = null
//            }
//        )
//    }
//
//    if (viewModel.isLoading) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator(color = Color.Gray)
//        }
//    } else {
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)) {
//
//            // Header Row with title and create button
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Setlists", fontSize = 28.sp, fontWeight = FontWeight.Bold)
//
//                Button(
//                    onClick = {
//                        isDialogOpen = true // ✅ Open modal
//                    },
//                    border = BorderStroke(2.dp, Color.Black),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Text("+", fontSize = 24.sp, color = Color.Black)
//                    Spacer(Modifier.width(4.dp))
//                    Text("Create new", fontWeight = FontWeight.Bold, color = Color.Black)
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // Search Bar
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                placeholder = { Text("Search Setlists") },
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(10.dp)),
//                singleLine = true,
//                shape = RoundedCornerShape(10.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    containerColor = Color(0xFFF0F0F0),
//                    unfocusedBorderColor = Color.Transparent,
//                    focusedBorderColor = Color.Transparent
//                )
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Tabs and Sort
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row {
//                    listOf("On my device", "Shared with me").forEach { tab ->
//                        val isActive = tab == selectedTab
//                        Button(
//                            onClick = { selectedTab = tab },
//                            shape = RoundedCornerShape(50),
//                            border = if (!isActive) BorderStroke(1.dp, Color.Black) else null,
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = if (isActive) Color.Black else Color.Transparent,
//                                contentColor = if (isActive) Color.White else Color.Black
//                            ),
//                            modifier = Modifier.padding(end = 8.dp)
//                        ) {
//                            Text(tab)
//                        }
//                    }
//                }
//
//                TextButton(onClick = { /* Sorting logic placeholder */ }) {
//                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
//                    Text("Sort")
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // Setlist Items
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                items(filteredSetlists) { setlist ->
//                    SetlistItem(
//                        setlist = setlist,
//                        onEditClick = {
//                            navController.navigate("edit_setlist/${setlist.id}")
//                        },
//                        onDeleteClick = {
//                            setlistToDelete = setlist
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun SetlistItem(
//    setlist: Setlist,
//    onEditClick: (Setlist) -> Unit,
//    onDeleteClick: (Setlist) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column {
//            Text(setlist.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
//            Text("Author's name", color = Color.Gray, fontSize = 14.sp) // You may bind real author name here
//        }
//
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            IconButton(onClick = { onEditClick(setlist) }) {
//                Icon(Icons.Default.Edit, contentDescription = "Edit")
//            }
//            IconButton(onClick = { onDeleteClick(setlist) }) {
//                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
//            }
//        }
//    }
}