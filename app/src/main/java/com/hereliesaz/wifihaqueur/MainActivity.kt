package com.hereliesaz.wifihaqueur

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.aznavrail.AzHostActivityLayout
import com.hereliesaz.aznavrail.model.AzButtonShape
import com.hereliesaz.aznavrail.model.AzDockingSide
import com.hereliesaz.wifihaqueur.ui.components.DictionarySelectionScreen
import com.hereliesaz.wifihaqueur.ui.theme.Primary
import com.hereliesaz.wifihaqueur.ui.theme.WifiHaqueurTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.startScan()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required for Wi-Fi scanning.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    viewModel.setDictionaryFromUri(uri)
                    Toast.makeText(this@MainActivity, "Loading custom dictionary...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error starting dictionary load", e)
                    Toast.makeText(this@MainActivity, "Error loading dictionary", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WifiHaqueurTheme {
                MainScreen(
                    viewModel = viewModel,
                    onScanClick = {
                        requestLocationPermission()
                    },
                    onPickFile = {
                        pickFileLauncher.launch("*/*")
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

            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
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
fun MainScreen(
    viewModel: MainViewModel,
    onScanClick: () -> Unit,
    onPickFile: () -> Unit
) {
    val scanResults by viewModel.scanResults.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val context = LocalContext.current
    val scanThrottled by viewModel.scanThrottled.collectAsState()
    val selectedNetwork by viewModel.selectedNetwork.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()
    val isAttacking by viewModel.isAttacking.collectAsState()
    val passwordsTried by viewModel.passwordsTried.collectAsState()
    val totalPasswords by viewModel.totalPasswords.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    var showDictionaryScreen by remember { mutableStateOf(false) }

    if (showDictionaryScreen) {
        DictionarySelectionScreen(
            onDismiss = { showDictionaryScreen = false },
            onDictionarySelected = { dictionary ->
                viewModel.setDictionary(dictionary.name)
                showDictionaryScreen = false
            },
            onPickFile = {
                onPickFile()
                showDictionaryScreen = false
            }
        )
    }

    LaunchedEffect(scanThrottled) {
        if (scanThrottled) {
            Toast.makeText(
                context,
                "Wi-Fi scanning is throttled. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    AzHostActivityLayout(
        navController = navController,
        modifier = Modifier.fillMaxSize(),
        currentDestination = currentDestination,
        isLandscape = isLandscape,
        initiallyExpanded = false
    ) {
        background(weight = 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        azConfig(
            packButtons = false,
            dockingSide = AzDockingSide.LEFT
        )

        azTheme(
            activeColor = Primary,
            defaultShape = AzButtonShape.RECTANGLE
        )

        azRailItem(id = "scan", text = "Scan", onClick = onScanClick)
        azRailItem(id = "attack", text = "Attack", onClick = { viewModel.startAttack() })
        azRailItem(
            id = "dictionary",
            text = "Dictionary",
            onClick = { showDictionaryScreen = true })

        onscreen {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp), // Apply horizontal padding to the main column
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val logListState = rememberLazyListState() // Remember LazyListState for the log

                    // Dynamic Content (Scan results, progress) - fills space above buttons
                    // This will take all available space above the buttons and log
                    Box( // Use Box to center fixed-size composables like CircularProgressIndicator/RadialProgressBar
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Takes the upper flexible space
                        contentAlignment = Alignment.Center // Centers content within this Box
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator()
                        } else if (downloadProgress != null) {
                            if (downloadProgress!! < 0f) {
                                CircularProgressIndicator()
                            } else {
                                CircularProgressIndicator(progress = downloadProgress!!)
                            }
                        } else if (isAttacking) {
                            RadialProgressBar(
                                progress = (passwordsTried.toFloat() / totalPasswords.toFloat()),
                                passwordsTried = passwordsTried,
                                totalPasswords = totalPasswords
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(), // Fill its parent Box
                                horizontalAlignment = Alignment.CenterHorizontally // Center items if they are smaller than max width
                            ) {
                                items(scanResults) { result ->
                                    NetworkListItem(
                                        result = result,
                                        isSelected = result.BSSID == selectedNetwork?.BSSID,
                                        onNetworkSelected = { viewModel.selectNetwork(it) }
                                    )
                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        color = Primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Spacer between dynamic content and log

                    // Log View - Fills the bottom half of the flexible space
                    LogView(
                        logMessages = logMessages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Takes the bottom half of the flexible space
                        listState = logListState // Pass the state to LogView
                    )

                    // LaunchedEffect for auto-scrolling log
                    LaunchedEffect(logMessages.size) { // Trigger when log messages change
                        if (logMessages.isNotEmpty()) {
                            // Check if the user is already at the bottom or scrolled up
                            val lastVisibleItemIndex =
                                logListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                            val isAtBottom =
                                lastVisibleItemIndex >= logMessages.lastIndex - 1 // Allow one item buffer

                            if (!logListState.isScrollInProgress && (isAtBottom || logListState.firstVisibleItemIndex == 0)) {
                                // If not scrolling and at bottom (or at top if list is small), scroll to latest
                                logListState.animateScrollToItem(logMessages.lastIndex)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Spacer before disclaimer
                }
            }
        }
    }
}

@Composable
fun NetworkListItem(
    result: ScanResult,
    isSelected: Boolean,
    onNetworkSelected: (ScanResult) -> Unit,
) {
    Text(
        text = result.SSID,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNetworkSelected(result) }
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent) // Highlight selected network
            .padding(16.dp)
    )
}

@Composable
fun LogView(
    logMessages: List<String>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) { // Added listState parameter
    LazyColumn(
        state = listState, // Apply the state to LazyColumn
        modifier = modifier // Apply the passed modifier here
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
    totalPasswords: Long,
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
                color = Color.Red,
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
