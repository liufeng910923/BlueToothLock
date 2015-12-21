package com.lncosie.ilandroidos.view;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.squareup.otto.Subscribe;

/**
 * Created by Administrator on 2015/11/18.
 */
public abstract class EventableActivity extends AppCompatActivity {
    public void backward(View v){
        this.onBackPressed();
    }
    protected boolean checkSendable(){
        if(!Net.get().isSendable()){
            Bus.post(new NetworkError());
            return false;
        }
        return true;
    }
}
