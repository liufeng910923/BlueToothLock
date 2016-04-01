package com.lncosie.ilandroidos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.BitmapTool;
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

//    /**
//     * @param circleImageView
//     */
//    public void setUserIcon(CircleImageView circleImageView) {
//        /**
//         *
//         * 显示用户自定义的头像
//         */
//        String userIconPath = user.image;
//        if (userIconPath != null) {
//            BitmapUtil.getInstance().setLocalImg(circleImageView, userIconPath);
//        } else {
//            //显示默认的头像
//            circleImageView.setImageResource(R.drawable.stack_of_photos);
//        }
//
//    }

    /**
     * 加载本地图片
     *
     * @param imageView 图片ImageView控件
     * @param imagePath 图片本地路径
     */
    public static void setLocalImg(CircleImageView imageView, String imagePath) {


        // 显示图片的配置
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565);

        DisplayImageOptions options = builder.build();
        if (imagePath != null){
            int mDegree = readPictureDegree(imagePath);
            if (0==mDegree)
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imagePath),
                        imageView, options);
            else{
                Bitmap bitmap = BitmapUtil.getDiskBitmap(imagePath);
                bitmap=toturn( bitmap,mDegree);
                imageView.setImageBitmap(bitmap);
            }
        }
        else
            imageView.setImageResource(R.drawable.stack_of_photos);
    }


    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    public static Bitmap toturn(Bitmap img,float degrees){
        if (img==null)
            return null;
        Matrix matrix = new Matrix();
        matrix.postRotate(+degrees); /*反转度数*/
        int width = img.getWidth();
        int height =img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        return img;
    }
}  