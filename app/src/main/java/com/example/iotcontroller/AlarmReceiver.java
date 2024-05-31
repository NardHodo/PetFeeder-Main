package com.example.iotcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    String[] message = {"Meal Time!", "Yay!", "Feeding time!", "Let's go!"};

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getStringExtra("alarm_time");
        Log.d("AlarmReceiver", "Alarm triggered at: " + alarmTime);
        int index = new Random().nextInt(message.length);
        MainActivity.RemoveText();
        NotificationHelper.showNotification(context, "Meal Time", message[index]);

        // Call the method to set the next alarm
        MainActivity.setNextAlarm(context);
    }
}
