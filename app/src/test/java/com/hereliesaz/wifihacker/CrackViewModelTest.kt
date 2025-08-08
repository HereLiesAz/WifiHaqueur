package com.hereliesaz.wifihacker

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowConnectivityManager

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@ExperimentalCoroutinesApi
class CrackViewModelTest {

    private lateinit var viewModel: CrackViewModel
    private lateinit var application: Application
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var shadowConnectivityManager: ShadowConnectivityManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        connectivityManager = application.getSystemService(ConnectivityManager::class.java)
        shadowConnectivityManager = org.robolectric.Shadows.shadowOf(connectivityManager)
        viewModel = spy(CrackViewModel(application))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun `test startCracking unsupported api level`() = runTest {
        viewModel.startCracking("test-ssid", "WPA")
        val status = viewModel.status.first()
        assert(status == "Cracking not supported on this Android version.")
    }

    @Test
    fun `test startCracking success`() = runTest {
        val passwords = listOf("password123", "correctpassword")
        doReturn(passwords).whenever(viewModel).downloadDictionaries()

        val job = launch {
            viewModel.startCracking("test-ssid", "WPA")
        }

        // Let the viewmodel post the network callback
        testDispatcher.scheduler.advanceUntilIdle()

        val networkCallback = shadowConnectivityManager.networkCallbacks.first()
        networkCallback.onAvailable(mock<Network>())

        val status = viewModel.status.first()
        assert(status.contains("correctpassword"))
        job.cancel()
    }

    @Test
    fun `test startCracking failure`() = runTest {
        val passwords = listOf("password123", "wrongpassword")
        doReturn(passwords).whenever(viewModel).downloadDictionaries()

        val job = launch {
            viewModel.startCracking("test-ssid", "WPA")
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val status = viewModel.status.first()
        assert(status == "Failed to crack password.")
        job.cancel()
    }
}
