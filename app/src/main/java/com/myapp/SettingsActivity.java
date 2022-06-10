/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    //Buttons
    private Button btnSelect;
    private Button btnCleanData;
    private Button btnSaveData;
    private Button btnBack;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchFingerprint;

    // Other view components
    private TextView txtViewDeviceName;
    private TextView txtViewDeviceAddress;
    private TextView txtViewCredentialsStatus;
    private EditText txtEditUsername;
    private EditText txtEditPin;

    // Other variables
    private String deviceName = null;
    private String deviceAddress = null;
    private String username = null;
    private String pin = null;
    private Boolean useBiometric;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        initView();
        getDataPreferences();
        setupButtonsClickListeners();
        displayTextViewsForBtDevice();
        displayCredentialsView();
        switchFingerprint.setChecked(useBiometric);
    }

    private void initView() {
        btnBack = findViewById(R.id.btnSettingsBack);
        btnSelect = findViewById(R.id.btnSelectDevice);
        btnCleanData = findViewById(R.id.btnCleanData);
        btnSaveData = findViewById(R.id.btnSaveData);
        switchFingerprint = findViewById(R.id.switchFingerprint);
        txtViewDeviceName = findViewById(R.id.txtViewName);
        txtViewDeviceAddress = findViewById(R.id.txtViewAddress);
        txtViewCredentialsStatus = findViewById(R.id.txtViewCredentialsStatus);
        txtEditUsername = findViewById(R.id.editTxtUsername);
        txtEditPin = findViewById(R.id.editTxtPin);
    }

    private void setupButtonsClickListeners() {
        btnBack.setOnClickListener(view -> goBack());
        btnSelect.setOnClickListener(view -> selectBtDevice());
        btnCleanData.setOnClickListener(view -> cleanPreferencesData());
        btnSaveData.setOnClickListener(view -> saveCredentials());
        switchFingerprint.setOnClickListener(view -> enableDisableBiometric());
    }

    private void getDataPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        deviceName = sharedPreferences.getString(MainActivity.PREFS_DEVICE_NAME, null);
        deviceAddress = sharedPreferences.getString(MainActivity.PREFS_DEVICE_ADDRESS, null);
        username = sharedPreferences.getString(MainActivity.PREFS_USERNAME, null);
        pin = sharedPreferences.getString(MainActivity.PREFS_PIN, null);
        useBiometric = sharedPreferences.getBoolean(MainActivity.PREFS_USE_BIOMETRIC, false);
    }

    @SuppressLint("SetTextI18n")
    private void displayTextViewsForBtDevice() {
        if(deviceName != null && deviceAddress != null) {
            txtViewDeviceName.setText(deviceName);
            txtViewDeviceAddress.setText(deviceAddress);
        } else {
            txtViewDeviceName.setText("None");
            txtViewDeviceAddress.setText("None");
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayCredentialsView() {
        if(username != null && pin != null) {
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

    private void enableDisableBiometric() {
        useBiometric = switchFingerprint.isChecked();
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MainActivity.PREFS_USE_BIOMETRIC, useBiometric);
        editor.apply();

        if(useBiometric) { displayNotification("Biometric sensor enabled."); }
        else { displayNotification("Biometric sensor disabled."); }
    }

    private void saveCredentials() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        username = txtEditUsername.getText().toString();
        pin = txtEditPin.getText().toString();
        txtEditUsername.setText("");
        txtEditPin.setText("");
        editor.putString(MainActivity.PREFS_USERNAME, username);
        editor.putString(MainActivity.PREFS_PIN, pin);
        editor.apply();
        displayNotification("Credentials saved.");
    }

    private void cleanPreferencesData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        refreshActivity();
        displayNotification("Data was erased.");
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void displayNotification(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}