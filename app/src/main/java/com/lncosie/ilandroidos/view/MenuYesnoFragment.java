package com.lncosie.ilandroidos.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.model.Applyable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuYesnoFragment extends DialogFragment implements View.OnClickListener {

    Applyable applyable;
    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.content)
    TextView content;
    @Bind(R.id.cancel)
    TextView cancel;
    @Bind(R.id.ok)
    TextView ok;

    int title;
    String message;
    public static MenuYesnoFragment newInstance(int title,String message,Applyable applyable) {
        MenuYesnoFragment fragment = new MenuYesnoFragment();
        fragment.applyable = applyable;
        fragment.title=title;
        fragment.message=message;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_menu_yesno, null);
        builder.setView(view);
        ButterKnife.bind(this, view);
        windowTitle.setText(title);
        content.setText(message);
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
            applyable.apply(null,null);
        this.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        //ButterKnife.bind(this, rootView);
        return rootView;
    }
}
