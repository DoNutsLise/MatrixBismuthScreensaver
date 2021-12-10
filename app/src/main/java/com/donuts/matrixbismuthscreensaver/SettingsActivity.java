package com.donuts.matrixbismuthscreensaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SettingsActivity is the main activity for the app. From this activity you can enable/disable the screensaver,
 * customize the settings of the raining code and change other settings.
 * All this stuff is actually done in the SettingsFragment.
 */


public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(CurrentTime.getCurrentTime("HH:mm:ss"), "SettingsActivity(onCreate): inflated");

        // load settings fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment(), "fragmentTag")
                .commit();
    }

}