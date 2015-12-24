package com.lncosie.ilandroidos.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.model.Applyable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuAuthFragment extends DialogFragment implements View.OnClickListener {

    Applyable applyable;
    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.menu_item_modify)
    TextView menuItemModify;
    @Bind(R.id.menu_item_delete)
    TextView menuItemDelete;

    long arg0;
    long arg1;

    public static MenuAuthFragment newInstance(long arg0, long arg1, Applyable applyable) {
        MenuAuthFragment fragment = new MenuAuthFragment();
        fragment.applyable = applyable;
        fragment.arg0 = arg0;
        fragment.arg1 = arg1;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_menu_auth, null);
        builder.setView(view);//.setNegativeButton(R.string.cancel,null);
        ButterKnife.bind(this, view);
        if (arg0 == 0) {
            if (arg1 == 0) {
                menuItemDelete.setVisibility(View.GONE);
            } else if (arg1 > 4) {
                menuItemModify.setVisibility(View.GONE);
            }
        } else if (arg0 == 1) {
            menuItemModify.setVisibility(View.GONE);
        }
        windowTitle.setText(R.string.menu_title);
        menuItemDelete.setOnClickListener(this);
        menuItemModify.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public void onClick(View v) {
        applyable.apply(v.getId() == menuItemDelete.getId() ? 0 : 1, null);
        this.dismiss();
    }
}
