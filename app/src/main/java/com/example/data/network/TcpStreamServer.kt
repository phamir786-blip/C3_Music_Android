package com.example.data.network

import android.util.Log
import com.example.model.AudioStreamFormat
import com.example.model.HeaderMode
import com.example.model.StreamingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TcpStreamServer(
    private val onStateChanged: (StreamingState, String?) -> Unit,
    private val onBytesTransmitted: (Long) -> Unit
) {
    private val TAG = "TcpStreamServer"
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: OutputStream? = null
    private val isRunning = AtomicBoolean(false)
    private val isClientConnected = AtomicBoolean(false)
    val totalBytesWritten = AtomicLong(0L)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var acceptJob: Job? = null

    @Synchronized
    fun startServer(port: Int, format: AudioStreamFormat, headerMode: HeaderMode): Boolean {
        stopServer()
        isRunning.set(true)
        onStateChanged(StreamingState.CONNECTING, "Waiting for ESP32-C3 to connect on port \$port...")
        return try {
            val server = ServerSocket(port)
            serverSocket = server
            acceptJob = scope.launch {
                while (isRunning.get() && !server.isClosed) {
                    try {
                        val client = server.accept()
                        Log.i(TAG, "ESP32-C3 connected from \${client.inetAddress.hostAddress}:\${client.port}")
                        client.tcpNoDelay = true
                        client.keepAlive = true
                        try { client.trafficClass = 0x10 } catch (_: Exception) {}
                        client.sendBufferSize = 32 * 1024
                        val os = BufferedOutputStream(client.getOutputStream(), 16 * 1024)

                        closeClient()
                        clientSocket = client
                        outputStream = os
                        isClientConnected.set(true)

                        val header = C3Protocol.getInitialHeader(format, headerMode)
                        if (header != null && header.isNotEmpty()) {
                            os.write(header)
                            os.flush()
                            totalBytesWritten.addAndGet(header.size.toLong())
                            onBytesTransmitted(totalBytesWritten.get())
                        }

                        onStateChanged(StreamingState.STREAMING, client.inetAddress?.hostAddress)
                    } catch (e: Exception) {
                        if (isRunning.get()) Log.w(TAG, "Accept loop exception: \${e.message}")
                    }
                }
            }
            true
        } catch (e: Exception) {
            val err = "Failed to start TCP server on port \$port: \${e.message}"
            Log.e(TAG, err)
            stopServer()
            onStateChanged(StreamingState.ERROR, err)
            false
        }
    }

    @Synchronized
    fun sendAudioChunk(buffer: ByteArray, offset: Int, length: Int): Boolean {
        if (!isClientConnected.get() || !isRunning.get()) return false
        return try {
            val os = outputStream ?: return false
            os.write(buffer, offset, length)
            os.flush()
            val total = totalBytesWritten.addAndGet(length.toLong())
            onBytesTransmitted(total)
            true
        } catch (e: IOException) {
            Log.w(TAG, "Client disconnected or transmission error: \${e.message}")
            closeClient()
            onStateChanged(StreamingState.CONNECTING, "C3 disconnected. Waiting for reconnect...")
            false
        }
    }

    @Synchronized
    private fun closeClient() {
        isClientConnected.set(false)
        try { outputStream?.flush() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        try { clientSocket?.close() } catch (_: Exception) {}
        outputStream = null
        clientSocket = null
    }

    @Synchronized
    fun stopServer() {
        isRunning.set(false)
        isClientConnected.set(false)
        acceptJob?.cancel()
        acceptJob = null
        closeClient()
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        onStateChanged(StreamingState.IDLE, null)
        Log.i(TAG, "TCP Server stopped")
    }

    fun isStreaming(): Boolean = isClientConnected.get() && isRunning.get()
}
