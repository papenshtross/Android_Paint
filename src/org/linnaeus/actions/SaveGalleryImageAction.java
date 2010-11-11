package org.linnaeus.actions;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import org.linnaeus.activity.PaintAreaActivity;
import org.linnaeus.activity.R;
import org.linnaeus.utils.FileUtils;
import org.linnaeus.utils.WarningAlert;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 10.11.2010
 * Time: 15:40:08
 */

public class SaveGalleryImageAction extends Action {

    @Override
    public void doAction(Context context, PaintAreaActivity.PaintView paintView) {

        Boolean isOk = FileUtils.saveLocalImage(context, paintView.getDrawableBitmapCopy(),
                FileUtils.SHARE_TEMP_FILE_NAME, false);

        if(isOk){
            try{
                Uri tmpFileUri = FileUtils.getLocalImagePath(context, FileUtils.SHARE_TEMP_FILE_NAME);

                if(tmpFileUri != null){

                    File file = new File(tmpFileUri.getPath());

                    String appName = context.getString(R.string.app_name);

                    String imageUrl = MediaStore.Images.Media
                            .insertImage(context.getContentResolver(),
                             file.getAbsolutePath(),
                             null, "Image from " + appName);

                    if(imageUrl == null){
                        WarningAlert.show(context, "Cannot insert image to gallery.");
                    }
                    else {
                        Toast.makeText(context, "Image was successfully saved to gallery.",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
            catch(Exception ex){
                WarningAlert.show(context, "Cannot share image on facebook: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
