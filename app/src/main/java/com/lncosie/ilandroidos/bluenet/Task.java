package com.lncosie.ilandroidos.bluenet;


public abstract class Task {
    NetTransfer net;
    long timeout = 4000;

    public Task(NetTransfer transfer) {
        this.net = transfer;
    }

    public long delayTime() {
        return 200;
    }

    protected long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    protected void onTimeout() {
    }

    protected void onTaskStart() {
    }

    protected abstract void onTaskDown();
}


