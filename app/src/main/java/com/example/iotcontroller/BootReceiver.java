package com.example.iotcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences preferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
            Map<String, ?> allAlarms = preferences.getAll();
            List<String> alarmStrings = new ArrayList<>();
            for (Map.Entry<String, ?> entry : allAlarms.entrySet()) {
                alarmStrings.add((String) entry.getValue());
            }

            MainActivity mainActivity = new MainActivity();
            List<CalendarAlarm> sortedAlarms = mainActivity.sortAlarms(new ArrayList<>(alarmStrings));
            for (CalendarAlarm calendarAlarm : sortedAlarms) {
                Calendar alarmCalendar = calendarAlarm.getCalendar();
                if (alarmCalendar.after(Calendar.getInstance())) {
                    mainActivity.scheduleAlarm(alarmCalendar, calendarAlarm.getOriginalString());
                }
            }
        }
    }
}
