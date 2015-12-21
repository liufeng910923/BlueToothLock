package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;


public class TimeWithUser extends Model {
    @Column(name = "MAC")
    public String mac;
    /**
     * 用户类型
     */
    @Column(name = "TYPE")
    public int type;
    /**
     * 用户id
     */
    @Column(name = "UID")
    public int uid;

    /**
     * 开门时间
     */
    @Column(name = "TIME")
    public long time;
    /**
     * 所属逻辑用户id
     */
    @Column(name = "GID")
    public long gid;
    @Column(name = "NAME")
    public String name;
    @Column(name = "IMAGE")
    public String image;

    @Override
    public String toString() {
        return name;
    }
}
