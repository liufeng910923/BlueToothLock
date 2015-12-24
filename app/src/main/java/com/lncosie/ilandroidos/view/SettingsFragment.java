package com.lncosie.ilandroidos.view;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ErrorPassword;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.DeviceConnedted;
import com.lncosie.ilandroidos.bus.DeviceDisconnected;
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
    boolean opAble = true;
    @Bind(R.id.net_tip)
    TextView net_tip;

    @Subscribe
    public void OnConnected(DeviceConnedted state) {
        net_tip.setVisibility(View.GONE);
    }

    @Subscribe
    public void OnConnected(DeviceDisconnected state) {
        net_tip.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.net_tip)
    public void connect() {
        super.autoConnet();
    }

    public SettingsFragment() {

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
    public void langChanged(LanguageChanged languageChanged) {
        //title.setText(R.string.setting);
        Applyable version = new Applyable() {
            @Override
            public void apply(Object bytes, Object arg1) {
                byte[] data = (byte[]) bytes;
                String formater = getString(R.string.version_formater);
                String text = String.format(formater, data[6], data[7], data[1], data[2], data[3], data[4], data[5]);
                versionText.setText(text);
            }
        };
        InterlockOperation.getVersion(version);
    }

    @Override
    protected void onActive(Object arg) {
        super.onActive(arg);
        updataUi();
    }

    void updataUi() {
        Applyable time = new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                timeText.setText((String) data);
            }
        };
        InterlockOperation.getTime(time);
        Applyable volumn = new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                setVolumnScroll.setProgress((int) data);
            }
        };
        InterlockOperation.getVolume(volumn);
        Applyable space = new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                spacePwdText.setText((String) arg0);
                spaceFingerText.setText((String) arg1);
            }
        };
        InterlockOperation.getSpace(space);
        Applyable version = new Applyable() {
            @Override
            public void apply(Object bytes, Object arg1) {
                byte[] data = (byte[]) bytes;
                String formater = getString(R.string.version_formater);
                String text = String.format(formater, data[6], data[7], data[1], data[2], data[3], data[4], data[5]);
                versionText.setText(text);
            }
        };
        InterlockOperation.getVersion(version);
    }

    @OnClick(R.id.frame_about)
    void frameAbout(View v) {
        Intent intent = new Intent(getContext(), AboutActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.frame_language)
    void frame_language(View v) {
        Applyable applyable = new Applyable() {
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
        MenuLanguageFragment fragment = MenuLanguageFragment.newInstance(applyable);
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    @OnClick(R.id.frame_reset_factory)
    void frame_reset_factory(View v) {
        if (checkSendable() == false) {
            return;
        }
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                if (DbHelper.checkPassword((String) data)) {
                    InterlockOperation.setToFactory();
                }else{
                    Bus.post(new ErrorPassword());
                }
            }
        };
        AuthPasswordFragment fragment = AuthPasswordFragment.newInstance(R.string.reset_titile, R.string.reset_factory_tip, applyable);
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    @OnClick(R.id.frame_reset_pwd)
    void frame_reset_pwd(View v) {
        if (checkSendable() == false) {
            return;
        }
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                if (DbHelper.checkPassword((String) data)) {
                    InterlockOperation.resetAdminPwd();
                }else{
                    Bus.post(new ErrorPassword());
                }
            }
        };
        AuthPasswordFragment fragment = AuthPasswordFragment.newInstance(R.string.reset_titile, R.string.reset_password_tip, applyable);
        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    @OnClick(R.id.frame_time)
    void frame_time(View v) {
        if (!checkSendable()) {
            return;
        }
        if (!operatprAble())
            return;
        final Calendar c = Calendar.getInstance();
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object data, Object arg1) {
                String text = String.format("%04d-%02d-%02d %02d:%02d",
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE));
                timeText.setText(text);
            }
        };

        InterlockOperation.setTime(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                applyable
        );
    }

    boolean operatprAble() {
        if (opAble == false) {
            return false;
        }
        opAble = false;
        timeText.postDelayed(new Runnable() {
            @Override
            public void run() {
                opAble = true;
            }
        }, 1000);
        return true;
    }
}
