package com.lncosie.ilandroidos.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.UserSet;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.utils.BitmapUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ScanAdapter extends BaseAdapter {
    private List<String> list;
    private LayoutInflater inflater;
    // 已经选择的图片本地路径集合
    private List<String> hasCheckList = new ArrayList<String>();
    private ActivityIconSeleted activityIconSelected;
    private Users user;
    private long userId;

    public ScanAdapter(ActivityIconSeleted activity, Users user, List<String> list) {
        this.list = list;
        this.user = user;
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
        CircleImageView iv = (CircleImageView)convertView.findViewById(R.id.iconselected_item_upload);
        BitmapUtil.setLocalImg(iv,path);
//
        return convertView;
    }







    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<String> getHasCheckList() {
        return hasCheckList;
    }

    public void setHasCheckList(List<String> hasCheckList) {
        this.hasCheckList = hasCheckList;
    }

}
