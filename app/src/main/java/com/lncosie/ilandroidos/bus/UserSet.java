package com.lncosie.ilandroidos.bus;

/**
 * author: Leo
 * Date: 2016/3/31
 * Time: 08:48
 * E-mail: liufeng910923@gmail.com
 * Funcation: 通知用户信息更改
 */
public class UserSet {

    long userId;

    public long getUserId() {
        return userId;
    }

    public UserSet(long userId) {
        this.userId = userId;
    }
}
