package com.eisaadil.sunshine;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    WeatherClass weather;

    TextView dateView;
    TextView maxView;
    TextView minView;
    TextView humidityView;
    TextView windSpeedView;
    TextView pressureView;
    TextView longDescView;
    TextView shortDescView;
    ImageView iconView;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_detail, container, false);



        String data = getActivity().getIntent().getExtras().getString(Intent.EXTRA_INTENT);
        weather = (new Gson()).fromJson(data, WeatherClass.class);

        view = constructAllViews(view);

        dateView.setText(weather.dateText);
        maxView.setText(Utility.getFormattedDecimal(weather.max)+ (char) 0x00B0);
        minView.setText(Utility.getFormattedDecimal(weather.min)+ (char) 0x00B0);
        pressureView.setText(String.valueOf(weather.pressure) + " hPa");
        humidityView.setText(String.valueOf(weather.humidity) + "%");
        windSpeedView.setText(String.valueOf(weather.windSpeed) + " km/h SW");
        longDescView.setText(Utility.capitalizeEachWord(weather.longDesc));
        shortDescView.setText(weather.shortDesc);
        iconView.setImageDrawable(Utility.getImageFromFileName(weather.icon, getActivity()));

        return view;
    }

    private View constructAllViews(View view){
        dateView = (TextView) view.findViewById(R.id.detail_date);
        maxView = (TextView) view.findViewById(R.id.detail_max);
        minView = (TextView) view.findViewById(R.id.detail_min);
        humidityView = (TextView) view.findViewById(R.id.detail_humidity);
        windSpeedView = (TextView) view.findViewById(R.id.detail_wind_speed);
        pressureView = (TextView) view.findViewById(R.id.detail_pressure);
        longDescView = (TextView) view.findViewById(R.id.detail_long_desc);
        shortDescView = (TextView) view.findViewById(R.id.detail_short_desc);
        //shortDescView.setPaintFlags(shortDescView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        iconView = (ImageView) view.findViewById(R.id.detail_icon);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detail_fragment, menu);


        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider!=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else{
            Log.d("DetailActivityFragment", "Can't share");
        }

    }

    private Intent createShareForecastIntent(){
        String locationFormatted = WeatherClass.getUserPreferenceCityCountry(getActivity());
        String temperatureUnit = WeatherClass.getUserPreferenceTemp(getActivity());
        String textToShare = "Hi friend! The weather in "+locationFormatted+" on "+weather.dateText+" is "
                +weather.max+"/"+weather.min +" "+temperatureUnit+". ";
        textToShare += "There will be "+weather.longDesc+" with a Humidity of "+
                weather.humidity+", Wind Speed of "+weather.windSpeed+" and Pressure of "+weather.pressure+". ";
        textToShare += "These details are bought to you by Eisa's Sunshine Weather App. Enjoy!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        else{
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);

        return shareIntent;

    }
}
