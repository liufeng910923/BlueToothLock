package com.lncosie.ilandroidos.bluenet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.DeviceDiscovered;
import com.lncosie.ilandroidos.model.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;


public class NetTransfer {

    public interface Hook
    {
        void onWrite(final String message);
        void onReceive(final String message);
    }
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    final static byte CONFORM_SUCCESS = 0x00;
    final static byte CONFORM_PACKAGE_ERROR = 0x01;
    BluetoothAdapter adapter;
    Handler taskThread;
    Queue<Task> commands = new LinkedList<Task>();
    List<OnNetStateChange> stateListioner = new ArrayList<OnNetStateChange>();
    Context appContext;
    BluetoothDevice device;
    BluetoothGattCallback rawCallback;
    BluetoothGatt rawConnection;
    BluetoothGattCharacteristic writer;

    Task        currentTask;
    SendTask    sender;

    TimeoutTask timeout;
    Heartbeat heartbeat;
    Task connector;
    Task scanner;

    //Transaction transaction;

    OnNetStateChange.NetState state = OnNetStateChange.NetState.Idel;
    boolean login = false;
    boolean connected = false;

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    Hook        hook;
    public void setupHook(Hook hook)
    {
        this.hook=hook;
    }
    public void init(Context appContext) {
        this.appContext=appContext;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter!=null)
            adapter.enable();
        taskThread = new Handler(Looper.getMainLooper());
        sender=new SendTask();
        timeout = new TimeoutTask();
        connector = new Connector(this);
        scanner = new Scanner(this);
        heartbeat=new Heartbeat();
        //transaction=new Transaction(this);
        buildConnection();
    }

    public OnNetStateChange.NetState getState() {
        return state;
    }

    public BluetoothDevice getConnected() {
        return rawConnection.getDevice();
    }

    public <Net extends NetTransfer> Net search(long timeout) {
        //reset();
        scanner.setTimeout(timeout);
        send(scanner);
        return (Net) this;
    }
    public <Net extends NetTransfer> Net  reset(){
        commands.clear();
        if(heartbeat!=null) {
            heartbeat.stopBeat();
            heartbeat.stopRetry();
        }
        device=null;
        eraseCurrentTask();
        stopScan();
        disconnect();
        enable();
        //retryLogin=0;
        retryConnect=0;
        return (Net)this;
    }
    public <Net extends NetTransfer> Net connect() {
        send(connector);
        return (Net) this;
    }
    public <Net extends NetTransfer> Net stopScan()
    {
        scanner.onTaskDown();
        return (Net)this;
    }
    public <Net extends NetTransfer> Net login() {
        send(new LoginTask(this, DbHelper.getPassword(device.getAddress())));
        return (Net) this;
    }

    public boolean isSendable() {
        return login&&connected;
    }

    public void disconnect() {
        if(state!=OnNetStateChange.NetState.Idel)
            fireStateChange(OnNetStateChange.NetState.Idel);
        login = false;
        connected=false;
        //stopScan();
        if (rawConnection != null) {
            ByteableTask disconnect=new ByteableTask(this,ByteableTask.CMD_DISCONNECT) {
                @Override
                protected void onTaskDown() {

                }
            };
            write(disconnect);
            Thread.yield();
            rawConnection.disconnect();
            rawConnection.close();
            Thread.yield();
            writer=null;
            rawConnection = null;
        }
    }
    public void disable()
    {
        if(adapter!=null)
            adapter.disable();
    }
    public void enable()
    {
        if(adapter!=null&&!adapter.isEnabled())
        {
            adapter.enable();
            while (!adapter.isEnabled())
            {
                try
                {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ie)
                {
                    // unexpected interruption while enabling bluetooth
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
        }
    }
    public <Net extends NetTransfer> Net send(Task task) {
        commands.add(task);
        sendNext();
        return (Net) this;
    }


    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
    public BluetoothDevice getDevice() {
        return device;
    }
    //long    retryLogin=0;
    long    retryConnect=0;
    void fireStateChange(final OnNetStateChange.NetState changed) {
        taskThread.post(new Runnable() {
            @Override
            public void run() {
                for (OnNetStateChange lis : stateListioner)
                    lis.onChange(changed);
            }
        });
        switch (changed) {
            case SearchTimeout:
            case Disconnected:
                commands.clear();
                state = OnNetStateChange.NetState.Idel;
                break;
            case ConnectFailed:
                retryConnect+=1;
                commands.clear();
                state = OnNetStateChange.NetState.Idel;
                if(false)//retryConnect<2
                {
                    //connect().login();
                }else{
                    retryConnect=0;
                    reset();
                }
                break;
            case LoginFailed:
                commands.clear();
                break;
            case LoginSuccess:
                heartbeat.startBeat();
                break;
            case Pending:
                return;
        }
        this.state = changed;
    }


    public void addStateListioner(OnNetStateChange listioner) {
        if (stateListioner.contains(listioner))
            return;
        stateListioner.add(listioner);
    }

    public void removeStateListioner(OnNetStateChange listioner) {
        stateListioner.remove(listioner);
    }

    void eraseCurrentTask() {
        taskThread.removeCallbacks(sender);
        taskThread.removeCallbacks(timeout);
        currentTask = null;
    }

    void sendNext() {
        if (currentTask != null)
        return;
        synchronized (commands) {
            try {
                currentTask = commands.poll();
                if(currentTask==null)
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
            if(hook!=null)
                hook.onWrite(bytesToHex(data));
            heartbeat.onWrite();
            Log.e("DATA","Send:"+bytesToHex(data));
            rawConnection.writeCharacteristic(writer);

        }
    }

    void onReceive(byte[] bytes) {
        taskThread.post(new ReceiveTask(bytes));
    }
    int lenidx(byte cmd)
    {
        if(ByteableTask.CMD_GET_HISTORY==cmd||ByteableTask.CMD_SYNC_IDS==cmd||ByteableTask.CMD_SYNC_IDS_FINGER==cmd)
            return 2;
        return 3;
    }
    int header_len(byte cmd)
    {
        if(ByteableTask.CMD_GET_HISTORY==cmd||ByteableTask.CMD_SYNC_IDS==cmd||ByteableTask.CMD_SYNC_IDS_FINGER==cmd)
            return 3;
        return 4;
    }
    static class Transaction
    {
        byte cmd=0;
        NetTransfer transfer;
        ByteableTask DebugStop;
        Transaction(NetTransfer transfer)
        {
            this.transfer=transfer;
            DebugStop=new ByteableTask(transfer,ByteableTask.CMD_DEBUG) {
                @Override
                protected void onTaskDown() {

                }
            };
        }
        void end()
        {
            this.cmd=0;
        }
        void reset(byte cmd)
        {
            if(cmd==ByteableTask.HEART_BEAT)
                return;
            if(this.cmd!=cmd&&this.cmd!=0)
            {
                postError();
            }
            this.cmd=cmd;
        }
        void check(byte[] bytes)
        {
            if(bytes[1]==ByteableTask.HEART_BEAT)
                return;
            if(bytes[1]!=cmd)
            {
                postError();
                return;
            }
            if(bytes[0]==ByteableTask.CMD_HEADER_FC)
            {
               cmd=0;
            }else if(bytes[0]==ByteableTask.CMD_HEADER_FD)
            {

            }else
            {
                postError();
            }
        }
        void postError()
        {

        }

    }
    void buildConnection() {
        rawCallback = new BluetoothGattCallback() {
            byte buffer[] = new byte[40];
            int expectLength = 0;
            int pos = 0;
            byte cmd=0;
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
                    connected =true;
                    if (connector.equals(currentTask)) {
                        connector.onTaskDown();
                        eraseCurrentTask();
                        sendNext();
                    }
                }

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                heartbeat.onRead();

                try {
                    byte[] bytes = characteristic.getValue();
                    int i=0;
                    while (i<bytes.length&&bytes[i]==-1){
                        i=i+1;
                    }
                    if(i!=0)
                        bytes= Arrays.copyOfRange(bytes,i,bytes.length);
                    Log.e("DATA","Recv:"+bytesToHex(bytes));
                    if(hook!=null)
                        hook.onWrite(bytesToHex(bytes));

                    if ((bytes[0] == ByteableTask.CMD_HEADER_FC||bytes[0] == ByteableTask.CMD_HEADER_FD)
                            && expectLength == 0 && bytes.length >= 4)
                    {
                        cmd=bytes[1];
                        int lenidx=lenidx(bytes[1]);
                        expectLength = bytes[lenidx] + header_len(bytes[1])+3;
                        pos = 0;
                        if (expectLength == 0)
                        {
                            if(hook!=null)
                                hook.onWrite(null);
                            return;
                        }
                    }
                    System.arraycopy(bytes, 0, buffer, pos, bytes.length);
                    pos = pos + bytes.length;
                    expectLength = expectLength - (bytes.length);
                    if(expectLength<0)
                    {
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

    void sendConformMessage(byte command,byte len,byte crc0,byte crc1) {
        byte[] sureRecevie = new DataBuilder()
                .append(ByteableTask.CMD_HEADER_FC)
                .append(new byte[]{0x0,0x4,command,len,crc0,crc1,0,0})
                .append(ByteableTask.CMD_END)
                .builder();
        fillCrc(sureRecevie);
        write(sureRecevie);
    }

    boolean proccess(final ByteableTask task, byte[] data) {
        byte crc0=0,crc1=0;
        byte contentlen = 0x0;
        try {

            int header_len=header_len(data[1]);
            byte cmd=0;
            cmd=data[1];
            //transaction.check(data);
            if(cmd!=task.command)
                return false;
            byte[] info = new byte[data.length - header_len - 3];
            crc0=data[data.length-3];
            crc1=data[data.length-2];
            System.arraycopy(data, header_len, info, 0, info.length);
            contentlen = data[2];
            task.setError(data[header_len]);
            task.fromBytes(info);
            //Log.d("bluenet.receive", bytesToHex(info) + "from" + bytesToHex(data));
        } catch (Exception e) {
            //sendConformMessage(task.command,contentlen,crc0,crc1);
            return false;
        }
        if (data[0]==ByteableTask.CMD_HEADER_FC) {
            taskThread.removeCallbacks(timeout);
            taskThread.post(new Runnable() {
                @Override
                public void run() {
                    task.onTaskDown();
                }
            });
            return true;
        } else{
            sendConformMessage(task.command,contentlen,crc0,crc1);
            return false;
        }
    }
    void write(ByteableTask task) {
        if(task.command==ByteableTask.CMD_ADD_ADMIN_FINGER
                ||task.command==ByteableTask.CMD_ADD_USER_FINGER
                ||task.command==ByteableTask.CMD_RESET_ALL)
        {
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
            init = init + (data[i]&0xFF);
        }
        data[len - 3] = (byte) (init >> 8);
        data[len - 2] = (byte) (init % 256);
    }

    class SendTask implements Runnable {
        @Override
        public void run() {
            currentTask.onTaskStart();
            if (currentTask instanceof ByteableTask) {
                write((ByteableTask) currentTask);
            }
            taskThread.postDelayed(timeout, currentTask.getTimeout());
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
                if (proccess((ByteableTask)currentTask, bytes)) {
                    eraseCurrentTask();
                    sendNext();
                }
            }
        }
    }

    class TimeoutTask implements Runnable {
        @Override
        public void run() {
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
    long        tryTimes=0;
    boolean     connect=true;
    void        onWrite()
    {
        if(net.login)
            delayBeat();
    }
    void        onRead()
    {
        boolean needfire=false;
        if(connect==false) {
            needfire = true;
            connect = true;
        }
        longwait=false;
        net.connected =true;
        if(net.login)
            delayBeat();
        if(needfire)
            net.fireStateChange(OnNetStateChange.NetState.Pending);

    }
    void changeLongHeatTickTime(boolean longtime)
    {
       longwait=longtime;
    }
    boolean longwait=false;
    long HEAT_TICK_TIME=4000;
    Runnable    sendbeat=new Runnable() {
        @Override
        public void run() {
            if(longwait)
            {
                net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
                return;
            }
            if(connect==false)
            {
                net.connected =false;
                net.fireStateChange(OnNetStateChange.NetState.Pending);
                //net.transaction.end();
                stopBeat();
                startRetry();
            }else
            {
                net.write(heartbeat);
                connect=false;
                net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
            }
        }
    };
    void startBeat()
    {
        connect=true;
        net.taskThread.removeCallbacks(sendbeat);
        net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME+2000);
    }
    Heartbeat()
    {
        net=Net.get();
    }
    void stopBeat()
    {
        net.taskThread.removeCallbacks(sendbeat);
    }
    void delayBeat()
    {
        net.taskThread.removeCallbacks(sendbeat);
        net.taskThread.postDelayed(sendbeat, HEAT_TICK_TIME);
    }

    void startRetry()
    {
        tryTimes=0;
        net.taskThread.post(this);
    }
    void stopRetry()
    {
        net.taskThread.removeCallbacks(Heartbeat.this);
    }
    @Override
    public void run() {
        if(net.isSendable())
        {
            stopRetry();
        }else
        {
            if(tryTimes>3)
            {
                stopRetry();
                net.disable();
                return;
            }
            tryTimes=tryTimes+1;
            net.connect().login();
            net.taskThread.postDelayed(this, 8000);
        }
    }
}


class Connector extends Task {

    public Connector(NetTransfer transfer) {
        super(transfer);
    }


    @Override
    public long delayTime()
    {
        return 500;
    }
    @Override
    public long getTimeout()
    {
        return 5000;
    }
    @Override
    protected void onTaskStart() {
        Log.e("DATA", "time to connected");


        if (transfer.device != null) {
            transfer.fireStateChange(OnNetStateChange.NetState.Connecting);
            transfer.rawConnection = transfer.device.connectGatt(transfer.appContext, false, transfer.rawCallback);
        }
    }
    @Override
    protected void onTaskDown() {
        transfer.fireStateChange(OnNetStateChange.NetState.Connected);
        Log.e("DATA","time connected");
    }
    @Override
    protected void onTimeout() {
        if(transfer.device==null)
        {
            transfer.reset();
            transfer.fireStateChange(OnNetStateChange.NetState.ConnectFailed);
            Log.e("DATA", "timeout connected");
        }
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
            Bus.post(new DeviceDiscovered(device));
        }
    };

    Scanner(NetTransfer transfer) {
        super(transfer);
    }
    @Override
    public long delayTime()
    {
        return 0;//wait bluetooth  device enable
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
        transfer.fireStateChange(OnNetStateChange.NetState.Searching);
        devices.clear();
        if(transfer.adapter!=null)
        {
            transfer.adapter.enable();
            transfer.adapter.startLeScan(scanner);
        }
    }

    public void stopScan() {
        if(transfer.adapter!= null)
        {
            transfer.adapter.stopLeScan(scanner);
        }
        devices.clear();
        transfer.eraseCurrentTask();
    }

    @Override
    protected void onTimeout() {
        stopScan();
        transfer.commands.clear();
        transfer.fireStateChange(OnNetStateChange.NetState.SearchTimeout);
    }
}

class LoginTask extends ByteableTask {
    public LoginTask(NetTransfer transfer, byte[] password) {
        super(transfer, ByteableTask.CMD_AUTH,password);
    }

    @Override
    protected void onTimeout() {
        Log.e("DATA", "timeout to login");
        transfer.fireStateChange(OnNetStateChange.NetState.LoginFailed);
    }
    public long delayTime() {
        return 1000;
    }
    public long getTimeout() {
        return 2000;
    }

    @Override
    protected void onTaskDown() {
        if(getError()!=0)
        {
            transfer.fireStateChange(OnNetStateChange.NetState.ConnectFailed);
            return;
        }
        Log.e("DATA", "time login");
        transfer.login = true;
        BluetoothDevice device=transfer.getConnected();
        //update last current address
        String mac=device.getAddress();
        transfer.fireStateChange(OnNetStateChange.NetState.LoginSuccess);
    }
    @Override
    protected void onTaskStart() {
        Log.e("DATA", "time to login");
        transfer.login = false;
    }
}
