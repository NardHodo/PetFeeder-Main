package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

public class Alarm_Settings extends AppCompatActivity {

    MaterialButton btnSaveChanges, btnDeleteAlarm;
    MaterialButton btnDiscard = findViewById(R.id.btnDiscardEdit);
    int selectedId = getIntent().getIntExtra("selectedId", -1);
    CardView selectedAlarmBox = findViewById(selectedId);
    ViewGroup parentLayout = (ViewGroup) selectedAlarmBox.getParent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);

        btnDiscard = findViewById(R.id.btnDiscardEdit);
        btnDiscard.setOnClickListener(v -> deleteAlarm());
    }

    public void deleteAlarm(){
        parentLayout.removeView(selectedAlarmBox);
        finish();
    }

}