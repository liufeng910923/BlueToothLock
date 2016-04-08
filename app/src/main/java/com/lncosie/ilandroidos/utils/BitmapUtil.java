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


    //避免了回忆的大图片，我会建议你重新缩放图像：
    private static final int MAX_HEIGHT = 216;
    private static final int MAX_WIDTH = 216;

    public static Bitmap decodeSampledBitmap(Context context, Uri selectedImage)
            throws IOException {
        if (selectedImage == null) {
            return BitmapFactory.decodeResource(context.getResources(),R.drawable.stack_of_photos);
        } else {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            imageStream = context.getContentResolver().openInputStream(selectedImage);
            Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

            img = rotateImageIfRequired(context, img, selectedImage);
            return img;
        }
    }


    /**
     * Rotate an image if required.
     *
     * @param img
     * @param selectedImage
     * @return
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = getRotation(context, selectedImage);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        } else {
            return img;
        }
    }

    /**
     * Get the rotation of the last image added.
     *
     * @param context
     * @param selectedImage
     * @return
     */
    private static int getRotation(Context context, Uri selectedImage) {
        int rotation = 0;
        ContentResolver content = context.getContentResolver();


        Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{"orientation", "date_added"}, null, null, "date_added desc");

        if (mediaCursor != null && mediaCursor.getCount() != 0) {
            while (mediaCursor.moveToNext()) {
                rotation = mediaCursor.getInt(0);
                break;
            }
        }
        mediaCursor.close();
        return rotation;
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

//    public static int calculateInSampleSize(BitmapFactory.Options options,
//                                            int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            // Calculate ratios of height and width to requested height and
//            // width
//            final int heightRatio = Math.round((float) height
//                    / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
//
//            // Choose the smallest ratio as inSampleSize value, this will
//            // guarantee
//            // a final image with both dimensions larger than or equal to the
//            // requested height and width.
//            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
//        }

//        return inSampleSize;
//    }


//    /**
//     * 通过Bitmap地址获取图片Bitmap；
//     *
//     * @param pathString
//     * @return
//     */
//    public Bitmap getDiskBitmap(String pathString) {
//        Bitmap bitmap = null;
//        try {
//            File file = new File(pathString);
//            if (file.exists()) {
//
//
//                DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
//                builder.cacheInMemory(true);
////                bitmap = BitmapFactory.decodeFile(pathString, );
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//            Log.d("getDiskBitmap", e.toString());
//        }
//        return bitmap;
//    }


//    /**
//     * 读取照片exif信息中的旋转角度
//     *
//     * @param path 照片路径
//     * @return角度
//     */
//    public static int readPictureDegree(String path) {
//        int degree = 0;
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return degree;
//    }

//    private static void setPictureDegreeZero(String path) {
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//            //修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
//            //例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
//            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
//            exifInterface.saveAttributes();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }

//    /**
//     * 旋转图片的角度；
//     *
//     * @param img
//     * @param degrees
//     * @return 旋转后的图片
//     */
//    public Bitmap toturn(Bitmap img, float degrees) {
//        if (img == null)
//            return null;
//        Matrix matrix = new Matrix();
//        matrix.postRotate(+degrees); /*反转度数*/
//        int width = img.getWidth() / 4;
//        int height = img.getHeight() / 4;
//        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
//        return img;
//    }

//    /**
//     * @param imageView
//     * @param imagePath
//     */
//    public void setLocalImg(CircleImageView imageView, String imagePath) {
//        Bitmap bitmap = null;
//        if (imagePath == null) {
//            imageView.setImageResource(R.drawable.stack_of_photos);
//
//        } else {
//            bitmap = ImageLoader.getInstance().loadImageSync(imagePath, getOptions());
////            bitmap = BitmapUtil.getInstance().getDiskBitmap(imagePath);
//            if (bitmap != null) {
//
//                int mDegree = BitmapUtil.getInstance().readPictureDegree(imagePath);
//                if (0 != mDegree) {
//                    bitmap = toturn(bitmap, mDegree);
//                    imageView.setImageBitmap(bitmap);
//                } else {
//                    ImageLoader.getInstance().loadImage(imagePath, getOptions(), null);
//                }
//            } else {
//                imageView.setImageResource(R.drawable.stack_of_photos);
//            }
//        }
//    }

}
