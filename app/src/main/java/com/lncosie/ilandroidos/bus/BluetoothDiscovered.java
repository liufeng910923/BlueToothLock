package com.lncosie.ilandroidos.bus;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2015/11/12.
 */
public class BluetoothDiscovered {
    public BluetoothDevice device;

    public BluetoothDiscovered(BluetoothDevice device) {
        this.device = device;
    }
}
