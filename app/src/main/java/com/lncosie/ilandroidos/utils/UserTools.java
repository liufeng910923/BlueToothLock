package com.lncosie.ilandroidos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ToturnBit;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.BitmapTool;
import com.lncosie.ilandroidos.view.AppActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

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

    public void setIcon(Context context,CircleImageView circleImageView ,String IconPath){
        if (IconPath==null||IconPath.length()==0){
            circleImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.stack_of_photos));
        }else {

            Uri imageUri = Uri.parse("file://"+IconPath);
            try {
                circleImageView.setImageBitmap(BitmapUtil.decodeSampledBitmap(context,imageUri));
            } catch (IOException e) {
                Log.e("BitmapUtil ","Uri Parse failed");
                e.printStackTrace();
            }
        }


    }








}