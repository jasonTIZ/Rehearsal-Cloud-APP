package com.app.rehearsalcloud.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.*

data class AudioTrack(
    val name: String,
    val filePath: String,
    val waveformData: FloatArray,
    val duration: Long,
    val sampleRate: Int
)

data class MultitrackSong(
    val songName: String,
    val tracks: List<AudioTrack>,
    val combinedWaveform: FloatArray,
    val totalDuration: Long
)

class AudioWaveformGenerator(private val context: Context) {
    companion object {
        private const val WAVEFORM_SAMPLES = 1000
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
    }

    suspend fun processSongFromBackend(
        songName: String,
        audioFiles: List<com.app.rehearsalcloud.model.AudioFile>
    ): MultitrackSong? {
        return withContext(Dispatchers.IO) {
            try {
                val processedTracks = audioFiles.map { file ->
                    val localPath = downloadFileIfNeeded(file.url, file.fileName)
                    processAudioTrack(localPath, file.instrument)
                }
                val combinedWaveform = combineWaveforms(processedTracks)
                val maxDuration = processedTracks.maxOfOrNull { it.duration } ?: 0L
                MultitrackSong(
                    songName = songName,
                    tracks = processedTracks,
                    combinedWaveform = combinedWaveform,
                    totalDuration = maxDuration
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun downloadFileIfNeeded(url: String, fileName: String): String {
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) {
            URL(url).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    private fun processAudioTrack(filePath: String, instrument: String): AudioTrack {
        val waveformData = generateWaveformFromFile(filePath)
        val metadata = extractMetadata(filePath)
        return AudioTrack(
            name = instrument,
            filePath = filePath,
            waveformData = waveformData,
            duration = metadata.first,
            sampleRate = metadata.second
        )
    }

    private fun extractMetadata(filePath: String): Pair<Long, Int> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val sampleRate = 44100
            Pair(duration, sampleRate)
        } catch (e: Exception) {
            Pair(0L, 44100)
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun generateWaveformFromFile(filePath: String): FloatArray {
        // Puedes mejorar esto para WAV, aquí solo es una aproximación para todos los formatos
        val waveform = FloatArray(WAVEFORM_SAMPLES)
        for (i in waveform.indices) {
            val progress = i.toFloat() / WAVEFORM_SAMPLES
            waveform[i] = abs(sin(progress * Math.PI * 6).toFloat()) * 0.8f
        }
        return waveform
    }

    private fun combineWaveforms(tracks: List<AudioTrack>): FloatArray {
        if (tracks.isEmpty()) return FloatArray(WAVEFORM_SAMPLES)
        val combinedWaveform = FloatArray(WAVEFORM_SAMPLES)
        for (i in combinedWaveform.indices) {
            var sum = 0f
            var count = 0
            tracks.forEach { track ->
                if (i < track.waveformData.size) {
                    sum += track.waveformData[i]
                    count++
                }
            }
            if (count > 0) {
                combinedWaveform[i] = (sum / count).coerceIn(0f, 1f)
            }
        }
        return combinedWaveform
    }
}