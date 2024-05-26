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

import android.Manifest;

import com.google.android.material.button.MaterialButton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final String WIFI_NAME = "\"Foodiee\"";
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
                            assert data != null;
                            alarmsToSend = data.getStringArrayListExtra("alarms");
                            for (String alarm : alarmsToSend) {
                                alarmsToSendToESP += alarm + "&";
                            }
                            sendCommand("ALLALARMS:" + alarmsToSendToESP);
                            alarmsToSendToESP = "";
                            updateUpcomingMealTime();
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

    private void updateUpcomingMealTime() {
        if (alarmsToSend != null && !alarmsToSend.isEmpty()) {
            String closestAlarm = findClosestAlarm(alarmsToSend);
            tvUpcomingMealTime.setText(closestAlarm);
        } else {
            tvUpcomingMealTime.setText("No alarms yet");
        }
    }


    private String findClosestAlarm(List<String> alarms) {
        Calendar now = Calendar.getInstance();
        Calendar closestAlarm = null;
        String closestAlarmString = null;

        for (String alarm : alarms) {
            String[] parts = alarm.split(";");
            if (parts.length >= 4) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                int amOrPm = Integer.parseInt(parts[2]);
                int isActive = Integer.parseInt(parts[3]);

                if (isActive == 1) {
                    if (amOrPm == 1 && hour != 12) {
                        hour += 12; // Convert PM to 24-hour format
                    } else if (amOrPm == 0 && hour == 12) {
                        hour = 0; // Handle 12 AM case
                    }

                    Calendar alarmTime = Calendar.getInstance();
                    alarmTime.set(Calendar.HOUR_OF_DAY, hour);
                    alarmTime.set(Calendar.MINUTE, minute);
                    alarmTime.set(Calendar.SECOND, 0);
                    alarmTime.set(Calendar.MILLISECOND, 0);

                    if (alarmTime.before(now)) {
                        alarmTime.add(Calendar.DAY_OF_MONTH, 1); // Set for the next day if time is in the past
                    }

                    if (closestAlarm == null || alarmTime.before(closestAlarm)) {
                        closestAlarm = alarmTime;
                        closestAlarmString = String.format("%02d:%02d %s", (hour % 12 == 0) ? 12 : hour % 12, minute, amOrPm == 0 ? "AM" : "PM");
                    }
                }
            }
        }

        return closestAlarmString != null ? closestAlarmString : "No alarms yet";
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
