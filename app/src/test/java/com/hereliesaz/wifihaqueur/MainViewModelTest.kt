package com.hereliesaz.wifihaqueur

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var application: Application
    private lateinit var spiedWifiManager: WifiManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        spiedWifiManager = spy(wifiManager)
        val mockApplication = spy(application)
        whenever(mockApplication.getSystemService(Context.WIFI_SERVICE)).thenReturn(spiedWifiManager)
        viewModel = MainViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test startScan initiates scan`() {
        viewModel.startScan()
        verify(spiedWifiManager).startScan()
    }

    @Test
    fun `test scan results are updated`() = runTest {
        val intent = Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        application.sendBroadcast(intent)

        val results = viewModel.scanResults.first()
        assert(results.isEmpty()) // Initially empty
    }
}
