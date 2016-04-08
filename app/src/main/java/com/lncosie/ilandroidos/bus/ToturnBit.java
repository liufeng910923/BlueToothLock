package com.lncosie.ilandroidos.bus;

import android.graphics.Bitmap;
import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * author: Leo
 * Date: 2016/4/5
 * Time: 11:33
 * E-mail: liufeng910923@gmail.com
 * Funcation: TODO
 */
public class ToturnBit {

    public CircleImageView imageView;
    public Bitmap bitmap;

    public ToturnBit(CircleImageView imageView, Bitmap bitmap) {
        this.imageView = imageView;
        this.bitmap = bitmap;
    }
}
