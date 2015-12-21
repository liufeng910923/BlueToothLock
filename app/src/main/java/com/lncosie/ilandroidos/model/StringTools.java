package com.lncosie.ilandroidos.model;

/**
 * Created by Administrator on 2015/11/13.
 */
public class StringTools {
    public static byte[] getPwdBytes(int uid,String p){
        byte[] id_with_password = new byte[]{25, 25, 0, 0, 0, 0, 0, 0};
        id_with_password[0] = (byte) (uid / 10);
        id_with_password[1] = (byte) (uid % 10);
        for (int i = 2; i < 8; i++) {
            id_with_password[i] = (byte) (p.charAt(i - 2) - '0');
        }
        return id_with_password;
    }
    public static byte[] getPwdBytes(String passwordWithId){
        byte[] id_with_password = new byte[]{0, 0, 1,2,3,4,5,6};
        if(passwordWithId!=null)
            for (int i = 0; i < 8; i++) {
                id_with_password[i] = (byte) (passwordWithId.charAt(i) - '0');
            }
        return id_with_password;
    }
    public static byte[] getPwdBytes(int uid,String p,String pRe) {

        if (p.length() < 6) {
            return null;
        }
        if (pRe.length() < 6) {
            return null;
        }
        if (!p.equals(pRe)) {
            return null;
        }
        return getPwdBytes(uid,p);
    }
}
