/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Buttons
    private Button btnSettings;
    private Button btnConnectDisconnect;
    private Button btnTmpSend; //TODO: In future needs removed

    // Other view components
    private Toolbar toolbar;
    private TextView txtViewRPiReply;
    private EditText editTxtTmpMsg; //TODO: In future needs removed

    // Bluetooth device details
    private String deviceName;
    private String deviceAddress;

    // Credentials
    private String username;
    private String pin;

    private boolean connectedToDevice = false;


    // Preferences variables
    public static final String PREFS_NAME = "sharedPrefs" ;
    public static final String PREFS_DEVICE_NAME = "deviceName";
    public static final String PREFS_DEVICE_ADDRESS = "deviceAddress";
    public static final String PREFS_USERNAME = "username";
    public static final String PREFS_PIN = "pin";

    // Variables for bluetooth connections
    public static Handler handler;
    private ClientBluetoothThread clientBluetoothThread;
    public final static int CONNECTING_STATUS = 1;
    public final static int MESSAGE_READ = 2;


    /**
     * Activity main function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); // Hide the activity toolbar

        initView(); // Initialise the view components
        getDataPreferences(); // Get data preferences
        setupButtonsClickListeners(); // Setup click listeners for all buttons

        // Disable vital view components
        btnConnectDisconnect.setEnabled(false);
        txtViewRPiReply.setText(""); //TODO: In future needs removed
        btnTmpSend.setEnabled(false); //TODO: In future needs removed
        editTxtTmpMsg.setEnabled(false); //TODO: In future needs removed


        // Check if all required data is set and enable the connect button
        if(!checkPrefsData()) {
            toolbar.setSubtitle("Settings Incomplete");

        } else {
            toolbar.setSubtitle("Ready to connect");
            btnConnectDisconnect.setEnabled(true);
        }
    }

    /**
     * Initialise the view components
     */
    private void initView() {
        btnSettings = findViewById(R.id.btnSettings);
        btnConnectDisconnect = findViewById(R.id.btnConnectDisconnect);
        btnTmpSend = findViewById(R.id.btnTmpSend); //TODO: In future needs removed
        toolbar = findViewById(R.id.toolbar);
        txtViewRPiReply = findViewById(R.id.txtViewRPiReply);
        editTxtTmpMsg = findViewById(R.id.editTxtMsg); //TODO: In future needs removed
    }

    /**
     * Get preferences data
     */
    private void getDataPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        deviceName = sharedPreferences.getString(PREFS_DEVICE_NAME, null);
        deviceAddress = sharedPreferences.getString(PREFS_DEVICE_ADDRESS, null);
        username = sharedPreferences.getString(PREFS_USERNAME, null);
        pin = sharedPreferences.getString(PREFS_PIN, null);
    }

    /**
     * Setup the click listeners for all buttons
     */
    private void setupButtonsClickListeners() {
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSettings();}
        });

        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectDisconnect();}
        });

        btnTmpSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg();}
        });
    }

    private boolean checkPrefsData() {
        if(deviceName != null && deviceAddress != null && username != null && pin != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handler for receiving bluetooth messages from BT device.
     * Reference: https://github.com/Hype47/droiduino/tree/master/DroiduinoBluetoothConnection
     */
    private void mainLoop() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){

                            case 1:
                                eventConnected();
                                break;

                            case -1:
                                eventDisconnected();
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String rpiMsg = msg.obj.toString(); // Read message from Arduino
                        txtViewRPiReply.setText("Reply: " + rpiMsg);
                        break;
                }
            }
        };
    }

    /**
     * Go to settings page
     */
    private void goToSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Function for the connect/disconnect button
     */
    private void connectDisconnect() {
        // Check if there is already a connection
        if(!connectedToDevice) {
            toolbar.setSubtitle("Connecting to " + deviceName + " ...");
            clientBluetoothThread = new ClientBluetoothThread(deviceAddress);
            mainLoop();
            clientBluetoothThread.start();
            btnConnectDisconnect.setEnabled(false);

        } else {
            btnConnectDisconnect.setEnabled(false);
            clientBluetoothThread.sendMessage("disconnect");
            clientBluetoothThread.keepRunning = false;
            try {clientBluetoothThread.join();} catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    /**
     * Function for the send button
     * TODO: This needs removed in future
     */
    private void sendMsg() {
        String msg = editTxtTmpMsg.getText().toString();
        editTxtTmpMsg.setText("");
        clientBluetoothThread.sendMessage(msg);
    }

    /**
     * Run this function when a device is connected
     */
    private void eventConnected() {
        toolbar.setSubtitle("Connected to " + deviceName);
        btnConnectDisconnect.setText("disconnect");
        btnConnectDisconnect.setEnabled(true);
        btnTmpSend.setEnabled(true); //TODO: In future needs removed
        editTxtTmpMsg.setEnabled(true); //TODO: In future needs removed
    }

    /**
     * Run this function when a device is disconnected.
     */
    private void eventDisconnected() {
        toolbar.setSubtitle("Ready to connect");
        btnConnectDisconnect.setText("connect");
        btnConnectDisconnect.setEnabled(true);
        txtViewRPiReply.setText(""); //TODO: In future needs removed
        btnTmpSend.setEnabled(false); //TODO: In future needs removed
        editTxtTmpMsg.setEnabled(false); //TODO: In future needs removed
    }
}
