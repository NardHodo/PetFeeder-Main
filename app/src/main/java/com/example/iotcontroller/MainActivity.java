package com.example.iotcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button btnGREEN, btnBLUE, btnRED, btnManage, btnCancel;
    Dialog dispenseDialog;

//    LinearLayout dialog_box;

    private final OkHttpClient client = new OkHttpClient();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //region Helps the app to communicate with ESP8266
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        NetworkRequest request = builder.build();
        connManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                connManager.bindProcessToNetwork(network);
            }
        });
        //endregion


        //region Declaration
        btnBLUE = findViewById(R.id.btnBLUE);
        btnGREEN = findViewById(R.id.btnGREEN);
        btnRED = findViewById(R.id.btnRED);
        btnManage = findViewById(R.id.btnManage);
        //endregion


        btnManage.setBackgroundColor(ContextCompat.getColor(this, R.color.manage_button));
        btnManage.setTextColor(ContextCompat.getColor(this, R.color.black));


        //region Dialog Box Functionality
        dispenseDialog = new Dialog(MainActivity.this);
        dispenseDialog.setContentView(R.layout.activity_dispense_dialog_box);
        btnCancel = dispenseDialog.findViewById(R.id.btnCancelDispense);
        Objects.requireNonNull(dispenseDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dispenseDialog.setCancelable(false);
        //endregion



        btnCancel.setOnClickListener(view -> dispenseDialog.dismiss());

        btnManage.setOnClickListener(view ->
        {
            Intent viewSchedule = new Intent(MainActivity.this, Schedule_View.class);
            startActivity(viewSchedule);
        });
        //region ESP8266 Communication Functions
        btnRED.setOnClickListener(view -> sendCommand("red"));
        btnGREEN.setOnClickListener(view -> sendCommand("green"));
        btnBLUE.setOnClickListener(view -> sendCommand("blue"));
        //endregion
    }


    public void sendCommand(String cmd) {
        new Thread(() -> {
            String command = "http://192.168.4.1/" + cmd;
            Log.d("Command------------------------------------------", command);
            Request request = new Request.Builder().url(command).build();
            try {
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String myResponse = response.body().string();
                final String cleanResponse; // remove HTML tags
                cleanResponse = myResponse.replaceAll("<.*?>", "");
                cleanResponse.replace("\n", ""); // remove all new line characters
                cleanResponse.replace("\r", ""); // remove all carriage characters
                cleanResponse.replace(" ", ""); // removes all space characters
                cleanResponse.replace("\t", ""); // removes all tab characters
                cleanResponse.trim();
                Log.d("Response  = ", cleanResponse);

                runOnUiThread(() -> {
//                            txtRES.setText(cleanResponse);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


}