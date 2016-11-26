package com.example.sunshine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.forecast_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new ForecastFragment to be placed in the activity layout
            ForecastFragment forecastFragment = new ForecastFragment();

            // Add the fragment to the 'forecast_fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.forecast_fragment_container, forecastFragment).commit();
        }
    }

}
