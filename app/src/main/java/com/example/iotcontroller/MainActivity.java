package com.example.iotcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import android.Manifest;

import com.google.android.material.color.ColorResourcesOverride;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final String WIFI_NAME = "\"Foodie\"";
    Button btnLights, btnManual, btnManualWater, btnManage, btnCancel, btnConnect, btnAutomatic;
    Dialog dispenseDialog;


    Thread receiver;

//    LinearLayout dialog_box;

    private final OkHttpClient client = new OkHttpClient();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //region Request permission if not yet granted
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);
        //endregion

        //region Declaration
        btnManual = findViewById(R.id.btnManual);
        btnLights = findViewById(R.id.btnLights);
        btnManualWater = findViewById(R.id.btnManualWater);
        btnManage = findViewById(R.id.btnManage);
        btnConnect = findViewById(R.id.btnConnect);
        btnAutomatic = findViewById(R.id.btnAutomatic);
        //Disable buttons on online
        enableDisabledButtons(false);
        //endregion



        //region Helps the app to communicate with ESP8266
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        NetworkRequest request = builder.build();
        connManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                connManager.bindProcessToNetwork(network);
            }
        });
        //endregion


        btnConnect.setBackgroundColor(getColor(R.color.connect_button));


        //region Dialog Box Functionality
        dispenseDialog = new Dialog(MainActivity.this);
        dispenseDialog.setContentView(R.layout.activity_dispense_dialog);
        btnCancel = dispenseDialog.findViewById(R.id.btnCancelDispense);
        Objects.requireNonNull(dispenseDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dispenseDialog.setCancelable(false);
        //endregion


        btnConnect.setOnClickListener(view -> getCurrentWifiSSID(this));

        btnCancel.setOnClickListener(view -> dispenseDialog.dismiss());

        btnManage.setOnClickListener(view ->
        {
            Intent viewSchedule = new Intent(MainActivity.this, Schedule_View.class);
            startActivity(viewSchedule);
        });
        //region ESP8266 Communication Functions
        btnManualWater.setOnClickListener(view -> sendCommand("red"));
        btnLights.setOnClickListener(view -> sendCommand("green"));
        btnManual.setOnClickListener(view -> sendCommand("blue"));
        //endregion
    }

    //Communicates with the ESP8266 via Wi-Fi
    public void sendCommand(String cmd) {
        Thread receiver = new Thread(() -> {
            String command = "http://192.168.4.1/" + cmd;
            Log.d("Command------------------------------------------", command);
            Request request = new Request.Builder().url(command).build();
            try {
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String myResponse = response.body().string();
                final String cleanResponse; // remove HTML tags
                cleanResponse = myResponse.replaceAll("<.*?>", "");
                cleanResponse.replace("\n", ""); // remove all new line characters
                cleanResponse.replace("\r", ""); // remove all carriage characters
                cleanResponse.replace(" ", ""); // removes all space characters
                cleanResponse.replace("\t", ""); // removes all tab characters
                cleanResponse.trim();
                Log.d("Response  = ", cleanResponse);
                checkESP8266Response(cleanResponse);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }
    //Checks the current network the user is connected and also checks if the user GPS/Location is open
    public void getCurrentWifiSSID(Context context) {
        String ssid = "";
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            Toast.makeText(getApplicationContext(), "Please open your GPS and Wi-Fi to connect to the internet", Toast.LENGTH_LONG).show();
        }else{

            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo;

            wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                ssid = wifiInfo.getSSID();
                if(ssid.equals(WIFI_NAME)){
                    btnConnect.setText("Connected");
                    Toast.makeText(getApplicationContext(), "Currently connected to " + ssid.replace("\"", ""), Toast.LENGTH_SHORT).show();
                    sendCommand("REPORTSTATUS:");
                    enableDisabledButtons(true);
                }else{
                    Toast.makeText(getApplicationContext(), "Wifi is not supported", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    void isOnline(){
        if(!receiver.isAlive()){
            receiver.start();
        }
    }
    void checkESP8266Response(String str){
        String[] cont = str.split(",");
        TextView tvLight = findViewById(R.id.tvLightStatus);
        TextView tvFood = findViewById(R.id.tvFoodPercentage);
        TextView tvWater = findViewById(R.id.tvWaterPercentage);
        for(int i = 1;i < cont.length;i++){
            switch(cont[i].split(":")[0]){
                case "Light":
                    if(cont[i].split(":")[1].equals("0")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvLight.setText("OFF");
                                btnLights.setText("Turn on");
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvLight.setText("ON");
                                btnLights.setText("Turn off");
                            }
                        });
                    }
                    break;
                case "Food":
                    String foodContent = cont[i].split(":")[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvFood.setText(foodContent);
                            if(foodContent.equals("LOW")){
                                tvFood.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                            }else{
                                tvFood.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                            }
                        }
                    });

                    break;
                case "Water":
                    String waterContent = cont[i].split(":")[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvWater.setText(waterContent);
                            if(waterContent.equals("LOW")){
                                tvWater.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                            }else{
                                tvWater.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                            }
                        }
                    });
                    break;
            }
        }
    }
    void enableDisabledButtons(boolean bool){
        btnManual.setEnabled(bool);
        btnLights.setEnabled(bool);
        btnManualWater.setEnabled(bool);
        btnManage.setEnabled(bool);
        btnAutomatic.setEnabled(bool);
        if(!bool){
            btnManual.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_color));
            btnLights.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_color));
            btnManualWater.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_color));
            btnManage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_color));
            btnAutomatic.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_color));
        }else{
            btnManual.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            btnLights.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            btnManualWater.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            btnManage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            btnAutomatic.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
    }
}