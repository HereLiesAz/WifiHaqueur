package com.hereliesaz.wifihaqueur.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

interface AzNavRailScope {
    fun azMenuItem(id: String, text: String, icon: ImageVector?, onClick: () -> Unit)
    fun azRailItem(id: String, text: String, icon: ImageVector?, color: Color? = null, onClick: () -> Unit)
    fun azMenuToggle(id: String, text: String, icon: ImageVector?, isChecked: Boolean, onClick: () -> Unit)
    fun azRailToggle(id: String, text: String, icon: ImageVector?, color: Color? = null, isChecked: Boolean, onClick: () -> Unit)
    fun azMenuCycler(id: String, text: String, icon: ImageVector?, options: List<String>, selectedOption: String, onClick: () -> Unit)
    fun azRailCycler(id: String, text: String, icon: ImageVector?, color: Color? = null, options: List<String>, selectedOption: String, onClick: () -> Unit)
    fun azSettings(displayAppNameInHeader: Boolean, packRailButtons: Boolean)
}

private class AzNavRailScopeImpl : AzNavRailScope {
    val items = mutableStateListOf<Pair<String, @Composable () -> Unit>>()

    override fun azMenuItem(id: String, text: String, icon: ImageVector?, onClick: () -> Unit) {
        items.add(id to {
            NavigationRailItem(
                selected = false,
                onClick = onClick,
                icon = { if (icon != null) Icon(icon, contentDescription = text) },
                label = { Text(text) }
            )
        })
    }

    override fun azRailItem(id: String, text: String, icon: ImageVector?, color: Color?, onClick: () -> Unit) {
        items.add(id to {
            NavigationRailItem(
                selected = false,
                onClick = onClick,
                icon = { if (icon != null) Icon(icon, contentDescription = text) },
                label = { Text(text) }
            )
        })
    }

    override fun azMenuToggle(id: String, text: String, icon: ImageVector?, isChecked: Boolean, onClick: () -> Unit) {
        // Not implemented for this version
    }

    override fun azRailToggle(id: String, text: String, icon: ImageVector?, color: Color?, isChecked: Boolean, onClick: () -> Unit) {
        // Not implemented for this version
    }

    override fun azMenuCycler(id: String, text: String, icon: ImageVector?, options: List<String>, selectedOption: String, onClick: () -> Unit) {
        // Not implemented for this version
    }

    override fun azRailCycler(id: String, text: String, icon: ImageVector?, color: Color?, options: List<String>, selectedOption: String, onClick: () -> Unit) {
        // Not implemented for this version
    }

    override fun azSettings(displayAppNameInHeader: Boolean, packRailButtons: Boolean) {
        // Not implemented for this version
    }
}

@Composable
fun AzNavRail(content: @Composable AzNavRailScope.() -> Unit) {
    val scope = remember { AzNavRailScopeImpl() }
    scope.content()

    NavigationRail {
        Column(
            modifier = Modifier.fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            scope.items.forEach { item ->
                item.second()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
