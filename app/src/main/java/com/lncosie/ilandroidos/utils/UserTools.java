package com.lncosie.ilandroidos.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * User: liufeng(549454273@qq.com)
 * Date: 2015-12-15
 * Time: 16:38
 * 设置用户参数的工具类
 */
public class UserTools {

    static UserTools instance;
    Users user;

    /**
     * @param user
     */
    private UserTools(Users user) {
        this.user = user;
    }

    public synchronized static UserTools getInstance(Users user) {
        if (instance == null)
            return new UserTools(user);
        else
            return instance;

    }

    /**
     * @param circleImageView
     */
    public void setUserIcon(CircleImageView circleImageView) {
        /**
         *
         * 显示用户自定义的头像
         */
        String userIconPath = user.image;
        if (userIconPath != null) {
            BitmapUtil.getInstance().setLocalImg(circleImageView, userIconPath);
        } else {
            //显示默认的头像
            circleImageView.setImageResource(R.drawable.stack_of_photos);
        }

    }

    /**
     * 加载本地图片
     *
     * @param imageView 图片ImageView控件
     * @param imagePath 图片本地路径
     */
    public static void setLocalImg(ImageView imageView, String imagePath) {

        // 显示图片的配置
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565);

        DisplayImageOptions options = builder.build();

        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imagePath),
                imageView, options);

    }
}  