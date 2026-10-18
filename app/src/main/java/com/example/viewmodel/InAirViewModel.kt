package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import android.net.Uri
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.file.FileShareManager
import com.example.model.BroadcastStatus
import com.example.model.ClientSession
import com.example.model.FileTransferItem
import com.example.model.NetworkProfile
import com.example.model.SecurityPreferences
import com.example.model.StreamConfig
import com.example.model.StreamFps
import com.example.model.StreamResolution
import com.example.network.HotspotManager
import com.example.network.InAirHttpServer
import com.example.screen.ScreenCaptureManager
import com.example.security.SecurityManager
import com.example.service.BroadcastForegroundService
import com.example.service.InAirAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class InAirViewModel(application: Application) : AndroidViewModel(application) {

    val hotspotManager = HotspotManager(application)
    val screenCaptureManager = ScreenCaptureManager(application)
    val fileShareManager = FileShareManager(application)
    val securityManager = SecurityManager()

    private var httpServer: InAirHttpServer? = null
    private var mediaProjection: MediaProjection? = null
    private var autoStopJob: Job? = null
    private var bandwidthCollectorJob: Job? = null

    private val _broadcastStatus = MutableStateFlow(BroadcastStatus.OFFLINE)
    val broadcastStatus: StateFlow<BroadcastStatus> = _broadcastStatus.asStateFlow()

    private val _streamConfig = MutableStateFlow(StreamConfig())
    val streamConfig: StateFlow<StreamConfig> = _streamConfig.asStateFlow()

    private val _bandwidthHistory = MutableStateFlow<List<Float>>(List(16) { 0f })
    val bandwidthHistory: StateFlow<List<Float>> = _bandwidthHistory.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    val networkProfile: StateFlow<NetworkProfile> = hotspotManager.networkProfile
    val isHotspotActive: StateFlow<Boolean> = hotspotManager.isHotspotRunning
    val latestPreviewBitmap: StateFlow<Bitmap?> = screenCaptureManager.latestPreviewBitmap
    val bandwidthKbps: StateFlow<Float> = screenCaptureManager.bandwidthKbps
    val connectedClients: StateFlow<List<ClientSession>> = securityManager.connectedClients
    val pendingApproval: StateFlow<ClientSession?> = securityManager.pendingApproval
    val blockedIps: StateFlow<Set<String>> = securityManager.blockedIps
    val securityPreferences: StateFlow<SecurityPreferences> = securityManager.preferences
    val transferQueue: StateFlow<List<FileTransferItem>> = fileShareManager.transferQueue
    val sharedFolderName: StateFlow<String> = fileShareManager.sharedFolderName

    init {
        hotspotManager.refreshNetworkInfo()

        BroadcastForegroundService.onStopRequested = {
            stopBroadcast()
        }

        // Bandwidth history sampling
        bandwidthCollectorJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_broadcastStatus.value == BroadcastStatus.BROADCASTING) {
                    val current = screenCaptureManager.bandwidthKbps.value
                    val list = _bandwidthHistory.value.toMutableList()
                    list.add(current)
                    if (list.size > 20) list.removeAt(0)
                    _bandwidthHistory.value = list
                }
            }
        }
    }

    fun startBroadcastWithProjection(projection: MediaProjection) {
        mediaProjection = projection
        _broadcastStatus.value = BroadcastStatus.STARTING

        val server = InAirHttpServer(
            port = 8080,
            screenCaptureManager = screenCaptureManager,
            fileShareManager = fileShareManager,
            securityManager = securityManager
        )
        httpServer = server
        server.start()

        val wm = getApplication<Application>().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        screenCaptureManager.startCapture(
            projection = projection,
            config = _streamConfig.value,
            densityDpi = metrics.densityDpi
        )

        val profile = networkProfile.value
        BroadcastForegroundService.start(getApplication(), profile.url)

        _broadcastStatus.value = BroadcastStatus.BROADCASTING
        startAutoStopTimerIfNeeded()
    }

    fun stopBroadcast() {
        _broadcastStatus.value = BroadcastStatus.STOPPING

        autoStopJob?.cancel()
        autoStopJob = null

        screenCaptureManager.stopCapture()
        httpServer?.stop()
        httpServer = null

        mediaProjection?.stop()
        mediaProjection = null

        BroadcastForegroundService.stop(getApplication())
        hotspotManager.stopLocalHotspot()

        _broadcastStatus.value = BroadcastStatus.OFFLINE
    }

    fun toggleBroadcast(onRequestMediaProjection: () -> Unit) {
        if (_broadcastStatus.value == BroadcastStatus.BROADCASTING || _broadcastStatus.value == BroadcastStatus.STARTING) {
            stopBroadcast()
        } else {
            onRequestMediaProjection()
        }
    }

    fun startLocalHotspot() {
        hotspotManager.startLocalHotspot(
            onSuccess = { profile ->
                _statusMessage.value = "Hotspot started: ${profile.ssid}"
            },
            onError = { err ->
                _statusMessage.value = err
            }
        )
    }

    fun setManualHotspot(ssid: String, pass: String) {
        hotspotManager.setManualNetworkProfile(ssid, pass)
    }

    fun setStreamResolution(res: StreamResolution) {
        val newConfig = _streamConfig.value.copy(resolution = res)
        _streamConfig.value = newConfig
        screenCaptureManager.updateConfig(newConfig)
        mediaProjection?.let { proj ->
            val wm = getApplication<Application>().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            screenCaptureManager.startCapture(proj, newConfig, metrics.densityDpi)
        }
    }

    fun setStreamFps(fps: StreamFps) {
        val newConfig = _streamConfig.value.copy(fps = fps)
        _streamConfig.value = newConfig
        screenCaptureManager.updateConfig(newConfig)
    }

    fun setSharedFolderUri(uri: Uri?) {
        fileShareManager.setSharedFolderUri(uri)
    }

    fun approveClient(ip: String) {
        securityManager.approveClient(ip)
    }

    fun rejectAndBlockClient(ip: String) {
        securityManager.rejectAndBlockClient(ip)
    }

    fun kickClient(ip: String) {
        securityManager.kickClient(ip)
    }

    fun unblockClient(ip: String) {
        securityManager.unblockClient(ip)
    }

    fun updateSecurityPreferences(prefs: SecurityPreferences) {
        securityManager.updatePreferences(prefs)
        startAutoStopTimerIfNeeded()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    private fun startAutoStopTimerIfNeeded() {
        autoStopJob?.cancel()
        val minutes = securityManager.preferences.value.autoStopMinutes
        if (minutes > 0) {
            autoStopJob = viewModelScope.launch {
                var idleSeconds = 0
                while (isActive && _broadcastStatus.value == BroadcastStatus.BROADCASTING) {
                    delay(5000)
                    if (connectedClients.value.isEmpty()) {
                        idleSeconds += 5
                        if (idleSeconds >= minutes * 60) {
                            stopBroadcast()
                            break
                        }
                    } else {
                        idleSeconds = 0
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBroadcast()
        bandwidthCollectorJob?.cancel()
    }
}
