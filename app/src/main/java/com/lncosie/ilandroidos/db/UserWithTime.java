package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;


public class UserWithTime extends Model {
    @Column(name = "GID")
    public long gid;
    @Column(name = "NAME")
    public String name;
    @Column(name = "IMAGE")
    public String image;
    @Column(name = "Pc")
    public int pAccounts;
    @Column(name = "Rc")
    public int rAccounts;
    @Column(name = "Fc")
    public int fAccounts;
    @Column(name = "TYPE")
    public int type;
    @Column(name = "TIME")
    public long time;
}
