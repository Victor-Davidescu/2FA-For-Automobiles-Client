/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import static android.content.ContentValues.TAG;


public class ClientBluetoothThread extends Thread {

    // Bluetooth related variables
    private BluetoothDevice btDevice;
    private final BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private InputStream btInStream;
    private OutputStream btOutStream;

    // Other variables
    public boolean connectedToDevice = false;
    public boolean keepRunning = true;

    /**
     * Class constructor
     */
    public ClientBluetoothThread(String address) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(address);
    }

    /**
     * Initialises the bluetooth socket
     * @return True if it succeed or False if failed to initialise.
     */
    @SuppressLint("MissingPermission")
    private boolean initialiseSocket() {
        UUID uuid = btDevice.getUuids()[0].getUuid();
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
            return false;
        }
    }


    /**
     * Establishes the connection to the bluetooth device
     * @return True if it succeed or False if failed.
     */
    @SuppressLint("MissingPermission")
    private boolean establishConnection() {
        btAdapter.cancelDiscovery();
        try {
            btSocket.connect();
            connectedToDevice = true;
            MainActivity.handler.obtainMessage(MainActivity.CONNECTING_STATUS, 1, -1).sendToTarget();
            return true;

        } catch (IOException connectException) {
            MainActivity.handler.obtainMessage(MainActivity.CONNECTING_STATUS, -1, -1).sendToTarget();
            return false;
        }
    }


    /**
     * Establishes input/output stream for the bluetooth connection.
     * @return True if it succeed or False if failed.
     */
    private boolean establishIO() {
        try {
            btInStream = btSocket.getInputStream();
            btOutStream = btSocket.getOutputStream();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Send message to the bluetooth device
     * @param msg String message
     */
    public void sendMessage(String key, String msg) {
        msg = Encryption.EncryptMessage(key, msg); // Encrypt msg
        key = null;

        byte[] bytes = msg.getBytes(); //converts entered String into bytes
        try {
            btOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("Send Error","Unable to send message",e);
        }
    }


    /**
     * Handles the communication with the bluetooth device
     */
    private void threadMainLoop() {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while (keepRunning) {
            try {
                buffer[bytes] = (byte) btInStream.read();
                String message;

                if (buffer[bytes] == '\n'){
                    message = new String(buffer,0,bytes);
                    MainActivity.handler.obtainMessage(MainActivity.MESSAGE_READ,message).sendToTarget();
                    bytes = 0;
                } else { bytes++; }

            } catch (IOException e) {
                e.getMessage();
                break;
            }
        }
    }


    /**
     *  Close the connection to the device
     */
    private void closeConnection() {
        try {
            btSocket.close();
            connectedToDevice = false;
            Log.e("Status", "Cannot connect to device");
        } catch (IOException closeException) {
            Log.e(TAG, "Could not close the client socket", closeException);
        }
    }


    /**
     * Client bluetooth handler main function
     */
    public void run() {

        // Initialise the bluetooth socket
        if(initialiseSocket()) {

            // Establish a connection with the bluetooth device
            if(establishConnection()) {

                // Establish input/output stream
                if(establishIO()) {

                    // Enter in the main loop for this thread
                    threadMainLoop();
                }
            }
        }
        // Close the connection at the end of execution
        closeConnection();
    }
}
