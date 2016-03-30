package com.lncosie.ilandroidos.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.IOException;

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
    public void setLocalImg(ImageView imageView, String imagePath) {

        // 显示图片的配置
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565);

        DisplayImageOptions options = builder.build();

        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imagePath),
                imageView, options);

    }


}
