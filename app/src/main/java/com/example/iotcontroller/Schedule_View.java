package com.example.iotcontroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Schedule_View extends AppCompatActivity {

    private BottomSheetDialog addAlarmDialog;
    private NumberPicker hourPicker, minPicker, amOrPmPicker;
    private int selectedHour = 1, selectedMinute = 0, selectedMeridiem = 0;
    private List<String> days = new ArrayList<>();
    private ArrayList<String> alarms = new ArrayList<>();
    private List<String> alarmsDay = new ArrayList<>();

    private RelativeLayout scheduleParent;
    private LinearLayout alarmScrollable;
    private final String[] timePeriods = {"AM", "PM"};
    private final String splitter = ":";
    private String alarmData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);
        initializeElements();

        alarmData = getIntent().getStringExtra("Alarm");
        if (alarmData != null && alarmData.length() >= 10) {
            addNewAlarm(true);
        }
    }

    private void initializeElements() {
        scheduleParent = findViewById(R.id.scheduleParent);
        alarmScrollable = findViewById(R.id.svAlarmScrollable);

        findViewById(R.id.btnBackButton).setOnClickListener(v -> {
            setResult(Activity.RESULT_OK, new Intent().putStringArrayListExtra("alarms", alarms));
            for(String x : alarms){
                Log.d("COCAINE", "FINISHED RESULT: " + x);
            }

            finish();
        });

        findViewById(R.id.btnAddAlarm).setOnClickListener(v -> displayAlarmEditor());

        addAlarmDialog = new BottomSheetDialog(this);
    }

    private void displayAlarmEditor() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_alarm_feature, null);
        addAlarmDialog.setContentView(contentView);
        addAlarmDialog.setCancelable(true);
        Objects.requireNonNull(addAlarmDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addAlarmDialog.show();
        initializeAddAlarmElements(contentView);
    }

    private void initializeAddAlarmElements(View contentView) {
        hourPicker = contentView.findViewById(R.id.hourPicker);
        minPicker = contentView.findViewById(R.id.minPicker);
        amOrPmPicker = contentView.findViewById(R.id.AM_PM_Picker);

        setupDayButtons(contentView);
        setupPickers();

        contentView.findViewById(R.id.btnCancelAlarm).setOnClickListener(v -> addAlarmDialog.dismiss());
        contentView.findViewById(R.id.btnAddAlarm).setOnClickListener(v -> addNewAlarm(false));
    }

    private void setupDayButtons(View contentView) {
        MaterialButton[] dayButtons = {
                contentView.findViewById(R.id.Sunday), contentView.findViewById(R.id.btnMonday), contentView.findViewById(R.id.btnTuesday),
                contentView.findViewById(R.id.btnWednesday), contentView.findViewById(R.id.btnThursday), contentView.findViewById(R.id.btnFriday),
                contentView.findViewById(R.id.btnSaturday)
        };

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < dayButtons.length; i++) {
            final int index = i;
            dayButtons[i].setOnClickListener(v -> toggleDaySelection(dayNames[index], dayButtons[index]));
        }
    }

    private void setupPickers() {
        Calendar rn = Calendar.getInstance();

        int hour = rn.get(Calendar.HOUR);
        int minute = rn.get(Calendar.MINUTE);
        int meridiem = rn.get(Calendar.AM_PM);

        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(hour);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedHour = newVal);

        minPicker.setMinValue(0);
        minPicker.setMaxValue(59);
        minPicker.setValue(minute);
        minPicker.setFormatter(value -> String.format("%02d", value));
        minPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedMinute = newVal);

        amOrPmPicker.setMinValue(0);
        amOrPmPicker.setMaxValue(timePeriods.length - 1);
        amOrPmPicker.setValue(meridiem);
        amOrPmPicker.setDisplayedValues(timePeriods);
        amOrPmPicker.setOnValueChangedListener((picker, oldVal, newVal) -> selectedMeridiem = newVal);

        selectedHour = hour;
        selectedMinute = minute;
        selectedMeridiem = meridiem;
    }

    @SuppressLint("SetTextI18n")
    private void addNewAlarm(boolean fromExistingData) {
        if (!fromExistingData) {
            if (days.isEmpty()) {
                Toast.makeText(Schedule_View.this, "Please Select At Least One Day", Toast.LENGTH_LONG).show();
                return;
            }

            String alarmTimeString = getSelectedHour() + ":" + getSelectedMinute() + " " + timePeriods[selectedMeridiem];
            String alarmString = getSelectedHour() + splitter + getSelectedMinute() + splitter + timePeriods[selectedMeridiem] + splitter + getFormattedDays(days) + splitter + "ON";
            String compareTimeString = getSelectedHour() + splitter + getSelectedMinute() + splitter + timePeriods[selectedMeridiem];
            Log.d("COCAINE", "SETDATA: " + alarmString);
            for(String x : alarms){
                String[] temporaryCompare = x.split(":");
                String formattedTemporaryCompare = temporaryCompare[0] + ":" + temporaryCompare[1] + ":" +temporaryCompare[2];
                if(compareTimeString.equals(formattedTemporaryCompare)){
                    Toast.makeText(Schedule_View.this, "Alarm already exists", Toast.LENGTH_LONG).show();
                    return;
                }
            }


            alarms.add(alarmString);
            alarmsDay.add(getFormattedDays(days));

            createAlarmCard(alarmTimeString, getFormattedDays(days), alarmString, true);
            addAlarmDialog.dismiss();
            days.clear();
        } else {
            String[] alarmsFromData = alarmData.split("&");
            for (String alarmDetail : alarmsFromData) {
                String[] alarmParts = alarmDetail.split(":");
                if (alarmParts.length < 2) continue;

                int hour = Integer.parseInt(alarmParts[0]);
                int minute = Integer.parseInt(alarmParts[1]);
                String meridiem = alarmParts[2];
                String[] daysArray = alarmParts[3].split("-");

                @SuppressLint("DefaultLocale")
                String alarmTimeString = String.format("%02d:%02d %s", hour, minute, meridiem);
                @SuppressLint("DefaultLocale")
                String alarmString = String.format("%02d%s%02d%s%s%s%s%s%s", hour, splitter, minute, splitter, meridiem, splitter, alarmParts[3], splitter, alarmParts[4]);

                String formattedDays = String.join("-", daysArray);

                if (!alarms.contains(alarmString)) {
                    alarms.add(alarmString.trim());
                    createAlarmCard(alarmTimeString, formattedDays, alarmString, alarmParts[4].trim().equals("ON"));
                }
            }
        }
    }

    private void createAlarmCard(String time, String day, String alarmString, boolean isOn) {
        @SuppressLint("InflateParams") View cardViewLayout = LayoutInflater.from(this).inflate(R.layout.cardview_copy, null);

        TextView duplicateTime = cardViewLayout.findViewById(R.id.tvAssignedTimeCopy);
        TextView duplicateDay = cardViewLayout.findViewById(R.id.tvAssignedDayCopy);
        ImageButton duplicateDelete = cardViewLayout.findViewById(R.id.btnDeleteAlarm);
        SwitchCompat switchCompat = cardViewLayout.findViewById(R.id.scheduleSwitchCopy);

        duplicateTime.setText(time);
        duplicateDay.setText(day);

        Dialog alarmWarning = createAlarmWarningDialog(cardViewLayout, alarmString, day);

        duplicateDelete.setOnClickListener(v -> alarmWarning.show());

        switchCompat.setChecked(isOn);

        //CHANGES THE ARRAYLIST CONTENT WHENEVER THE USER CHANGE THE SWITCH STATE
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {

            String forChecking = BuildStringWithoutSwitch(alarmString);
            Log.d("COCAINE", "CHECK ALARMSTRING FOR DUPLICATE: "+ forChecking);
            Log.d("COCAINE", "CHECK FOR DUPLICATE: "+ alarmString);
            int index = -1;
            forChecking = forChecking.trim();
            if(alarms.contains(forChecking + "ON")){
                index = alarms.indexOf(forChecking + "ON");
            } else if (alarms.contains(forChecking + "OFF")) {
                index = alarms.indexOf(forChecking + "OFF");
            }

            if (index != -1) {

                String[] parts = alarms.get(index).split(splitter);
                String changeAlarms = parts[0] + splitter + parts[1] + splitter + parts[2] + splitter + parts[3] + splitter +(isChecked ? "ON" : "OFF");
                alarms.set(index, changeAlarms);

                Log.d("COCAINE", "CHANGE ALARMS: " + changeAlarms);
            }
        });

        alarmScrollable.addView(cardViewLayout);
    }

    private Dialog createAlarmWarningDialog(View cardViewLayout, String alarmString, String day) {
        Dialog alarmWarning = new Dialog(Schedule_View.this);
        alarmWarning.setContentView(R.layout.alarm_delete_warning);
        alarmWarning.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alarmWarning.setCancelable(false);

        Button yesButton = alarmWarning.findViewById(R.id.btnConfirmAlarmDeletion);
        Button noButton = alarmWarning.findViewById(R.id.btnCancelAlarmDeletion);

        yesButton.setOnClickListener(v -> {
            ((ViewManager) cardViewLayout.getParent()).removeView(cardViewLayout);
            alarmWarning.dismiss();
            String forChecking = BuildStringWithoutSwitch(alarmString);
            forChecking = forChecking.trim();
            Log.d("COCAINE", "CHECK DELETE STRING: " + forChecking);
            int index = -1;

            if(alarms.contains(forChecking + "ON")){
                index = alarms.indexOf(forChecking + "ON");
            } else if (alarms.contains(forChecking + "OFF")) {
                index = alarms.indexOf(forChecking + "OFF");
            }
            if (index != -1) {
                alarms.remove(index);
                alarmsDay.remove(day);
                REVIEWALARMS();
            }
        });

        noButton.setOnClickListener(v -> alarmWarning.dismiss());

        return alarmWarning;
    }

    private void toggleDaySelection(String day, MaterialButton dayButton) {
        if (days.contains(day)) {
            days.remove(day);
            dayButton.setBackgroundColor(getResources().getColor(R.color.card_view_color));
        } else {
            days.add(day);
            dayButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
        }
    }

    private String getFormattedDays(List<String> days) {
        return String.join("-", days);
    }

    private void updateAlarmsWithDays() {
        for (int i = 0; i < alarms.size(); i++) {
            String[] parts = alarms.get(i).split(splitter);
            alarms.set(i, parts[0] + splitter + parts[1] + splitter + parts[2] + splitter + parts[3] + splitter + alarmsDay.get(i));
        }
    }

    private String BuildStringWithoutSwitch(String toChange){
        String[] temporaryStringForChecking = toChange.toString().split(splitter);
        String temporary = "";
        for (int i = 0; i < temporaryStringForChecking.length - 1;i++){
            temporary += temporaryStringForChecking[i]+splitter;
        }
        return temporary;
    }

    //FOR DEBUGGING PURPOSES
    private void REVIEWALARMS(){
        for (String x : alarms) {
            Log.d("COCAINE", "CHECK ALARMS: " + x);
        }

    }

    private String getSelectedHour() {
        String temp = String.format("%02d", selectedHour);
        if(temp.equals("00")){
            temp = "12";
        }
//        Log.d("COCAINE", temp + "CHECK FOR ERROR");
        return temp;
    }

    private String getSelectedMinute() {
        String temp =  String.format("%02d", selectedMinute);
//        Log.d("COCAINE", temp + "CHECK FOR ERROR");
        return temp;
    }
    @Override
    public void onBackPressed(){
        setResult(Activity.RESULT_OK, new Intent().putStringArrayListExtra("alarms", alarms));
        for(String x : alarms){
            Log.d("COCAINE", "FINISHED RESULT: " + x);
        }

        finish();
    }
}
