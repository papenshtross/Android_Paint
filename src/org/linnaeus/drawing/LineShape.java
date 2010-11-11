package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 10.11.2010
 * Time: 19:23:00
 */

public class LineShape extends RectShape {

    public LineShape(){
        super();
    }

    @Override
    public String getName() {
        return "Line";
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawLine(_rect.left, _rect.top, _rect.right, _rect.bottom, paint);
    }
}
