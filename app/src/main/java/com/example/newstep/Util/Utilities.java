package com.example.newstep.Util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateUtils;

import com.example.newstep.R;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utilities {
    public static String hexCodeForColor(String colorName) {
        switch(colorName){
            case "pink":
                return "#C9A6D6";
            case "purple":
                return "#877DE0";
            case "blue":
                return "#6A96E6";
            case "green":
                return "#91B2BD";
            case "orange":
                return "#F28B30";
            case "darkBlue":
                return "#3C3C64";

            default :
                return "#D7BDE2";
        }
    }
    public static String getRandomHexColor() {
        Random random = new Random();
        List<String> list = new ArrayList<>();
        list.add("#DDE6F2"); // soft desaturated blue-gray
        list.add("#CFCDE7"); // gentle lavender-gray
        list.add("#D9EAD3"); // soft sage green
        list.add("#EDE0D4"); // light warm beige
        list.add("#F6E7E7"); // dusty rose
        list.add("#E4D1EC"); // pale mauve

        return list.get(random.nextInt(list.size()));
    }

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
    private static final int[] POINT_THRESHOLDS = {
            0, 25, 75, 150, 300, 500, 1000, 2000, 4000, 10000
    };

    private static final int[] IMAGE_IDS = {
            R.drawable.first_step_badge,
            R.drawable.fog_badge,
            R.drawable.fire_badge,
            R.drawable.lighthouse_badge,
            R.drawable.sun_badge,
            R.drawable.nest_badge,
            R.drawable.safe_badge,
            R.drawable.star_badge,
            R.drawable.healer_badge,
            R.drawable.phoenix_badge
    };
    public static int getNextBadgeImageId(int points) {
        for (int i = 0; i < POINT_THRESHOLDS.length; i++) {
            if (points < POINT_THRESHOLDS[i]) {
                return IMAGE_IDS[i];
            }
        }
        return IMAGE_IDS[IMAGE_IDS.length - 1];
    }
    public static int getCurrBadgesImageId(int points){
        if (points < 25) {
            return R.drawable.first_step_badge;
        } else if (points < 75) {
            return R.drawable.fog_badge;
        } else if (points < 150) {
            return R.drawable.fire_badge;
        } else if (points < 300) {
            return R.drawable.lighthouse_badge;
        } else if (points < 500) {
            return R.drawable.sun_badge;
        } else if (points < 1000) {
            return R.drawable.nest_badge;
        } else if (points < 2000) {
            return R.drawable.safe_badge;
        } else if (points < 4000) {
            return R.drawable.star_badge;
        } else if (points < 10000) {
            return R.drawable.healer_badge;
        } else {
            return R.drawable.phoenix_badge;
        }
    }
public static int getMinBadge(int points){
    if (points < 25) {
        return 0;
    } else if (points < 75) {
        return 25;
    } else if (points < 150) {
        return 75;
    } else if (points < 300) {
        return 150;
    } else if (points < 500) {
        return 300;
    } else if (points < 1000) {
        return 500;
    } else if (points < 2000) {
        return 1000;
    } else if (points < 4000) {
        return 2000;
    } else if (points < 10000) {
        return 4000;
    } else {
        return 10000;
    }
}
    public static int getNextBadge(int points) {
        if (points < 25) {
            return 25;
        } else if (points < 75) {
            return 75;
        } else if (points < 150) {
            return 150;
        } else if (points < 300) {
            return 300;
        } else if (points < 500) {
            return 500;
        } else if (points < 1000) {
            return 1000;
        } else if (points < 2000) {
            return 2000;
        } else if (points < 4000) {
            return 4000;
        } else if (points < 10000) {
            return 10000;
        } else {
            return 10000;
        }
    }
}
