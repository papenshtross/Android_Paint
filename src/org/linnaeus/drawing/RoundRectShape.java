package org.linnaeus.drawing;

import android.graphics.*;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 17:57:43
 */
public class RoundRectShape extends RectShape {

    public RoundRectShape(){
        super();
    }

    @Override
    public String getName() {
        return "Round rectangle";
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawRoundRect(new RectF(_rect), 5, 5, paint);
    }
}
