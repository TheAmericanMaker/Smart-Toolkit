package com.smartutilities.app.feature.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

data class NetworkUiState(
    val isConnected: Boolean = false,
    val connectionType: String = "Unknown",
    val ipAddress: String = "N/A",
    val wifiSignalStrength: Int = 0,
    val linkSpeed: String = "N/A",
    val frequency: String = "N/A"
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

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
