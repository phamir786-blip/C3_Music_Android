package com.example.model

enum class StreamingState {
    IDLE,
    CONNECTING,
    STREAMING,
    RECONNECTING,
    DISCONNECTED,
    ERROR
}

enum class CaptureStatus {
    IDLE,
    INITIALIZING,
    CAPTURING,
    SILENCE,
    PAUSED,
    ERROR
}

enum class AudioSourceType(val displayName: String) {
    INTERNAL_AUDIO("Internal Audio"),
    MICROPHONE("Microphone")
}

enum class BufferLatencyPreset(val durationMs: Int, val displayName: String, val description: String) {
    LOW_LATENCY(10, "Low Latency (10ms)", "Minimal delay, best for fast 5GHz Wi-Fi"),
    BALANCED(25, "Balanced (25ms)", "Optimal trade-off between latency and jitter immunity"),
    SAFE(50, "Safe Buffer (50ms)", "Maximum dropout resistance on congested networks")
}
