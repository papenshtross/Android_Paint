package org.linnaeus.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 14:32:30
 */

public abstract class Shape {

    protected static final int DEFAULT_MARGIN = 15; 

    public abstract String getName();
    public abstract void draw(Canvas canvas, Paint paint, Region region);

    public Rect getRectWithDefaultMargins(Region region){

        Rect rect = new Rect();
        
        rect.set(region.getBounds().left + DEFAULT_MARGIN,
                 region.getBounds().top + DEFAULT_MARGIN,
                 region.getBounds().left + region.getBounds().width() - DEFAULT_MARGIN,
                 region.getBounds().top + region.getBounds().height() - DEFAULT_MARGIN);
        return rect;
    }

    public abstract void draw(Canvas canvas, Paint paint);
    public abstract void onTouchEvent(int x, int y, int action);
    public abstract void reset();
}
