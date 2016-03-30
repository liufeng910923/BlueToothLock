package com.lncosie.ilandroidos.bluenet;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lncosie.ilandroidos.bus.BluetoothConneted;
import com.lncosie.ilandroidos.bus.BluetoothDiscovered;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LoginFailed;
import com.lncosie.ilandroidos.bus.LoginSuccess;
import com.lncosie.ilandroidos.bus.DeviceDisconnected;
import com.lncosie.ilandroidos.model.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;


public class NetTransfer {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    final static byte CONFORM_SUCCESS = 0x00;
    final static byte CONFORM_PACKAGE_ERROR = 0x01;
    BluetoothAdapter adapter;
    Handler taskThread;
    Queue<Task> commands = new LinkedList<Task>();
    //List<OnNetStateChange> stateListioner = new ArrayList<OnNetStateChange>();
    Context appContext;
    BluetoothDevice device;
    BluetoothGattCallback rawCallback;
    BluetoothGatt rawConnection;
    BluetoothGattCharacteristic writer;
    Task currentTask;
    SendTask sender;
    TimeoutTask timeout;
    Heartbeat heartbeat;
    Connector connector;
    Task scanner;
    OnNetStateChange.NetState state = OnNetStateChange.NetState.Disconnected;

    boolean login = false;
    boolean connected = false;
    public boolean stateRetrying = false;
    Hook hook;

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void setupHook(Hook hook) {
        this.hook = hook;
    }

    public void init(Context appContext) {
        this.appContext = appContext;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null)
            adapter.enable();
        taskThread = new Handler(Looper.getMainLooper());
//        taskThread=new Handler();
        sender = new SendTask();
        timeout = new TimeoutTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            scanner = new ScannerHuawei(this);
        else
            scanner = new Scanner(this);

        heartbeat = new Heartbeat();
        connector = new Connector(this);
        buildConnection();
    }

    public OnNetStateChange.NetState getState() {
        return state;
    }

    public boolean shouldReLogin = false;

    public void setState(OnNetStateChange.NetState state) {
        this.state = state;
        Object send = null;
        switch (state) {
            case Disconnected:
                heartbeat.stopBeat();
                send = new DeviceDisconnected();
                break;
            case Searching:
                break;
            case Connecting:
                heartbeat.stopBeat();
                break;
            case Connected:
                send = new BluetoothConneted(false);
                break;
            case NeedPassword:
                send = new BluetoothConneted(true);
                this.state = OnNetStateChange.NetState.LoginFailed;
                break;
            case LoginFailed:
                send = new LoginFailed();
                break;
            case Login:
                shouldReLogin = true;
                heartbeat.startBeat();
                send = new LoginSuccess();
                break;
        }
        if (send != null)
            Bus.post(send);

    }


    public <Net extends NetTransfer> Net search(long timeout) {
        scanner.setTimeout(timeout);
        send(scanner);
        return (Net) this;
    }

    public <Net extends NetTransfer> Net reset() {
        shouldReLogin = false;
        if (device != null) {
            commands.clear();
            send(new LogoutTask(this));
        }
        return (Net) this;
    }

    void clearState() {
        if (heartbeat != null) {
            heartbeat.stopBeat();
            heartbeat.stopRetry();
        }
        login = false;
        connected = false;
        device = null;
        eraseCurrentTask();
        stopScan();
        if (rawConnection != null) {
            Log.e("DATA", "time to disconnected");
            rawConnection.disconnect();
            rawConnection.close();
        }
        writer = null;
        rawConnection = null;
        enable();
//        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothManager manager = (BluetoothManager)
                appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        if (adapter != null) {
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(mac);
            if (remoteDevice != null)
                device = remoteDevice;
        }
    }

    public <Net extends NetTransfer> Net connect() {
        send(connector);
        return (Net) this;
    }

    public <Net extends NetTransfer> Net stopScan() {
        setState(OnNetStateChange.NetState.Disconnected);
        scanner.onTaskDown();
        return (Net) this;
    }

    public <Net extends NetTransfer> Net login() {
        send(new LoginTask(this, DbHelper.getPassword(getMac())));
        return (Net) this;
    }

    public boolean isSendable() {
        return login && connected;
    }

    public void disconnect() {
        send(new LogoutTask(this));
    }

    public void disable() {
        if (adapter != null)
            adapter.disable();
    }

    public void enable() {
        if (adapter != null && !adapter.isEnabled()) {
            adapter.enable();
            while (!adapter.isEnabled()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
            init(appContext);
        }
    }

    public <Net extends NetTransfer> Net send(Task task) {
        commands.add(task);
        sendNext();
        return (Net) this;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    String mac = null;

    public String getMac() {
        return mac;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        if (device != null)
            this.mac = device.getAddress();
    }

    void eraseAllTask() {
        commands.clear();
        //eraseCurrentTask();
    }

    void eraseCurrentTask() {
        taskThread.removeCallbacks(sender);
        taskThread.removeCallbacks(timeout);
        currentTask = null;
    }

    void sendNext() {
        //如果当前任务不为空就return
        if (currentTask != null)
            return;
        synchronized (commands) {
            try {
                currentTask = commands.poll();
                if (currentTask == null)//如果轮询出来的task为空，return；
                    return;
                taskThread.postDelayed(sender, currentTask.delayTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void write(byte[] data) {
        if (rawConnection != null && writer != null) {
            writer.setValue(data);
            if (hook != null)
                hook.onWrite(bytesToHex(data));
            heartbeat.onWrite();
            Log.e("DATA", "Send:" + bytesToHex(data));
            rawConnection.writeCharacteristic(writer);

        }
    }

    void onReceive(byte[] bytes) {
        taskThread.post(new ReceiveTask(bytes));
    }

    int lenidx(byte cmd) {
        if (ByteableTask.CMD_GET_HISTORY == cmd || ByteableTask.CMD_SYNC_IDS == cmd || ByteableTask.CMD_SYNC_IDS_FINGER == cmd)
            return 2;
        return 3;
    }

    int header_len(byte cmd) {
        if (ByteableTask.CMD_GET_HISTORY == cmd || ByteableTask.CMD_SYNC_IDS == cmd || ByteableTask.CMD_SYNC_IDS_FINGER == cmd)
            return 3;
        return 4;
    }

    void buildConnection() {
        rawCallback = new BluetoothGattCallback() {
            byte buffer[] = new byte[40];
            int expectLength = 0;
            int pos = 0;
            byte cmd = 0;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.e("DATA", String.format("event:%d--%d", status, newState));

                super.onConnectionStateChange(gatt, status, newState);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    return;
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    Log.e("DATA", "time to discover");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // fireStateChange(OnNetStateChange.NetState.Disconnected);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                final String serviceUuid = "0000fee0-0000-1000-8000-00805f9b34fb";
                final String characterUuid = "0000fee1-0000-1000-8000-00805f9b34fb";
                final String clientUuid = "00002902-0000-1000-8000-00805f9b34fb";
                BluetoothGattService service = gatt.getService(UUID.fromString(serviceUuid));
                if (service != null) {
                    writer = service.getCharacteristic(UUID.fromString(characterUuid));
                    if (writer == null)
                        return;
                    gatt.setCharacteristicNotification(writer, true);
                    BluetoothGattDescriptor descriptor = writer.getDescriptor(UUID.fromString(clientUuid));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    connected = true;
                    if (connector.equals(currentTask)) {
                        connector.onTaskDown();
                        eraseCurrentTask();
                        sendNext();
                    }
                }
            }

            byte[] dump = null;
            long preTime = 0;

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                heartbeat.onRead();

                try {
                    long time = System.currentTimeMillis();
                    byte[] bytes = characteristic.getValue();
                    Log.e("DATA", "Recv:[" + time + "]" + bytesToHex(bytes) + "<--->" + time + ":" + preTime);
                    if (dump != null) {
                        if ((time - preTime) <= 4) {
                            Log.e("DATA", "Ignore");
                            return;
                        }
                    }

                    dump = bytes;
                    preTime = time;

                    int i = 0;
                    while (i < bytes.length && bytes[i] == -1) {
                        i = i + 1;
                    }
                    if (i != 0)
                        bytes = Arrays.copyOfRange(bytes, i, bytes.length);

                    if (hook != null)
                        hook.onWrite(bytesToHex(bytes));

                    if ((bytes[0] == ByteableTask.CMD_HEADER_FC || bytes[0] == ByteableTask.CMD_HEADER_FD)
                            && expectLength == 0 && bytes.length >= 4) {
                        cmd = bytes[1];
                        int lenidx = lenidx(bytes[1]);
                        expectLength = bytes[lenidx] + header_len(bytes[1]) + 3;
                        pos = 0;
                        if (expectLength == 0) {
                            if (hook != null)
                                hook.onWrite(null);
                            return;
                        }
                    }
                    System.arraycopy(bytes, 0, buffer, pos, bytes.length);
                    pos = pos + bytes.length;
                    expectLength = expectLength - (bytes.length);
                    if (expectLength < 0) {
                        expectLength = 0;
                        pos = 0;
                        return;
                    }

                    if (expectLength == 0) {
                        byte[] full = Arrays.copyOfRange(buffer, 0, pos);

                        onReceive(full);
                    }
                } catch (Exception e) {
                    expectLength = 0;
                    pos = 0;
                }
            }
        };
    }

    void sendConformMessage(byte command, byte len, byte crc0, byte crc1) {
        byte[] sureRecevie = new DataBuilder()
                .append(ByteableTask.CMD_HEADER_FC)
                .append(new byte[]{0x0, 0x4, command, len, crc0, crc1, 0, 0})
                .append(ByteableTask.CMD_END)
                .builder();
        fillCrc(sureRecevie);
        write(sureRecevie);
    }

    boolean proccess(final ByteableTask task, byte[] data) {
        byte crc0 = 0, crc1 = 0;
        byte contentlen = 0x0;
        try {

            int header_len = header_len(data[1]);
            byte cmd = 0;
            cmd = data[1];
            //transaction.check(data);
            if (cmd != task.command)
                return false;
            byte[] info = new byte[data.length - header_len - 3];
            crc0 = data[data.length - 3];
            crc1 = data[data.length - 2];
            System.arraycopy(data, header_len, info, 0, info.length);
            contentlen = data[2];
            task.setError(data[header_len]);
            task.fromBytes(info);
        } catch (Exception e) {
            return false;
        }
        if (data[0] == ByteableTask.CMD_HEADER_FC) {
            taskThread.removeCallbacks(timeout);
            taskThread.post(new Runnable() {
                @Override
                public void run() {
                    task.onTaskDown();
                }
            });
            return true;
        } else {
            sendConformMessage(task.command, contentlen, crc0, crc1);
            return false;
        }
    }

    void write(ByteableTask task) {
        if (task.command == ByteableTask.CMD_ADD_ADMIN_FINGER
                || task.command == ByteableTask.CMD_ADD_USER_FINGER
                || task.command == ByteableTask.CMD_RESET_ALL) {
            heartbeat.changeLongHeatTickTime(true);
        }

        byte[] data = task.toBytes();
        byte len = (byte) data.length;
        byte[] crc = new byte[]{0, 0};
        byte[] buffer = new DataBuilder()
                .append(ByteableTask.CMD_HEADER_FC)
                .append((byte) 0x0)//pack seq
                .append(len)
                .append(data)
                .append(crc)
                .append(ByteableTask.CMD_END)
                .builder();
        fillCrc(buffer);
        write(buffer);
        //transaction.reset(task.command);
    }

    void fillCrc(byte[] data) {
        int len = data.length;
        int init = 0;
        for (int i = 1; i < len - 3; i++) {
            init = init + (data[i] & 0xFF);
        }
        data[len - 3] = (byte) (init >> 8);
        data[len - 2] = (byte) (init % 256);
    }

    public interface Hook {
        void onWrite(final String message);

        void onReceive(final String message);
    }

    static class Transaction {
        byte cmd = 0;
        NetTransfer transfer;
        ByteableTask DebugStop;

        Transaction(NetTransfer transfer) {
            this.transfer = transfer;
            DebugStop = new ByteableTask(transfer, ByteableTask.CMD_DEBUG) {
                @Override
                protected void onTaskDown() {

                }
            };
        }

        void end() {
            this.cmd = 0;
        }

        void reset(byte cmd) {
            if (cmd == ByteableTask.HEART_BEAT)
                return;
            if (this.cmd != cmd && this.cmd != 0) {
                postError();
            }
            this.cmd = cmd;
        }

        void check(byte[] bytes) {
            if (bytes[1] == ByteableTask.HEART_BEAT)
                return;
            if (bytes[1] != cmd) {
                postError();
                return;
            }
            if (bytes[0] == ByteableTask.CMD_HEADER_FC) {
                cmd = 0;
            } else if (bytes[0] == ByteableTask.CMD_HEADER_FD) {

            } else {
                postError();
            }
        }

        void postError() {

        }

    }

    class SendTask implements Runnable {
        @Override
        public void run() {
            Task task = currentTask;
            task.onTaskStart();
            if (task instanceof ByteableTask) {
                write((ByteableTask) task);
            }
            taskThread.postDelayed(timeout, task.getTimeout());
        }
    }

    class ReceiveTask implements Runnable {
        byte[] bytes;

        ReceiveTask(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            if (currentTask instanceof ByteableTask) {
                if (proccess((ByteableTask) currentTask, bytes)) {
                    eraseCurrentTask();
                    sendNext();
                }
            }
        }
    }

    class TimeoutTask implements Runnable {
        @Override
        public void run() {
            if (currentTask != null)
                currentTask.onTimeout();
            eraseCurrentTask();
            sendNext();
        }
    }
}

class Heartbeat implements Runnable {
    ByteableTask heartbeat = new ByteableTask(null, ByteableTask.HEART_BEAT) {
        @Override
        protected void onTaskDown() {

        }
    };
    Net net;
    long tryTimes = 0;
    boolean connect = true;
    boolean longwait = false;
    long HEAT_TICK_TIME = 4000;
    Runnable sendbeat = new Runnable() {
        @Override
        public void run() {
            if (longwait) {
                net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
                return;
            }
            if (connect == false) {
                net.connected = false;
                //net.setState(OnNetStateChange.NetState.Connecting);
                stopBeat();
                startRetry();
            } else {
                net.write(heartbeat);
                connect = false;
                net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
            }
        }
    };

    Heartbeat() {
        net = Net.get();
    }

    void onWrite() {
        if (net.login)
            delayBeat();
    }

    void onRead() {
        boolean needfire = false;
        if (connect == false) {
            needfire = true;
            connect = true;
        }
        longwait = false;
        net.connected = true;
        if (net.login)
            delayBeat();
    }

    void changeLongHeatTickTime(boolean longtime) {
        longwait = longtime;
    }

    void startBeat() {
        connect = true;
        net.taskThread.removeCallbacks(sendbeat);
        net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME + 2000);
    }

    void stopBeat() {
        net.taskThread.removeCallbacks(sendbeat);
    }

    void delayBeat() {
        net.taskThread.removeCallbacks(sendbeat);
        net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
    }

    void startRetry() {
        tryTimes = 3;
        net.taskThread.post(this);
    }

    void stopRetry() {
        net.taskThread.removeCallbacks(Heartbeat.this);
    }

    @Override
    public void run() {
        if (net.isSendable()) {
            stopRetry();
        } else {
            tryTimes = tryTimes - 1;
            if (tryTimes > 0) {
                stopRetry();
                net.reset();
            } else {
                net.connect();
                net.taskThread.postDelayed(this, 8000);
            }

        }
    }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScannerHuawei extends Task {
    List<BluetoothDevice> devices = new ArrayList<>();
    android.bluetooth.le.ScanCallback callback = new android.bluetooth.le.ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (devices.contains(device))
                return;
            devices.add(device);
            Bus.post(new BluetoothDiscovered(device));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    ScannerHuawei(NetTransfer transfer) {
        super(transfer);
    }

    @Override
    public long delayTime() {
        return 100;//wait bluetooth  device enable
    }

    public long getTimeout() {
        return 5000;
    }

    @Override
    protected void onTaskDown() {
        stopScan();
    }

    @Override
    protected void onTaskStart() {
        net.setState(OnNetStateChange.NetState.Searching);
        devices.clear();
        if (net.adapter != null) {
            BluetoothLeScanner scanner = net.adapter.getBluetoothLeScanner();
            scanner.startScan(callback);
        }
    }

    public void stopScan() {
        if (net.adapter != null) {
            BluetoothLeScanner scanner = net.adapter.getBluetoothLeScanner();
            scanner.stopScan(callback);
        }
        devices.clear();
        net.eraseCurrentTask();
    }

    @Override
    protected void onTimeout() {
        stopScan();
        net.commands.clear();
    }
}

class Scanner extends Task {
    List<BluetoothDevice> devices = new ArrayList<>();
    BluetoothAdapter.LeScanCallback scanner = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (devices.contains(device))
                return;
            devices.add(device);
            Bus.post(new BluetoothDiscovered(device));
        }
    };

    Scanner(NetTransfer transfer) {
        super(transfer);
    }

    @Override
    public long delayTime() {
        return 100;//wait bluetooth  device enable
    }

    public long getTimeout() {
        return 5000;
    }

    @Override
    protected void onTaskDown() {
        stopScan();
    }

    @Override
    protected void onTaskStart() {
        net.setState(OnNetStateChange.NetState.Searching);
        devices.clear();
        if (net.adapter != null) {
            net.adapter.enable();
            net.adapter.startLeScan(scanner);
        }
    }

    public void stopScan() {
        if (net.adapter != null) {
            net.adapter.stopLeScan(scanner);
        }
        devices.clear();
        net.eraseCurrentTask();
    }

    @Override
    protected void onTimeout() {
        stopScan();
        net.commands.clear();
    }
}

class Connector extends Task {
    int tryCount = 2;
    boolean fire = true;

    public Connector(NetTransfer transfer) {
        super(transfer);
    }

    @Override
    public long delayTime() {
        return 200;
    }

    @Override
    public long getTimeout() {
        return 5000;
    }

    @Override
    protected void onTaskStart() {

        if (net.device != null) {
            try {
                Log.e("DATA", "time to connected");
                if (net.rawConnection != null) {
                    net.clearState();
                }
                net.rawConnection = net.device.connectGatt(net.appContext, false, net.rawCallback);
                net.setState(OnNetStateChange.NetState.Connecting);
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onTaskDown() {
        net.setState(OnNetStateChange.NetState.Connected);
        Log.e("DATA", "time connected");
    }

    @Override
    protected void onTimeout() {
        if (net.getState() != OnNetStateChange.NetState.Connected) {
            tryCount = tryCount - 1;
            if (tryCount > 0) {
                net.connect();
            } else {
                net.setState(OnNetStateChange.NetState.LoginFailed);
                Log.e("DATA", "timeout connected");
            }
        }

    }
}

class LogoutTask extends ByteableTask {

    public LogoutTask(NetTransfer transfer) {
        super(transfer, ByteableTask.CMD_DISCONNECT);
        net.eraseAllTask();
    }

    @Override
    protected void onTimeout() {
        clearState();
    }

    @Override
    protected void onTaskDown() {
        clearState();
    }

    public long delayTime() {
        return 100;
    }

    public long getTimeout() {
        return 1000;
    }


    @Override
    protected void onTaskStart() {

    }

    void clearState() {
        Bus.post(new DeviceDisconnected());
        net.setState(OnNetStateChange.NetState.Disconnected);
        net.clearState();
    }
}

class LoginTask extends ByteableTask {
    int tryCount = 2;

    public LoginTask(NetTransfer transfer, byte[] password) {
        super(transfer, ByteableTask.CMD_AUTH, password);
    }

    @Override
    protected void onTimeout() {
        Log.e("DATA", "timeout to login");
        net.setState(OnNetStateChange.NetState.Disconnected);
        retry(false);

    }

    public long delayTime() {
        return 100;
    }

    public long getTimeout() {
        return 2000;
    }

    @Override
    protected void onTaskDown() {
        if (getError() != 0) {
            net.setState(OnNetStateChange.NetState.NeedPassword);
            //retry(true);
            return;
        }
        Log.e("DATA", "time login");
        net.login = true;
        net.setState(OnNetStateChange.NetState.Login);
    }

    private void retry(boolean errorPwd) {
        tryCount = tryCount - 1;
        if (tryCount > 0) {
            net.stateRetrying = true;
            //net.send(new LogoutTask(net));
            //net.connect();
            net.send(this);
        } else {
            if (errorPwd) {
                net.setState(OnNetStateChange.NetState.NeedPassword);
            } else {
                net.setState(OnNetStateChange.NetState.LoginFailed);
            }
        }
    }

    @Override
    protected void onTaskStart() {
        Log.e("DATA", "time to login");
        net.login = false;
    }
}