package com.superiorinfotech.publicbuddy.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by admin on 4/24/2015.
 */
public class DateConvert {
    public static String convert1(String inputDate){

        String outPutDate = null;
        try {
            Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a").parse(inputDate);
            outPutDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            outPutDate = "2007-01-01 10:00:00 AM";
        }
        return outPutDate;
    }
    public static String convert2(String inputDate){

        String outPutDate = null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a").parse(inputDate);
            outPutDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            outPutDate = "2007-01-01";
        }
        return outPutDate;
    }
    public static String convert3(String inputDate){
        String outPutDate = null;

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a").parse(inputDate);
            outPutDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a").format(date);
            return outPutDate;

        } catch (ParseException e) {
            e.printStackTrace();
            return  "01/01/2007";
        }

    }
    public static String convert4(String inputDate){
        String outPutDate = null;

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate);
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            long now = date.getTime() + gmtOffset;


            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
            String dateString = formatter.format(new Date(now));
            return dateString;

        } catch (ParseException e) {
            e.printStackTrace();
            return  "01/01/2007";
        }

    }
    public static String convert5(String inputDate){
        String outPutDate = null;

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate);
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            long now = date.getTime() + gmtOffset;


            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String dateString = formatter.format(new Date(now));
            return dateString;

        } catch (ParseException e) {
            e.printStackTrace();
            return  "01/01/2007";
        }

    }
}
