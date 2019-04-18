/*
 * Copyright (C) 2016 Player One
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package player.efis.pfd;

import player.efis.common.DemGTOPO30;
import player.efis.common.AircraftData;
import player.efis.common.EFISMainActivity;
import player.efis.common.Gpx;
import player.efis.common.StratuxWiFiTask;
import player.efis.common.SensorComplementaryFilter;
import player.efis.common.prefs_t;
import player.ulib.UMath;
import player.ulib.UNavigation;
import player.ulib.UTrig;
import player.ulib.Unit;
import player.efis.common.orientation_t;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;

// sensor imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;



public class PFDMainActivity extends EFISMainActivity implements Listener, SensorEventListener, LocationListener
{
    public static final String PREFS_NAME = R.string.app_name + ".prefs";
    private PFDSurfaceView mGLView;

    // sensor members
    private SensorManager mSensorManager;

    // Location abstracts

    //
    //  Add the action bar buttons
    //
    //Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed()
    {
        if (mGLView.mRenderer.fatFingerActive == true) {
            mGLView.mRenderer.fatFingerActive = false;
            mGLView.mRenderer.setSpinnerParams();
        }
        else openOptionsMenu();
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.settings:
                // Launch settings activity
                Intent i = new Intent(this, PFDPrefSettings.class);
                startActivity(i);
                break;
            case R.id.manage:
                // Launch manage activity
                Intent j = new Intent(this, PFDPrefManage.class);
                startActivity(j);
                break;
            case R.id.quit:
                // Quit the app
                if (bLockedMode == false) {
                    finish();
                    //System.exit(0); // This is brutal, it does not exit gracefully
                }
                else Toast.makeText(this, "Locked Mode: Active", Toast.LENGTH_SHORT).show();
                break;
            // more code...
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // This code will catch the actual keypress.
    // for now we will leave the menu bar in case it is needed later
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new PFDSurfaceView(this);
        setContentView(mGLView);

        // Get the version number of the app
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        Toast.makeText(this, "Kwik EFIS version: " + version, Toast.LENGTH_LONG).show();

        try {
            mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
            registerSensorManagerListeners();
        }
        catch (Exception e) {
            Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
        }

        // testing for lightweight -- may or may not use
        sensorComplementaryFilter = new SensorComplementaryFilter();

        // Location
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
        // Criteria criteria = new Criteria();
        // provider = locationManager.getBestProvider(criteria, false);
        provider = LocationManager.GPS_PROVIDER;  // Always use the GPS as the provide
        locationManager.requestLocationUpdates(provider, GPS_UPDATE_PERIOD, GPS_UPDATE_DISTANCE, this);  // 400ms or 1m
        locationManager.addGpsStatusListener(this);
        //locationManager.addNmeaListener(this);
        Location location = locationManager.getLastKnownLocation(provider);
        // end location

        // Preferences
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        PreferenceManager.setDefaultValues(this, R.xml.manage, false);

        // Set the window to be full brightness
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = -1f;  // 1f = full bright 0 = selected
        getWindow().setAttributes(layout);

        // Restore persistent preferences
        restorePersistentSettings();

        // This should never happen but we catch and force it to something known it just in case
        if (mGLView.mRenderer.mWptSelName.length() != 4) mGLView.mRenderer.mWptSelName = "ZZZZ";
        if (mGLView.mRenderer.mAltSelName.length() != 5) mGLView.mRenderer.mWptSelName = "00000";

        // Instantiate a new apts gpx/xml
        mGpx = new Gpx(this);
        mGpx.loadDatabase(gps_lat, gps_lon);
        mDemGTOPO30 = new DemGTOPO30(this);
        //mDemGTOPO30.loadDemBuffer(gps_lat, gps_lon);

        // Wifi
        connectWiFi("stratux");
        mStratux = new StratuxWiFiTask("kwik");
        mStratux.execute();

        createMediaPlayer();
        mGLView.setTheme(colorTheme);

        // Overall the device is now ready.
        // The individual elements will be enabled or disabled by the location provided
        // based on availability
        mGLView.setServiceableDevice();
        //updateEFIS();
    }


    @Override
    protected void onStop()
    {
        savePersistentSettings();
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        //
        // Read and Set the user preferences
        //
        setUserPrefs();

        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        if (mStratux != null) {
            mStratux.finish();
            mStratux.cancel(true);
            mStratux = null;
        }

        releaseMediaPlayer();
        mGLView.onPause();

        locationManager.removeUpdates(this);
		unregisterSensorManagerListeners();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //
        // Read and Set the user preferences
        //
        setUserPrefs();

        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        createMediaPlayer();
        mGLView.onResume();

        if (bStratuxActive) {
            if (mStratux == null) {
                mStratux = new StratuxWiFiTask("pfd");
                mStratux.execute();
            }

        }
        else if (!bSimulatorActive) {
            gps_insky = 0;
            gps_infix = 0;
            locationManager.requestLocationUpdates(provider, GPS_UPDATE_PERIOD, GPS_UPDATE_DISTANCE, this);  // 400ms or 1m
        }
        registerSensorManagerListeners();
    }

    //
    // Sensor methods
    //
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch (sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // SENSOR_STATUS_ACCURACY_HIGH
                // SENSOR_STATUS_ACCURACY_MEDIUM
                // SENSOR_STATUS_ACCURACY_LOW
                // SENSOR_STATUS_UNRELIABLE
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorComplementaryFilter.setAccel(event.values);
                break;

            case Sensor.TYPE_GYROSCOPE:
                sensorComplementaryFilter.setGyro(event.values);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //b2b2 sensorFusion.setMagnet(event.values);
                break;

            case Sensor.TYPE_ORIENTATION:
                orientationAzimuth = event.values[0];
                orientationPitch = -event.values[2];
                orientationRoll = -event.values[1];
                break;

            case Sensor.TYPE_PRESSURE:
                // altitude = mSensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);
                break;
        }
        //updateEFIS();
    }


    @Override
    public void onLocationChanged(Location location)
    {
        if (bSimulatorActive) return;
        if (bStratuxActive) return;

        gps_lat = (float) location.getLatitude();
        gps_lon = (float) location.getLongitude();
        gps_agl = DemGTOPO30.calculateAgl(gps_lat, gps_lon, gps_altitude);

        if (location.hasSpeed()) {
            gps_speed = location.getSpeed();
            if (gps_speed == 0) gps_speed = 0.01f;  // nip div zero issues in the bud
            mGLView.setServiceableAsi();
            hasSpeed = true;
        }
        else {
            mGLView.setUnServiceableAsi();
            hasSpeed = false;
        }

        if (location.hasAltitude()) {
            gps_altitude = (float) location.getAltitude();
            gps_rateOfClimb = calculateRateOfClimb(gps_altitude);
            mGLView.setServiceableAlt();
        }
        else {
            mGLView.setUnServiceableAlt();
        }

        if (location.hasBearing()) {
            gps_course = (float) Math.toRadians(location.getBearing());
            gps_rateOfTurn = calculateRateOfTurn(gps_course);
            mGLView.setServiceableDi();
        }
        else {
            gps_rateOfTurn = 0;
            mGLView.setUnServiceableDi();
        }

        if (location.hasSpeed() && location.hasBearing()) {
            mGLView.setServiceableAh();
        }
        else {
            mGLView.setUnServiceableAh();
        }
        //updateEFIS();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        if (!LocationManager.GPS_PROVIDER.equals(provider)) {
            return;
        }
        // do something
    }

    //
    // SENSOR_DELAY_GAME    (20,000 microsecond delay),
    // SENSOR_DELAY_UI      (60,000 microsecond delay), or
    // SENSOR_DELAY_FASTEST (0 microsecond delay)
    //
    public void registerSensorManagerListeners()
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
    }

    public void unregisterSensorManagerListeners()
    {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)); //SENSOR_DELAY_FASTEST);
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));     //SENSOR_DELAY_FASTEST);
    }

    // Release the media player
    private void releaseMediaPlayer()
    {
        mpCautionTerrian.stop();
        mpCautionTerrian.release();

        mpFiveHundred.stop();
        mpFiveHundred.release();

        mpSinkRate.stop();
        mpSinkRate.release();

        mpStall.stop();
        mpStall.release();
    }

    // Create the media player
    private void createMediaPlayer()
    {
        mpCautionTerrian = MediaPlayer.create(this, R.raw.caution_terrain);
        mpCautionTerrian.setLooping(false);

        mpFiveHundred = MediaPlayer.create(this, R.raw.five_hundred);
        mpFiveHundred.setLooping(false);

        mpSinkRate = MediaPlayer.create(this, R.raw.sink_rate);
        mpSinkRate.setLooping(false);

        mpStall = MediaPlayer.create(this, R.raw.stall);
        mpStall.setLooping(false);
    }


    private void savePersistentSettings()
    {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        // Save persistent preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("WptSelName", mGLView.mRenderer.mWptSelName);
        editor.putString("WptSelComment", mGLView.mRenderer.mWptSelComment);
        editor.putFloat("WptSelLat", mGLView.mRenderer.mWptSelLat);
        editor.putFloat("WptSelLon", mGLView.mRenderer.mWptSelLon);
        editor.putFloat("mAltSelValue", mGLView.mRenderer.mAltSelValue);
        editor.putString("mAltSelName", mGLView.mRenderer.mAltSelName);
        editor.putFloat("mObsValue", mGLView.mRenderer.mObsValue);
        editor.putFloat("GpsLat", gps_lat);
        editor.putFloat("GpsLon", gps_lon);
        editor.putFloat("mMapZoom", mGLView.mRenderer.mMapZoom);
        editor.putInt("colorTheme", colorTheme);

        // Commit the edits
        editor.commit();
    }

    private void restorePersistentSettings()
    {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        // Restore persistent preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mGLView.mRenderer.mWptSelName = settings.getString("WptSelName", "ZZZZ");
        mGLView.mRenderer.mWptSelComment = settings.getString("WptSelComment", "Null Island");
        mGLView.mRenderer.mWptSelLat = settings.getFloat("WptSelLat", 00f);
        mGLView.mRenderer.mWptSelLon = settings.getFloat("WptSelLon", 00f);
        mGLView.mRenderer.mAltSelValue = settings.getFloat("mAltSelValue", 0f);
        mGLView.mRenderer.mAltSelName = settings.getString("mAltSelName", "00000");
        mGLView.mRenderer.mObsValue = settings.getFloat("mObsValue", 0f);
        colorTheme = settings.getInt("colorTheme", 0);
        mGLView.mRenderer.mMapZoom = settings.getFloat("mMapZoom", 20);

        // Restore last known location
        gps_lat = settings.getFloat("GpsLat", gps_lat);
        gps_lon = settings.getFloat("GpsLon", gps_lon);

        // Use the last orientation to start
        bLandscapeMode = settings.getBoolean("landscapeMode", false);
    }



    // This must be implemented otherwise the older
    // systems does not get seem to get updates.
    @Override
    public void onGpsStatusChanged(int state)
    {
        setGpsStatus();
    }


    @Override
    public void onProviderEnabled(String provider)
    {
        Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }
    // end location abs ------------------------


    private void setUserPrefs()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mGLView.setPrefs(prefs_t.DEM, settings.getBoolean("displayDEM", false));
        mGLView.setPrefs(prefs_t.TAPE, settings.getBoolean("displayTape", true));
        mGLView.setPrefs(prefs_t.MIRROR, settings.getBoolean("displayMirror", false));
        mGLView.setPrefs(prefs_t.INFO_PAGE, settings.getBoolean("infoPage", true));
        mGLView.setPrefs(prefs_t.REMOTE_INDICATOR, settings.getBoolean("displayRMI", true));
        mGLView.setPrefs(prefs_t.FLIGHT_DIRECTOR, settings.getBoolean("displayFlightDirector", false));
        // Only used in PFD
        mGLView.setPrefs(prefs_t.AH_COLOR, settings.getBoolean("displayAHColor", true));
        mGLView.setPrefs(prefs_t.HITS, settings.getBoolean("displayHITS", false));
        // Only used in MFD
        // mGLView.setPrefs(prefs_t.AIRSPACE, settings.getBoolean("displayAirspace", true));
        // AirspaceClass.A = settings.getBoolean("classA", true);
        // AirspaceClass.B = settings.getBoolean("classB", true);
        // AirspaceClass.C = settings.getBoolean("classC", true);
        // AirspaceClass.D = settings.getBoolean("classD", true);
        // AirspaceClass.E = settings.getBoolean("classE", true);
        // AirspaceClass.F = settings.getBoolean("classF", true);
        // AirspaceClass.G = settings.getBoolean("classG", true);
        // AirspaceClass.P = settings.getBoolean("classP", true);
        // AirspaceClass.R= settings.getBoolean("classR", true);
        // AirspaceClass.Q = settings.getBoolean("classQ", true);
        // AirspaceClass.CTR = settings.getBoolean("classCTR", true);

        bLockedMode = settings.getBoolean("lockedMode", false);
        sensorBias = Float.valueOf(settings.getString("sensorBias", "0.15f"));

        // If we changed to Demo mode, use the current GPS as seed location
        if (bSimulatorActive != settings.getBoolean("simulatorActive", false)) {
            if (gps_lon != 0 && gps_lat != 0) {
                _gps_lon = gps_lon;
                _gps_lat = gps_lat;
            }
        }

        bSimulatorActive = settings.getBoolean("simulatorActive", false);
        bStratuxActive = settings.getBoolean("stratuxActive", false);
        bHudMode = settings.getBoolean("displayMirror", false);

        // If the aircraft is changed, update the paramaters
        String s = settings.getString("AircraftModel", "RV8");
        AircraftData.setAircraftData(s); //mGLView.mRenderer.setAircraftData(s);  // refactored  to static model

        // landscape / portrait mode toggle
        bLandscapeMode = settings.getBoolean("landscapeMode", false);
        if (bLandscapeMode) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mGLView.mRenderer.Layout = PFDRenderer.layout_t.LANDSCAPE;
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mGLView.mRenderer.Layout = PFDRenderer.layout_t.PORTRAIT;
        }
        bLandscapeMode = settings.getBoolean("landscapeMode", false);

        // If we changed display schemes, a color gamma rec-calc is required
        int _colorTheme = Integer.valueOf(settings.getString("colorTheme", "0"));
        if (colorTheme != _colorTheme) {
            colorTheme = _colorTheme;
            savePersistentSettings();
            mGLView = new PFDSurfaceView(this);
            setContentView(mGLView);
            mGLView.setTheme(colorTheme);
            mGLView.invalidate();

            restorePersistentSettings();
            mGLView.setServiceableDevice();
            setUserPrefs();  // make a recursive call
        }
    }

    //
    // Flight Path Vector (the birdie)
    //
    protected void updateFPV()
    {
        float deltaA, fpvX = 0, fpvY = 0;

        //deltaA = UNavigation.compassRose180(gps_course - orientationAzimuth);
        fpvX = (float) filterfpvX.runningAverage(Math.toDegrees(Math.atan2(-gyro_rateOfTurn * 100.0f, gps_speed))); // a point 100m ahead of nose
        fpvY = (float) filterfpvY.runningAverage(Math.toDegrees(Math.atan2(gps_rateOfClimb * 1.0f, gps_speed)));    // simple RA of the two velocities

        mGLView.setFPV(fpvX, fpvY); // need to clean this up
    }

    //
    // Force a blank screen and no birdie
    //
    protected void forceBlankScreen()
    {
        rollValue = 0;
        pitchValue = -270;
        mGLView.setFPV(180, 180);
    }

    //
    // Stratux handler
    //
    protected int handleStratux()
    {
        int rv = super.handleStratux();

        if (rv == STRATUX_OK) {
            mGLView.setBannerMsg(false, " ");
            mGLView.setServiceableDevice();
            mGLView.setServiceableDi();
            mGLView.setServiceableAsi();
            mGLView.setServiceableAlt();
            mGLView.setServiceableAh();
            mGLView.setDisplayAirport(true);
            if (hasSpeed) {
                updateFPV();
            }
            mGLView.setBannerMsg(false, " ");
        }
        else if (rv == STRATUX_SERVICE) {
            // no loop running, no hope of restart -- it is hopeless
            mGLView.setUnServiceableDevice();
            mGLView.setBannerMsg(true, "STRATUX SERVICE");
        }
        else if (rv == STRATUX_DEVICE) {
            // no pulse
            mGLView.setUnServiceableDevice();
            mGLView.setBannerMsg(true, "STRATUX DEVICE");
        }
        else if (rv == STRATUX_WIFI) {
            // No Wifi
            mGLView.setUnServiceableDevice();
            mGLView.setBannerMsg(true, "STRATUX WIFI");
        }
        else if (rv == STRATUX_GPS) {
            // No GPS, but we may still have attitude
            // since all the fatal checks are prior
            mGLView.setServiceableDevice();
            mGLView.setServiceableAh();

            mGLView.setUnServiceableDi();   // also does the rose
            mGLView.setUnServiceableAsi();
            mGLView.setUnServiceableAlt();
            mGLView.setDisplayAirport(false);
            mGLView.setBannerMsg(true, " "); // "STRATUX GPS"
        }

        return rv;
    }
    
    //
    // Android  handler
    //
    protected boolean handleAndroid()
    {
        float[] gyro = new float[3]; // gyroscope vector, rad/sec
        float[] accel = new float[3]; // accelerometer vector, m/s^2

        //
        // Read the Sensors
        //
        if (bLandscapeMode) {
            if (bHudMode)
                sensorComplementaryFilter.setOrientation(orientation_t.HORIZONTAL_LANDSCAPE);
            else sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_LANDSCAPE);
        }
        else {
            if (bHudMode)
                sensorComplementaryFilter.setOrientation(orientation_t.HORIZONTAL_PORTRAIT);
            else sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_PORTRAIT);
        }

        sensorComplementaryFilter.getGyro(gyro);      // Use the gyroscopes for the attitude
        sensorComplementaryFilter.getAccel(accel);    // Use the accelerometer for G and slip

        if (bLandscapeMode) {
            gyro_rateOfTurn = (float) filterRateOfTurnGyro.runningAverage(-gyro[0]);
        }
        else {
            gyro_rateOfTurn = (float) filterRateOfTurnGyro.runningAverage(-gyro[1]);
        }
        slipValue = (float) -Math.toDegrees(Math.atan2(accel[0], accel[1]));

        loadfactor = sensorComplementaryFilter.getLoadFactor();
        loadfactor = filterG.runningAverage(loadfactor);

        //
        // Check if we have a valid GPS
        //
        hasGps = isGPSAvailable();

        //
        // Calculate the augmented bank angle and also the flight path vector
        //
        float deltaA, fpvX = 0, fpvY = 0;
        if (hasGps) {
            mGLView.setServiceableDevice();
            if (gps_speed > 5) {
                // Testing shows that reasonable value is sensorBias of 75% gps and 25% gyro on most older devices,
                // if the gyro and accelerometer are good quality and stable, use sensorBias of 100%
                rollValue = sensorComplementaryFilter.calculateBankAngle((sensorBias) * gyro_rateOfTurn + (1 - sensorBias) * gps_rateOfTurn, gps_speed);
                pitchValue = sensorComplementaryFilter.calculatePitchAngle(gps_rateOfClimb, gps_speed);

                // the Flight Path Vector (FPV)
                updateFPV();
                mGLView.setDisplayAirport(true);
            }
            if (gps_speed < 9) {  // m/s
                // taxi mode
                rollValue = 0;
                pitchValue = 0;
            }
        }
        else {
            // No GPS no speed ... no idea what the AH is
            //forceBlankScreen();
            mGLView.setUnServiceableDevice();
        }
		
        // for debug - set to true
        if (false) {
            hasGps = true;          //debug
            hasSpeed = true;        //debug
            gps_speed = 3;//60;     //m/s debug
            gps_rateOfClimb = 1.0f; //m/s debug
        }
        // end debug
        return true;
    }


    protected void updateDEM()
    {
        //
        // Handle the DEM buffer.
        // Load new data to the buffer when the horizon gets close to the edge or
        // if we have gone off the current tile.
        //
        float dem_dme = UNavigation.calcDme(mDemGTOPO30.lat0, mDemGTOPO30.lon0, gps_lat, gps_lon);

        //
        // Load new data into the buffer when the horizon gets close to the edge
        //
        // Check every 1 minute
        // 40 hz * 60 sec = 2400
        //if (ctr % 2400 == 0) {
        //if (ctr % 600 == 0) {
        {
            // See if we are close to the edge or
            // see if we are stuck on null island or even on the tile
            if ((dem_dme + DemGTOPO30.DEM_HORIZON > DemGTOPO30.BUFX / 4) ||
                    ((dem_dme != 0) && (mDemGTOPO30.isOnTile(gps_lat, gps_lon) == false))) {

                mGLView.setBannerMsg(true, "LOADING TERRAIN");
                mDemGTOPO30.loadDemBuffer(gps_lat, gps_lon);
                mGpx.loadDatabase(gps_lat, gps_lon);
                mGLView.setBannerMsg(false, " ");
            }
        }
    }


    //-------------------------------------------------------------------------
    // Effectively the main execution loop. updateEFIS will get called when
    // something changes, eg a sensor has new data or new gps fix becomes available.
    //
    protected void updateEFIS()
    {
        ctr++;

        //
        // Mode handlers
        //
        if (bSimulatorActive) {
            // Simulator handler
            mGLView.setSimulatorActive(true, "SIMULATOR");
            Simulate();
            // Set the GPS flag to true and
            // make all the instruments serviceable
            mGLView.setServiceableDevice();
            mGLView.setServiceableDi();
            mGLView.setServiceableAsi();
            mGLView.setServiceableAlt();
            mGLView.setServiceableAh();
            mGLView.setDisplayAirport(true);
            hasGps = true;
            hasSpeed = true;
        }
        else {
            // Clear the simulator spash
            mGLView.setSimulatorActive(false, " ");

            // Handle Stratux or Android sensors
            if (bStratuxActive) {
                // We are set to SENSOR_DELAY_UI approx 60ms
                // 5 x 60 will give 3 updates a second
                // if (ctr % 5 == 0)
                  handleStratux();
            }
            else {
                // Clear any banners that may be set
                mGLView.setBannerMsg(false, " ");
                handleAndroid();

                // Apply a little filtering to the pitch, bank (only for Android, not Stratux)
                pitchValue = filterPitch.runningAverage(pitchValue);
                rollValue = filterRoll.runningAverage(UNavigation.compassRose180(rollValue));
            }
        }

        float courseValue = filterGpsCourse.runningAverage(gps_course
                + (float) UTrig.M_2PI)
                % (float) UTrig.M_2PI;

        //
        // Get the battery percentage
        //
        float batteryPct = getRemainingBattery();



        // for debug - set to true
        if (false) {
            hasGps = true;          //debug
            hasSpeed = true;        //debug
            gps_speed = 3;//60;     //m/s debug
            gps_rateOfClimb = 1.0f; //m/s debug
            gps_course = (float) Math.toRadians(1); // debug
        }
        // end debug

        //
        // Pass the values to mGLView for updating
        //
        String s; // general purpose string

        mGLView.setPitch(pitchValue);                             // in degrees
        mGLView.setRoll(rollValue);                               // in degrees
        mGLView.setGForce(loadfactor);                            // in gunits
        mGLView.setSlip(slipValue);                               // in degrees
        mGLView.setVSI((int) Unit.MeterPerSecond.toFeetPerMinute(gps_rateOfClimb));  // in fpm
        mGLView.setTurn((sensorBias) * gyro_rateOfTurn + (1 - sensorBias) * gps_rateOfTurn);
        //mGLView.setHeading((float) Math.toDegrees(gps_course));  // in degrees
        mGLView.setHeading((float) Math.toDegrees(courseValue));  // in degrees
        mGLView.setALT((int) Unit.Meter.toFeet(gps_altitude));    // in Feet
        mGLView.setAGL((int) Unit.Meter.toFeet(gps_agl));         // in Feet
        mGLView.setASI(Unit.MeterPerSecond.toKnots(gps_speed));   // in knots
        mGLView.setLatLon(gps_lat, gps_lon);
        mGLView.setBatteryPct(batteryPct);                       // in percentage

        s = String.format("GPS %d / %d", gps_infix, gps_insky);
        mGLView.setGpsStatus(s);
        if (mStratux != null) mGLView.setTargets(mStratux); // traffic list

        if (bStratuxActive) mGLView.setActiveDevice("Stratux");
        else mGLView.setActiveDevice("Android");

        //
        // Audio cautions and messages
        //
        if (hasGps) {
            try {
                // We have new traffic
                // TODO: 2018-08-31 Implement a suitable detection and reporting strategy
                if (mStratux != null) {
                    if (mStratux.proximityAlert) {
                        if (!mpCautionTraffic.isPlaying()) mpCautionTraffic.start();
                        mStratux.proximityAlert = false;
                    }
                }

                // We are stalling, advise captain "Crash" of his imminent flight emergency
                if (hasSpeed
                        && (gps_speed < 3 + AircraftData.Vs0 / 2) // m/s, warn 3 m/s before stall
                        && (gps_agl > 0)) {
                    if (!mpStall.isPlaying()) mpStall.start();
                }

                // Sigh ... Now, we are plummeting to the ground, inform the prick on the stick of that
                if (gps_rateOfClimb < -10) { // m ~ 2000 fpm //gps_rateOfClimb * 196.8504f for fpm
                    if (!mpSinkRate.isPlaying()) mpSinkRate.start();
                }

                // We are at risk of becoming a wet spot somewhere on terra firma
                if (DemGTOPO30.demDataValid) {
                    // Play the "caution terrain" song above Vx
                    if ((gps_speed > AircraftData.Vx / 2)  // m/s
                            && (gps_agl > 0)
                            && (gps_agl < 100)) { // meters
                        if (!mpCautionTerrian.isPlaying()) mpCautionTerrian.start();
                    } // caution terrain

                    // Play the "five hundred" song when decending through 500ft
                    if ((_gps_agl > 152.4f)
                            && (gps_agl <= 152.4f)) { // 500ft
                        if (!mpFiveHundred.isPlaying()) mpFiveHundred.start();
                    }
				} // DemGTOPO30 required options
				
            }
            catch (IllegalStateException e) {
                //e.printStackTrace();
            }
        }
        _gps_agl = gps_agl; // save the previous height agl
    }

    protected void Simulate()
    {
        pitchValue = -sensorComplementaryFilter.getPitch();
        rollValue = -sensorComplementaryFilter.getRoll();

        pitchValue = 0.05f * (float) Math.random() + 0.75f * UMath.clamp(mGLView.mRenderer.commandPitch, -3, 3);
        rollValue = 0.05f * (float) Math.random() + 0.75f * mGLView.mRenderer.commandRoll;

        super.Simulate();
    }


}


