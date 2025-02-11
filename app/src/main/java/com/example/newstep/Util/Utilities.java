package com.example.newstep.Util;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    public static Timestamp convertToTimestamp(String dateString) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            Date date = sdf.parse(dateString);
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
