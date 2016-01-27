package com.lncosie.ilandroidos.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.BluetoothDiscovered;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceSelectorFragment extends DialogFragment implements AdapterView.OnItemClickListener {


    @Bind(R.id.device_search)
    ListView deviceSearch;
    DeviceAdapter adapter;

    public DeviceSelectorFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_device_selector, null);
        builder.setView(view)
                // Add action buttons
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Net.get().stopScan();
                            }
                        });
        ButterKnife.bind(this, view);
        adapter = new DeviceAdapter(inflater);
        deviceSearch.setAdapter(adapter);
        Bus.register(this);
        deviceSearch.setOnItemClickListener(this);
        search();
        Dialog dialog= builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        Bus.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Subscribe
    public void onScanResult(BluetoothDiscovered discovered) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.devices.add(discovered.device);
                adapter.notifyDataSetChanged();
            }
        });

    }

    void search() {
        Net net = Net.get();
        net.reset();
        net.search(5000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.selected = adapter.devices.get(position);
        this.dismiss();
        Net net = Net.get();
        net.stateRetrying=false;
        net.setDevice(adapter.devices.get(position));
        net.stopScan().connect();
    }

    static class ViewHolder {
        @Bind(R.id.device_name)
        TextView device_name;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(BluetoothDevice device) {
            String name = device.getName();
            if (name == null || name.length() == 0)
                name = device.getAddress();
            device_name.setText(name);
        }
    }

    static class DeviceAdapter extends BaseAdapter {
        LayoutInflater inflater;
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        BluetoothDevice selected = null;
        DeviceAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                View item = inflater.inflate(R.layout.item_device_search, null);
                holder = new ViewHolder(item);
                item.setTag(holder);
                convertView = item;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bind(devices.get(position));
            return convertView;
        }

    }

}
