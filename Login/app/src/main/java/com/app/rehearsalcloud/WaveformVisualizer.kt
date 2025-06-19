package com.app.rehearsalcloud.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.rehearsalcloud.audio.MultitrackSong

@Composable
fun WaveformVisualizer(
    song: MultitrackSong?,
    playbackPosition: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE6E6E6), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        if (song != null) {
            // Mezcla principal
            WaveformBar(
                waveformData = song.combinedWaveform,
                playbackPosition = playbackPosition,
                color = Color(0xFF00D9B0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(Modifier.height(8.dp))
            // Tracks individuales
            song.tracks.forEach { track ->
                Text(track.name, color = Color.DarkGray, modifier = Modifier.padding(bottom = 2.dp))
                WaveformBar(
                    waveformData = track.waveformData,
                    playbackPosition = playbackPosition,
                    color = Color(0xFF4ECDC4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay canción cargada", color = Color.Gray)
            }
        }
    }
}

@Composable
fun WaveformBar(
    waveformData: FloatArray,
    playbackPosition: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val step = width / waveformData.size
        val centerY = height / 2
        val playedX = width * playbackPosition.coerceIn(0f, 1f)
        waveformData.forEachIndexed { i, amp ->
            val x = i * step
            val barHeight = amp * (height / 2)
            drawLine(
                color = if (x <= playedX) color else color.copy(alpha = 0.3f),
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = step * 0.8f
            )
        }
    }
}