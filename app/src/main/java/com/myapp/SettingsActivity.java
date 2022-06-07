package com.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress = null;
    private String username = null;
    private String pin = null;

    private Button btnSelect;
    private Button btnCleanData;
    private Button btnSaveData;
    private Button btnBack;

    private TextView txtViewDeviceName;
    private TextView txtViewDeviceAddress;
    private TextView txtViewCredentialsStatus;

    private EditText txtEditUsername;
    private EditText txtEditPin;

    public static final String SHARED_PREFS = "sharedPrefs" ;
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_ADDRESS = "deviceAddress";
    public static final String USERNAME = "username";
    public static final String PIN = "pin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        initView();

        getDataPreferences();

        displayTextViewsForBtDevice();

        displayCredentialsView();


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {goBack();}
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectBtDevice();}
        });

        btnCleanData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {cleanPreferencesData();}
        });

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {savePreferencesData();}
        });
    }

    private void initView() {
        btnBack = findViewById(R.id.btnSettingsBack);
        btnSelect = findViewById(R.id.btnSelectDevice);
        btnCleanData = findViewById(R.id.btnCleanData);
        btnSaveData = findViewById(R.id.btnSaveData);

        txtViewDeviceName = findViewById(R.id.txtViewName);
        txtViewDeviceAddress = findViewById(R.id.txtViewAddress);
        txtViewCredentialsStatus = findViewById(R.id.txtViewCredentialsStatus);

        txtEditUsername = findViewById(R.id.editTxtUsername);
        txtEditPin = findViewById(R.id.editTxtPin);
    }


    private void getDataPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        deviceName = sharedPreferences.getString(DEVICE_NAME, null);
        deviceAddress = sharedPreferences.getString(DEVICE_ADDRESS, null);
        username = sharedPreferences.getString(USERNAME, null);
        pin = sharedPreferences.getString(PIN, null);
    }

    private void displayTextViewsForBtDevice() {
        if(deviceName != null && deviceAddress != null) {
            txtViewDeviceName.setText(deviceName);
            txtViewDeviceAddress.setText(deviceAddress);
        } else {
            txtViewDeviceName.setText("None");
            txtViewDeviceAddress.setText("None");
        }
    }

    private void displayCredentialsView() {
        if(username != null && pin != null) {
            txtEditUsername.setText(username);
            txtViewCredentialsStatus.setText("Credentials are stored.");
        } else {
            txtViewCredentialsStatus.setText("No credentials are stored");
        }
    }


    private void goBack() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void selectBtDevice() {
        Intent intent = new Intent(SettingsActivity.this, SelectDeviceActivity.class);
        startActivity(intent);
    }

    private void savePreferencesData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        username = txtEditUsername.getText().toString();
        pin = txtEditPin.getText().toString();
        txtEditPin.setText("");

        editor.putString(USERNAME, username);
        editor.putString(PIN, pin);
        editor.apply();
        refreshActivity();
    }

    private void cleanPreferencesData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        refreshActivity();
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}