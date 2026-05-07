package com.smarttoolkit.app.feature.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.smarttoolkit.app.data.db.HistoryDao
import com.smarttoolkit.app.data.db.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject

data class NetworkUiState(
    val isConnected: Boolean = false,
    val connectionType: String = "Unknown",
    val ipAddress: String = "N/A",
    val wifiSignalStrength: Int = 0,
    val linkSpeed: String = "N/A",
    val frequency: String = "N/A",
    val pingResult: String = "",
    val isPinging: Boolean = false
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    val history: StateFlow<List<HistoryEntry>> = historyDao.getByFeature("network")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }

        val isConnected = caps != null
        val connectionType = when {
            caps == null -> "Disconnected"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }

        val ip = getIpAddress()

        @Suppress("DEPRECATION")
        val wifiInfo = wm.connectionInfo
        val rssi = wifiInfo?.rssi ?: 0
        val linkSpd = wifiInfo?.linkSpeed?.let { "$it Mbps" } ?: "N/A"
        val freq = wifiInfo?.frequency?.let { "$it MHz" } ?: "N/A"

        _uiState.value = NetworkUiState(
            isConnected = isConnected,
            connectionType = connectionType,
            ipAddress = ip,
            wifiSignalStrength = WifiManager.calculateSignalLevel(rssi, 5),
            linkSpeed = linkSpd,
            frequency = freq
        )

        viewModelScope.launch {
            historyDao.insert(HistoryEntry(
                featureKey = "network",
                label = "$connectionType - $ip",
                value = if (isConnected) "Connected" else "Disconnected"
            ))
        }
    }

    fun ping() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPinging = true, pingResult = "")
            val result = withContext(Dispatchers.IO) {
                try {
                    val start = System.currentTimeMillis()
                    val reachable = InetAddress.getByName("8.8.8.8").isReachable(5000)
                    val elapsed = System.currentTimeMillis() - start
                    if (reachable) "${elapsed}ms" else "Unreachable"
                } catch (e: Exception) {
                    "Failed: ${e.message}"
                }
            }
            _uiState.value = _uiState.value.copy(isPinging = false, pingResult = result)
            historyDao.insert(HistoryEntry(featureKey = "network", label = "Ping 8.8.8.8: $result", value = result))
        }
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.clearFeature("network") }
    }

    private fun getIpAddress(): String {
        try {
            for (intf in NetworkInterface.getNetworkInterfaces()) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "N/A"
                    }
                }
            }
        } catch (_: Exception) {}
        return "N/A"
    }
}
