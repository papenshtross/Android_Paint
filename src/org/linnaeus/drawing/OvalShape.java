package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 15:06:34
 */

public class OvalShape extends RectShape {

    public OvalShape(){
        super();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawOval(new RectF(_rect), paint);
    }
}
