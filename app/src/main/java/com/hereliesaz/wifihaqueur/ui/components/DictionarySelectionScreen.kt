package com.hereliesaz.wifihaqueur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Dictionary(val name: String, val url: String)

val dictionaries = listOf(
    Dictionary("Default", ""), // Added Default
    Dictionary("Weak Password Set", "https://drive.google.com/file/d/10FtpZy28Ru68qWbXbQaBWW6dITyM3Leu/view?usp=drive_link"),
    Dictionary("RockYou", "https://drive.google.com/file/d/1Is4puS_7DsQLyXo3h5yxHYz8fOe_o_4h/view?usp=sharing"),
    Dictionary("Phone Numbers", "https://drive.google.com/file/d/104B7oTwz37IMpqNkcy1n8SD6gufKhbLL/view?usp=drive_link"),
    Dictionary("English", "https://drive.google.com/file/d/1-zGX4V4jmZ1J5O9HHvSQYkf92nQTccCf/view?usp=drive_link"),
    Dictionary("Birthdays (1980-2010)", "https://drive.google.com/file/d/10-5CLasOmnKKXyev3_DCBSd179lOkcGz/view?usp=drive_link"),
    Dictionary("500,000-Word Super List", "https://drive.google.com/file/d/1009Wo_1_Kp2smZU6gu58_KC1dg32P2uJ/view?usp=drive_link"),
    Dictionary("Two Million Password Set", "https://drive.google.com/file/d/1-cxYagogFXGdXDBg94P5o5i14U9Zi5bt/view?usp=drive_link"),
    Dictionary("20 Million Password Set", "https://drive.google.com/file/d/1-ff2HELXghs_YBniy4GTszONlywPULs2/view?usp=drive_link"),
    Dictionary("160 Million Password Set", "https://drive.google.com/file/d/1-XPAMKVJ77HFNB2qNxgeEtyJq1awkFeP/view?usp=drive_link")
)

@Composable
fun DictionarySelectionScreen(
    onDismiss: () -> Unit,
    onDictionarySelected: (Dictionary) -> Unit,
    onPickFile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text(
                text = "Open Local Dictionary...",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickFile() }
                    .padding(16.dp)
            )
            LazyColumn {
                items(dictionaries) { dictionary ->
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = dictionary.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDictionarySelected(dictionary) }
                                .padding(16.dp)
                        )
                    }
                }
            }
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}
