package com.lncosie.ilandroidos.bus;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2015/11/12.
 */
public class DeviceDiscovered {
    public BluetoothDevice device;
    public DeviceDiscovered(BluetoothDevice device){
        this.device=device;
    }
}
