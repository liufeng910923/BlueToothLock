package com.lncosie.ilandroidos.view;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.OnNetStateChange;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.HistoryChanged;
import com.lncosie.ilandroidos.bus.InputPwd;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.LoginSuccess;
import com.lncosie.ilandroidos.bus.DeviceDisconnected;
import com.lncosie.ilandroidos.bus.TryLogin;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.ConnectedLocks;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.Sync;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeFragment extends ActiveAbleFragment implements AdapterView.OnItemClickListener {

    @Bind(R.id.devices)
    ListView devices;
    @Bind(R.id.title)
    TextView title;
    DeviceAdapter adapter;
    @Bind(R.id.search_device)
    TextView searchDevice;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        //homefragment中已配置的device
        adapter = new DeviceAdapter(getLayoutInflater(savedInstanceState));
        devices.setAdapter(adapter);
        devices.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onActive(Object arg) {
    }

    @Subscribe
    public void langChanged(LanguageChanged languageChanged) {
        title.setText(R.string.home);
        searchDevice.setText(R.string.scan_device);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.search_device)
    void search_device() {
        adapter.disActive();
        DeviceSelectorFragment fragment = new DeviceSelectorFragment();
        fragment.show(getFragmentManager().beginTransaction(), "DialogFragment");
    }
    @Subscribe
    public void login(TryLogin a){
        Net net=Net.get();
        if(net.getState()== OnNetStateChange.NetState.Connected
                ||net.getState()== OnNetStateChange.NetState.LoginFailed)
        {
            net.login();
        }else
            net.connect().login();
    }

    boolean working=false;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /**forbid click too fast**/
        if(working)
            return;
        working=true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                working=false;
            }
        },500);

        final ConnectedLocks lock = adapter.lockses.get(position);
        if (adapter.isActiveLock(position)) {
            Applyable applyable = new Applyable() {
                @Override
                public void apply(Object arg0, Object arg1) {
                    Net.get().reset();
                }
            };
            MenuYesnoFragment fragment = MenuYesnoFragment.newInstance(R.string.disconnect_title,
                    getString(R.string.disconnect_tip), applyable);
            fragment.show(getFragmentManager(), "");

        } else {
            Applyable applyable = new Applyable() {
                @Override
                public void apply(Object arg0, Object arg1) {
                    Net net = Net.get();
                    net.stateRetrying=false;
                    BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(lock.mac);
                    net.reset().setDevice(bluetoothDevice);
                    net.connect();
                }
            };
            MenuYesnoFragment fragment = MenuYesnoFragment.newInstance(R.string.connect_to_title,
                    getString(R.string.connect_to), applyable);
            fragment.show(getFragmentManager(), "");
        }
    }

    ConnectedLocks activeLock = null;



    @Subscribe
    public void OnConnected(LoginSuccess state) {

        ConnectedLocks lock = null;
        BluetoothDevice device = Net.get().getDevice();
        DbHelper.setCurMac(device.getAddress());

        for (ConnectedLocks it : DbHelper.getLocks()) {
            if (device.getAddress().equals(it.mac)) {
                lock = it;
                if (device.getName()==null||device.getName().length()==0){
                    lock.name="HTL-"+lock.mac.substring(15,17);
                }
                DbHelper.insertLock(lock);
                break;
            }
        }

        adapter.reinit();
        for (ConnectedLocks it : adapter.lockses) {
            if (device.getAddress().equals(it.mac)) {
                lock = it;
                break;
            }
        }
        activeLock = lock;
        adapter.notifyDataSetChanged();
        /***Sync***/
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                Bus.post(new UsersChanged());
                Bus.post(new HistoryChanged());
            }
        };
        Sync.syncIds(applyable);
    }

    @Subscribe
    public void OnDisConnected(DeviceDisconnected state) {
        activeLock = null;
        adapter.notifyDataSetChanged();
    }

    class DeviceAdapter extends BaseAdapter {
        LayoutInflater inflater;
        List<ConnectedLocks> lockses = new ArrayList<ConnectedLocks>();
        DeviceAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
            reinit();
        }
        void reinit() {
            lockses.clear();

            for(ConnectedLocks lock:DbHelper.getLocks())
            {
                lockses.add(lock);
            }
            Log.d("lockses：","has been connected lockes"+lockses.toString());
        }
        @Override
        public int getCount() {
            return lockses.size();
        }

        @Override
        public Object getItem(int position) {
            return lockses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                View item = inflater.inflate(R.layout.item_devices, null);
                holder = new ViewHolder(item);
                item.setTag(holder);
                convertView = item;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //判断设备名字是否为空，如果为空将mac地址的末两位命名为名字。
            ConnectedLocks lock = lockses.get(position);
            if (lock.name==null||lock.name.length()==0){
                lock.name="HTL-"+lock.mac.substring(15,17);
            }
            Log.d("TEST",lock.toString());
            holder.bind(lock, activeLock == lock);
            return convertView;
        }

        public boolean isActiveLock(int position) {
            return activeLock == getItem(position);
        }

        void disActive() {
            activeLock = null;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        @Bind(R.id.device_img)
        ImageView device_img;
        @Bind(R.id.device_name_connected)
        TextView device_name_connected;
        @Bind(R.id.device_state)
        LanguageTextView device_state;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(final ConnectedLocks lock, final boolean active) {
            if (lock==null)
                return;
            else {

                Log.d("Lock name :",lock.name);
                device_name_connected.setText(lock.name);
                device_state.setTextRes(active ? R.string.connected : R.string.disconnected);
                device_img.setImageDrawable(active ? device_img.getResources().getDrawable(R.drawable.keepass_2) : device_img.getResources().getDrawable(R.drawable.keepass));
            }
        }
    }
}
