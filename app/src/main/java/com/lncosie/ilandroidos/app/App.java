package com.lncosie.ilandroidos.app;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.model.DbHelper;

import org.codejargon.feather.Feather;

import java.util.Locale;

public class App extends com.activeandroid.app.Application {
    Feather feather;

    public App() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DbHelper.DbInit();
        Net.get().init(this);
        Bus.busInit();
        feather = Feather.with();

    }

    void setLanguage() {
        String language = DbHelper.getLanguage();
        if (language != null) {
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = new Locale(language);
            res.updateConfiguration(conf, dm);
        }
    }

    @Override
    public void onTerminate() {
        Net.get().reset();
        super.onTerminate();
    }
}
