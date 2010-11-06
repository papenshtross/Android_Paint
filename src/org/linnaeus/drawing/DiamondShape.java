package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 18:03:55
 */

public class DiamondShape extends RectShape {

    public DiamondShape(){
        super();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {

        Path path = new Path();

        int horizontalMiddle = (_rect.right + _rect.left) /2;
        int verticalMiddle = (_rect.top + _rect.bottom) /2;

        path.moveTo(horizontalMiddle, _rect.top);
        path.lineTo(_rect.right, verticalMiddle);
        path.lineTo(horizontalMiddle, _rect.bottom);
        path.lineTo(_rect.left, verticalMiddle);
        path.lineTo(horizontalMiddle, _rect.top);
        path.close();

        canvas.drawPath(path, paint);
    }
}
