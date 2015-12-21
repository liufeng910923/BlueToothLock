package com.lncosie.ilandroidos.model;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.ByteableTask;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.NetTransfer;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.OperatorMessages;
import com.lncosie.ilandroidos.bus.TipOperation;


public class InterlockOperation {

    public static void  deleteId(int type,int id) {
        byte cmd=type==0?(id<5?ByteableTask.CMD_DEL_ADMIN_PWD:ByteableTask.CMD_DEL_PWD):
                (id<5?ByteableTask.CMD_DEL_ADMIN_FINGER:ByteableTask.CMD_DEL_USER_FINGER);
        Net.get().sendChecked(new NotifyabbleTask(Net.get(),cmd,new byte[]{(byte)(id/10),(byte)(id%10)}) {
            protected void onTaskDown(){
                Bus.post(new OperatorMessages.OpDelAuth(command,getError(),(byte)0,(byte)0));
            }
        });
    }
    public static void  deleteIdSlient(int type,int id,Applyable applyable) {
        byte cmd=type==0?(id<5?ByteableTask.CMD_DEL_ADMIN_PWD:ByteableTask.CMD_DEL_PWD):
                (id<5?ByteableTask.CMD_DEL_ADMIN_FINGER:ByteableTask.CMD_DEL_USER_FINGER);
        Net.get().sendChecked(new ByteableTask(Net.get(),cmd,new byte[]{(byte)(id/10),(byte)(id%10)}) {
            protected void onTaskDown(){
                if(getError()==0){
                    applyable.apply(null,null);
                }else {
                    applyable.cancel();
                }
            }
            protected void onTimeout(){
                super.onTimeout();
                applyable.cancel();
            }
        });
    }
    public static void  modifyPwd(int type,int id,byte[] idWithPwd) {
        Net.get().sendChecked(new NotifyabbleTask(Net.get(),ByteableTask.CMD_MODIFY_PWD,idWithPwd) {
            @Override
            protected void onTaskDown() {
                Bus.post(new TipOperation(getError(), R.string.succ_op_pwd));
            }
        });
    }
    public static void  setTime(int year,int month,int day,int hour,int minus){
        final byte[] time = TimeTools.toTime(year, month + 1, day, hour, minus);
        Net.get().sendChecked(new NotifyabbleTask(Net.get(),ByteableTask.CMD_SET_TIME, time) {
            @Override
            protected void onTaskDown() {
                Bus.post(new TipOperation(getError(), R.string.succ_op_time));
            }
        });
    }
    public static void  setVocice(int vocice){
        int    volume=0xaa;
        switch (vocice) {
            case 3:
                volume=0xaa;
                break;
            case 2:
                volume=0xbb;
                break;
            case 1:
                volume=0xcc;
                break;
            case 0:
                volume=0xdd;
                break;
        }
        Net.get().sendChecked(new NotifyabbleTask(Net.get(),ByteableTask.CMD_SET_VOLUMN, (byte)volume) {
            @Override
            protected void onTaskDown() {
                Bus.post(new TipOperation(getError() , R.string.succ_op_volume));
            }
        });
    }

    public static void setToFactory() {
        ByteableTask task=new NotifyabbleTask(Net.get(), ByteableTask.CMD_RESET_ALL) {
            @Override
            protected void onTaskDown() {
                Bus.post(new TipOperation(getError(), R.string.succ_op_factory));
            }
        };
        task.setTimeout(5000);
        Net.get().sendChecked(task);
    }
    public static void resetAdminPwd() {
        byte[] init={0,0,1,2,3,4,5,6};
        Net.get().sendChecked(new NotifyabbleTask(Net.get(), ByteableTask.CMD_MODIFY_PWD,init) {
            @Override
            protected void onTaskDown() {
                Bus.post(new TipOperation(getError(), R.string.succ_op_resetadmin));
            }
        });
    }
    public static void getVolume(final Applyable applyable) {
        Net.get().sendChecked(new NotifyabbleTask(Net.get(), ByteableTask.CMD_GET_VOLUMN) {
            @Override
            protected void onTaskDown() {
                if (getError() != 0)
                    return;
                byte[] data = getData();
                int pos = 0;
                switch (data[1]) {
                    case (byte) 0xaa:
                        pos = 3;
                        break;
                    case (byte) 0xbb:
                        pos = 2;
                        break;
                    case (byte) 0xcc:
                        pos = 1;
                        break;
                    case (byte) 0xdd:
                        pos = 0;
                        break;
                }
                applyable.apply(pos, null);
            }
        });
    }

    public static void  getVersion(final Applyable applyable){
        Net.get().sendChecked(new NotifyabbleTask(Net.get(), ByteableTask.CMD_GET_VERSION) {
            @Override
            protected void onTaskDown() {
                byte[] data = getData();
                String text = String.format("Version: %d.%d Model:HZ%d%d%d%d%d", data[6], data[7], data[1], data[2], data[3], data[4], data[5]);
                applyable.apply(text, null);
            }
        });
    }

    public static void getSpace(final Applyable applyable){
        Net.get().sendChecked(new NotifyabbleTask(Net.get(), ByteableTask.CMD_GET_FREE_SPACE) {
            @Override
            protected void onTaskDown() {
                byte[] data = getData();
                String d1 = String.format("%d/25", data[2]);
                String d2 = String.format("%d/100", data[4]);
                applyable.apply(d1, d2);
            }
        });
    }
    public static void  getTime(final Applyable applyable){
        Net.get().sendChecked(new NotifyabbleTask(Net.get(), ByteableTask.CMD_GET_TIME) {
            @Override
            protected void onTaskDown() {
                byte[] data = getData();
                String text=TimeTools.toString(TimeTools.toTime(data,0));
                applyable.apply(text, null);
            }
        });
    }
    public static  class TaskGetPwdID extends IdValueTask{
        public TaskGetPwdID(NetTransfer transfer, boolean admin) {
            super(transfer, admin ? ByteableTask.CMD_ADD_ADMIN_PWD : ByteableTask.CMD_ADD_USER_PWD, new byte[]{25, 25, 0, 0, 0, 0, 0, 0});
        }

    }
    public static class TaskAddPwd extends IdValueTask{
        public TaskAddPwd(NetTransfer transfer, boolean admin,byte[] password) {
            super(transfer, admin ? ByteableTask.CMD_ADD_ADMIN_PWD : ByteableTask.CMD_ADD_USER_PWD, password);
        }
    }
    public static class TaskAddFinger extends IdValueTask{
        public TaskAddFinger(NetTransfer transfer, boolean admin) {
            super(transfer, admin?ByteableTask.CMD_ADD_ADMIN_FINGER:ByteableTask.CMD_ADD_USER_FINGER);
            this.setTimeout(4500);
        }
        @Override
        protected void onTimeout() {
            Bus.post(new TipOperation(-1, R.string.timeout_op_finger));
        }
    }
    public abstract static class IdValueTask extends NotifyabbleTask{


        public IdValueTask(NetTransfer transfer, byte cmd) {
            super(transfer, cmd);
        }

        public IdValueTask(NetTransfer transfer, byte cmd, byte[] data) {
            super(transfer, cmd, data);
        }

        @Override
        protected void onTaskDown() {
            if (getError() != 0) {
                Bus.post(new OperatorMessages.OpAddAuth(command,getError(),(byte)0,(byte)0));
                return;
            }else {
                byte[] data = getData();
                int id = data[1];
                id =id*10+ data[2];
                byte type=0;
                if(command==ByteableTask.CMD_ADD_ADMIN_FINGER||command==ByteableTask.CMD_ADD_USER_FINGER){
                    type=1;
                }
                Bus.post(new OperatorMessages.OpAddAuth(command,getError(),type,(byte)id));
            }
        }
    }
    public abstract static class NotifyabbleTask extends ByteableTask{

        public NotifyabbleTask(NetTransfer transfer, byte cmd, byte data) {
            super(transfer, cmd, data);
        }

        public NotifyabbleTask(NetTransfer transfer, byte cmd) {
            super(transfer, cmd);
        }

        public NotifyabbleTask(NetTransfer transfer, byte cmd, byte[] data) {
            super(transfer, cmd, data);
        }
        @Override
        protected void onTimeout() {
            Bus.post(new TipOperation(-1,R.string.timeout_op));
        }
    }
}
