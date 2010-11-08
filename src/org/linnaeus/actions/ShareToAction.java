package org.linnaeus.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 23:06:27
 */
public class ShareToAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

        // TODO: progress bar

        Boolean isOk = FileUtils.saveLocalImage(context, paintView.getDrawableBitmap(),
                FileUtils.SHARE_TEMP_FILE_NAME, false);

        if(isOk){
            try{
                Uri tmpFileUri = FileUtils.getLocalImagePath(context, FileUtils.SHARE_TEMP_FILE_NAME);

                if(tmpFileUri != null){
                    Intent intent = new Intent();
                    intent.setType("image/jpeg");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, tmpFileUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(intent,"Share Image..."));
                }
            }
            catch(Exception ex){
                WarningAlert.show(context, "Cannot share image on facebook: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
