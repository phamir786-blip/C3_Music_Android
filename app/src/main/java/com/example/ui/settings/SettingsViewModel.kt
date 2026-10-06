package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.AppContainer
import com.example.domain.audio.AudioDeviceCapabilityDetector
import com.example.domain.audio.AudioFormatCapability
import com.example.model.AudioStreamFormat
import com.example.model.BufferLatencyPreset

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = AppContainer.getPreferences(application)
    val userPreferences = prefsRepo.userPreferences

    val supportedFormatCapabilities: List<AudioFormatCapability> =
        AudioDeviceCapabilityDetector.getSupportedPresets()

    fun updateTarget(host:String,port:Int){prefsRepo.updateTarget(host,port)}

    fun updateAudioFormat(format: AudioStreamFormat) {
        prefsRepo.updateAudioFormat(format.sampleRate, format.bitDepth, format.channelCount)
    }

    fun updateBufferPreset(preset: BufferLatencyPreset) {
        prefsRepo.updateBufferPreset(preset)
    }

    fun updateNetworkSettings(udpPort:Int,autoReconnect:Boolean,maxRetries:Int){prefsRepo.updateNetworkSettings(udpPort,autoReconnect,maxRetries)}

    fun updateAmoledDarkTheme(enabled: Boolean) {
        prefsRepo.updateAmoledDarkTheme(enabled)
    }

    fun updateRatePacing(enabled: Boolean) {
        prefsRepo.updateRatePacing(enabled)
    }

    fun updateMutePhoneWhileStreaming(enabled: Boolean) {
        prefsRepo.updateMutePhoneWhileStreaming(enabled)
    }
}
