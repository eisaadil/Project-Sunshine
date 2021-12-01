package com.eisaadil.sunshine.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.eisaadil.sunshine.data.Tables.LocationTable;
import com.eisaadil.sunshine.data.Tables.WeatherTable;

import java.util.ArrayList;

/**
 * Created by eisaadil on 24/01/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather.db";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationTable.TABLE_NAME + " (" +
                LocationTable._ID + " INTEGER PRIMARY KEY, " +
                LocationTable.COLUMN_LOCATION_FORMATTED + " TEXT NOT NULL, " +
                LocationTable.COLUMN_CITY + " TEXT NOT NULL, " +
                LocationTable.COLUMN_COUNTRY + " TEXT NOT NULL, " +
                LocationTable.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationTable.COLUMN_LONGITUDE + " REAL NOT NULL " +
                ");";
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE "+WeatherTable.TABLE_NAME+" ("+
                WeatherTable.COLUMN_LOCATION_ID + " INTEGER NOT NULL, " +
                WeatherTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeatherTable.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherTable.COLUMN_DATE_TEXT + " TEXT NOT NULL, " +
                WeatherTable.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherTable.COLUMN_LONG_DESC + " TEXT NOT NULL, " +
                WeatherTable.COLUMN_ICON + " TEXT NOT NULL, " +
                WeatherTable.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherTable.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                WeatherTable.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherTable.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherTable.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherTable.COLUMN_DEGREES + " REAL NOT NULL, " +
                // FOREIGN KEY (column_loc_key) REFERENCES LocationTable (_id)
                // This is done to create a link between the two fields
                "FOREIGN KEY (" + WeatherTable.COLUMN_LOCATION_ID + ") REFERENCES "+
                LocationTable.TABLE_NAME + " (" + LocationTable._ID + "), "+
                //Below, makes the two fields unique
                " UNIQUE (" + WeatherTable.COLUMN_DATE + ", " +
                WeatherTable.COLUMN_LOCATION_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+WeatherTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+LocationTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public ArrayList<Cursor> getData(String Query){ //ADM third party code
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
