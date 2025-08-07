package com.hereliesaz.wifihacker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hereliesaz.wifihacker.ui.theme.WifiHackerTheme

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiHackerTheme {
                InstructionsScreen()
            }
        }
    }
}

@Composable
fun InstructionsScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("This is the first application of its kind that I have seen. There is no need to root the device. The app will try 10,000 most common passwords on the access point and will let you know if it is able to crack the password. Here are a few more instructions.\\n\\n1. Try turning wifi on before cracking process begins.\\n2.The signal strength of the access point should be good.\\n3.Try cracking passwords of access points which have WPA/WPA2/WEP security.\\n4.Contact fsecurify@gmail.com in case of any problem. We are always here to help.")
    }
}
