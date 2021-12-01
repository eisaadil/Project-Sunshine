package com.eisaadil.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by eisaadil on 24/01/17.
 */

//Uri.parse() is only for Strings.

public class Tables {

    public static final String CONTENT_AUTHORITY = "com.eisaadil.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static final class WeatherTable implements BaseColumns{ //BaseColumns is used for _id

        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_LOCATION_ID = "location_id";
        public static final String COLUMN_DATE_TEXT = "date_text";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SHORT_DESC = "short_desc";
        public static final String COLUMN_LONG_DESC = "long_desc";
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND_SPEED = "wind_speed";
        public static final String COLUMN_DEGREES = "degrees";
        public static final String COLUMN_ICON = "icon";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();
        // "vnd.android.cursor.dir" is used for directory (list of items and "vnd.android.cursor.item" is used for single item path

        public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;



        public static Uri buildWeatherUri(long id) {
            //  content://com.eisaadil.android.sunshine.app/weather/4
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildWeatherLocationUri(String locationId) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934     (Returns directory)
            return CONTENT_URI.buildUpon().appendPath(locationId).build();
        }

        public static Uri buildWeatherLocationWithStartDateUri(String locationId, String startDate) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934/date_text=25_Jun_2016 (Returns item)
            return CONTENT_URI.buildUpon().appendPath(locationId)
                    .appendQueryParameter(COLUMN_DATE_TEXT, startDate).build();
        }
        public static Uri buildWeatherLocationWithDateUri(String locationSetting, String date) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934/149499393 (Returns item)
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }
        public static String getLocationIdFromUri(Uri uri) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934               (it is at index 1)
            return uri.getPathSegments().get(1);
        }
        public static String getDateFromUri(Uri uri) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934/149499393     (it is at index 2)
            return uri.getPathSegments().get(2);
        }
        public static String getStartDateFromUri(Uri uri) {
            //  content://com.eisaadil.android.sunshine.app/weather/495934/date_text=25_Jun_2016   (it is at param "date_text")
            return uri.getQueryParameter(COLUMN_DATE_TEXT);
        }

    }
    public static final class LocationTable implements BaseColumns{

        public static final String TABLE_NAME = "location";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_LOCATION_FORMATTED = "location_formatted";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";


        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        // "vnd.android.cursor.dir" is used for directory (list of items and "vnd.android.cursor.item" is used for single item path

        public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static Uri buildLocationUri(long _id){
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }


    }


}
