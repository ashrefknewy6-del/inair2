package com.example.security

import com.example.model.ClientSession
import com.example.model.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class SecurityManager {

    private val _preferences = MutableStateFlow(SecurityPreferences())
    val preferences: StateFlow<SecurityPreferences> = _preferences.asStateFlow()

    private val _connectedClients = MutableStateFlow<List<ClientSession>>(emptyList())
    val connectedClients: StateFlow<List<ClientSession>> = _connectedClients.asStateFlow()

    private val _blockedIps = MutableStateFlow<Set<String>>(emptySet())
    val blockedIps: StateFlow<Set<String>> = _blockedIps.asStateFlow()

    private val _pendingApproval = MutableStateFlow<ClientSession?>(null)
    val pendingApproval: StateFlow<ClientSession?> = _pendingApproval.asStateFlow()

    private val approvedIps = ConcurrentHashMap.newKeySet<String>()

    fun updatePreferences(prefs: SecurityPreferences) {
        _preferences.value = prefs
    }

    fun isIpBlocked(ip: String): Boolean {
        return _blockedIps.value.contains(ip)
    }

    fun onClientConnected(session: ClientSession) {
        val current = _connectedClients.value.filter { it.ipAddress != session.ipAddress }.toMutableList()
        current.add(session)
        _connectedClients.value = current

        if (_preferences.value.requireDeviceApproval && !approvedIps.contains(session.ipAddress)) {
            _pendingApproval.value = session
        } else {
            approvedIps.add(session.ipAddress)
        }
    }

    fun onClientDisconnected(ip: String) {
        _connectedClients.value = _connectedClients.value.filter { it.ipAddress != ip }
        if (_pendingApproval.value?.ipAddress == ip) {
            _pendingApproval.value = null
        }
    }

    fun approveClient(ip: String) {
        approvedIps.add(ip)
        if (_pendingApproval.value?.ipAddress == ip) {
            _pendingApproval.value = null
        }
        _connectedClients.value = _connectedClients.value.map {
            if (it.ipAddress == ip) it.copy(isApproved = true) else it
        }
    }

    fun rejectAndBlockClient(ip: String) {
        approvedIps.remove(ip)
        _blockedIps.value = _blockedIps.value + ip
        if (_pendingApproval.value?.ipAddress == ip) {
            _pendingApproval.value = null
        }
        _connectedClients.value = _connectedClients.value.filter { it.ipAddress != ip }
    }

    fun kickClient(ip: String) {
        approvedIps.remove(ip)
        _connectedClients.value = _connectedClients.value.filter { it.ipAddress != ip }
    }

    fun unblockClient(ip: String) {
        _blockedIps.value = _blockedIps.value - ip
    }

    fun verifyAccess(ip: String, pin: String?): Boolean {
        if (isIpBlocked(ip)) return false

        val prefs = _preferences.value
        if (prefs.isPinProtectionEnabled) {
            if (pin != prefs.pinCode) return false
        }

        if (prefs.requireDeviceApproval) {
            return approvedIps.contains(ip)
        }

        return true
    }
}
