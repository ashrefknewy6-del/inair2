package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InAirAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // InAir does not inspect or collect window content or user text
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceActive.value = false
        }
    }

    fun dispatchTap(normX: Float, normY: Float): Boolean {
        val displayMetrics = getDisplayMetrics()
        val px = (normX * displayMetrics.widthPixels).coerceIn(0f, displayMetrics.widthPixels.toFloat())
        val py = (normY * displayMetrics.heightPixels).coerceIn(0f, displayMetrics.heightPixels.toFloat())

        val path = Path().apply {
            moveTo(px, py)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 60)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun dispatchSwipe(fromNormX: Float, fromNormY: Float, toNormX: Float, toNormY: Float, durationMs: Long = 250): Boolean {
        val displayMetrics = getDisplayMetrics()
        val startX = (fromNormX * displayMetrics.widthPixels).coerceIn(0f, displayMetrics.widthPixels.toFloat())
        val startY = (fromNormY * displayMetrics.heightPixels).coerceIn(0f, displayMetrics.heightPixels.toFloat())
        val endX = (toNormX * displayMetrics.widthPixels).coerceIn(0f, displayMetrics.widthPixels.toFloat())
        val endY = (toNormY * displayMetrics.heightPixels).coerceIn(0f, displayMetrics.heightPixels.toFloat())

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(100))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun pressBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun pressHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun pressRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    private fun getDisplayMetrics(): DisplayMetrics {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return metrics
    }

    companion object {
        var instance: InAirAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${InAirAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(':').any { it.equals(expectedServiceName, ignoreCase = true) } || instance != null
        }
    }
}
