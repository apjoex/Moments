package com.x.memories.reusables;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.x.memories.models.Event;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by AKINDE-PETERS on 8/31/2016.
 */
public class Utilities {

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static String getTime(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        return date.format(currentLocalTime);
    }

    public static String daysAgo(String tochangeDate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date endDate = null;
        try {
            try {
                endDate = formatter.parse(tochangeDate);
            } catch (java.text.ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;

        String curentDateandTime = sdf.format(new Date());
        try {
            try {
                startDate = formatter.parse(curentDateandTime);
            } catch (java.text.ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
        }

        long diff = (startDate.getTime()) - (endDate.getTime());

        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
        int hours = (int) (diff / (1000 * 60 * 60));
        int minutes = (int) (diff / (1000 * 60));
        int seconds = (int) (diff / (1000));

        String h = "";
        if(numOfDays == 1){
            h = numOfDays+" day ago";
        }else if(numOfDays > 1){
            h = numOfDays+" days ago";
        }else if(hours == 1){
            h = hours+" hour ago";
        }else if(hours > 1){
            h = hours+" hours ago";
        }else if(minutes == 1){
            h = minutes+" minute ago";
        }else if(minutes > 1){
            h = minutes+" minutes ago";
        }else if(seconds > 0){
            h = seconds+" seconds ago";
        }
        return h;

    }

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }


    public static Event getCurrentEvent (SharedPreferences preferences) {
        //Retrieve configuration
        Gson gson = new Gson();
        String data = preferences.getString("Event_details", "");
        return gson.fromJson(data, Event.class);
    }

}
