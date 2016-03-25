package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Administrator on 2015/11/9.
 */
@Table(name = "ConnectedLocks")
public class ConnectedLocks extends Model {
    @Column(name = "NAME")
    public String name;
    @Column(name = "MAC")
    public String mac;
    @Column(name = "PASSWORD")
    public String password;

    @Override
    public String toString() {
        return "Lock:    "+name+"   "+mac+"     "+password;
    }
}
