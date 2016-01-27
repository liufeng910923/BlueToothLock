package com.lncosie.ilandroidos.bluenet;

/**
 * Created by Administrator on 2015/8/12.
 */
public interface OnNetStateChange {
    void onChange(NetState state);

    enum NetState {
        Disconnected,
        Searching,
        Connecting,
        Connected,
        NeedPassword,
        LoginFailed,
        Login
    }
}
