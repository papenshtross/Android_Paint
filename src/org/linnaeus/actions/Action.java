package org.linnaeus.actions;

import android.content.Context;
import org.linnaeus.activity.PaintAreaActivity;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 07.11.2010
 * Time: 17:05:17
 */

public abstract class Action {
    public abstract void doAction(Context context, PaintAreaActivity.PaintView paintView);
}
