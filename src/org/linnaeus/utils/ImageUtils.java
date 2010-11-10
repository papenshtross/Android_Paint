package org.linnaeus.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.linnaeus.activity.R;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 22:40:58
 */
public class ImageUtils {
    
    public static BitmapDrawable rotate(Context context, int resourceId){

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(180);
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        return new BitmapDrawable(rotatedBMP);
    }

    public static BitmapDrawable drawPoint(Context context, int resourceId, int color){

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);

        Bitmap image = Bitmap.createBitmap(bmp.getWidth(),
                                           bmp.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        
        canvas.drawBitmap(bmp, 0,0, mPaint);
        canvas.drawPoint(14, 38, mPaint);
        return new BitmapDrawable(image);
    }
}
