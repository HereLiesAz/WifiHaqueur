package com.hereliesaz.wifihacker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.hereliesaz.wifihacker.ui.theme.WifiHackerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.refreshScanResults()
            } else {
                // TODO: Show a dialog explaining why the permission is needed
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WifiHackerTheme {
                MainScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.refreshScanResults()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // TODO: Show a dialog explaining why the permission is needed
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val scanResults by viewModel.scanResults.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.refreshScanResults() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Refresh Networks")
        }
        LazyColumn {
            items(scanResults) { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            val intent = Intent(context, CrackActivity::class.java)
                            intent.putExtra("ssid", result.SSID)
                            intent.putExtra("detail", result.capabilities)
                            context.startActivity(intent)
                        }
                ) {
                    Text(text = result.SSID)
                }
            }
        }
    }
}
