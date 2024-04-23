package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

public class Schedule_View extends AppCompatActivity {

    Button btnBack;
    ImageButton addSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);


        //Button Function Declaration
        btnBack = findViewById(R.id.btnBackButton);
        addSchedule = findViewById(R.id.btnAddSchedule);
        //Back Button Function
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.exit_anim, R.anim.no_animation); // Use a blank no_animation for the entering activity

                finish();
            }
        });

        //Redirect to Add Schedule Activity
        addSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlarmEditor();
            }
        });




    }
    public void showAlarmEditor(){
        NumberPicker hourPicker, minutePicker, timePicker;
        Button btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;
        ImageButton backToManage;

        BottomSheetDialog addAlarm = new BottomSheetDialog(this);
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_alarm_feature, null);
        addAlarm.setContentView(contentView);
        addAlarm.setCancelable(true);


        addAlarm.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        hourPicker = addAlarm.findViewById(R.id.hourPicker);
        hourPicker.setMinValue(01);
        hourPicker.setMaxValue(12);

        minutePicker = addAlarm.findViewById(R.id.minutePicker);
        minutePicker.setMinValue(00);
        minutePicker.setMaxValue(59);


        minutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                // Format single-digit values with leading zero
                return String.format(Locale.getDefault(), "%02d", value);
            }
        });

        String[] time = {"AM","PM"};
        timePicker = addAlarm.findViewById(R.id.AM_PM_Picker);
        timePicker.setDisplayedValues(time);
        timePicker.setWrapSelectorWheel(true);


        btnSunday = addAlarm.findViewById(R.id.Sunday);



        if(hourPicker != null && minutePicker != null && timePicker != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hourPicker.setTextColor(R.style.NumberPickerStyle);
                minutePicker.setTextColor(R.style.NumberPickerStyle);
                timePicker.setTextColor(R.style.NumberPickerStyle);
            }
        }
        addAlarm.show();
    }


}