package org.linnaeus.drawing;

import android.graphics.*;
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
    public String getName() {
        return "Oval";
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawOval(new RectF(_rect), paint);
    }
}
