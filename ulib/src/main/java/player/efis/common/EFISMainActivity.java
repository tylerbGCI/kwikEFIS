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

package player.efis.common;

import player.efis.common.DemGTOPO30;
import player.efis.common.AircraftData;
import player.efis.common.Gpx;
import player.efis.common.SensorComplementaryFilter;
import player.efis.common.prefs_t;
import player.ulib.DigitalFilter;
import player.ulib.UNavigation;
import player.ulib.UTrig;
import player.ulib.Unit;
import player.efis.common.orientation_t;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle; 

// sensor imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater; 
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import java.util.Random;


public class EFISMainActivity extends Activity //implements Listener, SensorEventListener, LocationListener
{
    protected SensorComplementaryFilter sensorComplementaryFilter;
    // location members
    protected LocationManager locationManager;
    protected String provider;
    protected GpsStatus mGpsStatus = null;
    protected boolean bDemoMode = false;
    protected boolean bLockedMode = false;
    protected boolean bHudMode = false;
    protected boolean bLandscapeMode = false;
    protected boolean bColorThemeLight;
    protected static final int SLIP_SENS = 25; //50;	// Arbitrary choice
    private static final float STD_RATE = 0.0524f;	// = rate 1 = 3deg/s
    protected static final long GPS_UPDATE_PERIOD = 0;   //ms // 400
    protected static final long GPS_UPDATE_DISTANCE = 0; //ms // 1
    protected int calibrationCount = 0;

    // Location abstracts
    protected float gps_lat;            // in decimal degrees
    protected float gps_lon;            // in decimal degrees
    protected float gps_altitude;       // in m
    protected float gps_agl;            // in m
    protected float gps_speed;          // in m/s
    protected float gps_course;         // in radians
    protected float gps_rateOfClimb;    // in m/s
    protected float gps_rateOfTurn;     // in rad/s
    protected boolean hasSpeed;
    protected boolean hasGps;
    protected float orientationAzimuth;
    protected float orientationPitch;
    protected float orientationRoll;
    protected int gps_insky;
    protected int gps_infix;
    protected float sensorBias;           // gyroscope / GPS bias
    protected Gpx mGpx;                   // wpt database
    protected DemGTOPO30 mDemGTOPO30;     // dem database


    // Digital filters
    protected DigitalFilter filterRateOfTurnGyro = new DigitalFilter(16); //8
    protected DigitalFilter filterSlip = new DigitalFilter(32);           //32
    protected DigitalFilter filterRoll = new DigitalFilter(8);            //16
    protected DigitalFilter filterPitch = new DigitalFilter(8);           //16
    protected DigitalFilter filterRateOfClimb = new DigitalFilter(4);     //8
    //not used? DigitalFilter filterRateOfTurn = new DigitalFilter(4); //8
    protected DigitalFilter filterfpvX = new DigitalFilter(256);          //128
    protected DigitalFilter filterfpvY = new DigitalFilter(256);          //128
    protected DigitalFilter filterG = new DigitalFilter(32);              //32
    protected DigitalFilter filterGpsSpeed = new DigitalFilter(6);        //4
    protected DigitalFilter filterGpsAltitude = new DigitalFilter(6);     //4
    protected DigitalFilter filterGpsCourse = new DigitalFilter(6);       //4

    /*private MediaPlayer mpCautionTerrian;
    private MediaPlayer mpFiveHundred;
    private MediaPlayer mpSinkRate;
    private MediaPlayer mpStall;*/



    //-------------------------------------------------------------------------
    // Utility function to determine the direction of the turn and try to eliminate
    // the jitter around zero a little bit
    // Determine the direction of the turn based on the rotation and try to eliminate the jitter around zero a little bit
    // -1 for left turn
    // +1 for right turn
    //  0 for no turn
    static int rs = 0;  // variable to keep the running count
    private int getTurnDirection(float rotValue)
    {
        if (Math.signum(rotValue) > 0)	rs++;
        else rs--;

        final int JITTER_COUNT = 10;
        int turnDirection;
        if (rs > JITTER_COUNT) {
            rs = JITTER_COUNT;
            return 1;
        }
        else if (rs < -JITTER_COUNT) {
            rs = -JITTER_COUNT;
            return -1;
        }
        else return 0;
    }


    //-------------------------------------------------------------------------
    // Utility function to calculate rate of climb
    // Rate of climb in m/s
    protected static Time  time = new Time(); 	// Time class
    private static long   time_a, _time_a;
    private static float _altitude;   	// previous altitude
    protected float calculateRateOfClimb(float altitude)
    {
        float rateOfClimb = 0;
        long deltaT;
        float deltaAlt = altitude - _altitude;

        time.setToNow();
        time_a = time.toMillis(true);
        deltaT = time_a - _time_a;
        if (deltaT > 0) {
            rateOfClimb = 1000* filterRateOfClimb.runningAverage((float) deltaAlt /(float) deltaT); // m/s
            _time_a = time_a;
            _altitude = altitude; // save the previous altitude
        }
        return rateOfClimb;
    }


    //-------------------------------------------------------------------------
    // Utility function to calculate rate of turn
    // Rate of turn in rad/s
    private static float _course;   	// previous course
    private static long  time_c, _time_c;
    private static float _rateOfTurn;
    protected float calculateRateOfTurn(float course)
    {
        float rateOfTurn = 0;
        long deltaT;
        float deltaCrs = course - _course;

        // Handle the case around 0
        if (Math.abs(deltaCrs) > UTrig.M_PI_4) {
            _course = course;   // save the previous course
            return _rateOfTurn; // result would be rubbish, just return the previous rot
        }

        time.setToNow();
        time_c = time.toMillis(true);
        deltaT = time_c - _time_c;
        if (deltaT > 0) {
            rateOfTurn = 1000 * (float)(deltaCrs) / deltaT; // rad/s
            _time_c = time_c; // save the previous time
        }
        else {
            rateOfTurn = _rateOfTurn;
        }

        _course = course;         // save the previous course
        _rateOfTurn = rateOfTurn; // save the previous rate of turn
        return rateOfTurn;
    }


    //-------------------------------------------------------------------------
    // Utility function to calculate the remaining
    // battery in percentage.
    protected float getRemainingBattery()
    {
        // Get the battery percentage
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        return batteryPct;
    }


    //-------------------------------------------------------------------------
    // Utility function to Check GPS status
    //
    // We assume no GPS is available if there has not been a valid
    // altitude update in 10 seconds
    //
    protected boolean isGPSAvailable()
    {
        if (gps_infix > 3) return true;
        else return false;
    }

    //-------------------------------------------------------------------------
    // Utility function to Restart the App
    //
    // It is pretty brutal should save the persistent data first via onStop
    //
    private void restartEFISApp()
    {
        onStop();

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }



    //-------------------------------------------------------------------------
    // Utility function to do a simple simulation for demo mode
    // It acts like a crude flight simulator
    //
    protected float _gps_lat = 00.00f; protected float _gps_lon = 00.00f;   //null island
    float _gps_course = 0.96f; //1.74f;  //in radians
    float _gps_altitude = 3000; // meters
    protected float _gps_agl = 0; //meters

    float _gps_speed = 0;       // m/s
    long _sim_ms = 0, sim_ms;
    Random rand = new Random();

    protected void Simulate()
    {
        pitchValue = -sensorComplementaryFilter.getPitch();
        rollValue = -sensorComplementaryFilter.getRoll();

        hasSpeed = true;
        hasGps = true;

        final float setSpeed = 100; //65; // m/s

        if (Math.abs(pitchValue) > 10) {
            _gps_speed -= 0.01f * pitchValue;
            if (_gps_speed > setSpeed) _gps_speed =  setSpeed;
            if (_gps_speed < -setSpeed) _gps_speed = -setSpeed;
        }
        else {
            _gps_speed *= 0.99998;  // decay to zero
        }
        gps_speed = setSpeed + _gps_speed;
        gps_rateOfClimb = (pitchValue * gps_speed / 50 );

        _gps_altitude += (gps_rateOfClimb / 10);
        if (_gps_altitude < -100) _gps_altitude = -100;   //m
        if (_gps_altitude > 10000) _gps_altitude = 10000; //m
        gps_altitude = _gps_altitude;

        if (gps_speed != 0) {
            _gps_course += (rollValue * gps_speed / 1e6f );
            while (_gps_course > (UTrig.M_2PI)) _gps_course %= (UTrig.M_2PI);
            while (_gps_course < 0) _gps_course += (UTrig.M_2PI);
        }
        gps_course = _gps_course;

        time.setToNow();
        sim_ms = time.toMillis(true);
        float deltaT = (float) (sim_ms - _sim_ms) / 1000f / 3600f / 1.85f / 60f;  // in sec and scaled from meters to nm to degree

        /*
        //------------------------------------------------------------------------------------------
        // todo: Hardcoded for debugging
        //
        //deltaT = 0.0000124f; //  Ludicrous Speed
        deltaT = 0.00000124f; //  Warp Speed ~ 490m/s - mach 1.5
        //deltaT = 0.000000224f; // Super Speed2

        // YCMH 090 from Perth

        Random rnd = new Random();
        gps_course = _gps_course = (float) Math.toRadians(2);// 50 // + (float) rnd.nextGaussian() / 200;
        gps_speed = _gps_speed = 125;//100;  // m/s
        gps_altitude = UMath.toMeter(1500); //2048; //900; //3048; //Meter
        //rollValue = 0;// (float) rnd.nextGaussian() / 5;
        //pitchValue = 0;//(float) rnd.nextGaussian() / 20;
        //deltaT = 0; // freeze time, ie force stationary
        //
        // todo: Hardcoded for debugging
        //------------------------------------------------------------------------------------------
        // */

        _sim_ms = sim_ms;
        if ((deltaT > 0) && (deltaT < 0.0000125)) {
            gps_lon = _gps_lon += deltaT * gps_speed * Math.sin(gps_course);
            gps_lat = _gps_lat += deltaT * gps_speed * Math.cos(gps_course);

            if (gps_lon > 180) gps_lon = -180; if (gps_lon < -180) gps_lon = 180;
            if (gps_lat > 90) gps_lat = -90;   if (gps_lat < -90) gps_lat = 90;
        }
        gps_agl = DemGTOPO30.calculateAgl(gps_lat, gps_lon, gps_altitude);
    }


    //for landscape mode
    // private float azimuthValue;
    protected float rollValue;
    protected float pitchValue;
    protected float gyro_rateOfTurn;
    protected float loadfactor;
    protected float slipValue;
    protected int ctr = 0;

 /*

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

*/

}


/*
new AlertDialog.Builder(this)
                    .setMessage("Hello world!")
                    .setPositiveButton("OK", null)
                    .show();
*/


