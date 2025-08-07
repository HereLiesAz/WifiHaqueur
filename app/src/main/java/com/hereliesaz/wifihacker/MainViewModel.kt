package com.hereliesaz.wifihacker

import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun startScan() {
        viewModelScope.launch {
            // Deprecated in API 33, but needed for older versions
            wifiManager.startScan()
            _scanResults.value = wifiManager.scanResults
        }
    }
}
