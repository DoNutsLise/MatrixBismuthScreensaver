package com.donuts.matrixbismuthscreensaver;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


/**
 * MainActivity is here for debug purposes only. It's not needed for the screensaver functionality (only a daydream service is needed).
 * Comment it out in the manifest for production purposes.
 */


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}