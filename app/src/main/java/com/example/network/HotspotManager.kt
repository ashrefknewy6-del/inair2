package com.example.network

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.example.model.NetworkProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

class HotspotManager(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null

    private val _networkProfile = MutableStateFlow(NetworkProfile())
    val networkProfile: StateFlow<NetworkProfile> = _networkProfile.asStateFlow()

    private val _isHotspotRunning = MutableStateFlow(false)
    val isHotspotRunning: StateFlow<Boolean> = _isHotspotRunning.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun refreshNetworkInfo() {
        val currentIp = getDeviceIpAddress()
        _networkProfile.value = _networkProfile.value.copy(
            ipAddress = currentIp
        )
    }

    fun startLocalHotspot(onSuccess: (NetworkProfile) -> Unit, onError: (String) -> Unit) {
        if (wifiManager == null) {
            onError("Wi-Fi Manager unavailable")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        hotspotReservation = reservation
                        _isHotspotRunning.value = true

                        var ssid = "InAir-Hotspot"
                        var password = ""

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            reservation?.softApConfiguration?.let { config ->
                                ssid = config.ssid ?: ssid
                                password = config.passphrase ?: ""
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            reservation?.wifiConfiguration?.let { config ->
                                ssid = config.SSID ?: ssid
                                password = config.preSharedKey ?: ""
                            }
                        }

                        // Hotspot IP is typically 192.168.43.1 or 192.168.49.1 or detected interface IP
                        val detectedIp = getDeviceIpAddress()
                        val ip = if (detectedIp != "127.0.0.1") detectedIp else "192.168.43.1"

                        val profile = NetworkProfile(
                            ssid = ssid,
                            passphrase = password,
                            ipAddress = ip,
                            port = 8080,
                            isLocalHotspot = true
                        )
                        _networkProfile.value = profile
                        onSuccess(profile)
                    }

                    override fun onStopped() {
                        super.onStopped()
                        _isHotspotRunning.value = false
                        hotspotReservation = null
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        _isHotspotRunning.value = false
                        val errorText = when (reason) {
                            ERROR_NO_CHANNEL -> "Hotspot failure: No channel available"
                            ERROR_GENERIC -> "Hotspot failure: Generic error"
                            ERROR_INCOMPATIBLE_MODE -> "Hotspot failure: Incompatible Wi-Fi mode"
                            ERROR_TETHERING_DISALLOWED -> "Hotspot failure: Tethering is restricted"
                            else -> "Failed to start local hotspot (Code: $reason)"
                        }
                        _errorMessage.value = errorText
                        onError(errorText)
                    }
                }, Handler(Looper.getMainLooper()))
            } else {
                onError("Local-only hotspot requires Android 8.0+")
            }
        } catch (e: SecurityException) {
            val err = "Missing permissions to start local hotspot: ${e.localizedMessage}"
            _errorMessage.value = err
            onError(err)
        } catch (e: Exception) {
            val err = "Error starting hotspot: ${e.localizedMessage}"
            _errorMessage.value = err
            onError(err)
        }
    }

    fun stopLocalHotspot() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                hotspotReservation?.close()
                hotspotReservation = null
            }
            _isHotspotRunning.value = false
            refreshNetworkInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setManualNetworkProfile(ssid: String, pass: String) {
        val currentIp = getDeviceIpAddress()
        _networkProfile.value = NetworkProfile(
            ssid = ssid,
            passphrase = pass,
            ipAddress = currentIp,
            port = 8080,
            isLocalHotspot = false
        )
    }

    fun getDeviceIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                // Prefer wlan0, ap0, or softap
                val isWifiOrAp = intf.name.contains("wlan") || intf.name.contains("ap") || intf.name.contains("swlan")
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val hostAddress = addr.hostAddress ?: continue
                        if (isWifiOrAp) return hostAddress
                    }
                }
            }
            // Fallback second pass for any non-loopback IPv4
            val allInterfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            for (intf in allInterfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    companion object {
        fun openSystemHotspotSettings(context: Context) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.android.settings", "com.android.settings.TetherSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    val altIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(altIntent)
                }
            }
        }
    }
}
