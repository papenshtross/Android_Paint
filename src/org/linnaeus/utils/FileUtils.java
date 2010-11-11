package org.linnaeus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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

    public static final String FILE_NAME_EXT = ".png";
    public static final String STATE_TEMP_FILE_NAME = "tmpImage" + FILE_NAME_EXT;
    public static final String SHARE_TEMP_FILE_NAME = "shareImage" + FILE_NAME_EXT;

    public static Uri getLocalImagePath(Context context, String fileName){
        File file = context.getFileStreamPath(fileName);

        return file.exists() ? Uri.fromFile(file) : null;
    }

    public static Boolean saveLocalImage(
                Context context, Bitmap bitmap, String fileName, Boolean suppressErrors){

        Bitmap bitmapImage = Bitmap.createBitmap(bitmap);

        FileOutputStream fos;

        try {
            fos = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            bitmapImage.recycle();
            fos.flush();
            fos.close();
            return true;
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

    public static Boolean saveSDCardImage(
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
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                bitmapImage.recycle();
                fos.flush();
                fos.close();
                return true;
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
    }

    public static Boolean saveImage(Context context, Bitmap bitmap, String filePath){

         try{
            Bitmap bitmapImage = Bitmap.createBitmap(bitmap);

            FileOutputStream fos;

            try {

                String fileNameToSave = filePath;

                if(!fileNameToSave.endsWith(FILE_NAME_EXT)){
                    fileNameToSave += FILE_NAME_EXT;
                }

                fos = new FileOutputStream(fileNameToSave);
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                bitmapImage.recycle();
                fos.flush();
                fos.close();
                return true;
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
    }
}