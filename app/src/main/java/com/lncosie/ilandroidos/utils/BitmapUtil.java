package com.lncosie.ilandroidos.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.lncosie.ilandroidos.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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


    /**
     *
     * @param context
     * @param ImageUri
     * @return
     */
    public static Bitmap decodeSampledBitmap(Context context, Uri ImageUri) {

        Bitmap sampledBitmap = null;
        if (ImageUri == null) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.stack_of_photos);
        } else {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // Calculate inSampleSize

            options.inSampleSize = 2;
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            InputStream imageStream = null;
            try {
                imageStream = context.getContentResolver().openInputStream(ImageUri);
                sampledBitmap = BitmapFactory.decodeStream(imageStream, null, options);
                imageStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("BitmapUtils ", "DecodeSampledBitmap failed");
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.stack_of_photos);
            }

            return sampledBitmap;
        }
    }


    /**
     * Rotate an image if required.
     *
     * @param img
     * @param selectedImage
     * @return
     */
    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = readPictureDegree(selectedImage);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            boolean b = matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);

            img.recycle();
            return rotatedImg;
        } else {
            return img;
        }
    }


    /**
     * 如果有需要就旋转获取到bitmap图片
     *
     * @param context
     * @param selectedImage
     * @return 旋转后的bitmap
     */
    public static Bitmap rotateImageIfRequired(Context context, Uri selectedImage) {
        Bitmap rotateBitmap = null;
        int rotation = readPictureDegree(selectedImage);
        Bitmap bitmap = decodeSampledBitmap(context, selectedImage);
        if (rotation != 0) {
            rotateBitmap = toturn(bitmap, rotation);
        } else
            rotateBitmap = bitmap;
        return rotateBitmap;

    }


    /**
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /**
     * 通过Bitmap地址获取图片Bitmap；
     *
     * @param pathString
     * @return
     */
    public Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {


                DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
                builder.cacheInMemory(true);
//                bitmap = BitmapFactory.decodeFile(pathString, );
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.d("getDiskBitmap", e.toString());
        }
        return bitmap;
    }


    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(Uri path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path.getPath());
            degree = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (degree) {
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

    private static void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            //修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            //例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 旋转图片的角度；
     *
     * @param img
     * @param degrees
     * @return 旋转后的图片
     */
    public static Bitmap toturn(Bitmap img, float degrees) {
        if (img == null)
            return null;
        Matrix matrix = new Matrix();
        matrix.postRotate(+degrees); /*反转度数*/
        int width = img.getWidth() / 4;
        int height = img.getHeight() / 4;
        Bitmap rorateImg = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        img.recycle();
        return rorateImg;
    }


}
