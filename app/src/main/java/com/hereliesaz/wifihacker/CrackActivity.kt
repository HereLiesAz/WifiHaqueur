package com.hereliesaz.wifihacker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hereliesaz.wifihacker.ui.theme.WifiHackerTheme

class CrackActivity : ComponentActivity() {
    private val viewModel: CrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ssid = intent.getStringExtra("ssid") ?: ""
        val detail = intent.getStringExtra("detail") ?: ""

        setContent {
            WifiHackerTheme {
                CrackScreen(viewModel, ssid, detail)
            }
        }
    }
}

@Composable
fun CrackScreen(viewModel: CrackViewModel, ssid: String, detail: String) {
    val status by viewModel.status.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "SSID: $ssid")
        Text(text = "Detail: $detail")
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                viewModel.startCracking(ssid, detail)
            } else {
                Toast.makeText(context, "Cracking not supported on this Android version.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Start Cracking")
        }
        LinearProgressIndicator(progress = { progress / 100f })
        Text(text = "Status: $status")
    }
}
