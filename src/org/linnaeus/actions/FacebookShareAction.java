package org.linnaeus.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import org.linnaeus.activity.FBActivity;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 23:04:07
 */

public class FacebookShareAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

        // TODO: progress bar

        Boolean isOk = FileUtils.saveLocalImage(context, paintView.getDrawableBitmap(),
                FileUtils.SHARE_TEMP_FILE_NAME, false);

        if(isOk){
            try{
                Uri tmpFileUri = FileUtils.getLocalImagePath(context, FileUtils.SHARE_TEMP_FILE_NAME);

                if(tmpFileUri != null){
                   context.startActivity(new Intent(context, FBActivity.class));
                }
            }
            catch(Exception ex){
                WarningAlert.show(context, "Cannot share image on facebook: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
