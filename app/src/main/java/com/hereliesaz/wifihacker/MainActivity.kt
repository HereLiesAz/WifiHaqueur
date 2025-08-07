package com.hereliesaz.wifihacker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(viewModel)
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val scanResults by viewModel.scanResults.collectAsState()
    val context = LocalContext.current

    Column {
        Button(onClick = { viewModel.startScan() }) {
            Text("Scan Networks")
        }
        LazyColumn {
            items(scanResults) { result ->
                Row(
                    modifier = Modifier
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
