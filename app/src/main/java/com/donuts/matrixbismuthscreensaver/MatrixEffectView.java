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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MatrixEffectView extends View {

    private static final Random RANDOM = new Random();
    private static final List<int[]> CHAR_RANGE_LIST = new ArrayList<>(Arrays.asList(new int[] {48,90}, new int[] {12449,12650})); // Ranges in unicode characters for random character generation
    private List<MatrixColumnModel> MatrixColumnModelList;
    private int screenWidth, screenHeight;
    private Canvas canvas;
    private Bitmap bitmap;
    private static final int COLUMN_WIDTH = 50; // empirically found this to be good.
    private static final int PAINT_DELAY_MILLIS= 80; // canvas refresh frequency.
    private Bitmap bisLogoBitmap;
    private static final String MY_MESSAGE = "Bismuth Python Blockchain";
    private List<String> myMessageWordsList;

    private Paint paintText, paintBackground, paintBitmapBackground, paintInitialBackground;

    public MatrixEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.GREEN);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintBackground = new Paint();
        paintBackground.setColor(Color.BLACK);
        paintBackground.setAlpha(255);
        paintBackground.setStyle(Paint.Style.FILL);
        paintBitmapBackground = new Paint();
        paintBitmapBackground.setColor(Color.BLACK);
        paintInitialBackground = new Paint();
        paintInitialBackground.setColor(Color.BLACK);
        paintInitialBackground.setAlpha(255);
        paintInitialBackground.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*
         This method is called when your view is first assigned a size, and again if the size of your view changes for any reason.
         We just get view dimensions and create canvas here.
         */

        myMessageWordsList = Arrays.asList(MY_MESSAGE.split(" "));

        screenWidth = w; // width of the screen in pixels
        screenHeight = h; // height of the screen in pixels

        // create canvas
        bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paintInitialBackground);

        /*
         * initialize matrix columns:
         * it's a list of matrixColumnModel where number of elements in the list corresponds to number
         * of columns of text on the screen and each column has its own properties (e.g. text, font, size, etc)
         */
        
        MatrixColumnModelList = new ArrayList<>();
        for (int i = 0; i < screenWidth / COLUMN_WIDTH; i++){
            MatrixColumnModelList.add(initializeNewColumn(screenHeight)); // for the first initialization we randomly place columns on the screen
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

        // sleep for <PAINT_DELAY_MILLIS> before next canvas draw. Empirically found that 100ms is good enough.
        try{
            Thread.sleep(PAINT_DELAY_MILLIS);
        }catch( InterruptedException e){
            Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixEffect", "onDraw: "+
                    "failed to sleep.");
        }

        invalidate(); // invalidate canvas and call onDraw() - makes onDraw loop infinitely.
    }

    private void paintCanvas() {
        canvas.drawRect(0, 0, screenWidth, screenHeight, paintBackground);

        for(int i = 0; i < MatrixColumnModelList.size(); i++) {
            // for each column in the matrix:

            paintText.setTextSize(MatrixColumnModelList.get(i).textFontSize);

            for (int j = 0; j < MatrixColumnModelList.get(i).columnCharsList.size(); j++) {
                // for each character in the column:

                if (j == MatrixColumnModelList.get(i).columnCharsList.size() - 1) {
                    // if this is the last character in the column (the one at the bottom):
                    if (MatrixColumnModelList.get(i).charsVertPosIndexList.get(j)  > MatrixColumnModelList.get(i).textFinalHeightIndex){
                        //  if it is below the limit - generate a new column and start it from random position.
                        MatrixColumnModelList.set(i, initializeNewColumn(1)); // new column starts from the top
                        break;
                    }else {
                        // otherwise, we use the next character from the "pool" of characters.
                        MatrixColumnModelList.get(i).columnCharsList.set(j, MatrixColumnModelList.get(i).charsPoolList.get(MatrixColumnModelList.get(i).counter));
                        MatrixColumnModelList.get(i).counter += 1; // next iteration we get the next character from the pool of characters

                        paintText.setColor(Color.WHITE);
                    }
                } else {
                    // for all other characters assign the value of the one below it in the column.
                    MatrixColumnModelList.get(i).columnCharsList.set(j, MatrixColumnModelList.get(i).columnCharsList.get(j + 1));
                    paintText.setColor(Color.GREEN);

                    // add random flickering of random characters (substitute the current one with a random one):
                    if (Math.random() > 0.99){
                        MatrixColumnModelList.get(i).columnCharsList.set(j, generateRandomChar());
                        paintText.setColor(Color.WHITE);
                    }

                    // set alpha from 0 to 255 for all the elements in the column, so that the bottom element is fully visible and the top is barely visible.
                    paintText.setAlpha(255 / MatrixColumnModelList.get(i).columnCharsList.size() * j);
                }

                // draw the character
                canvas.drawText("" + MatrixColumnModelList.get(i).columnCharsList.get(j), (i+1) * COLUMN_WIDTH, MatrixColumnModelList.get(i).charsVertPosIndexList.get(j), paintText);

                // increase the vertical coordinate of the current character by one step
                MatrixColumnModelList.get(i).charsVertPosIndexList.set(j, MatrixColumnModelList.get(i).charsVertPosIndexList.get(j) + MatrixColumnModelList.get(i).textFontSize);
            }
        }
    }

    private MatrixColumnModel initializeNewColumn(int initialHeightLimit) {

        int columnTextLength = RANDOM.nextInt(screenHeight / COLUMN_WIDTH / 3 * 2 - screenHeight / COLUMN_WIDTH / 4) + screenHeight / COLUMN_WIDTH / 4; // random length.
        int font = RANDOM.nextInt(COLUMN_WIDTH - COLUMN_WIDTH/2)+COLUMN_WIDTH/2; // random font size

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

        // convert the chosen word to List<Character> for insertion into list of characters; also invert it for readability on the screen.
        List<Character> charsToInsertList = new ArrayList<>();
        for (int i = myMessageWordsList.get(indexOfWordToInsert).toCharArray().length - 1; i >= 0; i--) {
            charsToInsertList.add(myMessageWordsList.get(indexOfWordToInsert).toCharArray()[i]);
        }

        // now insert the word from the user's string into randomly generated text (one word per column in random place in the column).
        randomTextChars.addAll(RANDOM.nextInt(textLength/2), charsToInsertList); // this will increase the size of the list, which we don't want, so we will truncate it later.

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
        int rangeToSelectFrom = RANDOM.nextInt(CHAR_RANGE_LIST.size());

        // generate a random character from the chosen range.
        int randomIntFromRange = (int) (RANDOM.nextDouble() * (CHAR_RANGE_LIST.get(rangeToSelectFrom)[1]
                - CHAR_RANGE_LIST.get(rangeToSelectFrom)[0] + 1)) + CHAR_RANGE_LIST.get(rangeToSelectFrom)[0];

        return (char) randomIntFromRange;
    }
}
