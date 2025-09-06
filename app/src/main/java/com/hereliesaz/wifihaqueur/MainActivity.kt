package com.hereliesaz.wifihaqueur

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.hereliesaz.wifihaqueur.AppNavRail // Correct import for your new NavRail
import com.hereliesaz.wifihaqueur.DictionarySelectionDialog
import com.hereliesaz.wifihaqueur.ui.components.AzNavRail
import com.hereliesaz.wifihaqueur.ui.theme.Primary
import com.hereliesaz.wifihaqueur.ui.theme.WifiHaqueurTheme
import kotlinx.coroutines.launch

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
                // TODO: Handle the selected file URI
                // viewModel.setDictionaryFromFile(content) // This would be the ideal place
                Toast.makeText(this, "File selected: $uri", Toast.LENGTH_SHORT).show()
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

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
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
    val dictionary by viewModel.dictionary.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDictionaryDialog by remember { mutableStateOf(false) }

    if (showDictionaryDialog) {
        DictionarySelectionDialog(
            onDismiss = { showDictionaryDialog = false },
            onDictionarySelected = { dictionaryItem ->
                if (dictionaryItem.name == "Default") {
                    viewModel.setDictionary("Weak Password Set")
                } else {
                    viewModel.setDictionary(dictionaryItem.name)
                }
                showDictionaryDialog = false
            },
            onPickFile = {
                onPickFile()
                showDictionaryDialog = false
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

                AppNavRail(
                    onScanNetworks = {
                        onScanClick()
                        scope.launch { drawerState.close() }
                    },
                    onStartAttack = {
                        viewModel.startAttack()
                        scope.launch { drawerState.close() }
                    },
                    onSelectDictionary = {
                        showDictionaryDialog = true
                        scope.launch { drawerState.close() }
                    },
                )
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedIndex = remember { mutableIntStateOf(0) }
                val logListState = rememberLazyListState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        CircularProgressIndicator()
                    } else if (isAttacking) {
                        RadialProgressBar(
                            progress = if (totalPasswords > 0) passwordsTried.toFloat() / totalPasswords.toFloat() else 0f,
                            passwordsTried = passwordsTried,
                            totalPasswords = totalPasswords
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(scanResults) { result ->
                                NetworkListItem(
                                    result = result,
                                    isSelected = result.BSSID == selectedNetwork?.BSSID,
                                    onNetworkSelected = { viewModel.selectNetwork(it) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier,
                                    thickness = DividerDefaults.Thickness, // Use the constant directly
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, Primary),
                            MaterialTheme.shapes.extraSmall
                        )
                ) {
                    SegmentedButton(
                        selected = selectedIndex.intValue == 0,
                        onClick = { onScanClick(); selectedIndex.intValue = 0 },
                        enabled = !isScanning && !isAttacking,
                        modifier = Modifier.weight(0.5f),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (isScanning) MaterialTheme.colorScheme.primary else Color.Transparent,
                            inactiveContainerColor = Color.Transparent,
                        )
                    ) {
                        Text("Scan")
                    }

                    SegmentedButton(
                        selected = selectedIndex.intValue == 1,
                        onClick = { viewModel.startAttack(); selectedIndex.intValue = 1 },
                        enabled = selectedNetwork != null && !isAttacking && dictionary.isNotEmpty(),
                        modifier = Modifier.weight(0.5f),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (isAttacking) MaterialTheme.colorScheme.primary else Color.Transparent,
                            activeContentColor = if (isAttacking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            inactiveContainerColor = Color.Transparent,
                        )
                    ) {
                        Text("Attack")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LogView(
                    logMessages = logMessages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    listState = logListState
                )

                LaunchedEffect(logMessages.size) {
                    if (logMessages.isNotEmpty()) {
                        val lastVisibleItemIndex =
                            logListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                        val isAtBottom =
                            lastVisibleItemIndex >= logMessages.lastIndex - 1

                        if (!logListState.isScrollInProgress && (isAtBottom || logListState.firstVisibleItemIndex == 0)) {
                            logListState.animateScrollToItem(logMessages.lastIndex)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .padding(16.dp)
    )
}

@Composable
fun LogView(
    logMessages: List<String>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = listState,
        modifier = modifier
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
