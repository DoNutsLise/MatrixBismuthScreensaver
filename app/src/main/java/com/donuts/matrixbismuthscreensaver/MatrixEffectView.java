package com.donuts.matrixbismuthscreensaver;

/**
 * Class for drawing matrix rain with custom view.
 * https://developer.android.com/training/custom-views/custom-drawing.
 * How it works:
 * 1. Using canvas and painttext we display several columns of randomly generated characters. This characters move down with time (in infinite loop) creating Matrix code rain effect.
 * 2. Using MatrixColumnModel for each column of the characters we create a "pool" of randomly generated characters. Every loop iteration we pick the next character from the pool
 * and add it at the bottom of the column; all other characters just move one step down.
 * 3. To display user's message in the column we insert user defined text into the "pool" of randomly generated characters.
 * 4. Each column of characters has its own properties, e.g. font size or length. Different font size will also cause the columns to  have different speed of moving, since the characters move with the increment of font size.
 */

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MatrixEffectView extends View {

    private static final Random RANDOM = new Random();
    private static final List<int[]> CHAR_RANGE_LIST = new ArrayList<>(Arrays.asList(new int[] {48,90}, new int[] {12449,12650})); // Ranges in unicode characters for random character generation
    int rangeToSelectRandomCharFrom; // a range (one of two from CHAR_RANGE_LIST) from which we generate a random char (basically katakana or latin character is going to be next)
    int randomIntFromRange; // id of the randomly generated character in the range
    private List<MatrixColumnModel> matrixColumnModelList;
    private int screenWidth, screenHeight;
    private Canvas canvas;
    private Bitmap bitmap;
    private int rainSpeed; // is entered by a user from 10 to 100; but we later convert it to screen refresh frequency as 40000/rainSpeed to bring it close to empirically found 80ms corresponding to input of speed of 50.
    private int rainColor; // integer returned by the color picker preference
    private int backgroundTransparency;
    private int myMessageHighlightColorBisBlue;
    private int myMessageHighlightColorBisPurple;
    private boolean isHighlightMyMessage; // the words from My Message with be highlighted in Bismuth colors (randomly; either Bismuth pink or Bismuth blue)
    private boolean isBackgroundImage;
    private boolean isBackgroundImageAcquired;
    private boolean isBatteryStatus;
    private boolean isBatteryCharging;
    private Bitmap batteryChargingBitmap;
    private IntentFilter intentFilter;
    private Intent batteryStatus;
    private int batteryLevel;
    private Bitmap bisLogoBitmap;
    private String myMessage;
    private String pathToBackgroundImage;
    private List<String> myMessageWordsList;
    private int wordIndexStart; // a word from myMessage starting index in the column (for highlighting)
    private int wordLength; // a word from myMessage length (for highlighting)
    private int numberOfColumns, columnWidth;
    private Bitmap userImageBitmap;

    private Paint paintText, customImageBitmapBackground, paintBitmapBackground, batteryChargingBackground;

    public MatrixEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // get raining code customization parameters from the Settings
        myMessage  = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("myMessageEditTextPreference", "Bismuth Blockchain");
        myMessageWordsList = Arrays.asList(myMessage.split(" "));

        numberOfColumns  =Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("numOfColumnsListPreference", "25"));
        rainSpeed  = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("rainSpeedListPreference", "50"));
        rainColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("rainingCodeColorPreference", 0xff00ff00);
        isBatteryStatus = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isBatteryStatusSwitchPreference", true);
        isHighlightMyMessage = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isHighlightMyMessage", true);

        batteryChargingBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_battery_charging);

        myMessageHighlightColorBisBlue = ContextCompat.getColor(getContext(), R.color.Bismuth_Blue);
        myMessageHighlightColorBisPurple = ContextCompat.getColor(getContext(), R.color.Bismuth_Purple);

        isBackgroundImage = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isBackgroundImage", false);
        backgroundTransparency = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("backgroundTransparencyListPreference", "20"));

        // create bitmap for background from user's image
        pathToBackgroundImage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("backgroundImagePath", "");

        // check if we should use custom background image and try to create a bitmap from it:
        isBackgroundImageAcquired = false;
        if (isBackgroundImage) {
            // create bitmap for background from user's image
            pathToBackgroundImage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("backgroundImagePath", "");
            if (!pathToBackgroundImage.equals("")) {
                // if we have a valid path to user's file save in app's folder, check if file still there
                File file = new File(pathToBackgroundImage);
                if(file.exists()){
                    try{
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        userImageBitmap = BitmapFactory.decodeFile(file.toString(), bmOptions);
                        if(userImageBitmap!=null){
                            isBackgroundImageAcquired = true;
                        }
                    }catch(Exception ignored){
                    }
                }
            }
        }

        // some setup for drawing styles
        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(rainColor);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBitmapBackground = new Paint();
        paintBitmapBackground.setColor(Color.BLACK);
        paintBitmapBackground.setAlpha(255);

        customImageBitmapBackground = new Paint();
        customImageBitmapBackground.setColor(Color.BLACK);
        customImageBitmapBackground.setAlpha(255 - 255/100*backgroundTransparency);
        customImageBitmapBackground.setStyle(Paint.Style.FILL);

        batteryChargingBackground = new Paint();
        batteryChargingBackground.setColor(ContextCompat.getColor(getContext(), R.color.DimGray));
        batteryChargingBackground.setAlpha(255);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
         This method is called when your view is first assigned a size, and again if the size of your view changes for any reason.
         We just get view dimensions and create canvas here.
         */

        screenWidth = w; // width of the screen in pixels
        screenHeight = h; // height of the screen in pixels

        // create canvas
        if (isBackgroundImageAcquired){
            userImageBitmap = Bitmap.createScaledBitmap(userImageBitmap,screenWidth,screenHeight,true);
        }
        bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        // register battery monitoring intent
        if (isBatteryStatus) {
            intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = getContext().registerReceiver(null, intentFilter);
        }

        /*
         * initialize matrix columns:
         * it's a list of matrixColumnModel where number of elements in the list corresponds to number
         * of columns of text on the screen and each column has its own properties (e.g. text, font, size, etc)
         */

        columnWidth = screenWidth/numberOfColumns; // width of each column in pixels, which is also the maximum size of text font.
        matrixColumnModelList = new ArrayList<>();
        for (int i = 0; i < numberOfColumns; i++){
            matrixColumnModelList.add(initializeNewColumn(screenHeight)); // for the first initialization we randomly place columns on the screen
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
        * Do the absolute minimum in onDraw() because it is called frequently and will cause lags!
        * Perform all initializations before because they are expensive.
         */

        canvas.drawBitmap(bitmap, 0, 0, paintBitmapBackground);
        paintCanvas();

        // add battery charge level and icon at the bottom if requested
        if (isBatteryStatus){
            batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixEffect", "onDraw: "+
                    "battery level: " + batteryLevel);
        }

        // sleep for <PAINT_DELAY_MILLIS> before next canvas draw. Empirically found that 80-100ms is good enough.
        try{
            Thread.sleep(4000/rainSpeed); // 4000/rainspeed was calculated so that values of speed entered by the user (region from 10 to 100) converts roughly to desired range of delays in millis; where speed of 50 converts to 80 millis.
        }catch( InterruptedException e){
            Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixEffect", "onDraw: "+
                    "failed to sleep.");
        }

        invalidate(); // invalidate canvas and call onDraw() - makes onDraw loop infinitely.
    }

    private void paintCanvas() {

        if (isBackgroundImageAcquired) {
            canvas.drawBitmap(userImageBitmap, 0, 0, customImageBitmapBackground);
        }

        canvas.drawRect(0, 0, screenWidth, screenHeight, customImageBitmapBackground);
        //canvas.drawRect(0, screenHeight-100, screenWidth, screenHeight, batteryChargingBackground);
        //canvas.drawBitmap(batteryChargingBitmap, screenWidth/2, screenHeight-100, batteryChargingBackground);

        for(int i = 0; i < matrixColumnModelList.size(); i++) {
            // for each column in the matrix:

            if (isBatteryStatus) {
                paintText.setColor(rainColor);
                paintText.setTextSize(columnWidth);
                paintText.setAlpha(3);
                canvas.drawText("Battery charge " + batteryLevel + " %", (float) screenWidth/2, screenHeight-2*columnWidth, paintText);
            }

            paintText.setTextSize(matrixColumnModelList.get(i).textFontSize);

            wordIndexStart = 999;
            wordLength = 999;

            // identify which of the words from myMessage is present in the raining code column, where it
            // starts and its length - we need it later for highlighting the word with different color.
            for (int j = 0; j < myMessageWordsList.size(); j++) {
                if (matrixColumnModelList.get(i).columnCharsList.toString().replaceAll(", ", "").contains(myMessageWordsList.get(j))) {
                    // word is present in the column.
                    wordIndexStart = matrixColumnModelList.get(i).columnCharsList.toString().replaceAll(", ", "").indexOf(myMessageWordsList.get(j));
                    wordLength = myMessageWordsList.get(j).length();
                    break;
                }
            }

            for (int j = 0; j < matrixColumnModelList.get(i).columnCharsList.size(); j++) {
                // for each character in the column:
                paintText.setColor(rainColor);

                if (j == matrixColumnModelList.get(i).columnCharsList.size() - 1) {
                    // if this is the last character in the column (the one at the bottom):
                    if (matrixColumnModelList.get(i).charsVertPosIndexList.get(j)  > matrixColumnModelList.get(i).textFinalHeightIndex){
                        //  if it is below the limit - generate a new column and start it from random position.
                        matrixColumnModelList.set(i, initializeNewColumn(1)); // new column starts from the top
                        break;
                    }else {
                        // otherwise, we use the next character from the "pool" of characters.
                        matrixColumnModelList.get(i).columnCharsList.set(j, matrixColumnModelList.get(i).charsPoolList.get(matrixColumnModelList.get(i).counter));
                        matrixColumnModelList.get(i).counter += 1; // next iteration we get the next character from the pool of characters

                        paintText.setColor(Color.WHITE);
                    }
                } else {
                    // for all other characters assign the value of the one below it in the column.
                    matrixColumnModelList.get(i).columnCharsList.set(j, matrixColumnModelList.get(i).columnCharsList.get(j + 1));

                    // add random flickering of random characters (substitute the current one with a random one):
//                    if (Math.random() > 0.99){
//                        matrixColumnModelList.get(i).columnCharsList.set(j, generateRandomChar());
//                        paintText.setColor(Color.WHITE);
//                    }

                    // add a special effect: highlight each word from MY_MESSAGE in the column (change the color of this word)
                    if (isHighlightMyMessage){

                        if (j > wordIndexStart - 3 && j < wordIndexStart + wordLength - 2) {
                            paintText.setColor(myMessageHighlightColorBisBlue);
                            if (RANDOM.nextDouble() >= 0.5){
                                paintText.setColor(myMessageHighlightColorBisPurple);
                            }
                        }
                    }

                    // set alpha from 0 to 255 for all the elements in the column, so that the bottom element is fully visible and the top is barely visible.
                    paintText.setAlpha(255 / matrixColumnModelList.get(i).columnCharsList.size() * j);
                }

                // draw the character
                canvas.drawText("" + matrixColumnModelList.get(i).columnCharsList.get(j), (i+1) * columnWidth, matrixColumnModelList.get(i).charsVertPosIndexList.get(j), paintText);

                // increase the vertical coordinate of the current character by one step
                matrixColumnModelList.get(i).charsVertPosIndexList.set(j, matrixColumnModelList.get(i).charsVertPosIndexList.get(j) + matrixColumnModelList.get(i).textFontSize);
            }
        }
    }

    private MatrixColumnModel initializeNewColumn(int initialHeightLimit) {
        // initialHeightLimit is the initial position of the column on the screen (typically top of the screen; exception is the start of the application - then random position).

        int columnTextLength = RANDOM.nextInt(screenHeight / columnWidth /3 * 2 - screenHeight / columnWidth / 4) + screenHeight / columnWidth / 4; // random length.
        int font = RANDOM.nextInt(columnWidth - columnWidth /2)+ columnWidth /2; // random font size from half to full column width

        MatrixColumnModel matrixColumnModel = new MatrixColumnModel();
        matrixColumnModel.textFontSize = font;
        matrixColumnModel.columnCharsList = generateRandomText(columnTextLength);
        matrixColumnModel.charsPoolList = generateRandomText(screenHeight / font + columnTextLength);
        matrixColumnModel.textInitialHeightIndex = RANDOM.nextInt(initialHeightLimit);
        matrixColumnModel.textFinalHeightIndex =  RANDOM.nextInt(screenHeight - screenHeight/5*4) + screenHeight/5*4;
        matrixColumnModel.textFontStyle = 1;
        matrixColumnModel.textFallingSpeed = 1;
        matrixColumnModel.counter = 0;
        matrixColumnModel.charsVertPosIndexList = new ArrayList<>(); // a list of current positions of each character on the column; as the text falls  - positions change and we need to track them.
        for(int j = 0; j < matrixColumnModel.columnCharsList.size(); j++){
            matrixColumnModel.charsVertPosIndexList.add(j*matrixColumnModel.textFontSize - matrixColumnModel.columnCharsList.size()*matrixColumnModel.textFontSize + matrixColumnModel.textInitialHeightIndex);
        }
        return matrixColumnModel;
    }

    public List<Character> generateRandomText(int textLength){
        /*
         * generates a List of randomly generated chars; used for creating columns of falling chars.
         */

        List<Character> randomTextChars = new ArrayList<>();

        // generate random text
        for (int i = 0; i < textLength; i++) {
            randomTextChars.add(generateRandomChar());
        }

        // insert words from user's string into the text:
        // randomly choose which word from MY_Message to insert in this column
        int indexOfWordToInsert = RANDOM.nextInt(myMessageWordsList.size());

        // convert the chosen word to List<Character> for insertion into list of characters.
        List<Character> charsToInsertList = new ArrayList<>();
        for (int i = 0; i < myMessageWordsList.get(indexOfWordToInsert).toCharArray().length; i++) {
            charsToInsertList.add(myMessageWordsList.get(indexOfWordToInsert).toCharArray()[i]);
        }

        // now insert the word from the user's string into randomly generated text (one word per column in random place in the column).
        if (Math.random() > 0.7) {
            // insert the message word only in 30% of columns in order not to pollute the screen with flashing message words.
            randomTextChars.addAll(RANDOM.nextInt(textLength / 2), charsToInsertList); // this will increase the size of the list, which we don't want, so we will truncate it later.
        }

        return randomTextChars.subList(0, textLength); // truncate list to the original length.
    }

    public char generateRandomChar(){
        /*
         * Generates a random char chosen from defined ranges of chars. The range is also chosen randomly.
         * Characters used in the matrix rain:
         * 1. Half-width Japanese Katakana: chars from 12449 through 12615 range
         * 2. Western Latin letters and numerals: chars from 48 through 90 range
         * Source: https://scifi.stackexchange.com/questions/65525/what-is-the-digital-rain-seen-in-the-matrix-universe-made-of
         */

        // randomly choose the range of characters from which we will choose our character (Japanese or latin)
        rangeToSelectRandomCharFrom = RANDOM.nextInt(CHAR_RANGE_LIST.size());

        // generate a random character from the chosen range.
        randomIntFromRange = (int) (RANDOM.nextDouble() * (CHAR_RANGE_LIST.get(rangeToSelectRandomCharFrom)[1]
                - CHAR_RANGE_LIST.get(rangeToSelectRandomCharFrom)[0] + 1)) + CHAR_RANGE_LIST.get(rangeToSelectRandomCharFrom)[0];

        return (char) randomIntFromRange;
    }
}
