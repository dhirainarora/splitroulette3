package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

object SoundEffects {
    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-computed audio PCM buffers for zero-latency playback
    private val tickBuffer: ShortArray = generateTickSound()
    private val chimeBuffer: ShortArray = generateChimeSound()
    private val thumpBuffer: ShortArray = generateThumpSound()

    var isSoundEnabled: Boolean = true

    fun playTick() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(tickBuffer)
        }
    }

    fun playChime() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(chimeBuffer)
        }
    }

    fun playThump() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(thumpBuffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Release after playback finishes
            scope.launch {
                val durationMs = (buffer.size * 1000L) / SAMPLE_RATE + 50L
                kotlinx.coroutines.delay(durationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Graceful fallback if AudioTrack fails on container
        }
    }

    private fun generateTickSound(): ShortArray {
        val numSamples = (SAMPLE_RATE * 0.02).toInt() // 20 ms click
        val buffer = ShortArray(numSamples)
        val freq = 1760.0 // A6 click
        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-time * 180.0) // sharp decay
            val sample = (sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.6).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChimeSound(): ShortArray {
        val numSamples = (SAMPLE_RATE * 0.35).toInt() // 350 ms pleasant chord
        val buffer = ShortArray(numSamples)
        val f1 = 523.25 // C5
        val f2 = 659.25 // E5
        val f3 = 783.99 // G5
        val f4 = 1046.50 // C6
        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-time * 7.5) // smooth bell fade
            val s1 = sin(2.0 * Math.PI * f1 * time)
            val s2 = sin(2.0 * Math.PI * f2 * time) * 0.8
            val s3 = sin(2.0 * Math.PI * f3 * time) * 0.6
            val s4 = sin(2.0 * Math.PI * f4 * time) * 0.4
            val sample = ((s1 + s2 + s3 + s4) / 2.8 * envelope * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateThumpSound(): ShortArray {
        val numSamples = (SAMPLE_RATE * 0.08).toInt() // 80 ms low thump
        val buffer = ShortArray(numSamples)
        val freq = 120.0
        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-time * 40.0)
            val sample = (sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.7).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
