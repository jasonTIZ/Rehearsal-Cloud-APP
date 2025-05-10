package com.app.rehearsalcloud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.app.rehearsalcloud.ui.setlist.SetlistManagerView
import androidx.navigation.compose.composable
import com.app.rehearsalcloud.ui.setlist.EditSetlistScreen

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
        composable("home") {
            PlayerUI(navController)
        }
        composable("setlist_manager") {
            SetlistManagerView(navController)
        }

        composable("edit_setlist/{setlistId}") { backStackEntry ->
            val setlistId = backStackEntry.arguments?.getString("setlistId")?.toIntOrNull() ?: return@composable
            // You would ideally load the setlist here by the setlistId.
            EditSetlistScreen(setlistId = setlistId)
        }
    }
}

@Composable
fun PlayerUI(navController: NavHostController) {


    var showSetlistPopup by remember { mutableStateOf(false) }

    if (showSetlistPopup) {
        SetlistPopup(onDismiss = { showSetlistPopup = false }, navController = navController)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(10.dp))
                .shadow(4.dp)
        ) {
            ControlBar()
            ContentArea()
            TracksSection()
            BottomBar()
        }
        SidePanelButtons(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 150.dp),
            onSetlistClick = { showSetlistPopup = true }
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
    ) {
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
            .padding(horizontal = 10.dp)
    ) {
        // Red position marker
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
                        .height(17.dp) // 10% of 170dp
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
                    .size(width = 40.dp, height = 170.dp)
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
        val screenWidth = LocalConfiguration.current.screenWidthDp
        Box(
            modifier = Modifier
                .width((0.6f * screenWidth).dp)  // Multiply first, then convert to Dp
                .height(4.dp)
                .background(Color.Black, RoundedCornerShape(2.dp))
        )
    }
}

// SETLIST POP UP
@Composable
fun SidePanelButtons(modifier: Modifier = Modifier, onSetlistClick: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PanelButton(icon = Icons.Default.Menu, label = "Setlists", onClick = onSetlistClick)
    }
}

@Composable
fun PanelButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(80.dp)
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
fun SetlistPopup(onDismiss: () -> Unit, navController: NavHostController) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
         // Important for full-screen dialog
        Surface(
            modifier = Modifier
                .width(700.dp)
                .wrapContentHeight()
                .shadow(10.dp, shape = RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = Color.White
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ensayo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF333333))
                    TextButton(onClick = { navController.navigate("setlist_manager") }) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF00B8D4))
                        Spacer(Modifier.width(8.dp))
                        Text("Setlists", color = Color(0xFF00B8D4), fontSize = 16.sp)
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEAEAEA))

                // Song List
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SongItem(number = "1", title = "Same God", subtitle = "Original", key = "C", tempo = "145")
                    SongItem(number = "2", title = "Como Dijiste", subtitle = "Original", key = "A♭", tempo = "147")
                }

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                        .border(1.dp, Color(0xFFEAEAEA)),
                            horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.navigate("edit_setlist/3") }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00B8D4))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit setlist", color = Color(0xFF00B8D4), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(number: String, title: String, subtitle: String, key: String, tempo: String) {
    val bgColor = if (number.toIntOrNull()?.rem(2) == 1) Color.White else Color(0xFFF5F5F5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 20.dp)
            .border(BorderStroke(1.dp, Color(0xFFEAEAEA)), shape = RectangleShape) // Apply border to the whole row
            .padding(bottom = 1.dp), // To give space between rows if required
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            number,
            color = Color(0xFF00B8D4),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(25.dp),
            textAlign = TextAlign.Center
        )

        Image(
            painter = rememberAsyncImagePainter("https://hebbkx1anhila5yf.public.blob.vercel-storage.com/placeholder-ob7miW3mUreePYfXdVwkpFWHthzoR5.svg"),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
                .padding(horizontal = 15.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color(0xFF333333))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF888888))
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            //IconButton(onClick = { /* TODO: Menu */ }) {
            //    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color(0xFF666666))
            //}
            Text(key, fontSize = 14.sp, color = Color(0xFF333333))
            Text(tempo, fontSize = 14.sp, color = Color(0xFF333333))
            IconButton(onClick = { /* TODO: Remove */ }) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF666666))
            }
        }
    }
}

