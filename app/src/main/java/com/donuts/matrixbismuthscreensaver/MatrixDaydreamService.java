package com.donuts.matrixbismuthscreensaver;

import android.service.dreams.DreamService;
import android.util.Log;

/**
 * DayDream service is all that's needed for the screensaver app.
 * Use MainActivity for dev and debug, but comment it out on manifest for production - it's not needed for the app.
 * This Daydream service inflates the layout with a custom View (MatrixEffectView) where all the drawing of matrix rain is done.
 */

public class MatrixDaydreamService extends DreamService{

    @Override
    public void onAttachedToWindow() {
    //setup daydream
        super.onAttachedToWindow();
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixDaydreamService", "onAttachedToWindow: " +
                "Day dreaming window attached.");
        setInteractive(false);
        setFullscreen(true);

        setContentView(R.layout.matrix_effect);
    }

    @Override
    public void onDreamingStarted() {
        //daydream started
        super.onDreamingStarted();
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixDaydreamService", "onDreamingStarted: " +
                "Day dreaming started.");
    }

    @Override
    public void onDreamingStopped(){
    //daydream stopped
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixDaydreamService", "onDreamingStopped: "+
                "Day dreaming stopped.");
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
    //tidy up
        Log.d(CurrentTime.getCurrentTime("HH:mm:ss") + " MatrixDaydreamService", "onDetachedFromWindow: "+
                "Day dreaming window detached.");
        super.onDetachedFromWindow();
    }

}
