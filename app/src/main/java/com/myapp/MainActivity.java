/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Buttons
    private Button btnSettings;
    private Button btnConnectDisconnect;
    private Button btnLoginLogout;
    private Button btnSwitch;

    // Other view components
    private Toolbar toolbar;
    private TextView txtViewRPiReply;

    // Preferences variables
    public static final String PREFS_NAME = "sharedPrefs" ;
    public static final String PREFS_DEVICE_NAME = "deviceName";
    public static final String PREFS_DEVICE_ADDRESS = "deviceAddress";
    public static final String PREFS_USERNAME = "username";
    public static final String PREFS_PIN = "pin";
    public static final String PREFS_SECRET_KEY= "secretKey";

    // Variables for bluetooth connections
    public static Handler handler;
    private ClientBluetoothThread clientBluetoothThread;
    public final static int CONNECTING_STATUS = 1;
    public final static int MESSAGE_READ = 2;

    // Biometric sensor related variables
    private CancellationSignal cancellationSignal = null;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    // Other variables
    private String deviceName;
    private String deviceAddress;
    private String username;
    private String pin;
    private String secretKey;
    private boolean connectedToDevice = false;
    private boolean loggedIn = false;

    /**
     * Activity main function
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); // Hide the activity toolbar

        initView(); // Initialise the view components
        getDataPreferences(); // Get data preferences
        setupButtonsClickListeners(); // Setup click listeners for all buttons
        initBiometric(); // Initialise biometric sensor

        // Disable vital view components
        btnConnectDisconnect.setEnabled(false);
        btnLoginLogout.setEnabled(false);
        btnSwitch.setEnabled(false);
        txtViewRPiReply.setText("");

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
        btnLoginLogout = findViewById(R.id.btnLoginLogout);
        btnSwitch = findViewById(R.id.btnSwitch);
        toolbar = findViewById(R.id.toolbar);
        txtViewRPiReply = findViewById(R.id.txtViewRPiReply);
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
        secretKey = sharedPreferences.getString(PREFS_SECRET_KEY, null);
    }

    /**
     * Setup the click listeners for all buttons
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void setupButtonsClickListeners() {
        btnSettings.setOnClickListener(view -> goToSettings());
        btnConnectDisconnect.setOnClickListener(view -> clickConnectDisconnect());
        btnLoginLogout.setOnClickListener(view -> clickLoginLogout());
        btnSwitch.setOnClickListener(view -> clickRelay());
    }


    /**
     * Biometric sensor initializer
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void initBiometric() {
        authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                displayNotification("Authentication Error : " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                clientBluetoothThread.sendMessage(secretKey,"login-"+username+","+pin);
                displayNotification("Authentication Succeeded");
            }
        };
    }

    /**
     * Display biometric authentication
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void displayBiometric() {
        BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(
                getApplicationContext())
                .setTitle("Login Authentication")
                .setSubtitle("Fingerprint Sensor")
                .setDescription("Use your fingerprint to login")
                .setNegativeButton("Cancel", getMainExecutor(), (dialogInterface, i) -> displayNotification("Authentication Cancelled")).build();

        // start the authenticationCallback in
        // mainExecutor
        biometricPrompt.authenticate(
                getCancellationSignal(),
                getMainExecutor(),
                authenticationCallback);
    }


    private boolean checkPrefsData() {
        return deviceName != null
                && deviceAddress != null
                && username != null
                && pin != null
                && secretKey != null;
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
                        switch(msg.arg1) {
                            case 1:
                                stateConnected();
                                btnConnectDisconnect.setEnabled(true);
                                break;
                            case -1:
                                stateDisconnected();
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String rpiMsg = msg.obj.toString(); // Read message from Arduino
                        rpiMsg = rpiMsg.replaceAll("[\\n\\t ]", ""); //remove newline
                        rpiMsg = Encryption.DecryptMessage(secretKey, rpiMsg);
                        processReplyMsg(rpiMsg);
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
    private void clickConnectDisconnect() {
        // Check if there is already a connection
        if(!connectedToDevice) {
            toolbar.setSubtitle("Connecting to " + deviceName + " ...");
            btnConnectDisconnect.setEnabled(false);
            clientBluetoothThread = new ClientBluetoothThread(deviceAddress);
            mainLoop();
            clientBluetoothThread.start();
        } else {
            stateDisconnected();
            clientBluetoothThread.sendMessage(secretKey, "disconnect");
            clientBluetoothThread.keepRunning = false;
            try {clientBluetoothThread.join();} catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    /**
     * Function for button login/logout
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void clickLoginLogout() {
        if(!loggedIn) {
            displayBiometric();
        }
        else {

            clientBluetoothThread.sendMessage(secretKey,"logout");
        }
    }

    /**
     * Function for sending command to lock/unlock the switch
     */
    private void clickRelay() { clientBluetoothThread.sendMessage(secretKey, "switch"); }

    /**
     * Processes the messages received from the raspberry pi
     * @param msg String
     */
    private void processReplyMsg(String msg) {
        msg = msg.replaceAll("[\\n\\t]","");
        txtViewRPiReply.setText(String.format("Reply: %s", msg));
        switch (msg) {
            case "logged out":
                stateLoggedOut();
                break;
            case "logged in":
                stateLoggedIn();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void stateConnected() {
        connectedToDevice = true;
        loggedIn = false;
        toolbar.setSubtitle("Connected.");
        btnConnectDisconnect.setText("disconnect");
        btnLoginLogout.setEnabled(true);
        btnSwitch.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void stateDisconnected() {
        connectedToDevice = false;
        stateLoggedOut();
        toolbar.setSubtitle("Disconnected.");
        btnConnectDisconnect.setText("connect");
        btnLoginLogout.setEnabled(false);
    }

    /**
     * Change some view components based on logged in but still connected.
     */
    @SuppressLint("SetTextI18n")
    private void stateLoggedIn() {
        loggedIn = true;
        btnLoginLogout.setText("logout");
        btnSwitch.setEnabled(true);
    }

    /**
     * Change some view components based on logged out state, but still connected.
     */
    @SuppressLint("SetTextI18n")
    private void stateLoggedOut() {
        loggedIn = false;
        btnLoginLogout.setText("login");
        btnSwitch.setEnabled(false);
    }

    private CancellationSignal getCancellationSignal()
    {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> displayNotification("Authentication was Cancelled by the user"));
        return cancellationSignal;
    }

    /**
     * Display notification to the user
     * @param message Message
     */
    private void displayNotification(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
