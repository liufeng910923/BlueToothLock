package com.lncosie.ilandroidos.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.NetworkError;

/**
 * Created by Administrator on 2015/11/18.
 */
public abstract class EventableActivity extends AppCompatActivity {
    public void backward(View v) {
        this.onBackPressed();
    }

    protected boolean checkSendable() {
        if (!Net.get().isSendable()) {
            Bus.post(new NetworkError());
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Repass.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Repass.resume(this);
    }

    static class HideWhenTouchOutside {

        static void touch_hide_keyboard(View view,InputMethodManager imm) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        public static void setupUI(Activity activity){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            setupUI(activity,activity.getWindow().getDecorView(),imm);
            //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
}
