package com.example.iotcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        // Get the delay from the intent
        int delayMillis = getIntent().getIntExtra("DELAY", 5000);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingScreen.this, SuccessScreen.class);
            startActivity(intent);
            finish();
        }, delayMillis);
    }
}

