package com.eisaadil.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.eisaadil.sunshine.data.Tables.LocationTable;
import com.eisaadil.sunshine.data.Tables.WeatherTable;
import com.google.gson.Gson;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    WeatherListAdapter weatherListAdapter;
    LocationClass location;
    String result = "";
    String cityInput;
    WeatherClass[] weatherDataArray;

    int noOfDays = 7;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { //happens before onCreateView()
        //Utility.setDefaultPreference("location", "Riyadh", getActivity());
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.forecast_fragment, menu);


    }

    @Override
    public void onStart() {
        super.onStart();

        updateWeather(getContext());
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(WeatherClass.getUserPreferenceCityCountry(getContext()));

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(Context context){

        noOfDays = WeatherClass.getUserPreferenceNoOfDays(context);

        if (noOfDays>15) {
            Utility.setDefaultPreference(getString(R.string.pref_noOfDays_key), "15", context);
            updateWeather(context);
        }

        if (!Utility.checkIfInternet(context) && !cityInput.equals(getString(R.string.pref_location_default))){
            Utility.setDefaultPreference(getString(R.string.pref_location_key), getString(R.string.pref_location_default), context);
            Utility.setDefaultPreference(getString(R.string.pref_noOfDays_key), "7", context);
            updateWeather(context);
        }


        cityInput = WeatherClass.getUserPreferenceCity(context);

        if (cityInput.equals("")||cityInput.equals("Select Your Location"))
            return;


        //(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        Cursor c = getActivity().getContentResolver().query(LocationTable.CONTENT_URI, new String[]
                {LocationTable._ID, LocationTable.COLUMN_CITY, LocationTable.COLUMN_COUNTRY, LocationTable.COLUMN_LATITUDE, LocationTable.COLUMN_LONGITUDE, LocationTable.COLUMN_LOCATION_FORMATTED},
                LocationTable.COLUMN_CITY+" = ?", new String[]{cityInput},null);
        if(c.moveToFirst()){
            //Toast.makeText(context, "LOL I DIDNT CHECK INTERNET LOCATION", Toast.LENGTH_SHORT).show();
            Log.v("update weather","Record found");

            location = new LocationClass("");
            location.city = c.getString(c.getColumnIndex(LocationTable.COLUMN_CITY));
            location.country = c.getString(c.getColumnIndex(LocationTable.COLUMN_COUNTRY));
            location.latitude = c.getDouble(c.getColumnIndex(LocationTable.COLUMN_LATITUDE));
            location.longitude  = c.getDouble(c.getColumnIndex(LocationTable.COLUMN_LONGITUDE));
            location.locationFormatted = c.getString(c.getColumnIndex(LocationTable.COLUMN_LOCATION_FORMATTED));
            location.locationId = c.getLong(c.getColumnIndex(LocationTable._ID));



            Log.v("DB Location:",location.toString());

        }
        else{
            //Toast.makeText(context, "SHIT I CHECKED INTERNET LOCATION", Toast.LENGTH_SHORT).show();

            result = getWeatherResultRaw(cityInput, context);

            if (result.equals("FAILED")){
                Utility.setDefaultPreference(getString(R.string.pref_location_key), getString(R.string.pref_location_default), context);
                updateWeather(context);
                return;
            }

            location = new LocationClass(result);
            ContentValues values = new ContentValues();
            values.put(LocationTable.COLUMN_CITY, location.city);
            values.put(LocationTable.COLUMN_COUNTRY, location.country);
            values.put(LocationTable.COLUMN_LATITUDE, location.latitude);
            values.put(LocationTable.COLUMN_LOCATION_FORMATTED, location.locationFormatted);
            values.put(LocationTable.COLUMN_LONGITUDE, location.longitude);
            values.put(LocationTable._ID, location.locationId);



            Log.v("INTERNET Location:",location.toString());

            Cursor checkIfDuplicate = context.getContentResolver().query(LocationTable.CONTENT_URI, new String[]
                            {LocationTable._ID, LocationTable.COLUMN_CITY, LocationTable.COLUMN_COUNTRY, LocationTable.COLUMN_LATITUDE, LocationTable.COLUMN_LONGITUDE, LocationTable.COLUMN_LOCATION_FORMATTED},
                    LocationTable._ID+" = ?", new String[]{""+location.locationId},null); //Kochi and Cochin

            if (!checkIfDuplicate.moveToFirst())
                context.getContentResolver().insert(LocationTable.CONTENT_URI, values);
            checkIfDuplicate.close();
        }

        cityInput = location.city;
        Utility.setDefaultPreference(getString(R.string.pref_location_key), location.city, context);

        Utility.setDefaultPreference(getString(R.string.pref_location_formatted), location.locationFormatted, context);


        ((MainActivity)context).setActionBarTitle(location.locationFormatted);

        c.close();

        String weatherColumns[] = {
                        WeatherTable.COLUMN_LOCATION_ID,
                        WeatherTable.COLUMN_DATE_TEXT,
                        WeatherTable.COLUMN_DATE,
                        WeatherTable.COLUMN_SHORT_DESC,
                        WeatherTable.COLUMN_LONG_DESC,
                        WeatherTable.COLUMN_MIN_TEMP,
                        WeatherTable.COLUMN_MAX_TEMP,
                        WeatherTable.COLUMN_HUMIDITY,
                        WeatherTable.COLUMN_PRESSURE,
                        WeatherTable.COLUMN_WIND_SPEED,
                        WeatherTable.COLUMN_DEGREES,
                        WeatherTable.COLUMN_ICON};

        Cursor checkLastWeatherAvailability = context.getContentResolver().
                query(WeatherTable.CONTENT_URI,
                        weatherColumns,
                        WeatherTable.COLUMN_LOCATION_ID+" = ? AND "+ WeatherTable.COLUMN_DATE+" > ?",
                        new String[]{location.locationId+"", WeatherClass.getTimeAfterNDays(noOfDays)+""}, null); //greater than 6 days



        weatherDataArray = new WeatherClass[noOfDays];
        if (checkLastWeatherAvailability==null || checkLastWeatherAvailability.getCount()==0){

            //Toast.makeText(context, "SHIT I CHECKED INTERNET WEATHER", Toast.LENGTH_SHORT).show();

            //Internet Operations
            if (result.isEmpty()) result = getWeatherResultRaw(cityInput, context);
            weatherDataArray = WeatherClass.getWeatherArray(result, context).clone();

            ContentValues[] weatherValuesArray = new ContentValues[noOfDays];
            for (int i =0;i<noOfDays;i++){
                ContentValues weatherValues = new ContentValues();
                weatherValues.put(WeatherTable.COLUMN_LOCATION_ID, weatherDataArray[i].locationId);
                weatherValues.put(WeatherTable.COLUMN_DATE_TEXT, weatherDataArray[i].dateText);
                weatherValues.put(WeatherTable.COLUMN_DATE, weatherDataArray[i].date);
                weatherValues.put(WeatherTable.COLUMN_SHORT_DESC, weatherDataArray[i].shortDesc);
                weatherValues.put(WeatherTable.COLUMN_LONG_DESC, weatherDataArray[i].longDesc);
                weatherValues.put(WeatherTable.COLUMN_MIN_TEMP, weatherDataArray[i].min);
                weatherValues.put(WeatherTable.COLUMN_MAX_TEMP, weatherDataArray[i].max);
                weatherValues.put(WeatherTable.COLUMN_HUMIDITY, weatherDataArray[i].humidity);
                weatherValues.put(WeatherTable.COLUMN_PRESSURE, weatherDataArray[i].pressure);
                weatherValues.put(WeatherTable.COLUMN_WIND_SPEED, weatherDataArray[i].windSpeed);
                weatherValues.put(WeatherTable.COLUMN_DEGREES, weatherDataArray[i].degrees);
                weatherValues.put(WeatherTable.COLUMN_ICON, weatherDataArray[i].icon);
                weatherValuesArray[i] = weatherValues; //for bulk insert

                Log.v("INTERNET Weather", weatherDataArray[i].toString());

            }

            context.getContentResolver().bulkInsert(WeatherTable.CONTENT_URI, weatherValuesArray);

        }else{
            //Toast.makeText(context, "LOL I DIDNT CHECK INTERNET WEATHER", Toast.LENGTH_SHORT).show();
            //DB OPERATIONS
            Cursor getAllDaysWeatherData  = context.getContentResolver().
                    query(WeatherTable.CONTENT_URI,
                            weatherColumns,
                            WeatherTable.COLUMN_LOCATION_ID +" = ? AND "+WeatherTable.COLUMN_DATE + " > "+WeatherClass.getCurrentDay(),
                            new String[]{location.locationId+""}, WeatherTable.COLUMN_DATE+" ASC LIMIT "+noOfDays);
            if(getAllDaysWeatherData.moveToFirst()) {
                weatherDataArray = new WeatherClass[noOfDays];


                for (int i =0;i<noOfDays;i++){

                    WeatherClass weather = new WeatherClass(context);
                    weather.locationId = getAllDaysWeatherData.getLong(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_LOCATION_ID));
                    weather.dateText = getAllDaysWeatherData.getString(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_DATE_TEXT));
                    weather.date = getAllDaysWeatherData.getLong(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_DATE));
                    weather.shortDesc = getAllDaysWeatherData.getString(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_SHORT_DESC));
                    weather.longDesc = getAllDaysWeatherData.getString(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_LONG_DESC));
                    weather.min = getAllDaysWeatherData.getDouble(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_MIN_TEMP));
                    weather.max = getAllDaysWeatherData.getDouble(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_MAX_TEMP));
                    weather.humidity = getAllDaysWeatherData.getInt(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_HUMIDITY));
                    weather.pressure = getAllDaysWeatherData.getDouble(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_PRESSURE));
                    weather.windSpeed = getAllDaysWeatherData.getDouble(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_WIND_SPEED));
                    weather.degrees = getAllDaysWeatherData.getInt(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_DEGREES));
                    weather.icon = getAllDaysWeatherData.getString(getAllDaysWeatherData.getColumnIndex(WeatherTable.COLUMN_ICON));

                    weatherDataArray[i] = weather;

                    getAllDaysWeatherData.moveToNext();

                    Log.v("DB Weather", weather.toString());
                }
                //Collections.reverse(Arrays.asList(weatherDataArray));
            }

            getAllDaysWeatherData.close();
        }
        checkLastWeatherAvailability.close();



        if (WeatherClass.getUserPreferenceTemp(context).equals(context.getString(R.string.pref_units_fahrenheit)))
            weatherDataArray = WeatherClass.convertCelsiusToFahrenheit(weatherDataArray);





        for (WeatherClass w: weatherDataArray){
            try {
                Log.v("FINAL WEATHER DATA", w.toString());
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }

        }
    }

    public static String getWeatherResultRaw(String city, Context context) {
        GetWeatherTask getWeatherTask = new GetWeatherTask();
        String result;
        try {
            result = getWeatherTask.execute(city, ""+WeatherClass.getUserPreferenceNoOfDays(context)).get();

        } catch (Exception e) {
            result = "FAILED";
            e.printStackTrace();
            System.exit(0);
        }
        Log.v("RAW RESULT", result);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        updateWeather(rootView.getContext());
        RecyclerView recView = (RecyclerView) rootView.findViewById(R.id.recyclerView_forecast);
        recView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        weatherListAdapter = new WeatherListAdapter(rootView.getContext(), Arrays.asList(weatherDataArray));
        recView.setAdapter(weatherListAdapter);

        return rootView;
    }

}

class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.WeatherHolder>{

    private List<WeatherClass> weatherDataList;
    private LayoutInflater inflater;



    public WeatherListAdapter(Context context, List<WeatherClass> weatherDataList) {
        this.weatherDataList = weatherDataList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        //if today it returns 1, if another day it returns 0
        return ((position==0)?1:0);
    }


    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType==0)
            view = inflater.inflate(R.layout.list_item_forecast, parent, false);
        else
            view = inflater.inflate(R.layout.list_item_forecast_today, parent, false);

        return new WeatherHolder(view);
    }

    @Override
    public void onBindViewHolder(WeatherHolder holder, int position) {

        WeatherClass weather = weatherDataList.get(position);
        try {
            holder.date.setText(weather.dateText);
            holder.desc.setText(Utility.capitalizeEachWord(weather.longDesc));
            holder.max.setText(Utility.getFormattedDecimal(weather.max)+ (char) 0x00B0);
            holder.min.setText(Utility.getFormattedDecimal(weather.min)+ (char) 0x00B0);
            holder.icon.setImageDrawable(Utility.getImageFromFileName(weather.icon, holder.icon.getContext())); // we can get context from any object
        } catch (NullPointerException e){
            e.printStackTrace();
        }




    }

    class WeatherHolder extends RecyclerView.ViewHolder{

        private ImageView icon;
        private TextView date;
        private TextView desc;
        private TextView max;
        private TextView min;
        private View container;

        public WeatherHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.list_item_forecast_icon);
            date = (TextView) itemView.findViewById(R.id.list_item_forecast_date);
            desc = (TextView) itemView.findViewById(R.id.list_item_forecast_desc);
            max = (TextView) itemView.findViewById(R.id.list_item_forecast_max);
            min = (TextView) itemView.findViewById(R.id.list_item_forecast_min);
            container = itemView.findViewById(R.id.list_item_forecast_root);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //sending data to detailActivity to share and display
                    Intent intent = new Intent(icon.getContext(),DetailActivity.class).putExtra(Intent.EXTRA_INTENT, (new Gson()).toJson(weatherDataList.get(getAdapterPosition())));
                    icon.getContext().startActivity(intent);
                }
            });
        }
    }
}

class GetWeatherTask extends AsyncTask<String, Void, String>{

    @Override
    protected String doInBackground(String... params) {
        try{
            Log.w("Async Task", "I RAN INTERNET!!!!!");
            String city = params[0];
            int noOfDays = Integer.valueOf(params[1]);
            String appid = "b6747af8dd99081f3b2af227078218cc";

            final String CITY_PARAM = "q";
            final String DAYS_PARAM = "cnt";
            final String APP_ID_PARAM = "appid";
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

            //TO BUILD THIS URI
            //"http://api.openweathermap.org/data/2.5/forecast/daily?q=Riyadh&cnt=7&appid=b6747af8dd99081f3b2af227078218cc"


            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(CITY_PARAM, city)
                    .appendQueryParameter(DAYS_PARAM, ""+noOfDays)
                    .appendQueryParameter(APP_ID_PARAM, appid).build();

            URL url = new URL(builtUri.toString());
            Log.v("URI:",builtUri.toString());


            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            InputStream in = urlConnection.getInputStream();

            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            String result = "";
            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }
            Log.v("RESULT:",result);




            return result;

        }catch(Exception e){
            e.printStackTrace();
            return "FAILED";
        }
    }
}


class LocationClass{
    long locationId;
    double latitude;
    double longitude;
    String country;
    String city;
    String locationFormatted;

    LocationClass(String result){

        try {
            JSONObject allWeatherData = new JSONObject(result);

            city = allWeatherData.getJSONObject("city").getString("name");
            country = allWeatherData.getJSONObject("city").getString("country");

            //for example Riyadh, SA:
            locationFormatted = city + ", " + country;

            if (allWeatherData.getJSONObject("city").has("geoname_id")){
                latitude = allWeatherData.getJSONObject("city").getDouble("lat");
                longitude = allWeatherData.getJSONObject("city").getDouble("lon");

                locationId = allWeatherData.getJSONObject("city").getLong("geoname_id");
            }
            else if (allWeatherData.getJSONObject("city").has("id")){
                latitude = allWeatherData.getJSONObject("city").getJSONObject("coord").getDouble("lat");
                longitude = allWeatherData.getJSONObject("city").getJSONObject("coord").getDouble("lon");

                locationId = allWeatherData.getJSONObject("city").getLong("id");
            }



            Log.v("ALL LOCATION FETCHED",city+" "+country+" "+locationFormatted+" "+ latitude+" "+longitude+" "+locationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public String toString() {
        return getClass().getSimpleName() + "[city=" + city + "]"+ "[country=" + country + "]"+ "[locationFormatted=" + locationFormatted + "]"+ "[latitude=" + latitude + "]"+ "[longitude=" + longitude + "]"+ "[locationId=" + locationId + "]";
    }
}


class WeatherClass{

    double min;
    double max;
    long locationId;
    String shortDesc;
    String longDesc;
    long date; //epoch
    String dateText; //formatted date
    double pressure;
    double windSpeed;
    int degrees;
    int humidity;
    String icon;

    WeatherClass(Context context){

    }


    WeatherClass(String result, int dayIndex, Context context){

        try {
            JSONObject allWeatherData = new JSONObject(result);

            if (allWeatherData.getJSONObject("city").has("geoname_id")){
                locationId = allWeatherData.getJSONObject("city").getLong("geoname_id");
            }
            else if (allWeatherData.getJSONObject("city").has("id")){
                locationId = allWeatherData.getJSONObject("city").getLong("id");
            }





            JSONObject currentDay = allWeatherData.getJSONArray("list").getJSONObject(dayIndex);
            JSONObject currentDayTemp = currentDay.getJSONObject("temp");

            shortDesc = currentDay.getJSONArray("weather").getJSONObject(0).getString("main");
            longDesc = currentDay.getJSONArray("weather").getJSONObject(0).getString("description");

            max = currentDayTemp.getDouble("max")-273.15;
            min = currentDayTemp.getDouble("min")-273.15;

            max = Math.floor(max * 100) / 100;
            min = Math.floor(min * 100) / 100;

            date = currentDay.getLong("dt");

            Date date = new Date(this.date *1000);
            dateText = (new SimpleDateFormat("E, MMM d", Locale.ENGLISH)).format(date);

            pressure = currentDay.getDouble("pressure");
            windSpeed = currentDay.getDouble("speed");
            degrees = currentDay.getInt("deg");
            humidity = currentDay.getInt("humidity");

            icon = "weather_icon_"+currentDay.getJSONArray("weather").getJSONObject(0).getString("icon");

            Log.v("ALL WEATHER FETCHED: ", ""+ locationId + " "+ shortDesc+" "+longDesc+" "+max+" "+min+" "+date+" "+dateText+" "+pressure+" "+windSpeed+" "+degrees+" " +humidity+" "+icon);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public static WeatherClass[] getWeatherArray(String result, Context context){
        int noOfDays = getUserPreferenceNoOfDays(context);
        WeatherClass[] weatherArray = new WeatherClass[noOfDays];
        for (int i = 0;i<noOfDays;i++)
            weatherArray[i] = new WeatherClass(result, i, context);
        return weatherArray;
    }

    public static String[] getWeatherStringArray(String result, Context context){
        int noOfDays = getUserPreferenceNoOfDays(context);
        String[] weatherString = new String[noOfDays];
        for(int i=0;i<noOfDays;i++){
            weatherString[i] = ""+getWeatherArray(result, context)[i].dateText + " - ";
            weatherString[i] += getWeatherArray(result, context)[i].shortDesc + " - ";
            weatherString[i] += getWeatherArray(result, context)[i].max+ "/";
            weatherString[i] += getWeatherArray(result, context)[i].min;
        }
        return weatherString;
    }

    static double celsiusToFahrenheit(double x){
        x = (9.0/5.0)*x + 32;
        x = Math.round(x * 100.0) / 100.0; //rounding off to two decimal places
        return x;
    }

    static WeatherClass[] convertCelsiusToFahrenheit(WeatherClass[] weatherDataArray){
        try {
            for (WeatherClass w : weatherDataArray) {
                w.min = celsiusToFahrenheit(w.min);
                w.max = celsiusToFahrenheit(w.max);
            }
            for (WeatherClass w : weatherDataArray) {
                Log.v("INSIDE CONVERSION ARRAY", w.toString());
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        return weatherDataArray;
    }

    static String getUserPreferenceTemp(Context context){
        return Utility.getDefaultPreference(context.getString(R.string.pref_units_key), context.getString(R.string.pref_units_celsius), context);
    }

    static String getUserPreferenceCityCountry(Context context){
        return Utility.getDefaultPreference(context.getString(R.string.pref_location_formatted), context.getString(R.string.pref_location_formatted_default),context);
    }

    static String getUserPreferenceCity(Context context){
        return Utility.getDefaultPreference(context.getString(R.string.pref_location_key), context.getString(R.string.pref_location_default), context);
    }

    static int getUserPreferenceNoOfDays(Context context){
        return Integer.parseInt(Utility.getDefaultPreference(context.getString(R.string.pref_noOfDays_key), context.getString(R.string.pref_noOfDays_default), context));
    }

    static long getCurrentDay(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis()/1000-60000;
    }

    static long getTimeAfterNDays(int noOfDays){
        // 8 feb 2017 - 14 feb 2017
        //difference between times is 86400

        // last day = 1487073600(GMT)
        // LOWEST TIME FOR AFTER DAYS: 1487013600
        /*
            Dubai 1487059200
            Washington 1487016000
            New York 1487091600
            Tokyo 1487037600
            London 1487073600
            NZ 1487073600
            Riyadh 1487062800
         */

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DATE, noOfDays-1);

        return calendar.getTimeInMillis()/1000-60000;
    }

    @Override
    public String toString() {
        return "WeatherClass{" +
                "date=" + date +
                ", min=" + min +
                ", max=" + max +
                ", locationId=" + locationId +
                ", shortDesc='" + shortDesc + '\'' +
                ", longDesc='" + longDesc + '\'' +
                ", dateText='" + dateText + '\'' +
                ", pressure=" + pressure +
                ", windSpeed=" + windSpeed +
                ", degrees=" + degrees +
                ", humidity=" + humidity +
                ", icon='" + icon + '\'' +
                '}';
    }
}
