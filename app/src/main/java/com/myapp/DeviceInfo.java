package com.myapp;

public class DeviceInfo {

    private final String name;
    private final String address;

    /**
     * Class Constructor
     * @param name Device name
     * @param address MAC address
     */
    public DeviceInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    /**
     * Get the device name
     * @return Device Name
     */
    public String getName() { return name; }

    /**
     * Get the device address
     * @return Device MAC Address
     */
    public String getAddress() { return address; }
}
