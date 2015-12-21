package com.lncosie.ilandroidos.view;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.InterlockOperation;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsFragment extends ActiveAbleFragment {
    @Bind(R.id.time_text)
    TextView timeText;
    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.set_volumn_scroll)
    SeekBar setVolumnScroll;

    @Bind(R.id.space_pwd_text)
    TextView spacePwdText;
    @Bind(R.id.space_finger_text)
    TextView spaceFingerText;

    @Bind(R.id.version_text)
    TextView versionText;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        setVolumnScroll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!operatprAble())
                    return;
                InterlockOperation.setVocice(seekBar.getProgress());
            }
        });

        return view;
    }
    @Subscribe
    public void langChanged(LanguageChanged languageChanged){
        title.setText(R.string.setting);
    }
    @Override
    protected void onActive(Object arg){
        super.onActive(arg);
        updataUi();
    }
    void updataUi(){
        Applyable time=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                timeText.setText((String)data);
            }
        };
        InterlockOperation.getTime(time);
        Applyable volumn=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                setVolumnScroll.setProgress((int)data);
            }
        };
        InterlockOperation.getVolume(volumn);
        Applyable space=new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                spacePwdText.setText((String)arg0);
                spaceFingerText.setText((String)arg1);
            }
        };
        InterlockOperation.getSpace(space);
        Applyable version=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                versionText.setText((String)data);
            }
        };
        InterlockOperation.getVersion(version);
    }
    @OnClick(R.id.frame_about)
    void frameAbout(View v){
        Intent intent=new Intent(getContext(),AboutActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.frame_language)
    void frame_language(View v){
        Applyable applyable=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                Resources res = getResources();
                // Change locale settings in the app.
                DisplayMetrics dm = res.getDisplayMetrics();
                android.content.res.Configuration conf = res.getConfiguration();
                conf.locale = new Locale(arg1.toString());
                res.updateConfiguration(conf, dm);
                DbHelper.setLanguage(arg1.toString());
                Bus.post(new LanguageChanged());

            }
        };
        MenuLanguageFragment fragment=MenuLanguageFragment.newInstance(applyable);
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }
    @OnClick(R.id.frame_reset_factory)
    void frame_reset_factory(View v){
        if(checkSendable()==false){
            return;
        }
        Applyable applyable=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                if(DbHelper.checkPassword((String)data))
                {
                    InterlockOperation.setToFactory();
                }
            }
        };
        AuthPasswordFragment fragment=AuthPasswordFragment.newInstance(R.string.reset_titile,R.string.reset_factory_tip,applyable);
        fragment.show(getActivity().getSupportFragmentManager(),"");
    }

    @OnClick(R.id.frame_reset_pwd)
    void frame_reset_pwd(View v){
        if(checkSendable()==false){
            return;
        }
        Applyable applyable=new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                if(DbHelper.checkPassword((String)data))
                {
                    InterlockOperation.resetAdminPwd();
                }
            }
        };
        AuthPasswordFragment    fragment=AuthPasswordFragment.newInstance(R.string.reset_titile,R.string.reset_password_tip,applyable);
        fragment.show(getActivity().getSupportFragmentManager(),"");
    }


    @OnClick(R.id.frame_time)
    void frame_time(View v){
        if(checkSendable()==false){
            return;
        }
        if(!operatprAble())
            return;
        final Calendar c = Calendar.getInstance();
        InterlockOperation.setTime(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

    }

    boolean opAble=true;
    boolean operatprAble(){
        if(opAble==false){
            return false;
        }
        opAble=false;
        timeText.postDelayed(new Runnable() {
            @Override
            public void run() {
                opAble = true;
            }
        }, 1000);
        return true;
    }
}
