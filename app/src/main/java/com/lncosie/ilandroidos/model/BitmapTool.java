package com.lncosie.ilandroidos.model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import com.lncosie.ilandroidos.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/20.
 */
public class BitmapTool {

    private static Bitmap defimg = null;
    private static Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

    public static Bitmap decodeBitmap(Context context, String image) {
        if (image == null) {
            if (defimg == null) {
                defimg = BitmapFactory.decodeResource(context.getResources(), R.drawable.stack_of_photos);
            }
            return defimg;
        }
        byte[] bytearray = Base64.decode(image, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
    }

    public static Bitmap cropBitmap(Context context, Intent data, String img[]) {
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
        byte[] b = baos.toByteArray();
        if (img != null)
            img[0] = Base64.encodeToString(b, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    static public Bitmap cropBitmap(Context context, Uri uri, String img[]) {

        ContentResolver crs = context.getContentResolver();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(crs.openInputStream(uri), null, options);
            options.inSampleSize = calculateInSampleSize(options, 120, 120);
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(crs.openInputStream(uri), null, options);
            if (img != null) {
                img[0] = encode(bm);
            }
            return bm;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
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

    private static String encode(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);

        } catch (Exception e) {

        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Bitmap decode(String image) {
        if (image == null)
            return null;
        byte[] bytes = Base64.decode(image, Base64.NO_WRAP);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

}
