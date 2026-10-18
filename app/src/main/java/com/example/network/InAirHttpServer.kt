package com.example.network

import android.net.Uri
import com.example.file.FileShareManager
import com.example.model.ClientSession
import com.example.screen.ScreenCaptureManager
import com.example.security.SecurityManager
import com.example.service.InAirAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InAirHttpServer(
    private val port: Int = 8080,
    private val screenCaptureManager: ScreenCaptureManager,
    private val fileShareManager: FileShareManager,
    private val securityManager: SecurityManager
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeStreams = ConcurrentHashMap.newKeySet<Socket>()

    fun start() {
        if (serverSocket != null) return
        serverJob = scope.launch {
            try {
                val ss = ServerSocket(port)
                serverSocket = ss
                while (isActive && !ss.isClosed) {
                    try {
                        val clientSocket = ss.accept()
                        launch { handleClient(clientSocket) }
                    } catch (e: Exception) {
                        if (!ss.isClosed) e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        try {
            serverJob?.cancel()
            serverJob = null
            for (sock in activeStreams) {
                try { sock.close() } catch (ignored: Exception) {}
            }
            activeStreams.clear()
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress.hostAddress ?: "unknown"

        if (securityManager.isIpBlocked(clientIp)) {
            sendResponse(socket, 403, "text/plain", "Forbidden: IP is blocked".toByteArray())
            try { socket.close() } catch (ignored: Exception) {}
            return
        }

        try {
            val inputStream = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val requestLine = reader.readLine() ?: run {
                socket.close()
                return
            }

            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                socket.close()
                return
            }
            val method = parts[0].uppercase()
            val fullPath = parts[1]

            // Headers parsing
            val headers = mutableMapOf<String, String>()
            var line: String? = reader.readLine()
            var userAgent = "Web Browser"
            var contentLength = 0L

            while (!line.isNullOrEmpty()) {
                val colon = line.indexOf(':')
                if (colon > 0) {
                    val key = line.substring(0, colon).trim().lowercase()
                    val value = line.substring(colon + 1).trim()
                    headers[key] = value
                    if (key == "user-agent") userAgent = value
                    if (key == "content-length") contentLength = value.toLongOrNull() ?: 0L
                }
                line = reader.readLine()
            }

            val uri = Uri.parse("http://localhost$fullPath")
            val path = uri.path ?: "/"

            // Register client session
            val session = ClientSession(
                id = UUID.randomUUID().toString(),
                ipAddress = clientIp,
                userAgent = userAgent,
                connectedAtMillis = System.currentTimeMillis()
            )
            securityManager.onClientConnected(session)

            when {
                path == "/" || path == "/index.html" -> {
                    val bytes = WebDashboardAssets.INDEX_HTML.toByteArray(Charsets.UTF_8)
                    sendResponse(socket, 200, "text/html; charset=UTF-8", bytes)
                    socket.close()
                }

                path == "/stream.mjpg" -> {
                    handleMjpegStream(socket, clientIp)
                }

                path == "/api/status" -> {
                    val json = JSONObject().apply {
                        put("requiresPin", securityManager.preferences.value.isPinProtectionEnabled)
                        put("authorized", securityManager.verifyAccess(clientIp, null))
                        put("devicesCount", securityManager.connectedClients.value.size)
                        put("ip", clientIp)
                    }
                    sendResponse(socket, 200, "application/json", json.toString().toByteArray())
                    socket.close()
                }

                path == "/api/auth" && method == "POST" -> {
                    val charArray = CharArray(contentLength.toInt().coerceIn(0, 1024))
                    reader.read(charArray)
                    val body = String(charArray)
                    val pin = try { JSONObject(body).optString("pin") } catch (e: Exception) { "" }
                    val isMatch = pin == securityManager.preferences.value.pinCode
                    if (isMatch) {
                        securityManager.approveClient(clientIp)
                    }
                    val res = JSONObject().apply { put("success", isMatch) }
                    sendResponse(socket, 200, "application/json", res.toString().toByteArray())
                    socket.close()
                }

                path == "/api/control" && method == "POST" -> {
                    handleControlAction(uri)
                    val res = JSONObject().apply { put("status", "ok") }
                    sendResponse(socket, 200, "application/json", res.toString().toByteArray())
                    socket.close()
                }

                path == "/api/files" -> {
                    val files = fileShareManager.listSharedFiles()
                    val array = JSONArray()
                    for (f in files) {
                        array.put(JSONObject().apply {
                            put("name", f.name)
                            put("size", f.size)
                            put("isDirectory", f.isDirectory)
                            put("lastModified", f.lastModified)
                        })
                    }
                    sendResponse(socket, 200, "application/json", array.toString().toByteArray())
                    socket.close()
                }

                path == "/api/files/download" -> {
                    val fileName = uri.getQueryParameter("file")?.let { URLDecoder.decode(it, "UTF-8") }
                    if (fileName.isNullOrEmpty()) {
                        sendResponse(socket, 400, "text/plain", "Missing file param".toByteArray())
                        socket.close()
                        return
                    }
                    val inStream = fileShareManager.openFileInputStream(fileName)
                    if (inStream == null) {
                        sendResponse(socket, 404, "text/plain", "File Not Found".toByteArray())
                        socket.close()
                        return
                    }

                    val out = socket.getOutputStream()
                    val header = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Content-Disposition: attachment; filename=\"$fileName\"\r\n" +
                            "Connection: close\r\n\r\n").toByteArray()
                    out.write(header)
                    inStream.use { it.copyTo(out) }
                    out.flush()
                    socket.close()
                }

                path == "/api/files/upload" && method == "POST" -> {
                    val rawName = uri.getQueryParameter("name") ?: "upload_${System.currentTimeMillis()}"
                    val fileName = URLDecoder.decode(rawName, "UTF-8")
                    val saved = fileShareManager.saveIncomingFile(fileName, inputStream, contentLength)
                    val res = JSONObject().apply { put("success", saved) }
                    sendResponse(socket, 200, "application/json", res.toString().toByteArray())
                    socket.close()
                }

                else -> {
                    sendResponse(socket, 404, "text/plain", "Not Found".toByteArray())
                    socket.close()
                }
            }
        } catch (e: Exception) {
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    private fun handleControlAction(uri: Uri) {
        val action = uri.getQueryParameter("action") ?: return
        val service = InAirAccessibilityService.instance ?: return

        when (action) {
            "tap" -> {
                val x = uri.getQueryParameter("x")?.toFloatOrNull() ?: 0.5f
                val y = uri.getQueryParameter("y")?.toFloatOrNull() ?: 0.5f
                service.dispatchTap(x, y)
            }
            "swipe" -> {
                val x1 = uri.getQueryParameter("x1")?.toFloatOrNull() ?: 0.5f
                val y1 = uri.getQueryParameter("y1")?.toFloatOrNull() ?: 0.5f
                val x2 = uri.getQueryParameter("x2")?.toFloatOrNull() ?: 0.5f
                val y2 = uri.getQueryParameter("y2")?.toFloatOrNull() ?: 0.5f
                service.dispatchSwipe(x1, y1, x2, y2)
            }
            "back" -> service.pressBack()
            "home" -> service.pressHome()
            "recents" -> service.pressRecents()
            "notifications" -> service.openNotifications()
        }
    }

    private fun handleMjpegStream(socket: Socket, clientIp: String) {
        activeStreams.add(socket)
        val out: OutputStream
        try {
            out = BufferedOutputStream(socket.getOutputStream())
            val header = ("HTTP/1.1 200 OK\r\n" +
                    "Connection: close\r\n" +
                    "Server: InAir/1.0\r\n" +
                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                    "Pragma: no-cache\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=--frame\r\n\r\n").toByteArray()
            out.write(header)
            out.flush()

            // If there is an immediate cached frame, send it
            screenCaptureManager.latestJpegFrame.value?.let { firstFrame ->
                writeFrame(out, firstFrame)
            }

            // Register frame listener
            val listener: (ByteArray) -> Unit = { frameBytes ->
                try {
                    synchronized(out) {
                        writeFrame(out, frameBytes)
                    }
                } catch (e: Exception) {
                    activeStreams.remove(socket)
                    try { socket.close() } catch (ignored: Exception) {}
                }
            }

            screenCaptureManager.addFrameListener(listener)

            // Keep coroutine alive while socket is open
            while (!socket.isClosed && socket.isConnected && activeStreams.contains(socket)) {
                Thread.sleep(1000)
            }

            screenCaptureManager.removeFrameListener(listener)
        } catch (e: Exception) {
            // Client closed stream
        } finally {
            activeStreams.remove(socket)
            securityManager.onClientDisconnected(clientIp)
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    private fun writeFrame(out: OutputStream, frameBytes: ByteArray) {
        val frameHeader = ("--frame\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Content-Length: " + frameBytes.size + "\r\n\r\n").toByteArray()
        out.write(frameHeader)
        out.write(frameBytes)
        out.write("\r\n".toByteArray())
        out.flush()
    }

    private fun sendResponse(socket: Socket, statusCode: Int, contentType: String, data: ByteArray) {
        try {
            val statusText = when (statusCode) {
                200 -> "OK"
                400 -> "Bad Request"
                403 -> "Forbidden"
                404 -> "Not Found"
                else -> "Error"
            }
            val out = socket.getOutputStream()
            val header = ("HTTP/1.1 $statusCode $statusText\r\n" +
                    "Content-Type: $contentType\r\n" +
                    "Content-Length: " + data.size + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Connection: close\r\n\r\n").toByteArray()
            out.write(header)
            out.write(data)
            out.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
