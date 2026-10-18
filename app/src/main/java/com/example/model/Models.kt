package com.example.model

enum class BroadcastStatus {
    OFFLINE,
    STARTING,
    BROADCASTING,
    STOPPING,
    ERROR
}

enum class StreamResolution(val label: String, val width: Int, val height: Int) {
    LOW("720p", 720, 1280),
    MEDIUM("1080p", 1080, 1920),
    HIGH("1080p HD", 1080, 2400)
}

enum class StreamFps(val fps: Int) {
    FPS_15(15),
    FPS_30(30),
    FPS_60(60)
}

data class StreamConfig(
    val resolution: StreamResolution = StreamResolution.MEDIUM,
    val fps: StreamFps = StreamFps.FPS_30,
    val qualityPercent: Int = 75
)

data class ClientSession(
    val id: String,
    val ipAddress: String,
    val userAgent: String,
    val connectedAtMillis: Long,
    val isApproved: Boolean = false,
    val isBlocked: Boolean = false
)

enum class TransferStatus {
    PENDING,
    TRANSFERRING,
    COMPLETED,
    FAILED
}

data class FileTransferItem(
    val id: String,
    val fileName: String,
    val totalBytes: Long,
    val bytesTransferred: Long = 0,
    val isUpload: Boolean = false,
    val status: TransferStatus = TransferStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (totalBytes > 0) (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

data class NetworkProfile(
    val ssid: String = "",
    val passphrase: String = "",
    val ipAddress: String = "127.0.0.1",
    val port: Int = 8080,
    val isLocalHotspot: Boolean = false
) {
    val url: String
        get() = "http://$ipAddress:$port"
}

data class SecurityPreferences(
    val requireDeviceApproval: Boolean = true,
    val isPinProtectionEnabled: Boolean = false,
    val pinCode: String = "1234",
    val autoStopMinutes: Int = 0 // 0 means Never
)
