package com.donuts.matrixbismuthscreensaver;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Shows an alert dialogue with a custom message
 */

public class MyAlertDialogMessage {
    private Context mContext;

    public MyAlertDialogMessage(Context context) {
        mContext = context;
    }

    public void warningMessage(String dialogTitle, String dialogMessage) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(dialogMessage);
        alertDialog.setPositiveButton("OK", null);
        alertDialog.show();
    }
}