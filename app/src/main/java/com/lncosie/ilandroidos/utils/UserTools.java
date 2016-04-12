package com.lncosie.ilandroidos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lncosie.ilandroidos.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * User: liufeng(549454273@qq.com)
 * Date: 2015-12-15
 * Time: 16:38
 * 设置用户参数的工具类
 */
public class UserTools {

    static UserTools instance;

    //    Users user;
    private UserTools() {
    }

    public synchronized static UserTools getInstance() {
        if (instance == null)
            return new UserTools();
        else
            return instance;

    }

    /**
     * 设置用户头像。
     *
     * @param context
     * @param circleImageView
     * @param IconPath
     */
    public void setIcon(Context context, CircleImageView circleImageView, String IconPath) {
        if (IconPath == null || IconPath.length() == 0) {
            circleImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.stack_of_photos));
        } else {
            Uri imageUri = Uri.parse("file://" + IconPath);
            circleImageView.setImageBitmap(BitmapUtil.rotateImageIfRequired(context, imageUri));
        }


    }


}