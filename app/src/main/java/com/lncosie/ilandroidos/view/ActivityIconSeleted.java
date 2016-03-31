package com.lncosie.ilandroidos.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.Users;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * User: liufeng(549454273@qq.com)
 * Date: 2015-12-01
 * Time: 16:19
 * FIXME
 */
public class ActivityIconSeleted extends EventableActivity {

    @Bind(R.id.iconselected_icons_gv)
    GridView iconselected_icons_gv;
    // 进度对话框
    private ProgressDialog mProgressDialog;
    // 本地图片路径集合
    private ArrayList<String> urls = new ArrayList<String>();
    // 加载完成标示
    private final static int SCAN_OK = 1;

    public Users user;
    public Handler mhandler;
    private static Context context;
    private ScanAdapter scanadapter;
    private ImageLoader imageLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconselected);
        ButterKnife.bind(this);
        context = this;
        initData();
    }

    @Override
    protected void onPause() {
        pauseDetect = true;
        super.onPause();

    }

    @Override
    protected void onResume() {
        pauseDetect = true;
        super.onResume();
        initData();
    }

    public void initData() {
        urls =getImages();
        setAdapter();
        iconselected_icons_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                user.image=urls.get(position);
//                notifyDataSetChanged();
                user.save();
                Bus.post(new UsersChanged());
                Intent intent = new Intent(ActivityIconSeleted.this,UserViewDetailActivity.class);
                startActivity(intent);
//                finish();
            }
        });

    }



    /**
     * 设置适配器
     */
    private void setAdapter() {
        if (scanadapter == null) {
            scanadapter = new ScanAdapter(ActivityIconSeleted.this, user, urls);
            iconselected_icons_gv.setAdapter(scanadapter);
        } else {
            scanadapter.setList(urls);
        }
    }

    /**
     * 获取手机上的图片
     */
    private ArrayList<String> getImages() {
        ArrayList<String> mList=new ArrayList<>();
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD卡不存在", Toast.LENGTH_SHORT).show();
            return null;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mList.clear();
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = ActivityIconSeleted.this
                        .getContentResolver();

                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);

                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    mList.add(path);
                }
                mCursor.close();
            }
        }).start();
        return mList;

    }

    public void backward(View v) {
        Bus.post(new UsersChanged());
        super.backward(v);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }


}