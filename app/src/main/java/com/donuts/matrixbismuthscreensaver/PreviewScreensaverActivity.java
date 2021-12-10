package com.donuts.matrixbismuthscreensaver;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


/**
 * MainActivity is here for debug purposes only. It's not needed for the screensaver functionality (only a daydream service is needed).
 */


public class PreviewScreensaverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensaver_preview);
    }
}