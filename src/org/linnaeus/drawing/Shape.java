package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 14:32:30
 */

public abstract class Shape {
    public abstract void draw(Canvas canvas, Paint paint);
    public abstract void onTouchEvent(int x, int y, int action);
    public abstract void reset();
}
