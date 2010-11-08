package org.linnaeus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 07.11.10
 * Time: 21:17
 */
public class FileUtils {

    public static Boolean saveTempImageOnSDCard(
            Context context, Bitmap bitmap, String fileName, Boolean suppressErrors){

        try{
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                
                if(!suppressErrors) {
                    WarningAlert.show(context, "Cannot save image. SDCard is not mounted");
                }
                return false;
            }

            File imageDir = new File(Environment.getExternalStorageDirectory(),
                                     "data/org.linnaeus.paint/temp");

            if (!imageDir.exists()) {
                Boolean isOk = imageDir.mkdirs();

                if(!isOk){

                    if(!suppressErrors) {
                        WarningAlert.show(context, "Cannot initialize temporary directory.");
                    }
                    return false;
                }

            }

            Bitmap bitmapImage = Bitmap.createBitmap(bitmap);

            FileOutputStream fos;

            try {

                fos = new FileOutputStream(imageDir + "\\" + fileName);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
            catch(FileNotFoundException ex){
                if(!suppressErrors) {
                    WarningAlert.show(context, "Cannot save image file: " + ex.getMessage());
                }
                ex.printStackTrace();
                return false;
            }
            catch(IOException ex){
                if(!suppressErrors) {
                    WarningAlert.show(context, "Cannot save image file: " + ex.getMessage());
                }
                ex.printStackTrace();
                return false;
            }
        }
        catch(Exception ex){
            if(!suppressErrors) {
                WarningAlert.show(context, "Unexpected fault. Cannot save image file: " + ex.getMessage());
            }
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static Boolean saveImage(Context context, Bitmap bitmap, String filePath){

         try{
            Bitmap bitmapImage = Bitmap.createBitmap(bitmap);

            FileOutputStream fos;

            try {

                String fileNameToSave = filePath;

                if(!fileNameToSave.endsWith(".jpg")){
                    fileNameToSave += ".jpg";
                }

                fos = new FileOutputStream(fileNameToSave);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
            catch(FileNotFoundException ex){
                WarningAlert.show(context, "Cannot save image file: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
            catch(IOException ex){
                WarningAlert.show(context, "Cannot save image file: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
         }
         catch(Exception ex){
            WarningAlert.show(context, "Unexpected fault. Cannot save image file: " + ex.getMessage());
            ex.printStackTrace();
            return false;
         }

        return true;
    }
}