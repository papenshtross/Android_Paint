package org.linnaeus.actions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 11.11.2010
 * Time: 0:44:53
 */

public class AttachImageAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

        // TODO: progress bar

        Bitmap bitmap = paintView.getDrawableBitmapCopy();

        Boolean isOk = FileUtils.saveLocalImage(context, bitmap,
                FileUtils.SHARE_TEMP_FILE_NAME, false);

        bitmap.recycle();

        if(isOk){
            try{
                Uri tmpFileUri = FileUtils.getLocalImagePath(context, FileUtils.SHARE_TEMP_FILE_NAME);

                if(tmpFileUri != null){
                    Intent intent = new Intent();
                    intent.setType("image/jpeg");
                    intent.setAction(Intent.ACTION_ATTACH_DATA);
                    intent.putExtra(Intent.EXTRA_STREAM, tmpFileUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(intent,"Use image as..."));
                }
            }
            catch(Exception ex){
                WarningAlert.show(context, "Cannot start image action chooser: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
