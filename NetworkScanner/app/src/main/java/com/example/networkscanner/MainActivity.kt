package com.example.networkscanner

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.networkscanner.ui.theme.NetworkScannerTheme
import com.hereliesaz.aznavrail.AzNavRail

data class Dictionary(val name: String, val url: String)

class MainActivity : ComponentActivity() {

    private val dictionaries = listOf(
        Dictionary("RockYou dictionary", "https://drive.google.com/uc?export=download&id=1Is4puS_7DsQLyXo3h5yxHYz8fOe_o_4h"),
        Dictionary("phone numbers", "https://drive.google.com/uc?export=download&id=104B7oTwz37IMpqNkcy1n8SD6gufKhbLL"),
        Dictionary("Weak Password Set", "https://drive.google.com/uc?export=download&id=10FtpZy28Ru68qWbXbQaBWW6dITyM3Leu"),
        Dictionary("English", "https://drive.google.com/uc?export=download&id=1-zGX4V4jmZ1J5O9HHvSQYkf92nQTccCf"),
        Dictionary("Birthdays from 1980 to 2010", "https://drive.google.com/uc?export=download&id=10-5CLasOmnKKXyev3_DCBSd179lOkcGz"),
        Dictionary("500,000-Word Super List", "https://drive.google.com/uc?export=download&id=1009Wo_1_Kp2smZU6gu58_KC1dg32P2uJ"),
        Dictionary("Two Million Password Set", "https://drive.google.com/uc?export=download&id=1-cxYagogFXGdXDBg94P5o5i14U9Zi5bt"),
        Dictionary("10-Digit Numbers", "https://drive.google.com/uc?export=download&id=1-UUgpot08dghuKGWgWNKwRmCVY6Pmr_H"),
        Dictionary("20 Million Password Set", "https://drive.google.com/uc?export=download&id=1-ff2HELXghs_YBniy4GTszONlywPULs2"),
        Dictionary("160 Million Password Set", "https://drive.google.com/uc?export=download&id=1-XPAMKVJ77HFNB2qNxgeEtyJq1awkFeP")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkScannerTheme {
                var showDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current
                var selectedDictionaryName by remember { mutableStateOf("None") }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val fileName = it.path?.substringAfterLast("/") ?: "Unknown"
                        selectedDictionaryName = "Local: $fileName"
                        Toast.makeText(context, "Selected local dictionary: $fileName", Toast.LENGTH_SHORT).show()
                    }
                }


                if (showDialog) {
                    DictionarySelectionDialog(
                        onDismiss = { showDialog = false },
                        onDictionarySelected = { dictionary ->
                            downloadDictionary(context, dictionary)
                            selectedDictionaryName = dictionary.name
                            showDialog = false
                        },
                        onLocalFileSelected = {
                            filePickerLauncher.launch("*/*")
                            showDialog = false
                        },
                        dictionaries = dictionaries
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Row {
                        AzNavRail {
                            azRailItem(id = "scan", text = "Scan Networks", onClick = {
                                Toast.makeText(context, "Scan Networks clicked", Toast.LENGTH_SHORT).show()
                            })
                            azRailItem(id = "attack", text = "Start Attack", onClick = {
                                Toast.makeText(context, "Start Attack clicked", Toast.LENGTH_SHORT).show()
                            })
                            azRailItem(id = "dictionary", text = "Select Dictionary", onClick = { showDialog = true })
                        }
                        Text("Selected Dictionary: $selectedDictionaryName")
                    }
                }
            }
        }
    }
}

@Composable
fun DictionarySelectionDialog(
    onDismiss: () -> Unit,
    onDictionarySelected: (Dictionary) -> Unit,
    onLocalFileSelected: () -> Unit,
    dictionaries: List<Dictionary>
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dictionaries") },
        text = {
            LazyColumn {
                item {
                    TextButton(onClick = {
                        onLocalFileSelected()
                        onDismiss()
                    }) {
                        Text("Load")
                    }
                }
                items(dictionaries) { dictionary ->
                    TextButton(onClick = {
                        onDictionarySelected(dictionary)
                        onDismiss()
                    }) {
                        Text(dictionary.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Suppress("DEPRECATION")
fun downloadDictionary(context: Context, dictionary: Dictionary) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(dictionary.url))
            .setTitle(dictionary.name)
            .setDescription("Downloading dictionary...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${dictionary.name}.txt")
            .setAllowedOverMetered(true) // Allow download over mobile network
            .setAllowedOverRoaming(true)

        downloadManager.enqueue(request)
        Toast.makeText(context, "Starting download for ${dictionary.name}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
