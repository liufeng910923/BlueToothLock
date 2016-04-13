package com.lncosie.ilandroidos.bluenet;

import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.OperatorMessages;

public class Net extends NetTransfer {
    static Net glob;

    public static Net get() {
        if (glob == null)
            glob = new Net();
        return glob;
    }

    public void sendUncheck(Task task) {
        if (isSendable())
            super.send(task);
    }

    public void sendChecked(Task task) {
        if (isSendable()){
            super.send(task);
        }

    }
}
