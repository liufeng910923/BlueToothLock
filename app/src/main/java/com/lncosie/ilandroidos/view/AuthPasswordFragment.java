package com.lncosie.ilandroidos.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lncosie.ilandroidos.R;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.AuthDispear;
import com.lncosie.ilandroidos.bus.AuthSuccess;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LoginSuccess;
import com.lncosie.ilandroidos.bus.TipOperation;
import com.lncosie.ilandroidos.model.Applyable;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AuthPasswordFragment extends DialogFragment implements View.OnClickListener {


    Applyable applyable;
    int title;
    int tip;

    boolean diconnectIfCancel;
    @Bind(R.id.tip)
    TextView tipText;
    @Bind(R.id.cancel)
    TextView cancel;
    @Bind(R.id.ok)
    TextView ok;
    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.password)
    EditText password;
    static AuthPasswordFragment glob;
    private InputMethodManager mInputMethodManager;

    public AuthPasswordFragment() {
    }

    public static AuthPasswordFragment newInstance(boolean diconnectIfCancel, int title, int tip, Applyable applyable) {
        if (glob != null)
            return glob;

        AuthPasswordFragment fragment = new AuthPasswordFragment();
        fragment.applyable = applyable;
        fragment.title = title;
        fragment.tip = tip;
        fragment.diconnectIfCancel = diconnectIfCancel;
        glob = fragment;
        return fragment;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, "");
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Subscribe
    public void DeviceConnedted(LoginSuccess state) {
        this.dismiss();
    }

    @Subscribe
    public void AuthSuccess(AuthSuccess state) {
        this.dismiss();
    }

    @Subscribe
    public void AuthDispear(AuthDispear state) {
        this.dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_auth_password, null);
        builder.setView(view);
        ButterKnife.bind(this, view);
        Bus.register(this);
        if (title == R.string.password_with_id) {
            password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        }
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        Bus.unregister(this);
        glob = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ok.getId()) {
            int max = (title == R.string.password_with_id ? 8 : 6);
            if (password.length() == 0) {
                password.requestFocus();
                Bus.post(new TipOperation(-1, R.string.password_input_admin));
                return;
            } else if (password.length() < max) {
                password.requestFocus();
                Bus.post(new TipOperation(-1, R.string.password_shoter));
                return;
            }
            hideKeyboard();
//            //  TODO: 判断终端是什么机型，然后按照机型对应的隐藏输入键盘,小米隐藏键盘代码有问题
//            String manufacturer = android.os.Build.MANUFACTURER;
//            if (manufacturer.equals("Xiaomi")) {
//                //TODO :小米手机隐藏软键盘
//                Toast.makeText(getContext(), "隐藏小米软键盘", Toast.LENGTH_SHORT).show();
//            } else {
//                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//            }
            applyable.apply(password.getText().toString(), null);
            return;
        } else {
            if (diconnectIfCancel)
                Net.get().reset();
            this.dismiss();
        }

    }

    public void hideKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                if (mInputMethodManager == null) {
                    mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                }
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                mInputMethodManager.hideSoftInputFromWindow(
                        view.getWindowToken(), 0);
            }
//            kb_switch = false;
//            //
//            if (mEmojiconsPop.isShowing()) {
//                mEmojiconsPop.dismiss();
//                space_view.setVisibility(View.GONE);
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        windowTitle.setText(title);
        if (tip == 0)
            tipText.setVisibility(View.GONE);
        else {
            tipText.setText(tip);
        }
        //password.setHint(tip);
        return rootView;
    }

}
