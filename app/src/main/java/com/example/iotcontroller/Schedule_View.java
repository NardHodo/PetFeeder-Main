package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shawnlin.numberpicker.NumberPicker;

import org.w3c.dom.Text;


public class Schedule_View extends AppCompatActivity {

    Button btnBack;
    ImageButton addSchedule;
    String[] time = {"AM","PM"};
    com.shawnlin.numberpicker.NumberPicker hourPicker, minutePicker, timePicker;
    Button cancelAlarm, confirmAlarm;
    ImageButton backToManage;
    MaterialButton btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday, btnAddAlarm;
    CardView alarm;
    String alarmContent;
    int alarmCount = 0;
    String alarmId = "cvNewAlarm", switchID="swNewAlarm";
    int finalAlarmID, finalSwitchID;
    TextView alarmTime, repeat;

    SwitchCompat scheduleSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            alarmContent = extras.getString("Alarm");
        }

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

        addSchedule.setOnClickListener(view -> {
                showAlarmEditor();
        });
    }
    public void showAlarmEditor(){
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
        btnAddAlarm = addAlarm.findViewById(R.id.btnAddAlarm);

        btnSunday.setOnClickListener(view -> {
               btnSunday.setBackgroundColor(getColor(R.color.day_selected));
               btnMonday.setBackgroundColor(Color.TRANSPARENT);
               btnTuesday.setBackgroundColor(Color.TRANSPARENT);
               btnWednesday.setBackgroundColor(Color.TRANSPARENT);
               btnThursday.setBackgroundColor(Color.TRANSPARENT);
               btnFriday.setBackgroundColor(Color.TRANSPARENT);
               btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnMonday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(getColor(R.color.day_selected));
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnTuesday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(getColor(R.color.day_selected));
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnWednesday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(getColor(R.color.day_selected));
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnThursday.setOnClickListener(view-> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(getColor(R.color.day_selected));
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnFriday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(getColor(R.color.day_selected));
                btnSaturday.setBackgroundColor(Color.TRANSPARENT);
        });

        btnSaturday.setOnClickListener(view -> {
                btnSunday.setBackgroundColor(Color.TRANSPARENT);
                btnMonday.setBackgroundColor(Color.TRANSPARENT);
                btnTuesday.setBackgroundColor(Color.TRANSPARENT);
                btnWednesday.setBackgroundColor(Color.TRANSPARENT);
                btnThursday.setBackgroundColor(Color.TRANSPARENT);
                btnFriday.setBackgroundColor(Color.TRANSPARENT);
                btnSaturday.setBackgroundColor(getColor(R.color.day_selected));
        });
        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAlarm();
                Log.d("Black", minutePicker.getValue() + "");
                addAlarm.dismiss();
            }
        });

        //backToManage.setOnClickListener(v -> finish());

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

    public void addNewAlarm(){

        //Get Parent Elements
        CardView alarm = findViewById(R.id.cvAlarmSchedule);
        TextView alarmTime = alarm.findViewById(R.id.tvAssignedTime);
        SwitchCompat scheduleSetter = alarm.findViewById(R.id.scheduleSwitch);
        RelativeLayout alarmContainer = alarm.findViewById(R.id.rlAlarmContainer);

        // Create new views
        CardView nextAlarmBox = new CardView(this);
        RelativeLayout newAlarmContainer = new RelativeLayout(this);
        TextView nextAlarmTime = new TextView(this);
        SwitchCompat newSwitch = new SwitchCompat(this);


        //Set new Element's Unique ID (I think.....)
        finalAlarmID = getResources().getIdentifier(alarmId, "string", getPackageName());
        finalSwitchID = getResources().getIdentifier(switchID, "string", getPackageName());


        // Copy properties from existing views
        nextAlarmBox.setLayoutParams(alarm.getLayoutParams());
        nextAlarmBox.setCardBackgroundColor(alarm.getCardBackgroundColor());
        nextAlarmBox.setRadius(alarm.getRadius());
        nextAlarmTime.setLayoutParams(alarmTime.getLayoutParams());
        nextAlarmTime.setTextColor(alarmTime.getTextColors());
        //Sample Time
        nextAlarmTime.setText("00:00");
        nextAlarmTime.setTextSize(30);
        nextAlarmTime.setTypeface(alarmTime.getTypeface());

        //Relative Layout inside the Cardview
        newAlarmContainer.setLayoutParams(alarmContainer.getLayoutParams());
        newAlarmContainer.setPadding(30, 0, 30,0);

        //Switch
        newSwitch.setLayoutParams(scheduleSetter.getLayoutParams());

        //Set switch drawables to context only to prevent change of color with other switches
        newSwitch.setThumbDrawable(ContextCompat.getDrawable(this, R.drawable.thumb));
        newSwitch.setTrackDrawable(ContextCompat.getDrawable(this, R.drawable.track));

        // Add views to their respective parent
        alarmCount++;
        newSwitch.setId(finalSwitchID + alarmCount);
        ViewGroup parentLayout = (ViewGroup) alarm.getParent();
        int index = parentLayout.indexOfChild(alarm);
        newAlarmContainer.addView(nextAlarmTime);
        newAlarmContainer.addView(newSwitch);
        nextAlarmBox.addView(newAlarmContainer);
        nextAlarmBox.setId(finalAlarmID + alarmCount);
        parentLayout.addView(nextAlarmBox, index + 1);



    }




}