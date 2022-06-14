/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    //Buttons
    private Button btnSelect;
    private Button btnCleanData;
    private Button btnSaveCredentials;
    private Button btnBack;
    private Button btnSaveSecretKey;

    // Other view components
    private TextView txtViewDeviceName;
    private TextView txtViewDeviceAddress;
    private TextView txtViewCredentialsStatus;
    private TextView txtViewSecretKeyStatus;
    private EditText txtEditUsername;
    private EditText txtEditPin;
    private EditText txtEditSecretKey;

    // Other variables
    private String deviceName = null;
    private String deviceAddress = null;
    private String username = null;
    private String pin = null;
    private String secretKey = null;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        initView();
        initDataPreferences();

        getDataPreferences();
        setupButtonsClickListeners();
        displayTextViewsForBtDevice();
        displayCredentialsView();
        displaySecretKeyStatus();
    }

    private void initView() {
        btnBack = findViewById(R.id.btnSettingsBack);
        btnSelect = findViewById(R.id.btnSelectDevice);
        btnCleanData = findViewById(R.id.btnCleanData);
        btnSaveCredentials = findViewById(R.id.btnSaveData);
        btnSaveSecretKey = findViewById(R.id.btnSaveKey);

        txtViewDeviceName = findViewById(R.id.txtViewName);
        txtViewDeviceAddress = findViewById(R.id.txtViewAddress);
        txtViewCredentialsStatus = findViewById(R.id.txtViewCredentialsStatus);
        txtViewSecretKeyStatus = findViewById(R.id.txtViewSecretKeyStatus);

        txtEditUsername = findViewById(R.id.editTxtUsername);
        txtEditPin = findViewById(R.id.editTxtPin);
        txtEditSecretKey = findViewById(R.id.editTxtSecretKey);
    }

    private void setupButtonsClickListeners() {
        btnBack.setOnClickListener(view -> goBack());
        btnSelect.setOnClickListener(view -> selectBtDevice());
        btnCleanData.setOnClickListener(view -> cleanPreferencesData());
        btnSaveCredentials.setOnClickListener(view -> saveCredentials());
        btnSaveSecretKey.setOnClickListener(view -> saveSecretKey());
    }

    private void initDataPreferences() {
        try {
            String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    MainActivity.PREFS_NAME,
                    masterKey,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (Exception e) {
            displayNotification("Failed to get data preferences");
            e.printStackTrace();
        }
    }

    private void getDataPreferences() {
        deviceName = sharedPreferences.getString(MainActivity.PREFS_DEVICE_NAME, null);
        deviceAddress = sharedPreferences.getString(MainActivity.PREFS_DEVICE_ADDRESS, null);
        username = sharedPreferences.getString(MainActivity.PREFS_USERNAME, null);
        pin = sharedPreferences.getString(MainActivity.PREFS_PIN, null);
        secretKey = sharedPreferences.getString(MainActivity.PREFS_SECRET_KEY, null);
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

    @SuppressLint("SetTextI18n")
    private void displaySecretKeyStatus() {
        if(secretKey != null) {
            txtViewSecretKeyStatus.setText("Key stored.");
        } else {
            txtViewSecretKeyStatus.setText("No key stored.");
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

    private void saveCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        username = txtEditUsername.getText().toString();
        pin = txtEditPin.getText().toString();
        txtEditUsername.setText("");
        txtEditPin.setText("");
        editor.putString(MainActivity.PREFS_USERNAME, username);
        editor.putString(MainActivity.PREFS_PIN, pin);
        editor.apply();
        txtViewCredentialsStatus.setText("Credentials are stored.");
        displayNotification("Credentials saved.");
    }

    private void saveSecretKey() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        secretKey = txtEditSecretKey.getText().toString();
        txtEditSecretKey.setText("");
        editor.putString(MainActivity.PREFS_SECRET_KEY, secretKey);
        editor.apply();
        txtViewSecretKeyStatus.setText("Key stored.");
        displayNotification("Secret key saved.");
    }

    private void cleanPreferencesData() {
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