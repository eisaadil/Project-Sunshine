package com.eisaadil.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by eisaadil on 25/01/17.
 */

// TO USE A ContentProvider, CALL:
// weatherCursor = getContext().getContentResolver().query(Tables.WeatherTable.buildWeatherUri(4);
// FOR OTHER URIs too

public class WeatherProvider extends ContentProvider {

    private DatabaseHandler dbHandler;

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    public static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static UriMatcher buildUriMatcher() {

        matcher = new UriMatcher(UriMatcher.NO_MATCH); //default way to initialize
        final String authority = Tables.CONTENT_AUTHORITY;

        matcher.addURI(authority, Tables.PATH_WEATHER + "/", WEATHER);
        matcher.addURI(authority, Tables.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, Tables.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, Tables.PATH_LOCATION + "/", LOCATION);
        matcher.addURI(authority, Tables.PATH_LOCATION + "/#", LOCATION_ID);

         /*
            * is for string, # is for number
            FOR EXAMPLE:
            sURIMatcher.addURI("contacts", "people", PEOPLE);
            sURIMatcher.addURI("contacts", "people/#", PEOPLE_ID);
            sURIMatcher.addURI("contacts", "people/#/phones", PEOPLE_PHONES);
            sURIMatcher.addURI("contacts", "people/#/phones/#", PEOPLE_PHONES_ID);
            sURIMatcher.addURI("contacts", "people/#/contact_methods", PEOPLE_CONTACTMETHODS);
            sURIMatcher.addURI("contacts", "people/#/contact_methods/#", PEOPLE_CONTACTMETHODS_ID);

         */

        return matcher;
     }

    private static final SQLiteQueryBuilder sWeatherByLocationIdQueryBuilder;

    static{
        //ALL OTHER QUERIES ARE BUILT ON THIS QUERY THAT LINKS THE LINKS THE location_id AND _id
        sWeatherByLocationIdQueryBuilder = new SQLiteQueryBuilder();
            sWeatherByLocationIdQueryBuilder.setTables(
             Tables.WeatherTable.TABLE_NAME + " INNER JOIN " +
             Tables.LocationTable.TABLE_NAME +
             " ON " + Tables.WeatherTable.TABLE_NAME +  "." + Tables.WeatherTable.COLUMN_LOCATION_ID +
             " = " + Tables.LocationTable.TABLE_NAME + "." + Tables.LocationTable._ID);
        //  weather INNER JOIN location ON weather.location_id = location._ID

        buildUriMatcher();
    }

     private Cursor getWeatherByLocationId(Uri uri, String[] projection, String sortOrder) {
         String locationId = Tables.WeatherTable.getLocationIdFromUri(uri);
         String startDate = Tables.WeatherTable.getStartDateFromUri(uri);

         String[] selectionArgs;
         String selection;

         if (startDate == null) {
             selection = Tables.LocationTable.TABLE_NAME+ "." + Tables.LocationTable._ID + " = ? ";
             selectionArgs = new String[]{locationId};
         } else {
             selection =
                     Tables.LocationTable.TABLE_NAME+ "." + Tables.LocationTable._ID + " = ? AND " +
                             Tables.WeatherTable.COLUMN_DATE_TEXT + " >= ? ";
             selectionArgs = new String[]{locationId, startDate};
         }

         return sWeatherByLocationIdQueryBuilder.query(dbHandler.getReadableDatabase(),
                 projection,
                 selection,
                 selectionArgs,
                 null,
                 null,
                 sortOrder
         );
     }

     private Cursor getWeatherByLocationIdAndDate(Uri uri, String[] projection, String sortOrder) {

         String locationId = Tables.WeatherTable.getLocationIdFromUri(uri);
         String date = Tables.WeatherTable.getDateFromUri(uri);

         final String selection =
                 Tables.LocationTable.TABLE_NAME +"." + Tables.LocationTable._ID + " = ? AND " +
                         Tables.WeatherTable.COLUMN_DATE + " = ? ";

         return sWeatherByLocationIdQueryBuilder.query(dbHandler.getReadableDatabase(),
                 projection,
                 selection,
                 new String[]{locationId, date},
                 null,
                 null,
                 sortOrder
         );
     }


    @Override
    public boolean onCreate() {
        dbHandler = new DatabaseHandler(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)){
            case WEATHER:
                return Tables.WeatherTable.CONTENT_DIR_TYPE;
            case WEATHER_WITH_LOCATION:
                return Tables.WeatherTable.CONTENT_DIR_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return Tables.WeatherTable.CONTENT_ITEM_TYPE;
            case LOCATION:
                return Tables.LocationTable.CONTENT_DIR_TYPE;
            case LOCATION_ID:
                return Tables.LocationTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }



    @Nullable
    @Override //projection is columns
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         // Here's the switch statement that, given a URI, will determine what kind of request it is,
         // and query the database accordingly.
         Cursor retCursor;
         int m = matcher.match(uri);
         switch (m) {

             case WEATHER_WITH_LOCATION_AND_DATE:
                 // "weather/*/*"
                 retCursor = getWeatherByLocationIdAndDate(uri, projection, sortOrder);
                 break;

             case WEATHER_WITH_LOCATION:
                 // "weather/*"
                 retCursor = getWeatherByLocationId(uri, projection, sortOrder);
                 break;

             case WEATHER:
                 // "weather"
                 retCursor = dbHandler.getReadableDatabase().query(
                         Tables.WeatherTable.TABLE_NAME,
                         projection,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         sortOrder
                 );
                 break;
             case LOCATION:
                 Log.v("Location", "QUERY");
                 retCursor = dbHandler.getReadableDatabase().query(
                     Tables.LocationTable.TABLE_NAME, projection, selection, selectionArgs, null ,null ,sortOrder);
                 break;
             default:
                 Log.v("Location", "ERROR");
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
         retCursor.setNotificationUri(getContext().getContentResolver(), uri);

         return retCursor;
    }

     @Override
     public Uri insert(Uri uri, ContentValues values) {
         final SQLiteDatabase db = dbHandler.getWritableDatabase();
         Uri returnUri;

         long _id; //this is the id where the data is inserted. if 0 or 1 is returned, it failed
         //directly inserted to location or weather tables
         int match = matcher.match(uri);
         switch (match) {
             case WEATHER:
                 _id = db.insert(Tables.WeatherTable.TABLE_NAME, null, values);
                 if (_id > 0)
                     returnUri = Tables.WeatherTable.buildWeatherUri(_id);
                 else
                     throw new android.database.SQLException("Failed to insert row into " + uri);
                 break;
             case LOCATION:
                 _id = db.insert(Tables.LocationTable.TABLE_NAME, null, values);
                 if (_id > 0)
                     returnUri = Tables.LocationTable.buildLocationUri(_id);
                 else
                     throw new android.database.SQLException("Failed to insert row into " + uri);
                 break;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
         getContext().getContentResolver().notifyChange(uri, null); //notifies change to subdirectories
         return returnUri;
     }

     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         //passing null in selection and selectionArgs deletes all the records of the table
         final SQLiteDatabase db = dbHandler.getWritableDatabase();
         final int match = matcher.match(uri);
         int rowsDeleted;
         switch (match) {
             case WEATHER:
                 rowsDeleted = db.delete(Tables.WeatherTable.TABLE_NAME, selection, selectionArgs);
                 break;
             case LOCATION:
                 rowsDeleted = db.delete(Tables.LocationTable.TABLE_NAME, selection, selectionArgs);
                 break;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
         // Because a null deletes all rows
         if (selection == null || rowsDeleted != 0) { // <- Optional condition. Wouldn't make much of a difference
             getContext().getContentResolver().notifyChange(uri, null);
         }
         return rowsDeleted;
     }

     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         final SQLiteDatabase db = dbHandler.getWritableDatabase();
         int rowsUpdated;
         switch (matcher.match(uri)) {
             case WEATHER:
                 rowsUpdated = db.update(Tables.WeatherTable.TABLE_NAME, values, selection, selectionArgs);
                 break;
             case LOCATION:
                 rowsUpdated = db.update(Tables.LocationTable.TABLE_NAME, values, selection, selectionArgs);
                 break;
             default:
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
         }
         // Because a null deletes all rows
         if (selection == null || rowsUpdated != 0) {
             getContext().getContentResolver().notifyChange(uri, null);
         }
         return rowsUpdated;
     }

     @Override
     public int bulkInsert(Uri uri, ContentValues[] values) {
         final SQLiteDatabase db = dbHandler.getWritableDatabase();
         switch (matcher.match(uri)) {
             case WEATHER:
                 db.beginTransaction();
                 int returnCount = 0;
                 try {
                     for (ContentValues value : values) {
                         long _id = db.insert(Tables.WeatherTable.TABLE_NAME, null, value);
                         if (_id != -1) {
                             returnCount++;
                         }
                     }
                     db.setTransactionSuccessful();
                 } finally {
                     db.endTransaction();
                 }
                 getContext().getContentResolver().notifyChange(uri, null);
                 return returnCount;
             default:
                 return super.bulkInsert(uri, values);
         }
     }
 }

