package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


/**
 * 开锁历史
 */
@Table(name = "History")
public class History extends Model {
    /**
     *
     */
    @Column(name = "GID")
    public long gid;
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


}
