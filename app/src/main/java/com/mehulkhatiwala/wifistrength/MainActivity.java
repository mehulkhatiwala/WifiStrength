package com.mehulkhatiwala.wifistrength;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button setWifi;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    List<String> listOfProvider;
    ListAdapter adapter;
    ListView listViwProvider;

    public static final String TAG = "MainActivity";

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listOfProvider = new ArrayList<String>();

        /*setting the resources in class*/
        listViwProvider = findViewById(R.id.list_view_wifi);
        setWifi = findViewById(R.id.btn_wifi);

        //Checking for location permission
        int locPerm = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if(locPerm != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        }

        setWifi.setOnClickListener(this);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        /*checking wifi connection
         * if wifi is on searching available wifi provider*/
        if (wifiManager.isWifiEnabled() == true) {
            setWifi.setText("OFF");
            setWifi.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }else{
            setWifi.setText("ON");
            setWifi.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        /*opening a detail dialog of provider on click   */
        listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView< ?> parent, View view,
                                    int position, long id) {
                ImportDialog action = new ImportDialog(MainActivity.this, (wifiList.get(position)).toString());
                action.showDialog();
            }
        });
    }

    private void scaning() {
        Log.d(TAG,"in Scanning()");
        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();
        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi
        // connections changed
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }


    /*setting the functionality of ON/OFF button*/
    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View arg0) {
        // If android version is Q or above setWifiEnabled() will not work
        // For that we are programmatically open settings panel

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivity(panelIntent);

            if (wifiManager.isWifiEnabled()==true) {
                setWifi.setText("OFF");
                setWifi.setBackgroundColor(ContextCompat.getColor(MainActivity.this,android.R.color.holo_red_dark));
                listViwProvider.setVisibility(ListView.VISIBLE);
                finish();
            }else{
                setWifi.setText("ON");
                setWifi.setBackgroundColor(ContextCompat.getColor(MainActivity.this,android.R.color.holo_green_dark));
                listViwProvider.setVisibility(ListView.VISIBLE);
                scaning();
            }
        }else{
            /* if wifi is ON set it OFF
               and set button text "OFF" */
            if (wifiManager.isWifiEnabled() == true) {
                wifiManager.setWifiEnabled(false);
                setWifi.setText("OFF");
                setWifi.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                listViwProvider.setVisibility(ListView.GONE);
            }
            /* if wifi is OFF set it ON
             * set button text "ON"
             * and scan available wifi provider*/
            else if (wifiManager.isWifiEnabled() == false) {
                wifiManager.setWifiEnabled(true);
                setWifi.setText("ON");
                setWifi.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                listViwProvider.setVisibility(ListView.VISIBLE);
                scaning();
            }
        }

    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = wifiManager.getScanResults();
            Log.d(TAG,"in Broadcast receiver"+wifiList);
            /* sorting of wifi provider based on level */
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
                }
            });
            listOfProvider.clear();
            String providerName;
            for (int i = 0; i < wifiList.size(); i++) {
                /* to get SSID and BSSID of wifi provider*/
                providerName = String.format("%s\n%s", wifiList.get(i).SSID, wifiList.get(i).BSSID);
                listOfProvider.add(providerName);
            }
            /*setting list of all wifi provider in a List*/
            adapter = null;
            adapter = new CustomListAdapter(MainActivity.this,listOfProvider);
            listViwProvider.setAdapter(adapter);

            //adapter.notifyDataSetChanged();
        }
    }
}