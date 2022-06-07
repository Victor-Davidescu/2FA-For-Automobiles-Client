package com.myapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);
        getSupportActionBar().hide();

        //Back button
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {goBack();}
        });

        // Get the default bluetooth adapter
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check paired devices
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Check if there are paired devices
        if(!pairedDevices.isEmpty()) {

            // Put paired devices in a list and display them
            List<Object> deviceList = getDeviceInfoList(pairedDevices);
            displayPairedDevices(deviceList );

        } else {

        }
    }

    /**
     * Return the list of the paired bluetooth devices
     * @return List of DeviceInfo objects
     */
    private List<Object> getDeviceInfoList(Set<BluetoothDevice> pairedBtDevices) {
        List<Object> deviceInfoList = new ArrayList<>();

        // Convert from the set to list
        for(BluetoothDevice device : pairedBtDevices) {
            @SuppressLint("MissingPermission")
            String name = device.getName();
            String address = device.getAddress();

            // Append the device details to the list
            DeviceInfo deviceInfo = new DeviceInfo(name, address);
            deviceInfoList.add(deviceInfo);
        }
        return deviceInfoList;
    }

    private void displayPairedDevices(List<Object> deviceList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SelectDeviceAdapter deviceListAdapter = new SelectDeviceAdapter(this, deviceList);
        recyclerView.setAdapter(deviceListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void goBack() {
        Intent intent = new Intent(SelectDeviceActivity.this, MainActivity.class);
        startActivity(intent);
    }
}