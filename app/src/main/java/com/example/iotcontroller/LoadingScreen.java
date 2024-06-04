package com.example.iotcontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingScreen extends AppCompatActivity {

    private BroadcastReceiver loadingCompleteReceiver;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

// Register the receiver to listen for loading complete broadcasts
        loadingCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle the loading complete event
                Intent successIntent = new Intent(LoadingScreen.this, SuccessScreen.class);
                startActivity(successIntent);
                Log.d("LoadScreen", "A" + loadingCompleteReceiver);
                finish();
            }
        };

        IntentFilter filter = new IntentFilter("com.example.iotcontroller.LOADING_COMPLETE");
        registerReceiver(loadingCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // Get the delay from the intent
        int delayMillis = getIntent().getIntExtra("DELAY", 5000);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingScreen.this, SuccessScreen.class);
            startActivity(intent);
            finish();
        }, delayMillis);
    }

    @Override
    public void onBackPressed() {
//        Toast.makeText(this, "No!", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(loadingCompleteReceiver);
    }

}

