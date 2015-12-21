package com.lncosie.ilandroidos.bluenet;


import android.bluetooth.BluetoothDevice;

import java.util.List;

abstract class DeviceScanCallback {
    public boolean onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return true;
    }

    public void onBatchScanResults(List<BluetoothDevice> results) {
    }

    public void onScanFailed(int errorCode) {
    }
}

