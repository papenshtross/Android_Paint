package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 14:34:25
 */
public class PathShape extends Shape {

    private int _x;
    private int _y;
    private Path _path;

    public PathShape(){
        _path = new Path();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(_path, paint);
    }

    @Override
    public void onTouchEvent(int x, int y, int action) {

        switch(action){
            case MotionEvent.ACTION_DOWN: {
                _path.reset();
                _path.moveTo(x, y);
                _x = x;
                _y = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);
                _x = x;
                _y = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                _path.lineTo(_x, _y);
            }
        }
    }

    @Override
    public void reset() {
        _x = _y = 0;
        _path.reset();
    }
}
