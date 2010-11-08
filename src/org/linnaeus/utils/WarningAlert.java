package org.linnaeus.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 07.11.2010
 * Time: 22:29:01
 */

public class WarningAlert {

    public static void show(Context sender, String message){
        show(sender, "2D Graphical Editor", message);
    }

    public static void show(Context sender, String title, String message){

        AlertDialog alertDialog = new AlertDialog.Builder(sender).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setCancelable(true);
            alertDialog.setButton("Ok",//sender.getString(R.string.activity_alert_window_ok),
                                  new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { }
            });

        alertDialog.show();
    }
}