package com.lncosie.ilandroidos.view;

import android.support.v4.app.FragmentActivity;

import com.lncosie.ilandroidos.bluenet.Net;

/**
 * Created by galax on 2015/12/24.
 */
public class Repass {
    static long tick = 0;
    static boolean resume = false;
    static boolean exit = true;

    public static void exit() {
        exit = true;
    }

    public static void pause(FragmentActivity activity) {
        tick = System.currentTimeMillis();
        resume = false;
        activity.getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!resume) {
                    Net.get().disconnect();
                }
            }
        }, 1000);

    }

    public static void resume(FragmentActivity activity) {
        resume = true;
        if (exit == true) {
            exit = false;
            return;
        }
        long sencond = (System.currentTimeMillis() - tick);
        if (Net.get().shouldReLogin)
            if (sencond > 1000) {
                Net.get().connect();
            }
    }
}
