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
public class MenuUserFragment extends DialogFragment implements View.OnClickListener {

    Applyable applyable;

    long arg0;

    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.menu_item_view)
    TextView menuItemView;
    @Bind(R.id.menu_item_log)
    TextView menuItemLog;
    @Bind(R.id.menu_item_delete_user)
    TextView menuItemDeleteUser;

    public static MenuUserFragment newInstance(long arg0, Applyable applyable) {
        MenuUserFragment fragment = new MenuUserFragment();
        fragment.applyable = applyable;
        fragment.arg0 = arg0;

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_menu_user, null);
        builder.setView(view);//.setNegativeButton(R.string.cancel, null);
        ButterKnife.bind(this, view);
        windowTitle.setText(R.string.menu_title);
        menuItemView.setOnClickListener(this);
        menuItemLog.setOnClickListener(this);
        menuItemDeleteUser.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public void onClick(View v) {
        applyable.apply(v.getId() == menuItemView.getId() ? 0 :
                (v.getId() == menuItemLog.getId() ? 1 : 2), arg0);
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
