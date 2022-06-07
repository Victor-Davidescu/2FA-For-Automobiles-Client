package com.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SelectDeviceAdapter extends RecyclerView.Adapter<SelectDeviceAdapter.ViewHolder> {

    private final Context context;
    private final List<Object> deviceList;
    public static final String SHARED_PREFS = "sharedPrefs" ;
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_ADDRESS = "deviceAddress";


    /**
     * Class Constructor
     * @param context Context
     * @param deviceList Device list
     */
    public SelectDeviceAdapter(Context context, List<Object> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }


    @NonNull
    @Override
    public SelectDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectDeviceAdapter.ViewHolder holder, int position) {
        final DeviceInfo deviceInfo = (DeviceInfo) deviceList.get(position);
        holder.textName.setText(deviceInfo.getName());
        holder.textAddress.setText(deviceInfo.getAddress());

        // When a device is selected
        holder.linearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context,SettingsActivity.class);

            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(DEVICE_NAME, deviceInfo.getName());
            editor.putString(DEVICE_ADDRESS, deviceInfo.getAddress());
            editor.apply();

            // Send device details to the MainActivity
            //intent.putExtra("deviceName", deviceInfo.getName());
            //intent.putExtra("deviceAddress",deviceInfo.getAddress());


            // Call MainActivity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return deviceList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textViewName);
            textAddress = v.findViewById(R.id.textViewAddress);
            linearLayout = v.findViewById(R.id.linearLayout);
        }
    }
}
