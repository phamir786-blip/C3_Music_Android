package com.example.data.network

import android.util.Log
import com.example.domain.audio.AudioRingBuffer
import com.example.model.AudioStreamFormat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class PacedAudioTransmitter(
    private val ringBuffer: AudioRingBuffer,
    private val sendChunkToTransport: (buffer: ByteArray, offset: Int, length: Int) -> Boolean
) {
    private val TAG = "PacedAudioTransmitter"
    private val isRunning = AtomicBoolean(false)
    private var transmitterThread: Thread? = null
    private val chunkBuffer = ByteArray(BASE_CHUNK_SIZE)

    @Volatile var ratePacingEnabled: Boolean = true
    @Volatile var currentFormat: AudioStreamFormat = AudioStreamFormat.FORMAT_44K_16BIT_STEREO

    fun start(format: AudioStreamFormat) {
        if (isRunning.get()) return
        currentFormat = format
        isRunning.set(true)
        transmitterThread = Thread({
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            transmissionLoop()
        }, "C3-PacedTransmitterThread").apply {
            isDaemon = true
            start()
        }
        Log.i(TAG, "PacedAudioTransmitter started for ${format.displayName} (Pacing: $ratePacingEnabled)")
    }

    fun stop() {
        isRunning.set(false)
        transmitterThread?.interrupt()
        try { transmitterThread?.join(300) } catch (_: InterruptedException) {}
        transmitterThread = null
        ringBuffer.clear()
        Log.i(TAG, "PacedAudioTransmitter stopped")
    }

    private fun transmissionLoop() {
        var bytesPerSec = currentFormat.sampleRate.toLong() * currentFormat.frameSizeBytes
        if (bytesPerSec <= 0L) bytesPerSec = 176400L
        val chunkSize = frameAlignedChunkSize(currentFormat.frameSizeBytes)
        var nextScheduledTimeNs = System.nanoTime()
        val maxAllowedDriftNs = 50_000_000L

        while (isRunning.get()) {
            val available = ringBuffer.available
            val readLength = available.coerceAtMost(chunkSize)
            val readCount = if (readLength > 0) {
                ringBuffer.read(chunkBuffer, 0, readLength)
            } else {
                0
            }

            // Keep the transport clock continuous if capture briefly falls behind.
            // Consume any partial PCM available and zero-pad the rest of the chunk.
            if (readCount < chunkSize) {
                java.util.Arrays.fill(chunkBuffer, readCount, chunkSize, 0.toByte())
            }

            val chunkDurationNs = (chunkSize.toLong() * 1_000_000_000L) / bytesPerSec
            val hasBacklog = ringBuffer.available > (chunkSize * 2)

            if (ratePacingEnabled && !hasBacklog) {
                val now = System.nanoTime()
                if (nextScheduledTimeNs > now) {
                    LockSupport.parkNanos(nextScheduledTimeNs - now)
                    nextScheduledTimeNs += chunkDurationNs
                } else if ((now - nextScheduledTimeNs) > maxAllowedDriftNs) {
                    nextScheduledTimeNs = now + chunkDurationNs
                } else {
                    nextScheduledTimeNs += chunkDurationNs
                }
            } else if (hasBacklog) {
                nextScheduledTimeNs = System.nanoTime()
            }

            // Always send the complete scheduled chunk. When capture is briefly
            // late, the remaining bytes are intentional PCM silence rather than
            // a short network write that advances the receiver's stream clock.
            if (!sendChunkToTransport(chunkBuffer, 0, chunkSize)) {
                LockSupport.parkNanos(10_000_000L)
            }
        }
    }

    companion object {
        private const val BASE_CHUNK_SIZE = 2048

        internal fun frameAlignedChunkSize(frameSizeBytes: Int): Int {
            require(frameSizeBytes > 0) { "frameSizeBytes must be positive" }
            val frames = BASE_CHUNK_SIZE / frameSizeBytes
            return (if (frames > 0) frames else 1) * frameSizeBytes
        }
    }
}
