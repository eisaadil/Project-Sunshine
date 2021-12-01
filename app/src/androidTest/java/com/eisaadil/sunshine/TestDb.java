package com.eisaadil.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Log;

import com.eisaadil.sunshine.data.DatabaseHandler;
import com.eisaadil.sunshine.data.Tables.LocationTable;
import com.eisaadil.sunshine.data.Tables.WeatherTable;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by eisaadil on 24/01/17.
 */
@RunWith(AndroidJUnit4.class)
public class TestDb {

    Context mContext;

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        mContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.eisaadil.sunshine", mContext.getPackageName());


    }/*
    @Test
    public void TestDb() throws Throwable{
        try {
            mContext.deleteDatabase(DatabaseHandler.DATABASE_NAME);
            SQLiteDatabase db = new DatabaseHandler(mContext.getApplicationContext()).getWritableDatabase();
            assertEquals(true, db.isOpen());
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    @Test
    public void testInsertReadDb() {
        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "Riyadh";
        String testCityCountry = "Riyadh, SA";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DatabaseHandler dbHandler = new DatabaseHandler(mContext);
        try {
            SQLiteDatabase db = dbHandler.getWritableDatabase();
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(LocationTable.COLUMN_LOCATION_SETTING, testLocationSetting);
            values.put(LocationTable.COLUMN_CITY, testCityName);
            values.put(LocationTable.COLUMN_LATITUDE, testLatitude);
            values.put(LocationTable.COLUMN_LONGITUDE, testLongitude);
            values.put(LocationTable.COLUMN_LOCATION_FORMATTED, testCityCountry);
            long locationRowId;
            locationRowId = db.insert(LocationTable.TABLE_NAME, null, values);
            // Verify we got a row back.
            assertTrue(locationRowId != -1);
            Log.d("NEW ROW", "New row id: " + locationRowId);
            // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
            // the round trip.
            // Specify which columns you want.
            String[] columns = {
                    LocationTable._ID,
                    LocationTable.COLUMN_LOCATION_SETTING,
                    LocationTable.COLUMN_CITY,
                    LocationTable.COLUMN_LATITUDE,
                    LocationTable.COLUMN_LONGITUDE,
                    LocationTable.COLUMN_LOCATION_FORMATTED
            };
            // A cursor is your primary interface to the query results.
            Cursor cursor = db.query(
                    LocationTable.TABLE_NAME,  // Table to Query
                    columns,
                    null, // Columns for the "where" clause
                    null, // Values for the "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null // sort order
            );


            // If possible, move to the first row of the query results.
            if (cursor.moveToFirst()) {
                // Get the value in each column by finding the appropriate column index.
                int locationIndex = cursor.getColumnIndex(LocationTable.COLUMN_LOCATION_SETTING);
                String location = cursor.getString(locationIndex);
                int nameIndex = cursor.getColumnIndex((LocationTable.COLUMN_CITY));
                String name = cursor.getString(nameIndex);
                int latIndex = cursor.getColumnIndex((LocationTable.COLUMN_LATITUDE));
                double latitude = cursor.getDouble(latIndex);
                int longIndex = cursor.getColumnIndex((LocationTable.COLUMN_LONGITUDE));
                double longitude = cursor.getDouble(longIndex);
                int cityCountryIndex = cursor.getColumnIndex((LocationTable.COLUMN_LOCATION_FORMATTED));
                String cityCountry = cursor.getString(cityCountryIndex);
                // Hooray, data was returned!  Assert that it's the right data, and that the database
                // creation code is working as intended.
                // Then take a break.  We both know that wasn't easy.
                assertEquals(testCityName, name);
                assertEquals(testLocationSetting, location);
                assertEquals(testLatitude, latitude);
                assertEquals(testLongitude, longitude);
                assertEquals(testCityCountry, cityCountry);
                // Fantastic.  Now that we have a location, add some weather!
            } else {
                // That's weird, it works on MY machine...
                fail("No values returned :(");
            }
            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherTable.COLUMN_LOCATION_ID, locationRowId);
            weatherValues.put(WeatherTable.COLUMN_DATE_TEXT, "20141205");
            weatherValues.put(WeatherTable.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherTable.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherTable.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherTable.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherTable.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherTable.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherTable.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherTable.COLUMN_WEATHER_ID, 321);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * TODO YOUR CODE BELOW HERE FOR QUIZ
     * QUIZ - 4a - InsertReadDbTest
     * https://www.udacity.com/course/viewer#!/c-ud853/l-1639338560/e-1633698604/m-1633698605
     **/

        /* TODO Uncomment for
        4a - JUnit testing
        https://www.udacity.com/course/viewer#!/c-ud853/l-1639338560/m-1633698603
        dbHandler.close();
    }
    */

    /* TODO Uncomment for
    4a - Simplify Tests
    https://www.udacity.com/course/viewer#!/c-ud853/l-1639338560/e-1633698607/m-1615128666
    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherTable.COLUMN_LOCATION_ID, locationRowId);
        weatherValues.put(WeatherTable.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherTable.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherTable.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherTable.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherTable.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherTable.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherTable.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherTable.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherTable.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationTable.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationTable.COLUMN_CITY, "North Pole");
        testValues.put(LocationTable.COLUMN_LATITUDE, 64.7488);
        testValues.put(LocationTable.COLUMN_LONGITUDE, -147.353);
        return testValues;
    }
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
    static final String TEST_LOCATION = "99705";
    static final String TEST_DATE = "20141205";
    */


//}
}