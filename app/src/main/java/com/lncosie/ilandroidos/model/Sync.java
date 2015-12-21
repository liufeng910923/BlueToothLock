package com.lncosie.ilandroidos.model;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;

import com.lncosie.ilandroidos.bluenet.ByteableTask;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.NetTransfer;
import com.lncosie.ilandroidos.db.History;
import com.lncosie.ilandroidos.db.LockUsers;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.Users;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/8/12.
 */
public class Sync {

    public static void syncIds(Applyable apply) {
        Net net=Net.get();
        if(!net.isSendable()){
            if(apply!=null)
                apply.cancel();
            return;
        }

        net.sendChecked(new SynIDs(net, (byte) 0, null));
        net.sendChecked(new SynIDs(net, (byte) 1, apply));
    }

    public static void syncHistory(Applyable apply) {
        Net net=Net.get();
        if(!net.isSendable()){
            apply.cancel();
            return;
        }
        net.sendChecked(new SynHistory(Net.get(), apply));
    }
}

class SynIDs extends ByteableTask {
    public SynIDs(NetTransfer transfer, byte type,Applyable apply) {
        super(transfer, type == 0 ? ByteableTask.CMD_SYNC_IDS
                : ByteableTask.CMD_SYNC_IDS_FINGER);
        this.type = type;
        this.apply=apply;
    }

    protected long getTimeout() {
        return 10000;
    }
    byte type = 0;
    Applyable   apply;

    @Override
    protected void onTimeout() {
        if(apply!=null)
            apply.cancel();
    }

    @Override
    protected void onTaskDown() {

        final byte[] bytes = getData();
        try {
            List<Byte> remote = new ArrayList<>();
            List<Byte> local = new ArrayList<>();
            for (int i = 0; i < bytes.length - 1; i += 2) {
                remote.add(bytes[i]);
            }
            List<LockUsers> locals = DbHelper.getCurLockUsers(type);
            for (LockUsers user : locals) {
                local.add(user.uid());
            }
            remote.removeAll(local);
            addHandAddUsers(type, remote);
            remote.clear();
            for (int i = 0; i < bytes.length - 1; i += 2) {
                remote.add(bytes[i]);
            }
            for (LockUsers id : locals) {
                if (id.type == type && !remote.contains(id.uid())) {
                    DbHelper.deleteAuth(id);
                }
            }
            if(apply!=null)
                apply.apply(null, null);
            //removeHandDelUsers(locals);
        } catch (Exception e) {
            e.printStackTrace();
            if(apply!=null)
                apply.cancel();
        }
    }



    static void addHandAddUsers(byte type, List<Byte> users) {
        String mac = DbHelper.getCurMac();
        for (Byte id : users) {
            Users user = new Users();
            user.mac = mac;
            user.name = String.format("%s%d",type==0?"密码用户":"指纹用户",id);
            user.save();
            UserDetail  detail = new UserDetail();
            detail.uid = id;
            detail.type = type;
            detail.gid = user.getId();
            detail.save();
        }
    }
}

class SynHistory extends ByteableTask {
    Applyable applyable;
    public SynHistory(NetTransfer transfer,Applyable whenDown) {
        super(transfer, ByteableTask.CMD_GET_HISTORY);
        this.applyable =whenDown;
    }

    protected long getTimeout() {
        return 30_000;
    }

    @Override
    protected void onTimeout() {

        if(applyable !=null)
        {
            //Toast.makeText(transfer.appContext, R.string.sync_failed,Toast.LENGTH_SHORT).show();
            applyable.cancel();
        }
    }

    @Override
    protected void onTaskDown() {
        DbHelper.eraseHistory();
        byte[] data = getData();
        //if(data.length%7!=0)
        //    return;
        try {
            //ActiveAndroid.beginTransaction();
            DbHelper.eraseHistory();
            long time = 0;
            for (int i = 0; i < data.length - 1; i = i + 7) {
                time = toDate(data, i + 2);
                if (time != 0) {
                    History  history = new History();
                    history.type = toType(data[i + 0]);
                    history.uid = data[i + 1];
                    history.time = time;
                    history.gid=0;
                    history.save();
                }
            }
            DbHelper.syncDbHistory();
        } catch (Exception e) {
            //ActiveAndroid.endTransaction();
            if(applyable !=null)
                applyable.cancel();
            return;
        }
        //ActiveAndroid.setTransactionSuccessful();
        //ActiveAndroid.endTransaction();
        if(applyable !=null){
            applyable.apply(null, null);
        }
    }

    long toDate(byte[] data, int index) {
        long ti = 0;
        for (int i = index; i < index + 5; i++) {
            ti = ti * 100 + data[i];
        }
        return ti;
    }

    byte toType(byte type) {
        //42(开门方式41密码42指纹43感应卡44遥控)
        switch (type) {
            case 0x41:
                return 0;
            case 0x42:
                return 1;
            //case 43:
            //    return 0;
            case 0x44:
                return 2;
            default:
                return 0;
        }

    }

}