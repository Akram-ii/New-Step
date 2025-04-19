package com.example.newstep.Util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateUtils;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utilities {
    public static String getMonthFormat(int month) {
        switch (month){
            case 1:
                return "JAN";
            case 2:
                return "FEB";
            case 3:
                return "MAR";
            case 4:
                return "APR";
            case 5:
                return "MAY";
            case 6:
                return "JUN";
            case 7:
                return "JUL";
            case 8:
                return "AUG";
            case 9:
                return "SEP";
            case 10:
                return "OCT";
            case 11:
                return "NOV";
            case 12:
                return "DEC";
            default:
                return "";
        }
    }
    public static String timestampToString(Timestamp timestamp){
        String s= new SimpleDateFormat("yyyy/MM/dd HH:mm").format(timestamp.toDate());
        String monthOrDay = s.substring(5, 7);
        int month=Integer.parseInt(monthOrDay);
        String year=s.substring(0,4);
        String day=s.substring(8,10);
        return getMonthFormat(month)+" "+day+" "+year+" "+s.substring(11,s.length());
    }

    public static String timestampToStringNoDetail(Timestamp timestamp){
        String s= new SimpleDateFormat("yyyy/MM/dd").format(timestamp.toDate());
        String monthOrDay = s.substring(5, 7);
        int month=Integer.parseInt(monthOrDay);
        String year=s.substring(0,4);
        String day=s.substring(8,10);
        return getMonthFormat(month)+" "+day+" "+year;
    }
    
    public static String getRelativeTime(Timestamp timestamp) {
        if (timestamp == null) return "Unknown";

        long time = timestamp.getSeconds() * 1000;
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }
    public static void vibratePhone(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    public static  String hexCodeForColor(String colorName) {
        switch(colorName){
            case "pink":
                return "#C9A6D6";
            case "purple":
                return "#877DE0";
            case "blue":
                return "#6A96E6";
            case "green":
                return "#91B2BD";
            case "gray":
                return "#6C757D";
            case "darkBlue":
                return "#3C3C64";
            case  "orange"  :
                return "#F28B30";

            default :
                return "#D7BDE2";
        }
    }
}
