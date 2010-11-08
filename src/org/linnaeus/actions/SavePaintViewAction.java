package org.linnaeus.actions;

import android.content.Context;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.utils.FileUtils;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 23:30:14
 */

public class SavePaintViewAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

        try{
            FileUtils.saveLocalImage(context, paintView.getDrawableBitmap(),
                    FileUtils.STATE_TEMP_FILE_NAME, true);
        }
        catch(Exception ex){
            //WarningAlert.show(this, "Cannot save paint area: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
