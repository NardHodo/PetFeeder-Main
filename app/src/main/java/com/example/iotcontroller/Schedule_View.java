package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shawnlin.numberpicker.NumberPicker;


import java.util.ArrayList;


public class Schedule_View extends AppCompatActivity {

    private BottomSheetDialog addAlarm;
    private Button btnCancelAlarmAdd, btnConfirmAlarmAdd;
    private MaterialButton btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;
    private NumberPicker hourPicker, minPicker, amORpmPicker;
    private int selectedHour = 1, selectedMinute = 1;
    private RelativeLayout scheduleParent;
    private ArrayList<String> days;
    ImageButton  btnAddAlarm, btnDeleteAlarm;
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
        btnBacktoManage = findViewById(R.id.btnBackButton);
        btnAddAlarm = findViewById(R.id.btnAddAlarm);
        btnAddAlarm.setOnClickListener(v -> displayAlarmEditor());
        btnBacktoManage.setOnClickListener(v -> finish());
        addAlarm = new BottomSheetDialog(this);
        scheduleParent = findViewById(R.id.scheduleParent);
        btnDeleteAlarm = findViewById(R.id.btnDeleteAlarm);
        btnDeleteAlarm.setOnClickListener(v -> toggleCheckboxVisibility());
        days = new ArrayList<>();



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

            //Day Buttons
            btnSunday = addAlarm.findViewById(R.id.Sunday);
            btnMonday = addAlarm.findViewById(R.id.btnMonday);
            btnTuesday = addAlarm.findViewById(R.id.btnTuesday);
            btnWednesday = addAlarm.findViewById(R.id.btnWednesday);
            btnThursday= addAlarm.findViewById(R.id.btnThursday);
            btnFriday = addAlarm.findViewById(R.id.btnFriday);
            btnSaturday = addAlarm.findViewById(R.id.btnSaturday);

            btnSunday.setOnClickListener(v -> addDay("Sunday", btnSunday));
            btnMonday.setOnClickListener(v -> addDay("Monday", btnMonday));
            btnTuesday.setOnClickListener(v -> addDay("Tuesday", btnTuesday));
            btnWednesday.setOnClickListener(v -> addDay("Wednesday", btnWednesday));
            btnThursday.setOnClickListener(v -> addDay("Thursday", btnThursday));
            btnFriday.setOnClickListener(v -> addDay("Friday", btnFriday));
            btnSaturday.setOnClickListener(v -> addDay("Saturday", btnSaturday));

            //
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

        LayoutInflater inflater = LayoutInflater.from(this);
        View cardViewLayout = inflater.inflate(R.layout.cardview_copy, null);
        //Get the textview from the duplicate
        TextView duplicateTime = cardViewLayout.findViewById(R.id.tvAssignedTimeCopy);
        TextView duplicateDay = cardViewLayout.findViewById(R.id.tvAssignedDayCopy);

        duplicateTime.setText(getSelectedHour() + ":" + getMinute() + " " + ((amORpmPicker.getValue() == 0)?"AM":"PM"));
        duplicateDay.setText(getFormattedDays(days.toArray(new String[0])));

        //Add the cardview to the layout
        LinearLayout alarmScrollable = findViewById(R.id.svAlarmScrollable);

        // Dismiss the addAlarm dialog or any other action you want to take
        //addAlarm.dismiss();
        //days.clear();

        if(days.isEmpty()){
            Toast.makeText(Schedule_View.this, "Please Select At Least One Day", Toast.LENGTH_LONG).show();
        } else{
            alarmScrollable.addView(cardViewLayout);
            addAlarm.dismiss();
            days.clear();
        }


    }


    public String getMinute() {
        String finalMinute = String.format("%02d", selectedMinute);
        return finalMinute;
    }

    public String getSelectedHour(){
        String finalHour = String.format("%02d", selectedHour);
        return finalHour;
    }

    public static String getFormattedDays(String[] days){
        StringBuilder format = new StringBuilder();

        for(int i = 0; i < days.length; i++){
            format.append(days[i]);
            if(i < days.length - 1) {
                format.append(", ");
            }
        }

        return format.toString();
    }

    public void addDay(String dayAssigned, Button selectedDay){
        boolean toggled = false;

        if (!toggled) {
            if (!days.contains(dayAssigned)) {
                days.add(dayAssigned);
                selectedDay.setBackgroundColor(getColor(R.color.disabled_color));

            }
        } else {
            if (days.contains(dayAssigned)) {
                days.remove(dayAssigned);
                selectedDay.setBackgroundColor(getColor(R.color.card_view_color));
            }
        }
    }

    private void toggleCheckboxVisibility(){
        for(int i = 0; i < scheduleParent.getChildCount(); i++){
            View view = scheduleParent.getChildAt(i);
            if (view instanceof CardView) {
                CardView cardView = (CardView) view;
                toggleCheckboxVisibilityInCardView(cardView);
            }
        }
    }

    private void toggleCheckboxVisibilityInCardView(CardView cardView) {
        // Find the linear layout within the CardView
        RelativeLayout linearLayout = cardView.findViewById(R.id.rlAlarmContainer);

        // Iterate through each child view (checkbox) in the linear layout
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                // Toggle visibility of checkboxes
                if (checkBox.getVisibility() == View.VISIBLE) {
                    checkBox.setVisibility(View.INVISIBLE);
                } else {
                    checkBox.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
