package com.lncosie.ilandroidos.view.n;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.model.Applyable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */


public class SetupDescriptionFragment extends DialogFragment {
    int title;
    Applyable apply;
    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.description)
    EditText description;


    public SetupDescriptionFragment() {

    }

    public static SetupDescriptionFragment newInstance(int tilte, Applyable onApply) {
        SetupDescriptionFragment fragment = new SetupDescriptionFragment();
        fragment.title = tilte;
        fragment.apply = onApply;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_setup_description, null);
        builder.setView(view)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String pwd=null;
                                apply.apply(pwd, null);
                            }
                        });

        ButterKnife.bind(this, view);

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
    void    checkPwd(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        windowTitle.setText(title);
        return rootView;
    }
}
