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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import com.hereliesaz.wifihacker.ui.theme.Red
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.hereliesaz.wifihacker.ui.theme.WifiHackerTheme
import android.net.wifi.ScanResult

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.startScan()
            } else {
                Toast.makeText(this, "Location permission is required for Wi-Fi scanning.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WifiHackerTheme {
                MainScreen(
                    viewModel = viewModel,
                    onScanClick = {
                        requestLocationPermission()
                    }
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
                viewModel.startScan()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onScanClick: () -> Unit) {
    val scanResults by viewModel.scanResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val context = LocalContext.current
    val scanThrottled by viewModel.scanThrottled.collectAsState()
    val selectedNetwork by viewModel.selectedNetwork.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    val isAttacking by viewModel.isAttacking.collectAsState()
    val passwordsTried by viewModel.passwordsTried.collectAsState()
    val totalPasswords by viewModel.totalPasswords.collectAsState()

    LaunchedEffect(scanThrottled) {
        if (scanThrottled) {
            Toast.makeText(context, "Wi-Fi scanning is throttled. Please try again later.", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onScanClick,
                enabled = !isScanning && !isAttacking
            ) {
                Text("Scan Networks")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning) {
                CircularProgressIndicator()
            } else if (isAttacking) {
                RadialProgressBar(
                    progress = (passwordsTried.toFloat() / totalPasswords.toFloat()),
                    passwordsTried = passwordsTried,
                    totalPasswords = totalPasswords
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(scanResults) { result ->
                        NetworkListItem(
                            result = result,
                            isSelected = result.BSSID == selectedNetwork?.BSSID,
                            onNetworkSelected = { viewModel.selectNetwork(it) }
                        )
                        Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.startAttack() },
                enabled = selectedNetwork != null && !isAttacking
            ) {
                Text("Start Attack")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LogView(logMessages = logMessages)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Disclaimer: This app is for educational purposes only. Do not use it for illegal activities.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NetworkListItem(
    result: ScanResult,
    isSelected: Boolean,
    onNetworkSelected: (ScanResult) -> Unit
) {
    Text(
        text = result.SSID,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNetworkSelected(result) }
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(16.dp)
    )
}

@Composable
fun LogView(logMessages: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        items(logMessages) { message ->
            Text(
                text = message,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun RadialProgressBar(
    progress: Float,
    passwordsTried: Long,
    totalPasswords: Long
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
        ) {
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "App Icon",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onScanClick,
                enabled = !isScanning && !isAttacking
            ) {
                Text("Scan Networks")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning) {
                CircularProgressIndicator()
            } else if (isAttacking) {
                RadialProgressBar(
                    progress = (passwordsTried.toFloat() / totalPasswords.toFloat()),
                    passwordsTried = passwordsTried,
                    totalPasswords = totalPasswords
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(scanResults) { result ->
                        NetworkListItem(
                            result = result,
                            isSelected = result.BSSID == selectedNetwork?.BSSID,
                            onNetworkSelected = { viewModel.selectNetwork(it) }
                        )
                        Divider(color = Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.startAttack() },
                enabled = selectedNetwork != null && !isAttacking
            ) {
                Text("Start Attack")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LogView(logMessages = logMessages)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Disclaimer: This app is for educational purposes only. Do not use it for illegal activities.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NetworkListItem(
    result: ScanResult,
    isSelected: Boolean,
    onNetworkSelected: (ScanResult) -> Unit
) {
    Text(
        text = result.SSID,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNetworkSelected(result) }
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(16.dp)
    )
}

@Composable
fun LogView(logMessages: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        items(logMessages) { message ->
            Text(
                text = message,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun RadialProgressBar(
    progress: Float,
    passwordsTried: Long,
    totalPasswords: Long
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
        ) {
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
        }
        Text(
            text = "$passwordsTried / $totalPasswords",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
