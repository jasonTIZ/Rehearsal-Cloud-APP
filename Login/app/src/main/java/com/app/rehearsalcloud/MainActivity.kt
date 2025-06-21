package com.app.rehearsalcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.rehearsalcloud.api.RetrofitClient
import com.app.rehearsalcloud.model.AppDatabase
import com.app.rehearsalcloud.model.setlist.SetlistWithSongsWithAudioFile
import com.app.rehearsalcloud.repository.SetlistRepository
import com.app.rehearsalcloud.repository.SongRepository
import com.app.rehearsalcloud.ui.setlist.EditSetlistScreen
import com.app.rehearsalcloud.ui.setlist.SetlistManagerView
import com.app.rehearsalcloud.ui.song.SongManagerView
import com.app.rehearsalcloud.viewmodel.SetlistViewModel
import com.app.rehearsalcloud.viewmodel.SetlistViewModelFactory
import com.app.rehearsalcloud.viewmodel.SongViewModel
import com.app.rehearsalcloud.viewmodel.SongViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Navigate()
        }
    }
}

@Composable
fun Navigate() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable(
            route = "home?setlistId={setlistId}&openPopup={openPopup}",
            arguments = listOf(
                navArgument("setlistId") {
                    type = NavType.StringType
                    defaultValue = "-1"
                },
                navArgument("openPopup") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            PlayerUI(navController)
        }
        composable("setlist_library") {
            SetlistManagerView(navController)
        }
        composable("song_library") {
            SongManagerView(navController)
        }
        composable("edit_setlist/{setlistId}") { backStackEntry ->
            val setlistId = backStackEntry.arguments?.getString("setlistId")?.toIntOrNull() ?: return@composable
            EditSetlistScreen(
                setlistId = setlistId,
                navController = navController
            )
        }
    }
}

@Composable
fun PlayerUI(navController: NavHostController) {
    val context = LocalContext.current
    val setlistViewModel: SetlistViewModel = viewModel(
        factory = SetlistViewModelFactory(
            SetlistRepository(
                AppDatabase.getDatabase(context).setlistDao(),
                RetrofitClient.setlistApiService
            )
        )
    )
    val songViewModel: SongViewModel = viewModel(
        factory = SongViewModelFactory(
            SongRepository(
                AppDatabase.getDatabase(context).songDao(),
                RetrofitClient.songApiService
            )
        )
    )

    var showSetlistPopup by remember { mutableStateOf(false) }
    val currentEntry = navController.currentBackStackEntryAsState().value
    val setlistId = currentEntry?.arguments?.getString("setlistId")?.toIntOrNull() ?: -1
    val openPopup = currentEntry?.arguments?.getBoolean("openPopup") ?: false

    LaunchedEffect(setlistId, openPopup) {
        if (setlistId != null && openPopup) {
            setlistViewModel.loadSetlistWithSongs(setlistId)
            showSetlistPopup = true
        }
    }

    if (showSetlistPopup) {
        if (setlistId != null) {
            val selectedSetlistWithSongs by setlistViewModel.selectedSetlistWithSongs.collectAsState()
            SetlistPopup(
                setlistWithSongs = selectedSetlistWithSongs,
                onDismiss = {
                    showSetlistPopup = false
                    navController.popBackStack("home", inclusive = false)
                },
                navController = navController,
                onPlaySong = { songId, audioId ->
                    songViewModel.playAudioFile(songId, audioId, context)
                }
            )
        } else {
            EmptySetlistPopup(
                onDismiss = { showSetlistPopup = false },
                navController = navController
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(10.dp))
                .shadow(4.dp)
        ) {
            ControlBar()
            ContentArea()
            TracksSection()
            BottomBar()
        }
        SidePanelButtons(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 80.dp),
            onSetlistClick = { showSetlistPopup = true },
            onSongClick = { navController.navigate("song_library") }
        )
    }
}

@Composable
fun ControlBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ControlButton(Icons.Default.SkipPrevious)
            ControlButton(Icons.Default.PlayArrow, active = true)
            ControlButton(Icons.Default.Stop)
            ControlButton(Icons.Default.SkipNext)
        }

        Box(
            modifier = Modifier
                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            Text("00:00 / 00:00", color = Color(0xFF333333), fontSize = 14.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ControlTextButton("145")
            ControlTextButton("C")
            ControlButton(Icons.Default.Settings)
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, active: Boolean = false) {
    val bgColor = if (active) Color(0xFF00D9B0) else Color(0xFFE0E0E0)
    val iconColor = if (active) Color.White else Color(0xFF333333)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { },
        contentAlignment = Alignment.Center
    )
    {
        Icon(icon, contentDescription = null, tint = iconColor)
    }
}

@Composable
fun ControlTextButton(text: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFF333333), fontSize = 14.sp)
    }
}

@Composable
fun ContentArea() {
    Box(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .background(Color(0xFFE6E6E6), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color(0xFFFF3B30))
                .offset(x = 70.dp)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFFF3B30), RoundedCornerShape(4.dp))
                .offset(x = 67.dp, y = 0.dp)
        )
    }
}

@Composable
fun TracksSection() {
    Column(modifier = Modifier.padding(10.dp)) {
        Text("Master", fontSize = 12.sp, color = Color(0xFF333333), modifier = Modifier.padding(bottom = 5.dp))
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(170.dp)
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(17.dp)
                        .background(Color(0xFFFF3B30))
                        .align(Alignment.BottomCenter)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFFFF3B30))
                        .align(Alignment.BottomCenter)
                        .offset(y = (-17).dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            Box(
                modifier = Modifier
                    .size(width = 70.dp, height = 170.dp)
                    .background(Color(0xFFD0E6EC), RoundedCornerShape(5.dp))
            )
        }
    }
}

@Composable
fun BottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(4.dp)
                .background(Color.Black, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun SidePanelButtons(modifier: Modifier = Modifier, onSetlistClick: () -> Unit, onSongClick: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PanelButton(icon = Icons.Default.Menu, label = "Setlists", onClick = onSetlistClick)
        PanelButton(icon = Icons.Default.AddCircleOutline, label = "Songs", onClick = onSongClick)
    }
}

@Composable
fun PanelButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(85.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF333333), modifier = Modifier.size(16.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF333333))
    }
}

@Composable
fun EmptySetlistPopup(
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .shadow(10.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "No Setlist Selected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF333333)
                )
                TextButton(
                    onClick = {
                        navController.navigate("setlist_library")
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Setlist",
                            tint = Color(0xFF00B8D4)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add Setlist",
                            color = Color(0xFF00B8D4),
                            fontSize = 16.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        navController.navigate("setlist_library")
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Setlists", tint = Color(0xFF00B8D4))
                        Spacer(Modifier.width(8.dp))
                        Text("Setlists", color = Color(0xFF00B8D4), fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        navController.navigate("edit_setlist/0")
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Setlist", tint = Color(0xFF00B8D4))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit setlist", color = Color(0xFF00B8D4), fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun SetlistPopup(
    setlistWithSongs: SetlistWithSongsWithAudioFile?,
    onDismiss: () -> Unit,
    navController: NavHostController,
    onPlaySong: (songId: Int, audioId: Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .shadow(10.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color.White
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        setlistWithSongs?.setlist?.name ?: "Setlist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { navController.navigate("song_library") }) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "Add Song",
                                tint = Color(0xFF00B8D4)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Add", color = Color(0xFF00B8D4), fontSize = 16.sp)
                        }
                        TextButton(onClick = { navController.navigate("setlist_library") }) {
                            Icon(Icons.Default.Menu, contentDescription = "Setlists", tint = Color(0xFF00B8D4))
                            Spacer(Modifier.width(8.dp))
                            Text("Setlists", color = Color(0xFF00B8D4), fontSize = 16.sp)
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEAEAEA))

                if (setlistWithSongs?.songs.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No songs in this setlist",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        setlistWithSongs?.songs?.forEachIndexed { index, songWithAudioFiles ->
                            SongItem(
                                number = (index + 1).toString(),
                                title = songWithAudioFiles.song.songName,
                                subtitle = songWithAudioFiles.song.artist,
                                key = songWithAudioFiles.song.tone ?: "N/A",
                                tempo = songWithAudioFiles.song.bpm.toString() ?: "N/A",
                                onPlayClick = {
                                    songWithAudioFiles.audioFiles.firstOrNull()?.let { audioFile ->
                                        onPlaySong(songWithAudioFiles.song.id, audioFile.id)
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                        .border(1.dp, Color(0xFFEAEAEA)),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        setlistWithSongs?.setlist?.id?.let { id ->
                            navController.navigate("edit_setlist/$id")
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Setlist", tint = Color(0xFF00B8D4))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit setlist", color = Color(0xFF00B8D4), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(
    number: String,
    title: String,
    subtitle: String,
    key: String,
    tempo: String,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                number,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                modifier = Modifier.width(24.dp)
            )
            IconButton(onClick = { onPlayClick() }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF00B8D4))
            }
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(key, fontSize = 14.sp, color = Color(0xFF333333))
            Text(tempo, fontSize = 14.sp, color = Color(0xFF333333))
        }
    }
}