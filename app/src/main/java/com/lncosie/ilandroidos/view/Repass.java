package com.lncosie.ilandroidos.view;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.AuthSuccess;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ErrorPassword;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;

/**
 * Created by galax on 2015/12/24.
 */
public class Repass{
    static long tick=0;
    static boolean exit=false;

    public static void exit(){
        exit=true;
    }
    public static void pause(){
        tick= System.currentTimeMillis();
    }
    public static void resume(FragmentActivity activity){
        if(exit)
        {
            exit=false;
            return;
        }
        long sencond=(System.currentTimeMillis()-tick);
        if(Net.get().isSendable())
        if(sencond>1000){
            Applyable applay=new Applyable() {
                @Override
                public void apply(Object arg0, Object arg1) {
                    if(DbHelper.checkPassword((String)arg0)){
                        Bus.post(new AuthSuccess());
                    }else{
                        Bus.post(new ErrorPassword());
                    }
                }
            };
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AuthPasswordFragment auth=AuthPasswordFragment.newInstance(true,R.string.password_with_id,R.string.password_with_id_prompt,applay);
                    auth.show(activity.getSupportFragmentManager(),"");
                }
            });

        }
    }
}
