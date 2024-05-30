package com.example.iotcontroller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import android.Manifest;

import com.google.android.material.button.MaterialButton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final String WIFI_NAME = "\"Foodie\"";
    String alarmContent = "", alarmsToSendToESP = "";
    Button btnLights, btnManual, btnManualWater, btnManage, btnCancel, btnConnect, btnAutomatic;
    Dialog dispenseDialog, warningDialog, connectedDialog;

    MaterialButton btnCloseConnectionInfo, btnDisconnect;

    ArrayList<String> alarmsToSend;

    TextView connectedWifi, tvUpcomingMealTime;

    ActivityResultLauncher<Intent> intentLauncher;

    private final OkHttpClient client = new OkHttpClient();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permission if not yet granted
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PackageManager.PERMISSION_GRANTED);

        // Declare buttons and other views
        btnManual = findViewById(R.id.btnManual);
        btnLights = findViewById(R.id.btnLights);
        btnManualWater = findViewById(R.id.btnManualWater);
        btnManage = findViewById(R.id.btnManage);
        btnConnect = findViewById(R.id.btnConnect);
        btnAutomatic = findViewById(R.id.btnAutomatic);
        tvUpcomingMealTime = findViewById(R.id.tvUpcomingMealTime);
        enableDisabledButtons(false);

        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                alarmsToSend = data.getStringArrayListExtra("alarms");
                                if (alarmsToSend != null) {
                                    for (String alarm : alarmsToSend) {
                                        alarmsToSendToESP += alarm + "&";
                                    }
                                    sendCommand("ALLALARMS:" + alarmsToSendToESP);
                                    alarmsToSendToESP = "";
                                    updateUpcomingMealTime();
                                }
                            }
                        }
                    }
                }
        );



        // Set up network request for ESP8266 communication
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

        btnConnect.setBackgroundColor(getColor(R.color.connect_button));

        // Initialize dialogs
        setupDialogs();

        // Set button click listeners
        btnConnect.setOnClickListener(view -> getCurrentWifiSSID(this));
        btnCancel.setOnClickListener(view -> dispenseDialog.dismiss());
        btnManage.setOnClickListener(view -> sendCommand("GETALARM:"));
        btnManualWater.setOnClickListener(view -> sendCommand("red"));
        btnLights.setOnClickListener(view -> sendCommand("green"));
        btnManual.setOnClickListener(view -> sendCommand("blue"));
    }

    //Updates the upcoming meal time in the dashboard
    private void updateUpcomingMealTime() {
        if (alarmsToSend != null && !alarmsToSend.isEmpty()) {
            long currentTimeMillis = System.currentTimeMillis();
            long upcomingAlarmTimeMillis = Long.MAX_VALUE;
            String upcomingAlarmTimeString = "";

            for (String alarm : alarmsToSend) {
                String[] parts = alarm.split(";");
                if (parts.length > 3 && "1".equals(parts[3])) {
                    // Convert alarm time to milliseconds
                    int hourOfDay = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    Calendar alarmTime = Calendar.getInstance();
                    alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarmTime.set(Calendar.MINUTE, minute);
                    alarmTime.set(Calendar.SECOND, 0);
                    alarmTime.set(Calendar.MILLISECOND, 0);

                    // Check if alarm time is in the future and update upcomingAlarmTimeMillis
                    long alarmTimeMillis = alarmTime.getTimeInMillis();
                    if (alarmTimeMillis > currentTimeMillis && alarmTimeMillis < upcomingAlarmTimeMillis) {
                        upcomingAlarmTimeMillis = alarmTimeMillis;
                        upcomingAlarmTimeString = String.format(Locale.getDefault(), "%02d:%02d %s",
                                hourOfDay > 12 ? hourOfDay - 12 : hourOfDay,
                                minute,
                                hourOfDay < 12 ? "AM" : "PM");
                    }
                }
            }

            if (!upcomingAlarmTimeString.isEmpty()) {
                // Display the upcoming alarm time
                tvUpcomingMealTime.setText(upcomingAlarmTimeString);
            } else {
                // No upcoming alarm found
                tvUpcomingMealTime.setText("No upcoming alarms");
            }
        } else {
            // No alarms set
            tvUpcomingMealTime.setText("No alarms set");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the displayed upcoming meal time when the activity resumes
        updateUpcomingMealTime();
    }

    private void setupDialogs() {
        dispenseDialog = new Dialog(MainActivity.this);
        dispenseDialog.setContentView(R.layout.activity_dispense_dialog);
        btnCancel = dispenseDialog.findViewById(R.id.btnCancelDispense);
        Objects.requireNonNull(dispenseDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dispenseDialog.setCancelable(false);

        warningDialog = new Dialog(MainActivity.this);
        warningDialog.setContentView(R.layout.activity_warning_dialog);
        warningDialog.setCancelable(true);
        warningDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        connectedDialog = new Dialog(MainActivity.this);
        connectedDialog.setContentView(R.layout.connected_wifi_dialog);
        connectedDialog.setCancelable(true);
        connectedDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        connectedWifi = connectedDialog.findViewById(R.id.tvCurrentlyConnectedWifi);

        btnCloseConnectionInfo = connectedDialog.findViewById(R.id.btnCloseDisconnect);
        btnDisconnect = connectedDialog.findViewById(R.id.btnDisconnect);

        btnCloseConnectionInfo.setOnClickListener(v -> connectedDialog.dismiss());
        btnDisconnect.setOnClickListener(v -> {
            // Implement disconnect logic if required
            connectedDialog.dismiss();
        });
    }

    //Sends Command to ESP8266 like Alarms, Data Gathering.
    public void sendCommand(String cmd) {
        Thread receiver = new Thread(() -> {
            String command = "http://192.168.4.1/" + cmd;
            Log.d("Command", command);
            Request request = new Request.Builder().url(command).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.body() != null) {
                    String myResponse = response.body().string();
                    //TODO FIND THE BUG THAT REMOVES THE COMMA ON THE DAYS
                    //TODO MAKE SURE THE SWITCH WORKS PROPERLY
                    String cleanResponse = myResponse.replaceAll("<.*?>", "");
                        cleanResponse.replace("\n", "");
                        cleanResponse.replace("\r", "");
                        cleanResponse.replace(" ", "");
                        cleanResponse.replace("\t", "");
                        cleanResponse.trim();
                    Log.d("ResponseAgain", cleanResponse);
                    checkESP8266Response(cleanResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiver.start();
    }

    //Checks the WiFi connection of the phone if it is the correct WiFi SSID
    public void getCurrentWifiSSID(Context context) {
        String ssid = "";
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            Toast.makeText(getApplicationContext(), "Please open your GPS and Wi-Fi to connect to the internet", Toast.LENGTH_LONG).show();
        } else {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                ssid = wifiInfo.getSSID();
                if (ssid.equals(WIFI_NAME)) {
                    btnConnect.setText("Connected");
                    Toast.makeText(getApplicationContext(), "Currently connected to " + ssid.replace("\"", ""), Toast.LENGTH_SHORT).show();
                    sendCommand("REPORTSTATUS:");
                    enableDisabledButtons(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Wifi is not supported", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Checks the response of ESP8266 and breaks down the data received
    void checkESP8266Response(String str) {
        String[] cont = str.split(",");
        TextView tvLight = findViewById(R.id.tvLightStatus);
        TextView tvFood = findViewById(R.id.tvFoodPercentage);
        TextView tvWater = findViewById(R.id.tvWaterPercentage);

        alarmContent = "";
        for (int i = 1; i < cont.length; i++) {
            if(cont[0].equals("") || cont[0].isEmpty())continue;
            Log.d("ResponseParts", cont[i]);
            if (cont[0].equals("REPORTSTATUS:")) {
                switch (cont[i].split(":")[0]) {
                    case "Light":
                        String lightStatus = cont[i].split(":")[1];
                        runOnUiThread(() -> {
                            tvLight.setText(lightStatus.equals("0") ? "OFF" : "ON");
                            btnLights.setText(lightStatus.equals("0") ? "Turn on" : "Turn off");
                        });
                        break;
                    case "Food":
                        String foodContent = cont[i].split(":")[1];
                        runOnUiThread(() -> {
                            tvFood.setText(foodContent);
                            tvFood.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                    foodContent.equals("EMPTY") ? R.color.red : R.color.white));
                        });
                        break;
                    case "Water":
                        String waterContent = cont[i].split(":")[1];
                        runOnUiThread(() -> {
                            tvWater.setText(waterContent);
                            tvWater.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                    waterContent.equals("EMPTY") ? R.color.red : R.color.white));
                        });
                        break;
                }
            } else if (cont[0].equals("ALARM:")) {
                alarmContent += cont[i];
            }
        }
        if(cont[0].equals("ALARM:")){
            Intent viewSchedule = new Intent(MainActivity.this, Schedule_View.class);
            viewSchedule.putExtra("Alarm", alarmContent);
            intentLauncher.launch(viewSchedule);
        }
    }

    //Disables dashboard buttons if the phone is not connected to the correct WiFi and vice versa
    void enableDisabledButtons(boolean enabled) {
        btnManual.setEnabled(enabled);
        btnLights.setEnabled(enabled);
        btnManualWater.setEnabled(enabled);
        btnManage.setEnabled(enabled);
        btnAutomatic.setEnabled(enabled);
        int color = ContextCompat.getColor(getApplicationContext(), enabled ? R.color.white : R.color.disabled_color);
        btnManual.setTextColor(color);
        btnLights.setTextColor(color);
        btnManualWater.setTextColor(color);
        btnManage.setTextColor(color);
        btnAutomatic.setTextColor(color);
    }
}
