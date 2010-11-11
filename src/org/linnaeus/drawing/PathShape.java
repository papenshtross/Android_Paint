package org.linnaeus.drawing;

import android.graphics.*;
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
    public String getName() {
        return "Curve";
    }

    @Override
    public void draw(Canvas canvas, Paint paint, Region region) {

        if(_path.isEmpty()){

            Rect rect = getRectWithDefaultMargins(region);

            int x = rect.left;
            int y = rect.bottom;

            int xPile = (rect.left + rect.right) /6;
            int yPile = (rect.top + rect.bottom) /6;

            _path.moveTo(x, y);

            _x = x;
            _y = y;

            x = 2 * xPile;
            y = 4 * yPile;

            _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);

            _x = x;
            _y = y;

            x = 3 * xPile;
            y = 4 * yPile;

            _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);

            _x = x;
            _y = y;

            x = 4 * xPile;
            y = 2 * yPile;

            _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);

            _x = x;
            _y = y;

            x = rect.right;
            y = rect.top;
            
            _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);

            _x = x;
            _y = y;

            _path.lineTo(_x, _y);

            draw(canvas, paint);
            reset();
        }
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
