package com.lncosie.ilandroidos.model;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ErrorPassword;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.TipOperation;
import com.squareup.otto.Subscribe;

public class GrobMessage {
    Context context;
    Toast toast;
    public GrobMessage(Context context){
        Bus.register(this);
        this.context=context;
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    @Subscribe
    public void    OnNetDisconnect(ErrorPassword error){
        toast.cancel();
        toast=Toast.makeText(context, R.string.password_error, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    @Subscribe
    public void    OnNetDisconnect(NetworkError error){
        toast.cancel();
        toast=Toast.makeText(context, R.string.network_disconnect, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Subscribe
    public void     showTip(TipOperation tip){
        toast.cancel();
        Toast toast=Toast.makeText(context, tip.message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
