package com.hereliesaz.wifihaqueur

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AzNavRail(
    onScanNetworks: () -> Unit,
    onStartAttack: () -> Unit,
    onSelectDictionary: () -> Unit
) {
    NavigationRail {
        Column(
            modifier = Modifier.fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationRailItem(
                selected = false,
                onClick = onScanNetworks,
                icon = { Icon(Icons.Filled.NetworkWifi, contentDescription = "Scan Networks") },
                label = { Text("Scan") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            NavigationRailItem(
                selected = false,
                onClick = onStartAttack,
                icon = { Icon(Icons.Default.Security, contentDescription = "Start Attack") },
                label = { Text("Attack") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            NavigationRailItem(
                selected = false,
                onClick = onSelectDictionary,
                icon = { Icon(Icons.Default.FileOpen, contentDescription = "Select Dictionary") },
                label = { Text("Dictionary") }
            )
        }
    }
}
