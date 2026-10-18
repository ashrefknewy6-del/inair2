package com.example.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import com.example.model.StreamConfig
import com.example.model.StreamFps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.CopyOnWriteArrayList

class ScreenCaptureManager(private val context: Context) {

    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var targetFps: StreamFps = StreamFps.FPS_30
    private var compressionQuality: Int = 75
    private var lastFrameTimestampMs: Long = 0L

    private val _latestJpegFrame = MutableStateFlow<ByteArray?>(null)
    val latestJpegFrame: StateFlow<ByteArray?> = _latestJpegFrame.asStateFlow()

    private val _latestPreviewBitmap = MutableStateFlow<Bitmap?>(null)
    val latestPreviewBitmap: StateFlow<Bitmap?> = _latestPreviewBitmap.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _bandwidthKbps = MutableStateFlow(0f)
    val bandwidthKbps: StateFlow<Float> = _bandwidthKbps.asStateFlow()

    private var bytesInWindow = 0L
    private var windowStartMs = System.currentTimeMillis()

    // Listeners for active HTTP MJPEG streams
    private val frameListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()

    fun addFrameListener(listener: (ByteArray) -> Unit) {
        frameListeners.add(listener)
    }

    fun removeFrameListener(listener: (ByteArray) -> Unit) {
        frameListeners.remove(listener)
    }

    fun startCapture(
        projection: MediaProjection,
        config: StreamConfig,
        densityDpi: Int
    ) {
        stopCapture()

        targetFps = config.fps
        compressionQuality = config.qualityPercent

        val thread = HandlerThread("InAir-ScreenCaptureThread").apply { start() }
        backgroundThread = thread
        val handler = Handler(thread.looper)
        backgroundHandler = handler

        val width = config.resolution.width
        val height = config.resolution.height

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        reader.setOnImageAvailableListener({ ir ->
            val now = System.currentTimeMillis()
            val minInterval = 1000L / targetFps.fps
            if (now - lastFrameTimestampMs < minInterval) {
                // Rate limit to target FPS
                ir.acquireLatestImage()?.close()
                return@setOnImageAvailableListener
            }
            lastFrameTimestampMs = now

            val image = ir.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                val planes = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                // Crop to exact width and height if there was row padding
                val cleanBitmap = if (rowPadding == 0) {
                    bitmap
                } else {
                    val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                    bitmap.recycle()
                    cropped
                }

                // Compress to JPEG for stream
                val stream = ByteArrayOutputStream()
                cleanBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, stream)
                val jpegBytes = stream.toByteArray()

                // Update bandwidth tracking
                bytesInWindow += jpegBytes.size
                val elapsed = now - windowStartMs
                if (elapsed >= 1000L) {
                    _bandwidthKbps.value = (bytesInWindow.toFloat() / 1024f) / (elapsed.toFloat() / 1000f)
                    bytesInWindow = 0L
                    windowStartMs = now
                }

                _latestJpegFrame.value = jpegBytes
                _latestPreviewBitmap.value = cleanBitmap

                // Dispatch to active MJPEG streams
                for (listener in frameListeners) {
                    listener(jpegBytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }, handler)

        virtualDisplay = projection.createVirtualDisplay(
            "InAirScreenShare",
            width,
            height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            handler
        )

        _isCapturing.value = true
    }

    fun updateConfig(config: StreamConfig) {
        targetFps = config.fps
        compressionQuality = config.qualityPercent
    }

    fun stopCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null

            imageReader?.close()
            imageReader = null

            backgroundThread?.quitSafely()
            backgroundThread = null
            backgroundHandler = null

            _isCapturing.value = false
            _bandwidthKbps.value = 0f
            _latestJpegFrame.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
