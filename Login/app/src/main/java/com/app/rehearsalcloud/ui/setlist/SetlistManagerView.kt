package com.app.rehearsalcloud.ui.setlist

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.AppDatabase
import com.app.rehearsalcloud.model.setlist.Setlist
import com.app.rehearsalcloud.repository.SetlistRepository
import com.app.rehearsalcloud.viewmodel.SetlistViewModel
import com.app.rehearsalcloud.viewmodel.SetlistViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SetlistManagerView(navController: NavHostController) {
    val viewModel: SetlistViewModel = viewModel(
        factory = SetlistViewModelFactory(
            SetlistRepository(
                AppDatabase.getDatabase(LocalContext.current).setlistDao(),
                RetrofitClient.setlistApiService
            )
        )
    )

    var selectedTab by remember { mutableStateOf("On my device") }
    var searchQuery by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var setlistToDelete by remember { mutableStateOf<Setlist?>(null) }

    var selectedDate by remember { mutableStateOf("MM/dd/yyyy") }
    var name by remember { mutableStateOf("") }

    val filteredSetlists = viewModel.setlists.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.loadSetlists()
    }

    if (isDialogOpen) {
        EditSetlistDialog(
            setlistId = null,
            onDismiss = {
                isDialogOpen = false
                name = ""
                selectedDate = "MM/dd/yyyy"
            },
            onEdit = { _, setlistName, date ->
                if (viewModel.validateDate(date)) {
                    viewModel.createSetlist(setlistName, date)
                    isDialogOpen = false
                    name = ""
                    selectedDate = "MM/dd/yyyy"
                }
            },
            initialName = name,
            initialDate = selectedDate,
            onDateChange = { newDate -> selectedDate = newDate },
            onNameChange = { newName -> name = newName }
        )
    }

    setlistToDelete?.let { setlist ->
        ConfirmDeleteDialog(
            onDismiss = { setlistToDelete = null },
            onConfirmDelete = {
                viewModel.deleteSetlist(setlist.id)
                setlistToDelete = null
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
                Text("Setlists", fontSize = 28.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { isDialogOpen = true },
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
                placeholder = { Text("Search Setlists") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(10.dp)),
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

                TextButton(onClick = { /* Sorting logic placeholder */ }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    Text("Sort")
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSetlists) { setlist ->
                    SetlistItem(
                        setlist = setlist,
                        onEditClick = {
                            navController.navigate("edit_setlist/${setlist.id}")
                        },
                        onDeleteClick = {
                            setlistToDelete = setlist
                        },
                        onSelectClick = {
                            navController.navigate("home?setlistId=${setlist.id}&openPopup=true")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SetlistItem(
    setlist: Setlist,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .clickable { onSelectClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(setlist.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = formatDateForDisplay(setlist.date),
                color = Color.Gray,
                fontSize = 14.sp
            )
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

fun formatDateForDisplay(dateLong: Long): String {
    return try {
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        outputFormat.format(Date(dateLong))
    } catch (e: Exception) {
        "MM/dd/yyyy"
    }
}