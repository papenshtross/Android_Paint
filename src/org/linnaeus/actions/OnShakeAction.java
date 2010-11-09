package org.linnaeus.actions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import org.linnaeus.activity.PaintAreaActivity;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 08.11.2010
 * Time: 23:17:32
 */

public class OnShakeAction {

    public static AlertDialog _dialog;

    public void doAction(Context context, final PaintAreaActivity.PaintView paintView){

        if(_dialog != null && _dialog.isShowing()){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Are you sure you want to clear drawing?")
                .setCancelable(false)
                .setTitle("Drawing shake")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        paintView.onClear();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        _dialog = builder.create();
        _dialog.show();
    }
}
