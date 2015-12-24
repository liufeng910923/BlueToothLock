package com.lncosie.ilandroidos.bus;

import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Administrator on 2015/11/10.
 */
public class Bus {
    static com.squareup.otto.Bus bus;

    public static void busInit() {
        bus = new com.squareup.otto.Bus(ThreadEnforcer.ANY);
    }

    public static void post(Object message) {
        bus.post(message);
    }

    public static void register(Object receiver) {
        bus.register(receiver);
    }

    public static void unregister(Object receiver) {
        bus.unregister(receiver);
    }
}
