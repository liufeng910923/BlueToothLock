package com.lncosie.ilandroidos.bluenet;


public abstract class Task {
    NetTransfer transfer;
    public Task(NetTransfer transfer) {
        this.transfer = transfer;
    }
    long    timeout=4000;
    public long delayTime() {
        return 200;
    }
    public void setTimeout(long timeout) {
        this.timeout=timeout;
    }
    protected long getTimeout() {
        return timeout;
    }
    protected void onTimeout() {
    }
    protected void onTaskStart() {
    }
    protected abstract void onTaskDown();
}


