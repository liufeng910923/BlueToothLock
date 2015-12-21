package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name = "Users")
public class Users extends Model {
    @Column(name = "MAC")
    public String mac;
    @Column(name = "NAME")
    public String name;
    @Column(name = "IMAGE")
    public String image;
}
