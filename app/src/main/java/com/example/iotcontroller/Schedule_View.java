package com.example.iotcontroller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shawnlin.numberpicker.NumberPicker;

import org.w3c.dom.Text;


public class Schedule_View extends AppCompatActivity {

    private BottomSheetDialog addAlarm;
    private Button btnCancelAlarmAdd, btnConfirmAlarmAdd;
    private NumberPicker hourPicker, minPicker, amORpmPicker;
    private int selectedHour = 0, selectedMinute = 0;
    ImageButton  btnAddAlarm;
    String[] time = {"AM", "PM"};
    Button btnBacktoManage;
    CardView alarmBox;
    TextView alarmTime, alarmDay;
    SwitchCompat alarmSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);
        initalizeElements();
    }


    public void initalizeElements(){
        alarmBox = findViewById(R.id.cvAlarmSchedule);
        alarmTime = findViewById(R.id.tvAssignedTime);
        alarmDay = findViewById(R.id.tvAssignedDay);
        alarmSwitch = findViewById(R.id.scheduleSwitch);
        btnBacktoManage = findViewById(R.id.btnBackButton);
        btnAddAlarm = findViewById(R.id.btnAddAlarm);
        btnAddAlarm.setOnClickListener(v -> displayAlarmEditor());
        btnBacktoManage.setOnClickListener(v -> finish());
        addAlarm = new BottomSheetDialog(this);



    }

    void displayAlarmEditor(){
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_alarm_feature, null);
        addAlarm.setContentView(contentView);
        addAlarm.setCancelable(true);
        addAlarm.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addAlarm.show();
        initializeAddAlarmElements(addAlarm);
    }


    void initializeAddAlarmElements(BottomSheetDialog addAlarm) {
        if (addAlarm != null) {
            hourPicker = addAlarm.findViewById(R.id.hourPicker);
            minPicker = addAlarm.findViewById(R.id.minPicker);
            amORpmPicker = addAlarm.findViewById(R.id.AM_PM_Picker);
            btnCancelAlarmAdd = addAlarm.findViewById(R.id.btnCancelAlarm);
            btnConfirmAlarmAdd = addAlarm.findViewById(R.id.btnAddAlarm);

            btnCancelAlarmAdd.setOnClickListener(v -> addAlarm.dismiss());

            hourPicker.setMinValue(1);
            hourPicker.setMaxValue(12);
            hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        selectedHour = newVal;
                }
            });

            minPicker = addAlarm.findViewById(R.id.minPicker);
            minPicker.setMinValue(00);
            minPicker.setMaxValue(59);
            minPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int value) {
                    return String.format("%02d", value);
                }
            });
            minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        selectedMinute = newVal;
                }
            });

            amORpmPicker = addAlarm.findViewById(R.id.AM_PM_Picker);
            amORpmPicker.setMinValue(0);
            amORpmPicker.setMaxValue(time.length - 1);
            amORpmPicker.setDisplayedValues(time);

            btnConfirmAlarmAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewAlarm();
                }
            });

        }
    }
    void addNewAlarm() {
        RelativeLayout scheduleLayout = findViewById(R.id.scheduleParent);
        LinearLayout alarmScrollable = scheduleLayout.findViewById(R.id.svAlarmScrollable);
        CardView parent = alarmScrollable.findViewById(R.id.cvAlarmSchedule);

        // Create a new instance of the NewAlarmBox class
        newAlarmBox newAlarm = new newAlarmBox(parent.getContext(), null, alarmScrollable);

        // Set the time and switch state for the new alarm
        newAlarm.setTime(getSelectedHour(), getMinute());
        newAlarm.setNewSwitch(false);

        // Add the new alarm box to the LinearLayout
        alarmScrollable.addView(newAlarm);

        // Dismiss the addAlarm dialog or any other action you want to take
        addAlarm.dismiss();
    }
    public String getMinute() {
        String finalMinute = String.format("%02d", selectedMinute);
        return finalMinute;
    }

    public String getSelectedHour(){
        String finalHour = String.format("%02d", selectedHour);
        return finalHour;
    }


}

class newAlarmBox extends CardView {

    private TextView newAlarmTime;
    private TextView newAlarmDay;
    private SwitchCompat newSwitch;

    public newAlarmBox(Context context, LinearLayout parentLayout) {
        super(context);
        initializeCardView(context, parentLayout);
    }

    public newAlarmBox(Context context, @Nullable AttributeSet attrs, LinearLayout parentLayout) {
        super(context, attrs);
        initializeCardView(context, parentLayout);
    }

    public newAlarmBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr, LinearLayout parentLayout) {
        super(context, attrs, defStyleAttr);
        initializeCardView(context, parentLayout);
    }

    private void initializeCardView(Context context, LinearLayout parentLayout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_schedule_view, this, true);

        newAlarmTime = findViewById(R.id.tvAssignedTime);
        newAlarmDay = findViewById(R.id.tvAssignedDay);
        newSwitch = findViewById(R.id.scheduleSwitch);
    }

    public void setTime(String hour, String minute) {
        newAlarmTime.setText(hour + ":" + minute);
    }

    public void setDay(String day) {
        newAlarmDay.setText(day);
    }

    public void setNewSwitch(boolean state) {
        newSwitch.setChecked(state);
    }

}