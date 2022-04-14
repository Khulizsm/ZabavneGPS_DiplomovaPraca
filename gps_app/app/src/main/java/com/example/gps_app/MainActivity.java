package com.example.gps_app;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_playerMode, btn_editorMode, btn_describe, btn_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        setButtons();
        setButtonListeners();

        final Handler gps_enable = new Handler();
        gps_enable.postDelayed(new Runnable() {
            @Override
            public void run() {
                locationEnabled();
                gps_enable.postDelayed(this, 10000);
            }
        }, 10000);

    }

    private void setButtons() {
        btn_playerMode = findViewById(R.id.btn_MainPagePlayerMode);
        btn_playerMode.setText(R.string.playerMode);

        btn_editorMode = findViewById(R.id.btn_MainPageCreativeMode);
        btn_editorMode.setText(R.string.editorMode);

        btn_describe = findViewById(R.id.btn_MainPageApplicationDescribe);
        btn_describe.setText(R.string.applicationDescribe);

        btn_exit = findViewById(R.id.btn_MainPageExit);
        btn_exit.setText(R.string.exit);

        btn_playerMode.setClickable(false);
        btn_editorMode.setClickable(false);
    }

    private void setButtonListeners(){
        btn_playerMode.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, MainPlayerActivity.class);
            startActivity(i);
        });

        btn_editorMode.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, MainEditorActivity.class);
            startActivity(i);
        });

        btn_describe.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, DescriptionActivity.class);
            startActivity(i);
        });

        btn_exit.setOnClickListener(view -> {
            finish();
            System.exit(0);
        });
    }


    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.turnOnGps)
                    .setPositiveButton(R.string.Settings, (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.notTurnOnGps, null)
                    .show();
        } else {
            if (!btn_editorMode.isClickable() && !btn_playerMode.isClickable()) {
                btn_playerMode.setClickable(true);
                btn_editorMode.setClickable(true);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        locationEnabled();
    }
}