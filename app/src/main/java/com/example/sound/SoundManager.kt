package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    var isSoundEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playRollSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Rapid rattle of 6 clicks with slight pitch variance
            for (i in 0 until 5) {
                val freq = 450.0 + (i * 80)
                generateTone(freq, 25, 0.4f)
                Thread.sleep(30)
            }
        }
    }

    fun playMoveSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Crisp 'pop/click' sound
            generateTone(523.25, 45, 0.5f) // C5
        }
    }

    fun playCaptureSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Dramatic descending sweep & strike
            generateTone(880.0, 50, 0.6f)
            generateTone(440.0, 70, 0.7f)
            generateTone(220.0, 100, 0.5f)
        }
    }

    fun playSafeSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Magical bell chime for safe star
            generateTone(659.25, 60, 0.5f) // E5
            generateTone(880.0, 90, 0.6f)  // A5
        }
    }

    fun playVictorySound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Celebratory fanfare (C - E - G - high C)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
            for (freq in notes) {
                generateTone(freq, 120, 0.6f)
                Thread.sleep(130)
            }
        }
    }

    fun playCoinSound() {
        if (!isSoundEnabled) return
        scope.launch {
            generateTone(987.77, 60, 0.5f) // B5
            Thread.sleep(60)
            generateTone(1318.51, 100, 0.6f) // E6
        }
    }

    private fun generateTone(frequencyHz: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 44100
            val numSamples = (durationMs * sampleRate) / 1000
            val generatedSnd = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val sample = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / frequencyHz))
                // Apply subtle envelope attack/decay to prevent audio pop
                val envelope = when {
                    i < numSamples * 0.1 -> (i / (numSamples * 0.1)).toFloat()
                    i > numSamples * 0.8 -> ((numSamples - i) / (numSamples * 0.2)).toFloat()
                    else -> 1.0f
                }
                generatedSnd[i] = (sample * 32767 * volume * envelope).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, numSamples)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio generation failures gracefully
        }
    }
}
