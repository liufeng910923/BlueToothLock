package com.lncosie.ilandroidos.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.OnNetStateChange;
import com.lncosie.ilandroidos.bus.BluetoothConneted;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.TryLogin;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.squareup.otto.Subscribe;

import java.util.Stack;

/**
 * Created by Administrator on 2015/11/18.
 */
public abstract class EventableActivity extends AppCompatActivity {


    static EventableActivity lastactivity=null;
    static boolean pauseDetect=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    protected boolean checkSendable() {
        if (!Net.get().isSendable()) {
            Bus.post(new NetworkError());
            return false;
        }
        return true;
    }
    public void backward(View v) {
        this.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(pauseDetect)
            return;
        Repass.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastactivity=this;
        if(pauseDetect)
            return;
        Repass.resume(this);
    }

    static class HideWhenTouchOutside {

        static void touch_hide_keyboard(View view,InputMethodManager imm) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        public static void setupUI(Activity activity){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            setupUI(activity,activity.getWindow().getDecorView(),imm);

        }
         public static void setupUI(Activity activity, View view,InputMethodManager imm) {

            if (!(view instanceof EditText)) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        touch_hide_keyboard(view,imm);
                        return false;
                    }
                });
            }
            //If a layout container, iterate over children and seed recursion.
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View innerView = ((ViewGroup) view).getChildAt(i);
                    setupUI(activity, innerView,imm);
                }
            }
        }

    }

    void showLoginPassword(final boolean isErrorPassword){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Net net=Net.get();
                if(net.stateRetrying==false)
                {
                    showLogin(isErrorPassword);
                }
            }
        });
    }
    void showLogin(final boolean isErrorPassword) {
        if(AuthPasswordFragment.glob!=null)
            return;


        if(isErrorPassword||!shouldShowPattern()){
            Applyable applyable = new Applyable() {
                @Override
                public void apply(Object arg0, Object arg1) {
                    DbHelper.setPassword(Net.get().getMac(), (String) arg0);
                    Bus.post(new TryLogin());
                }
            };
            AuthPasswordFragment fragment = AuthPasswordFragment.newInstance(true, R.string.password_with_id, 0, applyable);
            fragment.show(lastactivity.getSupportFragmentManager(),"");
        }else{
            Intent intent=new Intent(this,PatternActivity.class);
            startActivity(intent);
        }
    }

    boolean shouldShowPattern(){
        if(DbHelper.getPassword(Net.get().getMac())==null){
            return false;
        }
        String pass=DbHelper.getPatternPwd();
        if(pass!=null&&pass.length()!=0) {
            return true;
        }
        return false;
    }
}
