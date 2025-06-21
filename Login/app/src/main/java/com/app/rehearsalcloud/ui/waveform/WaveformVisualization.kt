// WaveformVisualization.kt
package com.app.rehearsalcloud.ui.waveform

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.rehearsalcloud.model.audiofile.AudioFile
import kotlin.math.*

data class WaveformData(
    val samples: List<Float>, // Normalized samples (-1.0 to 1.0)
    val duration: Float, // Duration in seconds
    val sampleRate: Int = 44100
)

data class TrackWaveform(
    val audioFile: AudioFile,
    val waveformData: WaveformData?,
    val color: Color,
    val isVisible: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f
)

@Composable
fun MultiTrackWaveformView(
    tracks: List<TrackWaveform>,
    currentPosition: Float = 0f, // Position in seconds
    duration: Float = 0f,
    onPositionChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var playheadPosition by remember { mutableStateOf(currentPosition) }
    val density = LocalDensity.current

    LaunchedEffect(currentPosition) {
        playheadPosition = currentPosition
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Timeline ruler
        TimelineRuler(
            duration = duration,
            currentPosition = playheadPosition,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        // Master waveform (combined view)
        if (tracks.isNotEmpty()) {
            MasterWaveformTrack(
                tracks = tracks,
                currentPosition = playheadPosition,
                duration = duration,
                onPositionChange = { newPosition ->
                    playheadPosition = newPosition
                    onPositionChange(newPosition)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 4.dp)
            )
        }

        // Individual tracks
        tracks.forEach { track ->
            if (track.isVisible) {
                IndividualTrackWaveform(
                    track = track,
                    currentPosition = playheadPosition,
                    duration = duration,
                    onPositionChange = { newPosition ->
                        playheadPosition = newPosition
                        onPositionChange(newPosition)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineRuler(
    duration: Float,
    currentPosition: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Draw timeline marks
        val timeInterval = when {
            duration <= 60f -> 5f // 5 second intervals
            duration <= 300f -> 30f // 30 second intervals
            else -> 60f // 1 minute intervals
        }
        
        val marks = (0..(duration / timeInterval).toInt()).map { it * timeInterval }
        
        marks.forEach { time ->
            val x = (time / duration) * width
            
            // Draw tick mark
            drawLine(
                color = Color(0xFF666666),
                start = Offset(x, height * 0.7f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw time label (simplified for space)
            // You might want to use actual text drawing here
        }
        
        // Draw playhead position
        val playheadX = (currentPosition / duration) * width
        drawLine(
            color = Color(0xFFFF3B30),
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun MasterWaveformTrack(
    tracks: List<TrackWaveform>,
    currentPosition: Float,
    duration: Float,
    onPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "Master",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.weight(1f))
            // Volume indicator could go here
        }
        
        Canvas(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE6E6E6))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val newPosition = (change.position.x / size.width) * duration
                        onPositionChange(newPosition.coerceIn(0f, duration))
                    }
                }
        ) {
            drawMasterWaveform(tracks, currentPosition, duration)
        }
    }
}

@Composable
private fun IndividualTrackWaveform(
    track: TrackWaveform,
    currentPosition: Float,
    duration: Float,
    onPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row {
        // Track info
        Column(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = inferInstrument(track.audioFile.fileName).replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (track.isMuted) Color(0xFF999999) else Color(0xFF333333)
            )
            Text(
                text = "${(track.audioFile.fileSize / 1024 / 1024).toInt()} MB",
                fontSize = 8.sp,
                color = Color(0xFF666666)
            )
        }
        
        // Waveform
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0F0F0))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val newPosition = (change.position.x / size.width) * duration
                        onPositionChange(newPosition.coerceIn(0f, duration))
                    }
                }
        ) {
            drawIndividualWaveform(track, currentPosition, duration)
        }
    }
}

private fun DrawScope.drawMasterWaveform(
    tracks: List<TrackWaveform>,
    currentPosition: Float,
    duration: Float
) {
    val width = size.width
    val height = size.height
    val centerY = height / 2f
    
    // Combine all visible tracks for master view
    val visibleTracks = tracks.filter { it.isVisible && !it.isMuted }
    
    if (visibleTracks.isEmpty() || duration <= 0f) return
    
    // Generate combined waveform (simplified)
    val samplesPerPixel = 100 // Adjust based on performance needs
    val totalPixels = width.toInt()
    
    val path = Path()
    var isFirstPoint = true
    
    for (x in 0 until totalPixels) {
        val timePosition = (x.toFloat() / totalPixels) * duration
        
        // Combine amplitudes from all visible tracks
        var combinedAmplitude = 0f
        visibleTracks.forEach { track ->
            track.waveformData?.let { waveform ->
                val sampleIndex = (timePosition / waveform.duration * waveform.samples.size).toInt()
                if (sampleIndex < waveform.samples.size) {
                    combinedAmplitude += waveform.samples[sampleIndex] * track.volume
                }
            }
        }
        
        // Normalize combined amplitude
        combinedAmplitude = combinedAmplitude.coerceIn(-1f, 1f)
        val y = centerY - (combinedAmplitude * centerY * 0.8f)
        
        if (isFirstPoint) {
            path.moveTo(x.toFloat(), y)
            isFirstPoint = false
        } else {
            path.lineTo(x.toFloat(), y)
        }
    }
    
    // Draw waveform
    drawPath(
        path = path,
        color = Color(0xFF00D9B0),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
    )
    
    // Draw playhead
    val playheadX = (currentPosition / duration) * width
    drawLine(
        color = Color(0xFFFF3B30),
        start = Offset(playheadX, 0f),
        end = Offset(playheadX, height),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawIndividualWaveform(
    track: TrackWaveform,
    currentPosition: Float,
    duration: Float
) {
    val width = size.width
    val height = size.height
    val centerY = height / 2f
    
    track.waveformData?.let { waveform ->
        val samplesPerPixel = maxOf(1, waveform.samples.size / width.toInt())
        val path = Path()
        var isFirstPoint = true
        
        for (x in 0 until width.toInt()) {
            val sampleIndex = (x * samplesPerPixel).coerceAtMost(waveform.samples.size - 1)
            val amplitude = waveform.samples[sampleIndex] * track.volume
            val y = centerY - (amplitude * centerY * 0.9f)
            
            if (isFirstPoint) {
                path.moveTo(x.toFloat(), y)
                isFirstPoint = false
            } else {
                path.lineTo(x.toFloat(), y)
            }
        }
        
        // Draw waveform
        val waveformColor = if (track.isMuted) 
            track.color.copy(alpha = 0.3f) else track.color
            
        drawPath(
            path = path,
            color = waveformColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    } ?: run {
        // Draw placeholder when waveform data is not available
        drawLine(
            color = Color(0xFFCCCCCC),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Draw playhead
    if (duration > 0f) {
        val playheadX = (currentPosition / duration) * width
        drawLine(
            color = Color(0xFFFF3B30),
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

// Utility function to generate dummy waveform data for testing
fun generateDummyWaveformData(audioFile: AudioFile): WaveformData {
    val duration = 180f // 3 minutes dummy duration
    val sampleCount = 1000 // Simplified sample count
    val samples = (0 until sampleCount).map { i ->
        val frequency = when (inferInstrument(audioFile.fileName).lowercase()) {
            "bass" -> 0.5f
            "drums" -> 2.0f
            "guitar" -> 1.0f
            else -> 1.0f
        }
        sin(i * frequency * 0.1f).toFloat() * 0.8f
    }
    
    return WaveformData(
        samples = samples,
        duration = duration,
        sampleRate = 44100
    )
}
 fun inferInstrument(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            "drum" in lower -> "drums"
            "bajo" in lower || "bass" in lower -> "bass"
            "guitar" in lower || "guitarra" in lower -> "guitar"
            "piano" in lower -> "piano"
            "voice" in lower || "vocal" in lower || "voz" in lower -> "vocals"
            "guia" in lower -> "guia"
            else -> "other"
        }
    }
// Track colors for different instruments
fun getInstrumentColor(instrument: String): Color {
    return when (instrument.lowercase()) {
        "drums" -> Color(0xFFFF6B6B)
        "bass" -> Color(0xFF4ECDC4)
        "guitar" -> Color(0xFF45B7D1)
        "vocals" -> Color(0xFF96CEB4)
        "keys", "keyboard", "piano" -> Color(0xFFFECA57)
        "synth" -> Color(0xFF6C5CE7)
        else -> Color(0xFF74B9FF)
    }
   
}