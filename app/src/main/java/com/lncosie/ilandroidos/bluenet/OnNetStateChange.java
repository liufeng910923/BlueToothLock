package com.lncosie.ilandroidos.bluenet;

/**
 * Created by Administrator on 2015/8/12.
 */
public interface OnNetStateChange {
    void onChange(NetState state);

    public enum NetState {
        Idel,
        Searching,
        SearchTimeout,
        Connecting,
        Connected,
        ConnectFailed,
        LoginSuccess,
        LoginFailed,
        Disconnected,
        Pending

    }
}
