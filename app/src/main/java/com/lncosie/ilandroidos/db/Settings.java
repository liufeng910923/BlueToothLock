package com.lncosie.ilandroidos.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Administrator on 2015/11/9.
 */
@Table(name = "Settings")
public class Settings extends Model {
    @Column(name = "NAME")
    public String name;
    @Column(name = "PASSWORD")
    public String password;
    @Column(name = "MAC")
    public String mac;
    @Column(name = "Language")
    public String language;
}
