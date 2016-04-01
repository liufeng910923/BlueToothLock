package com.lncosie.ilandroidos.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2015/6/29.
 */
public class BitmapUtil {
    private static BitmapUtil instance;

    private BitmapUtil() {
    }

    /**
     * singel instance;
     *
     * @return instance;
     */
    public static BitmapUtil getInstance() {
        if (instance == null) {
            synchronized (BitmapUtil.class) {
                if (instance == null) {
                    instance = new BitmapUtil();
                }
            }
        }
        return instance;
    }

    public Bitmap getSmallBitmap(Activity activity, Uri uri) throws IOException {

        ContentResolver crs = activity.getContentResolver();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(crs.openInputStream(uri), null, options);
        options.inSampleSize = calculateInSampleSize(options, 120, 120);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeStream(crs.openInputStream(uri), null, options);
        if (bm == null) {
            return null;
        }
        return bm;

    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        return inSampleSize;
    }


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

        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imagePath),
                imageView, options);

    }


    /**
     * @param urlpath
     * @return Bitmap
     * 根据图片url获取图片对象
     */
    public  Bitmap getBitMBitmap(String urlpath) {
        Bitmap map = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * @param urlpath
     * @return Bitmap
     * 根据url获取布局背景的对象
     */
    public static Drawable getDrawable(String urlpath){
        Drawable d = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            d = Drawable.createFromStream(in, "background.jpg");
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }


    public static Bitmap getDiskBitmap(String pathString)
    {
        Bitmap bitmap = null;
        try
        {
            File file = new File(pathString);
            if(file.exists())
            {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e)
        {
            // TODO: handle exception
        }


        return bitmap;
    }




}
