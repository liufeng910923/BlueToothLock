package com.lncosie.ilandroidos.bluenet;

public class DataBuilder {
    byte[] buffer = new byte[20];
    int pos = 0;

    public DataBuilder() {

    }

    void alloc(int append) {
        if (pos + append > buffer.length) {
            byte a[] = new byte[buffer.length * 2];
            System.arraycopy(buffer, 0, a, 0, pos);
            buffer = a;
        }
    }

    public DataBuilder append(byte[] bytes) {
        if (bytes == null)
            return this;
        alloc(bytes.length);
        System.arraycopy(bytes, 0, buffer, pos, bytes.length);
        pos = pos + bytes.length;
        return this;
    }

    public DataBuilder append(byte bytes) {
        alloc(1);
        buffer[pos] = bytes;
        pos = pos + 1;
        return this;
    }

    public DataBuilder append(DataBuilder bytes) {
        if (bytes == null)
            return this;
        return append(bytes.builder());
    }

    public byte[] builder() {
        byte[] ret = new byte[pos];
        System.arraycopy(buffer, 0, ret, 0, pos);
        return ret;
    }
}