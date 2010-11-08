package org.linnaeus.actions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 23:31:13
 */

public class RestorePaintViewAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

         try{
            Uri tmpFileUri = FileUtils.getLocalImagePath(context, FileUtils.STATE_TEMP_FILE_NAME);

            if(tmpFileUri != null){
                Bitmap bitmapImage = BitmapFactory.decodeFile(tmpFileUri.getPath());
                paintView.setDrawableBitmap(bitmapImage);
            }
        }
        catch(Exception ex){
            WarningAlert.show(context, "Cannot restore paint area: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
