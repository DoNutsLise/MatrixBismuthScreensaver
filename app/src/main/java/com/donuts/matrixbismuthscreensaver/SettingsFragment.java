package com.donuts.matrixbismuthscreensaver;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.rarepebble.colorpicker.ColorPreference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * SettingsFragment.
 * Some of the preferences are defined in preference.xml, but many others are created or modified programmatically in onCreatePreferences(), because
 * it's easier to fill them in programmatically, e.g. listPreferences.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, InterfaceAlertDialogCallback {

    private ActivityResultLauncher<Intent> imageSelectionActivityResultLauncher;
    private Preference helpScreensaverPreferences;
    private Preference launchScreenSettingsPreference;
    private Preference screensaverPreviewPreference;

    private Preference helpCustomizationPreferences;
    private SwitchPreference isMyMessageCustomization;
    private SwitchPreference isHighlightMyMessage;
    private Preference myMessageEditTextPreference;
    private Preference isBackgroundImagePreference;
    private Preference selectBackgroundImagePreference;
    private Preference backgroundTransparencyListPreference;

    private Preference appVersionPreferences;
    Preference appSourceCodeLinkPreferences;
    private Preference appLicencePreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " SettingsFragment", "onCreatePreferences: "+
                "called");

        //inflate the views from preferences.xml
        setPreferencesFromResource(R.xml.preferences, rootKey);

        /*
         *  find all the preferences
         */
        helpScreensaverPreferences = findPreference("helpScreensaverPreferences");
        launchScreenSettingsPreference = findPreference("launchScreenSettings");
        screensaverPreviewPreference = findPreference("screensaverPreview");

        helpCustomizationPreferences = findPreference("helpCustomizationPreferences");

        isMyMessageCustomization = findPreference("isMyMessageCustomization");
        isHighlightMyMessage = findPreference("isHighlightMyMessage");
        myMessageEditTextPreference = findPreference("myMessageEditTextPreference");
        isBackgroundImagePreference = findPreference("isBackgroundImage");
        selectBackgroundImagePreference = findPreference("selectBackgroundImage");
        backgroundTransparencyListPreference = findPreference("backgroundTransparencyListPreference");

        appVersionPreferences = findPreference("appVersion");
        appSourceCodeLinkPreferences = (Preference) findPreference("appSourceCodeLink");
        appLicencePreferences = findPreference("appLicence");

        /*
         *  Register listener for image selecting (background image selector). It is called from the selectBackgroundImage preference.
         * This receives an intent with an image chosen by the user, compresses it and saves in a local folder for further use as background.
         */
        imageSelectionActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getResultCode() == Activity.RESULT_OK) {
                            try {
                                // get the chosen by the user image (uri of the image)
                                Intent intent = activityResult.getData();
                                Uri selectedImageUri = intent.getData();

                                // compress it and save bitmap in the app's folder for further use.
                                if (null != selectedImageUri) {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                                    ContextWrapper contextWrapper = new ContextWrapper(getActivity());
                                    File directory = contextWrapper.getDir("backgroundImageFolder", Context.MODE_PRIVATE);
                                    File file = new File(directory, "backgroundImageFile" + ".jpg");

                                    FileOutputStream fileOutputStream;
                                    fileOutputStream = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();

                                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("backgroundImagePath", file.toString()).apply();
                                    Toast.makeText(getActivity(), "Image selected", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getActivity(), "Failed to set the image.", Toast.LENGTH_LONG).show();
                                }
                            }catch (IOException e) {
                                Toast.makeText(getActivity(), "Failed to set the image.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getActivity(), "Failed to set the image.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        /*
        * Add/modify some of the preference as required (certain preference is easier to create/modify programmatically
        * rather than defining them in xml file)
         */

        // 1. appVersion preference from "About" Category
        if (appVersionPreferences != null){appVersionPreferences.setSummary(BuildConfig.VERSION_NAME);}

        // 2. check if My Message customization has been already unlocked previously and if so, then delete this preference from the preference screen and enable the customization preference.
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("isMyMessageCustomization", false)){
            PreferenceGroup preferenceGroup = findPreference("customization");
            preferenceGroup.removePreference(isMyMessageCustomization);
            myMessageEditTextPreference.setEnabled(true);

        }else{
            myMessageEditTextPreference.setEnabled(false);
            isHighlightMyMessage.setEnabled(false);
        }

        // 3. depending on the value of isBackgroundImagePreference we enable/disable selectBackgroundImagePreference
        // which allows user to choose his custom image for background.
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("isBackgroundImage", false)){
            selectBackgroundImagePreference.setEnabled(true);
            backgroundTransparencyListPreference.setEnabled(true);
        }else{
            selectBackgroundImagePreference.setEnabled(false);
            backgroundTransparencyListPreference.setEnabled(false);
        }

        Log.d(CurrentTime.getCurrentTime("HH:mm:ss"), "SettingsFragment(onCreatePreferences): inflated");
    }

    // this is required for "color picker" library: https://github.com/martin-stone/hsv-alpha-color-picker-android
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ColorPreference) {
            ((ColorPreference) preference).showDialog(this, 0);
        } else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss"), "SettingsFragment(onResume): called");
        //register the preferenceChange listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        /*
        * respond to clicks on different preferences.
         */

        // 1.1 Help with the first preference category
        helpScreensaverPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch help dialog box
                MyAlertDialogMessage myAlertDialogMessage = new MyAlertDialogMessage(getActivity());
                myAlertDialogMessage.warningMessage("Screensaver settings help.", "1. To enable/disable the screensaver please leave the app and navigate to your phone's Settings -> Display -> Screen saver or simply click the 'Turn screensaver on/off' option below and the app will take you there.\n2. To preview the screensaver choose the 'Preview screensaver' option below.");
                return true;
            }
        });

        // 1.2 preference that opens screensaver menu on the phone to enable/disable the screensaver
        launchScreenSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // open phone's daydream settings
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_DREAM_SETTINGS);
                    startActivity(intent);
                }catch(Exception e){
                    MyAlertDialogMessage myAlertDialogMessage = new MyAlertDialogMessage(getActivity());
                    myAlertDialogMessage.warningMessage("Error!", "Failed to access your phone's settings; please navigate to your phone's Settings -> Display -> Screen saver to enable the screensaver");
                }
                return true;
            }
        });

        // 1.3 launch activity to preview the screensaver
        screensaverPreviewPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch PreviewScreensaverActivity
                Intent intent = new Intent(requireActivity().getApplicationContext(), PreviewScreensaverActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // 2.1 help with the second preference category: launch a dialog with help.
        helpCustomizationPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch help dialog box
                MyAlertDialogMessage myAlertDialogMessage = new MyAlertDialogMessage(getActivity());
                myAlertDialogMessage.warningMessage("Customization settings help.", getActivity().getString(R.string.helpCustomizationPreferencesString));
                return true;
            }
        });

        // 2.2
        isMyMessageCustomization.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch customization unlocking challenge

                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("isMyMessageCustomization", false)){
                    CustomizationUnlockChallenge customizationUnlockChallenge = new CustomizationUnlockChallenge(getActivity());
                            customizationUnlockChallenge.setOnAlertDialogCallbackListener(SettingsFragment.this);
                    customizationUnlockChallenge.challengeDialog();

                }else{
                    myMessageEditTextPreference.setEnabled(false);
                    isHighlightMyMessage.setEnabled(false);
                }
                return true;
            }
        });

        // enable disable background image
        isBackgroundImagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // depending on the value of isBackgroundImagePreference we enable/disable selectBackgroundImagePreference
                // which allows user to choose his custom image for background
                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("isBackgroundImage", false)){
                    selectBackgroundImagePreference.setEnabled(true);
                    backgroundTransparencyListPreference.setEnabled(true);
                }else{
                    selectBackgroundImagePreference.setEnabled(false);
                    backgroundTransparencyListPreference.setEnabled(false);
                }
                return true;
            }
        });

        // 2.select background image
        selectBackgroundImagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch image selection intents
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                // this is a new way of doings startActivityForResult(chooserIntent, bla);
                imageSelectionActivityResultLauncher.launch(chooserIntent);

                return true;
            }
        });

        //3.2 licence preference from "About" Category
        appSourceCodeLinkPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch help dialog box
                Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("https://github.com/DoNutsLise/MatrixBismuthScreensaver") );
                startActivity( browse );
                return true;
            }
        });

        //3.3 licence preference from "About" Category
        appLicencePreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // launch help dialog box
                MyAlertDialogMessage myAlertDialogMessage = new MyAlertDialogMessage(getActivity());
                myAlertDialogMessage.warningMessage("Licence", getActivity().getString(R.string.app_license));
                return true;
            }
        });
    }

    /*
    * This runs whenever any preference is changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceKey) {
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss"), "SettingsFragment(onSharedPreferenceChanged): called");

        // find which preference changed
        Preference preference = findPreference(preferenceKey);

        // logic here
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss"), "SettingsFragment(onStop): called");
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // handle the response of the challenge to unlock My Message customization preference (result comes from CustomizationUnlockChallenge.class which is called when "isMyMessageCustomization" preference is clicked)
    @Override
    public void onAlertDialogCallback(String answer) {
        MyAlertDialogMessage myAlertDialogMessage = new MyAlertDialogMessage(getActivity());
        if (answer.equalsIgnoreCase("Python")) {
            // if the answer is correct
            myAlertDialogMessage.warningMessage("Success", "Correct. Customization preference is unlocked.");
            myMessageEditTextPreference.setEnabled(true);
            isHighlightMyMessage.setEnabled(true);
        }else if (answer.equals("")){
            // if user cancelled the challenge, we return empty string which is equivalent to failing the challenge
            isMyMessageCustomization.setChecked(false);
            myMessageEditTextPreference.setEnabled(false);
            isHighlightMyMessage.setEnabled(false);
        }else {
            // if the user answered incorrectly
            myAlertDialogMessage.warningMessage("Incorrect", "Try again");
            isMyMessageCustomization.setChecked(false);
            myMessageEditTextPreference.setEnabled(false);
            isHighlightMyMessage.setEnabled(false);
        }
    }
}