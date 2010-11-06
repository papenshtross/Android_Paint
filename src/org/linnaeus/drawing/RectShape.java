package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 14:32:58
 */
public class RectShape extends Shape {

    private int _x, _y;
    protected Rect _rect;

    public RectShape() {
        _rect = new Rect(0, 0, 0, 0);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(_rect, paint);
    }

    @Override
    public void onTouchEvent(int x, int y, int action) {

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                _rect.set(x, y, x, y);
                _x = x;
                _y = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                _rect.set(_x, _y, x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                _rect.set(_x, _y, x, y);
            }
        }
    }

    @Override
    public void reset() {
        _x = _y = 0;
        _rect.set(_x, _y, _x, _y);
    }
}
