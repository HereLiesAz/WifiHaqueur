package com.hereliesaz.wifihaqueur

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hereliesaz.aznavrail.model.NavItem 
import com.hereliesaz.aznavrail.model.NavItemData
import com.hereliesaz.aznavrail.model.NavRailHeader
import com.hereliesaz.aznavrail.model.NavRailMenuSection
import com.hereliesaz.aznavrail.model.PredefinedAction
import com.hereliesaz.aznavrail.ui.AzNavRail

@Composable
fun AppNavRail(
    onScanNetworks: () -> Unit,
    onStartAttack: () -> Unit,
    onSelectDictionary: () -> Unit
) {
    val context = LocalContext.current
    val appName = context.packageManager.getApplicationLabel(context.applicationInfo).toString()

    AzNavRail(
        appName = "WifiHaqueur",
        header = NavRailHeader {  },
        onPredefinedAction = { action ->
            when (action) {
                PredefinedAction.HOME -> {
                    onScanNetworks()
                }
                PredefinedAction.ABOUT -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/hereliesaz/$appName")
                    )
                    context.startActivity(intent)
                }
                PredefinedAction.FEEDBACK -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:hereliesaz@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "$appName - Feedback")
                    }
                    context.startActivity(intent)
                }
                else -> { }
            }
        },
        menuSections = listOf(
            NavRailMenuSection(
                title = "",
                items = listOf(
                    NavItem(
                        text = "Scan",
                        data = NavItemData.Action(predefinedAction = PredefinedAction.HOME),
                        showOnRail = true
                    ),
                    NavItem(
                        text = "Attack",
                        data = NavItemData.Toggle(
                            initialIsChecked = false,
                            onStateChange = { _ -> onStartAttack() }
                        ),
                        showOnRail = true
                    ),
                    NavItem(
                        text = "Dic. Pick",
                        data = NavItemData.Toggle(
                            initialIsChecked = false,
                            onStateChange = { _ -> onSelectDictionary() }
                        ),
                        showOnRail = true
                    ),
                )
            )
        ),
        footerItems = listOf( 
            NavItem( 
                text = "About",
                data = NavItemData.Action(predefinedAction = PredefinedAction.ABOUT)
            ),
            NavItem( 
                text = "Feedback",
                data = NavItemData.Action(predefinedAction = PredefinedAction.FEEDBACK)
            )
        )
    )
}
