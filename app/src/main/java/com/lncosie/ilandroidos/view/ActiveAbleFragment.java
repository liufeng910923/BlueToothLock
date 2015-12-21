package com.lncosie.ilandroidos.view;

import android.support.v4.app.Fragment;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.squareup.otto.Subscribe;

/**
 * Created by Administrator on 2015/11/17.
 */
public class ActiveAbleFragment extends Fragment {
    protected void onActive(Object arg){

    }
    protected boolean checkSendable(){
        if(true)
            return true;
        if(!Net.get().isSendable()){
            Bus.post(new NetworkError());
            return false;
        }
        return true;
    }

}
