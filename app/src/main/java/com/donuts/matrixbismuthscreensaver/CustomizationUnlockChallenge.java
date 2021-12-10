package com.donuts.matrixbismuthscreensaver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * A class (AlerDialog) which pops up when "isMyMessageCustomization" preference is clicked. User is asked to complete the challenge
 * by answering a question in order to unlock customization preference. The result (string entered by user) is returned to the SettingsFragment
 * via InterfaceAlertDialogCallback.
 */

public class CustomizationUnlockChallenge {
    private Context mContext;
    boolean isChallenge = false;
    private InterfaceAlertDialogCallback listener;

    public CustomizationUnlockChallenge(Context context) {
        mContext = context;
    }

    public void setOnAlertDialogCallbackListener(InterfaceAlertDialogCallback listener) {
        this.listener = listener;
    }

    public void challengeDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Unlock 'My Message' customization.");
        alertDialog.setMessage("To unlock the preference please answer a simple question: Which programming language Bismuth Platform is based on?\nYou can easily find the answer on Bismuth website (https://bismuth.live");

        // Set up the input EditText widget
        final EditText input = new EditText(mContext);
        alertDialog.setView(input);

        // Set up the buttons
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                listener.onAlertDialogCallback(value);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if the user cancelled the challenge we return empty string which is equivalent to failing the challenge.
                listener.onAlertDialogCallback("");
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}