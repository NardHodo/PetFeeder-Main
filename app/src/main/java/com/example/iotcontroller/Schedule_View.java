package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shawnlin.numberpicker.NumberPicker;


public class Schedule_View extends AppCompatActivity {

    Button btnBack;
    ImageButton addSchedule;
    String[] time = {"AM","PM"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);


        //Button Function Declaration
        btnBack = findViewById(R.id.btnBackButton);
        addSchedule = findViewById(R.id.btnAddAlarm);
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
        com.shawnlin.numberpicker.NumberPicker hourPicker, minutePicker, timePicker;
        Button cancelAlarm, confirmAlarm;
        ImageButton backToManage;
        MaterialButton btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;

        BottomSheetDialog addAlarm = new BottomSheetDialog(this);
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_alarm_feature, null);
        addAlarm.setContentView(contentView);
        addAlarm.setCancelable(true);


        addAlarm.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        cancelAlarm = addAlarm.findViewById(R.id.btnCancelAlarm);
        confirmAlarm = addAlarm.findViewById(R.id.btnAddAlarm);

        cancelAlarm.setBackgroundColor(getColor(R.color.dialog_boxes_button));
        confirmAlarm.setBackgroundColor(getColor(R.color.dialog_boxes_button));


        hourPicker = addAlarm.findViewById(R.id.hourPicker);
        hourPicker.setMinValue(01);
        hourPicker.setMaxValue(12);

        minutePicker = addAlarm.findViewById(R.id.minPicker);
        minutePicker.setMinValue(00);
        minutePicker.setMaxValue(59);

        timePicker = addAlarm.findViewById(R.id.AM_PM_Picker);
        timePicker.setMinValue(1);
        timePicker.setDisplayedValues(time);
        timePicker.setContentDescription("AM and PM");

        btnSunday = addAlarm.findViewById(R.id.Sunday);
        btnMonday = addAlarm.findViewById(R.id.btnMonday);
        btnTuesday = addAlarm.findViewById(R.id.btnTuesday);
        btnWednesday = addAlarm.findViewById(R.id.btnWednesday);
        btnThursday = addAlarm.findViewById(R.id.btnThursday);
        btnFriday = addAlarm.findViewById(R.id.btnFriday);
        btnSaturday = addAlarm.findViewById(R.id.btnSaturday);

        btnSunday.setOnClickListener(view -> {
               btnSunday.setBackgroundColor(getColor(R.color.black));
               btnMonday.setBackgroundColor(Color.TRANSPARENT);
               btnTuesday.setBackgroundColor(Color.TRANSPARENT);
               btnWednesday.setBackgroundColor(Color.TRANSPARENT);
               btnThursday.setBackgroundColor(Color.TRANSPARENT);
               btnFriday.setBackgroundColor(Color.TRANSPARENT);
               btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnMonday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(getColor(R.color.black));
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnTuesday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(getColor(R.color.black));
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnWednesday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(getColor(R.color.black));
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnThursday.setOnClickListener(view-> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(getColor(R.color.black));
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnFriday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(getColor(R.color.black));
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnSaturday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(getColor(R.color.black));
        });




        minutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });

        cancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAlarm.dismiss();
            }
        });




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