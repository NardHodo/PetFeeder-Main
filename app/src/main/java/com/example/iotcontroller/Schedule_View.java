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
import java.util.Objects;


public class Schedule_View extends AppCompatActivity {

    private BottomSheetDialog addAlarm;
    private Button btnCancelAlarmAdd, btnConfirmAlarmAdd;
    private MaterialButton btnSunday, btnMonday, btnTuesday, btnWednesday, btnThursday, btnFriday, btnSaturday;
    private NumberPicker hourPicker, minPicker, amORpmPicker;
    private int selectedHour = 1, selectedMinute = 1, selectedMeridiem = 1;

    private RelativeLayout scheduleParent;
    private ArrayList<String> days, alarms = new ArrayList<String>();
    ImageButton  btnAddAlarm;
    String[] time = {"AM", "PM"};
    Button btnBackToManage;
    CardView alarmBox;
    TextView alarmTime, alarmDay;
    SwitchCompat alarmSwitch;
    final String splitter = ";";
    String alarmDatas = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);
        initializeElements();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            alarmDatas = extras.getString("Alarm");
            Log.d("CONTS", alarmDatas);
            addNewAlarm(true);
        }

    }


    public void initializeElements(){

        alarmBox = findViewById(R.id.cvAlarmSchedule);
        btnBackToManage = findViewById(R.id.btnBackButton);
        btnAddAlarm = findViewById(R.id.btnAddAlarm);
        scheduleParent = findViewById(R.id.scheduleParent);

        btnAddAlarm.setOnClickListener(v -> displayAlarmEditor());
        btnBackToManage.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK, new Intent().putExtra("alarms", alarms));
            finish();
            alarms = new ArrayList<String>();
        });
        addAlarm = new BottomSheetDialog(this);
        days = new ArrayList<>();



    }

    void displayAlarmEditor(){
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_alarm_feature, null);
        addAlarm.setContentView(contentView);
        addAlarm.setCancelable(true);
        Objects.requireNonNull(addAlarm.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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

            btnSunday.setOnClickListener(v -> addDay("Sun", btnSunday));
            btnMonday.setOnClickListener(v -> addDay("Mon", btnMonday));
            btnTuesday.setOnClickListener(v -> addDay("Tue", btnTuesday));
            btnWednesday.setOnClickListener(v -> addDay("Wed", btnWednesday));
            btnThursday.setOnClickListener(v -> addDay("Thu", btnThursday));
            btnFriday.setOnClickListener(v -> addDay("Fri", btnFriday));
            btnSaturday.setOnClickListener(v -> addDay("Sat", btnSaturday));

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
            assert minPicker != null;
            minPicker.setMinValue(00);
            minPicker.setMaxValue(59);
            minPicker.setFormatter(new NumberPicker.Formatter() {
                @SuppressLint("DefaultLocale")
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
            assert amORpmPicker != null;
            amORpmPicker.setMinValue(0);
            amORpmPicker.setMaxValue(time.length - 1);
            amORpmPicker.setDisplayedValues(time);

            btnConfirmAlarmAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewAlarm(false);
                }
            });

        }
    }

    @SuppressLint("SetTextI18n")
    void addNewAlarm(boolean isTrue) {
        if(!isTrue) {
            boolean[] isOn = {false};
            LayoutInflater inflater = LayoutInflater.from(this);
            View cardViewLayout = inflater.inflate(R.layout.cardview_copy, null);
            //Get the textview from the duplicate
            TextView duplicateTime = cardViewLayout.findViewById(R.id.tvAssignedTimeCopy);
            TextView duplicateDay = cardViewLayout.findViewById(R.id.tvAssignedDayCopy);
            ImageButton duplicateDelete = cardViewLayout.findViewById(R.id.btnDeleteAlarm);
            SwitchCompat switchMe = cardViewLayout.findViewById(R.id.scheduleSwitchCopy);

            //Warning Dialog for Alarm Deletion
            Dialog alarmWarning = new Dialog(Schedule_View.this);
            alarmWarning.setContentView(R.layout.alarm_delete_warning);
            alarmWarning.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            alarmWarning.setCancelable(false);

            //Warning Dialog Buttons
            Button btnCancelAlarmDeletion = alarmWarning.findViewById(R.id.btnCancelAlarmDeletion);
            Button btnConfirmAlarmDeletion = alarmWarning.findViewById(R.id.btnConfirmAlarmDeletion);

            duplicateDelete.setOnClickListener(v -> alarmWarning.show());
            btnCancelAlarmDeletion.setOnClickListener(v -> alarmWarning.dismiss());
            //CardView deletion function
            btnConfirmAlarmDeletion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewManager) cardViewLayout.getParent()).removeView(cardViewLayout);
                    String tempoRemover = duplicateTime.getText().toString();
                    tempoRemover = tempoRemover.replace(";", ":");
                    alarms.remove(tempoRemover);
                    for (String x : alarms) {
                        Log.d("COCAINE", x);
                    }
                    alarmWarning.dismiss();
                }
            });
            //On off listener
            switchMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Switches", isOn[0] + "");
                    alarms.contains(duplicateTime.getText());
                    String content = duplicateTime.getText() + "";
                    String[] newContent = content.split(" ");
                    int indexOfContent = alarms.indexOf(newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                    isOn[0] = !isOn[0];
                    alarms.set(indexOfContent, newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                    Log.d("COCAINE", newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                }
            });

            selectedMeridiem = amORpmPicker.getValue();

            duplicateTime.setText(getSelectedHour() + ":" + getSelectedMinute() + " " + ((selectedMeridiem == 0) ? "AM" : "PM"));
            duplicateDay.setText(getFormattedDays(days.toArray(new String[0])));

            //Add the cardview to the layout
            LinearLayout alarmScrollable = findViewById(R.id.svAlarmScrollable);


            if (days.isEmpty()) {
                Toast.makeText(Schedule_View.this, "Please Select At Least One Day", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < alarms.size(); i++) {
                    String[] splitContent = alarms.get(i).split(splitter);
                    String combine = splitContent[0] + splitter + splitContent[1] + splitter + splitContent[2];
                    if (combine.equals(getSelectedHour() + splitter + getSelectedMinute() + splitter + selectedMeridiem)) {
                        Toast.makeText(Schedule_View.this, "Alarm already exists", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                alarmScrollable.addView(cardViewLayout);
                //Adds data to be sent to ESP8266
                alarms.add((!alarms.isEmpty()) ? alarms.size() : 0, getSelectedHour() + splitter + getSelectedMinute() + splitter + selectedMeridiem + ";0");
                addAlarm.dismiss();
                days.clear();
            }

        } else if (isTrue && alarmDatas != null) {
            String[] temporary = alarmDatas.split(">");
            for (int j = 0; j < temporary.length - 1; j++) {
                String[] mirediemSplitter = temporary[j].split("-");
                String[] temporaryContent = mirediemSplitter[0].split(":");
                Log.d("CONTS", temporaryContent[0]);

                String hourFormatted = (Integer.parseInt(temporaryContent[0]) < 10)?"0"+temporaryContent[0]:temporaryContent[0];
                String minuteFormatted = temporaryContent[1];
                int mirediem = (mirediemSplitter[1].equals("PM"))?1:0;

                boolean[] isOn = {false};
                LayoutInflater inflater = LayoutInflater.from(this);
                View cardViewLayout = inflater.inflate(R.layout.cardview_copy, null);
                //Get the textview from the duplicate
                TextView duplicateTime = cardViewLayout.findViewById(R.id.tvAssignedTimeCopy);
                TextView duplicateDay = cardViewLayout.findViewById(R.id.tvAssignedDayCopy);
                ImageButton duplicateDelete = cardViewLayout.findViewById(R.id.btnDeleteAlarm);
                SwitchCompat switchMe = cardViewLayout.findViewById(R.id.scheduleSwitchCopy);

                //Warning Dialog for Alarm Deletion
                Dialog alarmWarning = new Dialog(Schedule_View.this);
                alarmWarning.setContentView(R.layout.alarm_delete_warning);
                alarmWarning.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                alarmWarning.setCancelable(false);

                //Warning Dialog Buttons
                Button btnCancelAlarmDeletion = alarmWarning.findViewById(R.id.btnCancelAlarmDeletion);
                Button btnConfirmAlarmDeletion = alarmWarning.findViewById(R.id.btnConfirmAlarmDeletion);

                duplicateDelete.setOnClickListener(v -> alarmWarning.show());
                btnCancelAlarmDeletion.setOnClickListener(v -> alarmWarning.dismiss());
                //CardView deletion function
                btnConfirmAlarmDeletion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ViewManager) cardViewLayout.getParent()).removeView(cardViewLayout);
                        String tempoRemover = duplicateTime.getText().toString();
                        tempoRemover = tempoRemover.replace(":", ";");
                        String[] temp = tempoRemover.split(" ");
                        tempoRemover = temp[0];
                        if(temp[1].equals("PM")){
                            tempoRemover += ";1";
                        }else{
                            tempoRemover += ";0";
                        }
                        alarms.remove(tempoRemover+";1");
                        alarms.remove(tempoRemover+";0");
                        Log.d("COCAINE", tempoRemover);
                        for (String x : alarms) {

                            Log.d("COCAINE", x + "ASL");
                        }
                        alarmWarning.dismiss();
                    }
                });
                //On off listener
                switchMe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Switches", isOn[0] + "");
                        alarms.contains(duplicateTime.getText());
                        String content = duplicateTime.getText() + "";
                        String[] newContent = content.split(" ");
                        int indexOfContent = alarms.indexOf(newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                        isOn[0] = !isOn[0];
                        alarms.set(indexOfContent, newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                        Log.d("COCAINE", newContent[0].replace(":", ";") + splitter + (newContent[1].equals("PM") ? "1" : "0") + splitter + (isOn[0] ? "1" : "0"));
                    }
                });

                duplicateTime.setText(hourFormatted + ":" + minuteFormatted + " " + ((mirediem == 0) ? "AM" : "PM"));
                duplicateDay.setText(getFormattedDays(days.toArray(new String[0])));

                //Add the cardview to the layout
                LinearLayout alarmScrollable = findViewById(R.id.svAlarmScrollable);



                for (int i = 0; i < alarms.size(); i++) {
                    String[] splitContent = alarms.get(i).split(splitter);
                    String combine = splitContent[0] + splitter + splitContent[1] + splitter + splitContent[2];
                    if (combine.equals(hourFormatted + splitter + minuteFormatted + splitter + mirediem)) {
                        Toast.makeText(Schedule_View.this, "Alarm already exists", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                alarmScrollable.addView(cardViewLayout);
                //Adds data to be sent to ESP8266
                alarms.add((!alarms.isEmpty()) ? alarms.size() : 0, hourFormatted + splitter + minuteFormatted + splitter + mirediem + ";0");
                addAlarm.dismiss();
                days.clear();

            }
        }
    }


    public String getSelectedMinute() {
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
