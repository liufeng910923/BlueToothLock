package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


/**
 * 逻辑用户和智能锁用户的关联表
 */
@Table(name = "UserDetail")
public class UserDetail extends Model {

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
}