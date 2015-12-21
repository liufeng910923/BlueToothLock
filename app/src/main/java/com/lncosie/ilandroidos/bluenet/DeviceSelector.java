package com.lncosie.ilandroidos.bluenet;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.List;


public interface DeviceSelector{

    public abstract BluetoothDevice select();
    public boolean onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) ;

    public void onBatchScanResults(List<BluetoothDevice> results) ;
    public void onScanFailed(int errorCode) ;
    public class MacSelector implements DeviceSelector{
        String mac;
        public MacSelector(String mac){
            this.mac=mac;
        }
        @Override
        public BluetoothDevice select() {
            return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        }

        @Override
        public boolean onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            return false;
        }

        @Override
        public void onBatchScanResults(List<BluetoothDevice> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {

        }
    }
}

