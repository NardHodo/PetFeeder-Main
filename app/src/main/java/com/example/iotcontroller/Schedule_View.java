package com.example.iotcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Schedule_View extends AppCompatActivity {

    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);


        //Back Button Declaration
        btnBack = findViewById(R.id.btnBackButton);
        //Back Button Function
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.exit_anim, R.anim.no_animation); // Use a blank no_animation for the entering activity

                finish();


            }
        });


    }


}