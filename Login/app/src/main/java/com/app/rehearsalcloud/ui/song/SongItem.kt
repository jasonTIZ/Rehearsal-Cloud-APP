package com.app.rehearsalcloud.ui.song

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

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
