package com.lncosie.ilandroidos.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.utils.UserTools;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ScanAdapter extends BaseAdapter {
    private List<String> list;
    private LayoutInflater inflater;

    // 已经选择的图片本地路径集合
    private ActivityIconSeleted activityIconSelected;

    public ScanAdapter(ActivityIconSeleted activity, List<String> list) {
        this.list = list;
        this.activityIconSelected = activity;
        inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_iconselected_upload, null);
        }

        String path = list.get(position);
        Log.d("imgScan path", path);

        // 图片控件
        CircleImageView iv = (CircleImageView)convertView.findViewById(
                R.id.iconselected_item_upload);
        UserTools.getInstance().setIcon(
                activityIconSelected.getBaseContext(),iv,path);
        return convertView;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }


}
