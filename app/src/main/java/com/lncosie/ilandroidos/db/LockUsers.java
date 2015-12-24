package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;


//@Table(name = "LockUsers")
public class LockUsers extends Model {

    /**
     * 逻辑用户id
     */
    @Column(name = "GID")
    public long gid;
    /**
     * 锁用户类型
     */
    @Column(name = "TYPE")
    public int type;
    /**
     * 锁用户id
     */
    @Column(name = "UID")
    public int uid;

    public byte type() {
        return (byte) type;
    }

    public byte uid() {
        return (byte) uid;
    }
}