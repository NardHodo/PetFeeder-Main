package com.example.iotcontroller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    ArrayList<String> days = new ArrayList<>(Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"));

    MaterialButton btnCloseConnectionInfo, btnDisconnect;

    ArrayList<String> alarmsToSend;

    TextView connectedWifi, tvUpcomingMealTime;

    ActivityResultLauncher<Intent> intentLauncher;

    private static final Map<String, Integer> DAY_OF_WEEK_MAP = new HashMap<>();

    private final OkHttpClient client = new OkHttpClient();

    static {
        DAY_OF_WEEK_MAP.put("SUN", Calendar.SUNDAY);
        DAY_OF_WEEK_MAP.put("MON", Calendar.MONDAY);
        DAY_OF_WEEK_MAP.put("TUE", Calendar.TUESDAY);
        DAY_OF_WEEK_MAP.put("WED", Calendar.WEDNESDAY);
        DAY_OF_WEEK_MAP.put("THU", Calendar.THURSDAY);
        DAY_OF_WEEK_MAP.put("FRI", Calendar.FRIDAY);
        DAY_OF_WEEK_MAP.put("SAT", Calendar.SATURDAY);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permission if not yet granted
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.USE_EXACT_ALARM,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.POST_NOTIFICATIONS
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
                            for (int i = 0; i < alarmsToSend.size(); i++) {
                                Log.d("ALARMAGAIN", alarmsToSend.get(i));
                                if (i == alarmsToSend.size() - 1) {
                                    alarmsToSendToESP += alarmsToSend.get(i);
                                } else {
                                    alarmsToSendToESP += alarmsToSend.get(i) + "&";
                                }
                            }
                            sendCommand("ALLALARMS:" + alarmsToSendToESP);
                            Log.d("ALARM", alarmsToSendToESP);
                            alarmsToSendToESP = "";
                            updateUpcomingMealTime(alarmsToSend);
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
        btnManage.setOnClickListener(view -> {
            Intent viewSchedule = new Intent(MainActivity.this, Schedule_View.class);
            intentLauncher.launch(viewSchedule);
            sendCommand("GETALARM:");
        });
        btnManualWater.setOnClickListener(view -> sendCommand("red"));
        btnLights.setOnClickListener(view -> sendCommand("green"));
        btnManual.setOnClickListener(view -> sendCommand("blue"));
    }

    void scheduleAlarm(Calendar calendar, String alarmTime) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarm_time", alarmTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - 30000, pendingIntent);
    }

    private void updateUpcomingMealTime(ArrayList<String> upcomingAlarm) {
        Calendar rn = Calendar.getInstance();
        if (!(upcomingAlarm.size() < 0)) {
            List<CalendarAlarm> sortedAlarms = sortAlarms(upcomingAlarm);

            for (CalendarAlarm calendarAlarm : sortedAlarms) {
                Calendar alarmCalendar = calendarAlarm.getCalendar();
                if (alarmCalendar.after(rn)) {
                    String[] timeParts = calendarAlarm.getOriginalString().split(":");
                    tvUpcomingMealTime.setText(timeParts[0] + ":" + timeParts[1] + " " + timeParts[2]);
                    scheduleAlarm(alarmCalendar, calendarAlarm.getOriginalString());
                    return;
                }
            }
            tvUpcomingMealTime.setText("No upcoming");
        } else {
            tvUpcomingMealTime.setText("No alarms yet");
        }
    }

    private void saveAlarmData(List<CalendarAlarm> alarms) {
        SharedPreferences preferences = getSharedPreferences("alarms", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < alarms.size(); i++) {
            editor.putString("alarm_" + i, alarms.get(i).getOriginalString());
        }
        editor.apply();
    }

    public static List<CalendarAlarm> sortAlarms(ArrayList<String> alarms) {
        List<CalendarAlarm> calendarAlarms = new ArrayList<>();

        for (String alarm : alarms) {
            calendarAlarms.addAll(parseAlarmDateTime(alarm));
        }

        Collections.sort(calendarAlarms, new Comparator<CalendarAlarm>() {
            @Override
            public int compare(CalendarAlarm alarm1, CalendarAlarm alarm2) {
                return alarm1.getCalendar().compareTo(alarm2.getCalendar());
            }
        });

        return calendarAlarms;
    }

    private static List<CalendarAlarm> parseAlarmDateTime(String alarmDateTime) {
        List<CalendarAlarm> calendarAlarms = new ArrayList<>();
        String[] parts = alarmDateTime.split(":");
        String timePart = parts[0] + ":" + parts[1] + " " + parts[2];
        String[] dayParts = parts[3].split("-");

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        for (String dayPart : dayParts) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, DAY_OF_WEEK_MAP.get(dayPart.toUpperCase()));

            try {
                Date date = dateFormat.parse(timePart);
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(date);

                calendar.set(Calendar.HOUR, timeCalendar.get(Calendar.HOUR));
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.AM_PM, timeCalendar.get(Calendar.AM_PM));
                calendarAlarms.add(new CalendarAlarm(calendar, alarmDateTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return calendarAlarms;
    }

    private void setupDialogs() {
        dispenseDialog = new Dialog(MainActivity.this);
        dispenseDialog.setContentView(R.layout.activity_dispense_dialog);
        btnCancel = dispenseDialog.findViewById(R.id.btnCancelDispense);
        Objects.requireNonNull(dispenseDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dispenseDialog.setCancelable(false);

//        warningDialog = new Dialog(MainActivity.this);
//        warningDialog.setContentView(R.layout.activity_warning_dialog);
//        warningDialog.setCancelable(true);
//        warningDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void enableDisabledButtons(boolean status) {
        btnManage.setEnabled(status);
        btnLights.setEnabled(status);
        btnManualWater.setEnabled(status);
        btnManual.setEnabled(status);
    }

    private void getCurrentWifiSSID(Context context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String ssid = wifiInfo.getSSID();
                if (ssid.equals(WIFI_NAME)) {
                    enableDisabledButtons(true);

                } else {
                    Toast.makeText(this, "Make sure you are connected to the correct WiFi", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    private void sendCommand(String command) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://192.168.4.1/" + command)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                }
            } catch (Exception e) {
                Log.e("NETWORK", "Error in network request", e);
            }
        }).start();
    }
}

class CalendarAlarm {
    private Calendar calendar;
    private String originalString;

    public CalendarAlarm(Calendar calendar, String originalString) {
        this.calendar = calendar;
        this.originalString = originalString;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getOriginalString() {
        return originalString;
    }
}
