package com.lncosie.ilandroidos.bluenet;


public abstract class ByteableTask extends Task {
    public final static byte CMD_DEBUG = 0x2f;
    public final static byte HEART_BEAT = 0x24;
    public final static byte CMD_AUTH = 0x01;
    public final static byte CMD_DISCONNECT = 0x00;

    public final static byte CMD_ADD_ADMIN_FINGER = 0x02;
    public final static byte CMD_ADD_ADMIN_PWD = 0x03;
    public final static byte CMD_DEL_ADMIN_FINGER = 0x04;
    public final static byte CMD_DEL_ADMIN_PWD = 0x05;

    public final static byte CMD_MODIFY_AUTH = 0x06;
    public final static byte CMD_DEL_ALL_USER_FINGER = 0x07;
    public final static byte CMD_DEL_ALL_USER_PWD = 0x08;
    public final static byte CMD_MODIFY_PWD = 0x09;

    public final static byte CMD_DEL_USER_FINGER = 0x0c;
    public final static byte CMD_DEL_PWD = 0x0d;

    public final static byte CMD_ADD_USER_FINGER = 0x10;
    public final static byte CMD_ADD_USER_PWD = 0x11;

    public final static byte CMD_GET_USER_INFO = 0x14;
    public final static byte CMD_GET_TIME = 0x26;
    public final static byte CMD_GET_VOLUMN = 0x27;
    public final static byte CMD_GET_SAFE_STATE = 0x17;

    public final static byte CMD_SET_TIME = 0x18;
    public final static byte CMD_SET_VOLUMN = 0x19;
    public final static byte CMD_SET_SAFE_STATE = 0x1a;

    public final static byte CMD_RESET_ALL = 0x1b;
    public final static byte CMD_RESET_ADMIN = 0x1b;
    public final static byte CMD_RESET_HISTORY = 0x1c;
    public final static byte CMD_GET_HISTORY = 0x20;
    public final static byte CMD_GET_FREE_SPACE = 0x28;
    public final static byte CMD_GET_VERSION = 0x29;

    public final static byte CMD_SYNC_IDS = 0x21;
    public final static byte CMD_SYNC_IDS_FINGER = 0x22;


    final static byte CMD_HEADER_FC = -0x04;
    final static byte CMD_HEADER_FD = -0x03;
    final static byte CMD_END = -0x02;
    public byte command;
    DataBuilder send = new DataBuilder();
    DataBuilder receive = new DataBuilder();
    byte error;

    public ByteableTask(NetTransfer transfer, byte cmd) {
        super(transfer);
        command = cmd;
        send.append(cmd);
    }

    public ByteableTask(NetTransfer transfer, byte cmd, byte data) {
        super(transfer);
        command = cmd;
        send.append(cmd).append(data);
    }

    public ByteableTask(NetTransfer transfer, byte cmd, byte[] data) {
        super(transfer);
        command = cmd;
        send.append(cmd).append(data);
    }

    public ByteableTask(NetTransfer transfer, byte cmd, byte[] data1, byte[] data2) {
        super(transfer);
        command = cmd;
        send.append(cmd).append(data1).append(data2);
    }

    public byte getError() {
        return error;
    }

    public void setError(byte error) {
        this.error = error;
    }

    public void fromBytes(byte[] data) {
        receive.append(data);
    }

    public byte[] toBytes() {
        return send.builder();
    }

    protected byte[] getData() {
        return receive.builder();
    }
}
