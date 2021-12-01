package com.eisaadil.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.eisaadil.sunshine.data.AndroidDatabaseManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onStart() {
        super.onStart();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TextView) findViewById(R.id.toolbar_title)).setText(WeatherClass.getUserPreferenceCityCountry(getApplicationContext()));
    }

    public void setActionBarTitle(String title){
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }
        else if (id == R.id.action_open_DB){
            startActivity(new Intent(getApplicationContext(), AndroidDatabaseManager.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        String cityInput = WeatherClass.getUserPreferenceCity(getApplicationContext());

        //BUILD THIS URI: geo:0,0?q=my+street+address
        String baseGeoAddress = "geo:0,0?";
        Uri geolocation = Uri.parse(baseGeoAddress).buildUpon().appendQueryParameter("q", cityInput).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);
        startActivity(intent);

    }


}
