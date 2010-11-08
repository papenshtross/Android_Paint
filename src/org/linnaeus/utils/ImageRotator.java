package org.linnaeus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import org.linnaeus.activity.R;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 22:40:58
 */
public class ImageRotator {
    
    public static BitmapDrawable rotate(Context context, int resourceId){

        Bitmap bmp = BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.ic_menu_revert);
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(180);
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        return new BitmapDrawable(rotatedBMP);
    }
}
