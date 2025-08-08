package com.hereliesaz.wifihacker

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val wifiManager =
        application.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                fetchScanResults()
            } else {
                // Scan failed, handle failure if needed.
                fetchScanResults()
            }
        }
    }

    private fun fetchScanResults() {
        // Requires ACCESS_FINE_LOCATION permission.
        // From Android 13, returns an empty list if location is disabled.
        try {
            @Suppress("DEPRECATION")
            _scanResults.value = wifiManager.scanResults
        } catch (e: SecurityException) {
            _scanResults.value = emptyList()
        }
    }

    init {
        fetchScanResults()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        getApplication<Application>().registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun refreshScanResults() {
        fetchScanResults()
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(wifiScanReceiver)
    }
}
