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
public class MenuLanguageFragment extends DialogFragment implements View.OnClickListener {

    Applyable applyable;
    @Bind(R.id.window_title)
    TextView windowTitle;
    @Bind(R.id.menu_item_chinese)
    TextView menu_item_chinese;
    @Bind(R.id.menu_item_english)
    TextView menu_item_english;

    public static MenuLanguageFragment newInstance(Applyable applyable) {
        MenuLanguageFragment fragment = new MenuLanguageFragment();
        fragment.applyable = applyable;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_menu_lanuage, null);
        builder.setView(view);//.setNegativeButton(R.string.cancel,null);
        ButterKnife.bind(this, view);
        windowTitle.setText(R.string.menu_title);
        menu_item_english.setOnClickListener(this);
        menu_item_chinese.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        applyable.apply(v.getId() == menu_item_english.getId() ? 0 : 1, v.getTag());
        this.dismiss();
    }
}
