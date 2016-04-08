package com.lncosie.ilandroidos.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.BluetoothConneted;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.UserSet;
import com.lncosie.ilandroidos.bus.ViewUserLog;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.bus.GrobMessage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AppActivity extends EventableActivity {


    @Bind(R.id.container)
    ViewPager container;
    @Bind(R.id.page_home)
    RadioButton pageHome;
    @Bind(R.id.page_history)
    RadioButton pageHistory;
    @Bind(R.id.page_users)
    RadioButton pageUsers;
    @Bind(R.id.page_setting)
    RadioButton pageSetting;
    GrobMessage grob;
    RadioButton prevActive;
    private SectionsPagerAdapter adapter;

    public Context context;
    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    ImageView imageView =(ImageView)msg.obj;
                    imageView.setImageBitmap(bitmap);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        ButterKnife.bind(this);
        Bus.register(this);
        context=this;
        grob = new GrobMessage(this);
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        container.setOffscreenPageLimit(4);
        container.setAdapter(adapter);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
    }

    @Override
    public void onBackPressed() {
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                exit();
            }
        };
        MenuYesnoFragment fragment = MenuYesnoFragment.newInstance(R.string.exit,
                getString(R.string.exit_tip), applyable);
        fragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onPause(){
//        pauseDetect=false;
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Subscribe
    public void setPause(UserSet userSet){
        pauseDetect=true;
    }

    void exit() {
        Net.get().reset();
        Repass.exit();
        super.onBackPressed();
    }
    @Subscribe
    public void bluetoothConneted(BluetoothConneted state){
        showLoginPassword(state.needPassword);
    }
    @Subscribe
    public void langChanged(LanguageChanged languageChanged) {
        changeLang(container);
        pageHome.setText(R.string.home);
        pageUsers.setText(R.string.users);
        pageHistory.setText(R.string.history);
        pageSetting.setText(R.string.setting);
    }

    void changeLang(ViewGroup g) {
        for (int i = 0; i < g.getChildCount(); i++) {
            View v = g.getChildAt(i);
            if (v instanceof LanguageTextView) {
                LanguageTextView lang = (LanguageTextView) v;
                lang.languageChanged();
            }
            if (v instanceof LanguageEditView) {
                LanguageEditView lang = (LanguageEditView) v;
                lang.languageChanged();
            } else if (v instanceof ViewGroup) {
                changeLang((ViewGroup) v);
            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }

    @Subscribe
    public void viewUserLog(ViewUserLog userLog) {
        estateButton(pageHistory);
        activePage(2, userLog.gid);
    }

    public void clickNegative(View v) {
        Object tag = estateButton((RadioButton) v);
        Integer index = Integer.valueOf((String) tag);
        if (container.getCurrentItem() == index)
            return;
        activePage(index, null);
    }

    private Object estateButton(RadioButton v) {
        RadioButton button = v;
        button.setChecked(true);
        Object tag = button.getTag();
        if (prevActive != null)
            prevActive.setTextColor(getResources().getColor(android.R.color.black));
        else
            pageHome.setTextColor(getResources().getColor(android.R.color.black));
        button.setTextColor(getResources().getColor(R.color.colorActiveText));
        prevActive = button;
        return tag;
    }

    private void activePage(int index, Object arg) {
        ((ActiveAbleFragment) adapter.getItem(index)).onActive(arg);
        container.setCurrentItem(index);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ActiveAbleFragment fragments[] = new ActiveAbleFragment[4];
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            if (fragments[position] == null) {

                ActiveAbleFragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = new HomeFragment();
                        break;
                    case 1:
                        fragment = new UsersFragment();
                        break;
                    case 2:
                        fragment = new HistoryFragment();
                        break;
                    case 3:
                        fragment = new SettingsFragment();
                        break;
                }

                fragments[position] = fragment;
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int res = 0;
            switch (position) {
                case 0:
                    res = R.string.app_name;
                    break;
                case 1:
                    res = R.string.app_name;
                    break;
                case 2:
                    res = R.string.app_name;
                    break;
                case 3:
                    res = R.string.app_name;
                    break;
            }
            return getString(res);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
        }
    }

}
