package com.example.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaSyncEvent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

class VoiceAudioManager(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Real-time microphone amplitude level (0.0f to 1.0f)
    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    // Voice Note Recorder
    private var mediaRecorder: MediaRecorder? = null
    private var currentVoiceNoteFile: File? = null
    private var voiceNoteStartTime: Long = 0L

    private val _isRecordingVoiceNote = MutableStateFlow(false)
    val isRecordingVoiceNote: StateFlow<Boolean> = _isRecordingVoiceNote.asStateFlow()

    // Media Player for Voice Notes
    private var mediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingFile = MutableStateFlow<String?>(null)
    val currentlyPlayingFile: StateFlow<String?> = _currentlyPlayingFile.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var progressJob: Job? = null

    /**
     * Start live microphone amplitude monitoring (requires RECORD_AUDIO permission)
     */
    fun startMicMonitoring() {
        if (recordingJob?.isActive == true) return

        recordingJob = scope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                if (bufferSize <= 0) return@launch

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.startRecording()
                    val buffer = ShortArray(bufferSize / 2)

                    while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readSize > 0) {
                            var sum = 0.0
                            for (i in 0 until readSize) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = Math.sqrt(sum / readSize)
                            // Normalize RMS (typical max short ~32767)
                            val normalized = (rms / 3000.0).toFloat().coerceIn(0f, 1f)
                            _micAmplitude.value = normalized
                        }
                        delay(60)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("VoiceAudioManager", "Microphone permission not granted", e)
                _micAmplitude.value = 0f
            } catch (e: Exception) {
                Log.e("VoiceAudioManager", "Error reading mic amplitude", e)
                _micAmplitude.value = 0f
            }
        }
    }

    /**
     * Stop microphone amplitude monitoring
     */
    fun stopMicMonitoring() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e("VoiceAudioManager", "Error stopping audioRecord", e)
        }
        _micAmplitude.value = 0f
    }

    /**
     * Start recording a voice note message
     */
    fun startVoiceNoteRecording(): File? {
        stopVoiceNoteRecording() // Reset any previous

        val file = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.mp3")
        currentVoiceNoteFile = file
        voiceNoteStartTime = System.currentTimeMillis()

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            _isRecordingVoiceNote.value = true
            return file
        } catch (e: Exception) {
            Log.e("VoiceAudioManager", "Failed to start MediaRecorder", e)
            _isRecordingVoiceNote.value = false
            return null
        }
    }

    /**
     * Stop recording voice note and return Pair(File, durationMs)
     */
    fun stopVoiceNoteRecording(): Pair<File, Long>? {
        val file = currentVoiceNoteFile ?: return null
        val duration = System.currentTimeMillis() - voiceNoteStartTime

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceAudioManager", "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
            _isRecordingVoiceNote.value = false
            currentVoiceNoteFile = null
        }

        return if (file.exists() && file.length() > 0) Pair(file, duration) else null
    }

    /**
     * Play or pause voice note file
     */
    fun playVoiceNote(filePath: String) {
        if (_currentlyPlayingFile.value == filePath && mediaPlayer?.isPlaying == true) {
            pauseVoiceNote()
            return
        }

        stopVoiceNotePlayback()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopVoiceNotePlayback()
                }
            }
            _currentlyPlayingFile.value = filePath

            progressJob?.cancel()
            progressJob = scope.launch {
                while (isActive && mediaPlayer?.isPlaying == true) {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val total = mediaPlayer?.duration ?: 1
                    _playbackProgress.value = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    delay(100)
                }
            }
        } catch (e: IOException) {
            Log.e("VoiceAudioManager", "Error playing voice note", e)
            stopVoiceNotePlayback()
        }
    }

    fun pauseVoiceNote() {
        mediaPlayer?.pause()
        _currentlyPlayingFile.value = null
        progressJob?.cancel()
    }

    fun stopVoiceNotePlayback() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("VoiceAudioManager", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            _currentlyPlayingFile.value = null
            _playbackProgress.value = 0f
        }
    }

    fun release() {
        stopMicMonitoring()
        stopVoiceNoteRecording()
        stopVoiceNotePlayback()
        scope.cancel()
    }
}
