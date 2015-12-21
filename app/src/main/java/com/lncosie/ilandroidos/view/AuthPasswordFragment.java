package com.lncosie.ilandroidos.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.TipOperation;
import com.lncosie.ilandroidos.model.Applyable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AuthPasswordFragment extends DialogFragment implements View.OnClickListener{


    Applyable applyable;
    int     title;
    int     tip;
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
    public static AuthPasswordFragment    newInstance(int title,int tip,Applyable applyable){
        AuthPasswordFragment    fragment=new AuthPasswordFragment();
        fragment.applyable=applyable;
        fragment.title=title;
        fragment.tip=tip;
        return fragment;
    }

    public AuthPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_auth_password, null);
        builder.setView(view);
        ButterKnife.bind(this, view);
        if(title==R.string.password_with_id){
            password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        }
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == ok.getId())
        {
            int max=(title==R.string.password_with_id?8:6);
            if(password.length()<max){
                password.requestFocus();
                Bus.post(new TipOperation(-1, R.string.password_shoter));
                return;
            }
            applyable.apply(password.getText().toString(), null);
        }
        this.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        windowTitle.setText(title);
        tipText.setText(tip);
        return rootView;
    }

}
