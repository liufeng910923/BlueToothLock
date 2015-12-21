package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;



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
    public byte type(){return (byte)type;}
    /**
     * 锁用户id
     */
    @Column(name = "UID")
    public int uid;
    public byte uid(){return (byte)uid;}
}