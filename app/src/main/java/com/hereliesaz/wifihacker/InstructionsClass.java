package com.hereliesaz.wifihacker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Faizan Ahmad on 1/1/2017.
 */
public class InstructionsClass extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction_layout);
        TextView instruction = (TextView) findViewById(R.id.textView4);
        instruction.setText("This is the first application of its kind that I have seen. There is no need to root the device. The app will try 10,000 most common passwords on the access point and will let you know if it is able to crack the password. Here are a few more instructions.\n\n1. Try turning wifi on before cracking process begins.\n2.The signal strength of the access point should be good.\n3.Try cracking passwords of access points which have WPA/WPA2/WEP security.\n4.Contact fsecurify@gmail.com in case of any problem. We are always here to help.");
    }
}
