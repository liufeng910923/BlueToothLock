package com.lncosie.ilandroidos.bluenet;

/**
 * Created by Administrator on 2015/8/12.
 */
public class AddPasswordUser extends ByteableTask {
    public AddPasswordUser(NetTransfer transfer, byte cmd, byte[] data1, byte[] data2) {
        super(transfer, cmd, data1, data2);
    }

    @Override
    protected void onTaskDown() {

    }
}
