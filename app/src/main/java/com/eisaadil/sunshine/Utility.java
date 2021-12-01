package com.eisaadil.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Created by eisaadil on 10/02/17.
 */

public class Utility {
    public static boolean checkIfInternet(Context context) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Log.d("CHECK IF INTERNET","no internet connection");
            Toast.makeText(context, "Please find a stable internet connection", Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                Log.d("CHECK IF INTERNET"," internet connection available...");
                return true;
            }
            else
            {
                Log.d("CHECK IF INTERNET"," internet connection");
                return true;
            }

        }
    }
    public static String capitalizeEachWord(final String line) {
        String[] arr = line.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static Drawable getImageFromFileName(String fileName, Context context){
        String uri = "@drawable/"+fileName;

        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

        return context.getResources().getDrawable(imageResource);
    }

    public static void setDefaultPreference(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaultPreference(String key, String defaultPref, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defaultPref);
    }

    public static String getFormattedDecimal(double x){ //for max or min
        DecimalFormat decim = new DecimalFormat("0");
        return decim.format(x);
    }




}
