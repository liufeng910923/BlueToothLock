package com.lncosie.ilandroidos.db;

import android.widget.ImageView;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.utils.BitmapUtil;
import com.lncosie.ilandroidos.utils.UserTools;

import de.hdodenhof.circleimageview.CircleImageView;


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

    @Override
    public String toString() {
        return "user :"
                +"gid: "+gid
                +"name  "+name
                +"image  "+image
                +"pAccounts  "+pAccounts
                +"rAccounts  "+rAccounts
                +"fAccounts  "+fAccounts
                +"type  "+type
                +"time  "+time
                ;
    }



}
