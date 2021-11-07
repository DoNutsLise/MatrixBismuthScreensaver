package com.donuts.matrixbismuthscreensaver;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentTime {
    public static String getCurrentTime(String timeFormat){
        Format format = new SimpleDateFormat(timeFormat);
        return format.format(new Date());
    }
}