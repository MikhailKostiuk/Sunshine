package com.example.sunshine.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.example.sunshine.data.WeatherContract.WeatherEntry;
import com.example.sunshine.data.WeatherContract.LocationEntry;


public class TestProvider extends AndroidTestCase {

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                WeatherProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + WeatherContract.CONTENT_AUTHORITY,
                    providerInfo.authority, WeatherContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicWeatherQuery() {
        // insert test records into the database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = TestUtilities.insertNorthPoleLocationValues(mContext);

        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", weatherRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", weatherCursor, weatherValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicLocationQueries() {
        // insert test records into the database
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = TestUtilities.insertNorthPoleLocationValues(mContext);

        // Test the basic content provider query
        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicLocationQueries, location query", locationCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    locationCursor.getNotificationUri(), LocationEntry.CONTENT_URI);
        }
    }

    /*
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://com.example.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.sunshine/weather/
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.sunshine/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                WeatherEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.example.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.sunshine/weather/1419120000
        assertEquals("Error: the WeatherEntry CONTENT_URI with location and date should return WeatherEntry.CONTENT_ITEM_TYPE",
                WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.sunshine/location
        assertEquals("Error: the LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
                LocationEntry.CONTENT_TYPE, type);
    }
}
