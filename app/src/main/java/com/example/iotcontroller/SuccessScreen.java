package com.example.iotcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SuccessScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sucess_screen);

        Button btnSuccess = findViewById(R.id.btnSuccess);

        btnSuccess.setOnClickListener(view -> {
            finish();
        });

    }
}