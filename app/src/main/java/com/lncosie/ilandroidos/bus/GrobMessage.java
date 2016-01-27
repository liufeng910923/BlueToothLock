package com.lncosie.ilandroidos.bus;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.lncosie.ilandroidos.R;
import com.squareup.otto.Subscribe;

public class GrobMessage {
    Activity context;
    Toast toast;

    public GrobMessage(Activity context) {
        Bus.register(this);
        this.context = context;
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
    }
    @Subscribe
    public void OnNetDisconnect(ErrorPassword error) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                toast = Toast.makeText(context, R.string.password_error, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
    @Subscribe
    public void OnNetDisconnect(NetworkError error) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                toast = Toast.makeText(context, R.string.network_disconnect, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
    @Subscribe
    public void OnDisConnected(LoginFailed state) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                toast = Toast.makeText(context, R.string.connect_failed, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }
    @Subscribe
    public void showTip(TipOperation tip) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                Toast toast = Toast.makeText(context, tip.message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }
}
