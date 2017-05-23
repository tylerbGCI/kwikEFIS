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


import player.ulib.SensorComplementaryFilter;
import player.ulib.DigitalFilter;
import player.ulib.UNavigation;
import player.ulib.UTrig;
import player.ulib.orientation_t;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle; 
// sensor imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.format.Time;
//import android.view.KeyEvent;
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


public class EFISMainActivity extends Activity implements Listener, SensorEventListener, LocationListener
{
	public static final String PREFS_NAME = R.string.app_name + ".prefs";

	private EFISSurfaceView mGLView;

	// sensor members
	private SensorManager mSensorManager;
	//private Sensor mRotationSensor;
	//private static final int SENSOR_DELAY = 500 * 1000; // 500ms

	//b2b2 private SensorFusion sensorFusion;
	private SensorComplementaryFilter sensorComplementaryFilter;

	// location members
	private LocationManager locationManager;
	private String provider;
	private GpsStatus mGpsStatus = null;

	private boolean bDemoMode = false;
	private boolean bLockedMode = false;
	private boolean bHudMode = false;
    private boolean bLandscapeMode = false;

	private static final int SLIP_SENS = 25; //50;	// Arbitrary choice
	private static final float STD_RATE = 0.0524f;	// = rate 1 = 3deg/s

	private static final long GPS_UPDATE_PERIOD = 0;   //ms // 400
	private static final long GPS_UPDATE_DISTANCE = 0; //ms // 1

	int calibrationCount = 0;

	// Location abstracts
	protected float gps_lat;
	protected float gps_lon;
	protected float gps_altitude;
	protected float gps_speed;
	protected float gps_course;
	protected float gps_rateOfClimb;
	protected float gps_rateOfTurn;

	protected boolean hasSpeed;
	protected boolean hasGps;
	protected float orientationAzimuth;
	protected float orientationPitch;
	protected float orientationRoll;

	private int gps_insky;
	private int gps_infix;

	private float sensorBias;

	private Gpx mGpx;  // wpt database
    private DemGTOPO30 mDemGTOPO30;  // dem database

	// Digital filters
	DigitalFilter filterRateOfTurnGyro = new DigitalFilter(16);   //8
	DigitalFilter filterSlip = new DigitalFilter(32);  //32
	DigitalFilter filterRoll = new DigitalFilter(8);   //16
	DigitalFilter filterPitch = new DigitalFilter(8); //16
	DigitalFilter filterRateOfClimb = new DigitalFilter(4); //8
	//not used? DigitalFilter filterRateOfTurn = new DigitalFilter(4); //8
	DigitalFilter filterfpvX = new DigitalFilter(256); //128
	DigitalFilter filterfpvY = new DigitalFilter(256); //128
	DigitalFilter filterG = new DigitalFilter(32); //32
	DigitalFilter filterGpsSpeed = new DigitalFilter(6); //4
	DigitalFilter filterGpsAltitude = new DigitalFilter(6); //4
	DigitalFilter filterGpsCourse = new DigitalFilter(6); //4


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
		//this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed()
	{
		if (bLockedMode == false) {
		  finish();
		  super.onBackPressed(); // remove comment to enable back button
		}
		else {
			Toast.makeText(this, "Locked Mode: Active", Toast.LENGTH_SHORT).show();
		}
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//updateMenuTitles();
		switch (item.getItemId()) {
            case R.id.settings:
                // Launch settings activity
                Intent i = new Intent(this, EFISPrefSettings.class);
                startActivity(i);
                break;
            case R.id.manage:
                // Launch manage activity
                Intent j = new Intent(this, EFISPrefManage.class);
                startActivity(j);
                break;
                // more code...
            default:
                return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/*  
	It causes problems with the new "improved" Samsung devices.
	
	// This code will catch the actual keypress.
	// for now we will leave the menu bar in case it is needed later 
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{ 
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			//do your work ...
			// Launch settings activity
			Intent i = new Intent(this, AppPreferences.class); 
			startActivity(i);  
			return true;
		}
		return super.onKeyDown(keyCode, event);  
	} 
	*/

	/* This does not seem to do anything 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);

		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		mGLView.setPrefs(prefs_t.TERRAIN, SP.getBoolean("displayTerrain", true));
		mGLView.setPrefs(prefs_t.TAPE,    SP.getBoolean("displayTape", true));
		mGLView.setPrefs(prefs_t.MIRROR,  SP.getBoolean("displayMirror", false));
		bDemoMode = SP.getBoolean("demoMode", false);
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new EFISSurfaceView(this);
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
		Toast.makeText(this, "kwik EFIS version: " + version, Toast.LENGTH_LONG).show();

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
		//Criteria criteria = new Criteria();
		//provider = locationManager.getBestProvider(criteria, false);
		provider = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(provider, GPS_UPDATE_PERIOD, GPS_UPDATE_DISTANCE, this);  // 400ms or 1m
		locationManager.addGpsStatusListener(this);
		Location location = locationManager.getLastKnownLocation(provider);
		// end location

		// Preferences
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		PreferenceManager.setDefaultValues(this, R.xml.manage , false);

		// Set the window to be full brightness
		WindowManager.LayoutParams layout = getWindow().getAttributes();
    	layout.screenBrightness = -1f;  // 1f = full bright 0 = selected
    	getWindow().setAttributes(layout);

		// Restore persistent preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	mGLView.mRenderer.mWptSelName = settings.getString("WptSelName", "YSEN");
    	mGLView.mRenderer.mWptSelComment = settings.getString("WptSelComment", "Serpentine");
    	mGLView.mRenderer.mWptSelLat = settings.getFloat("WptSelLat", -32.395000f);
    	mGLView.mRenderer.mWptSelLon = settings.getFloat("WptSelLon", 115.871000f);
        mGLView.mRenderer.mAltSelValue = settings.getFloat("mAltSelValue", 0f);
        mGLView.mRenderer.mAltSelName = settings.getString("mAltSelName", "00000");
        mGLView.mRenderer.mObsValue = settings.getFloat("mObsValue", 0f);

    	// This should never happen but we catch and force it to something known it just in case
    	if (mGLView.mRenderer.mWptSelName.length() != 4) mGLView.mRenderer.mWptSelName = "YSEN";
        if (mGLView.mRenderer.mAltSelName.length() != 5) mGLView.mRenderer.mWptSelName = "00000";

	    String region = settings.getString("RegionDatabase", "zar.aus");

        // Use the last orientation to start
        bLandscapeMode = settings.getBoolean("landscapeMode", false);

		// Instantiate a new apts gpx/xml
		mGpx = new Gpx(this);
		mGpx.loadDatabase(region);

        mDemGTOPO30 = new DemGTOPO30(this);
        mDemGTOPO30.loadDatabase(region);

		// Overall the device is now ready.
		// The individual elements will be enabled or disabled by the location provided
		// based on availability
		mGLView.setServiceableDevice();
	}


	@Override
  protected void onStop()
    {
        super.onStop();

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

        // need to add the aircraft --- todo
        //editor.putString("AircraftModel", mGLView.mRenderer.mAcraftModel.toString());
        // editor.putString("RegionDatabase", mGpx.region);  // happens automatically ?

        // Commit the edits
        editor.commit();
    }



	public void registerSensorManagerListeners()
	{
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 	mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 		mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
		//b2 mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 	mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
		//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 		mSensorManager.SENSOR_DELAY_UI); //SENSOR_DELAY_FASTEST);
		//b2 mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 	mSensorManager.SENSOR_DELAY_UI);
	}

	public void unregisterSensorManagerListeners()
	{
		mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)); //SENSOR_DELAY_FASTEST);
		mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)); //SENSOR_DELAY_FASTEST);
		//b2 mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)); //SENSOR_DELAY_FASTEST);
		//mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)); //SENSOR_DELAY_FASTEST);
		//b2 mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
	}


	@Override
	protected void onPause()
	{
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();

		locationManager.removeUpdates(this);
		unregisterSensorManagerListeners();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();

		locationManager.requestLocationUpdates(provider, GPS_UPDATE_PERIOD, GPS_UPDATE_DISTANCE, this);  // 400ms or 1m
		//locationManager.addNmeaListener(this);

		registerSensorManagerListeners();
	}

	//
	// Sensor methods
	//
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		switch(sensor.getType()) {
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
			//b2b2 sensorFusion.setAccel(event.values);
			//b2b2 sensorFusion.calculateAccMagOrientation();
			sensorComplementaryFilter.setAccel(event.values);
			break;

		case Sensor.TYPE_GYROSCOPE:
			//b2b2 sensorFusion.gyroFunction(event);
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
		updateEFIS(/*event.values*/);
	}


	@Override
	public void onLocationChanged(Location location)
	{
		if (!bDemoMode) {
			gps_lat =  (float) location.getLatitude();
			gps_lon = (float) location.getLongitude();

			if (location.hasSpeed()) {
				//gps_speed = filterGpsSpeed.runningAverage(location.getSpeed());
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
				//gps_altitude = filterGpsAltitude.runningAverage(location.getAltitude());
				gps_altitude = (float) location.getAltitude();
				gps_rateOfClimb = calculateRateOfClimb(gps_altitude);
				mGLView.setServiceableAlt();
			}
			else {
				mGLView.setUnServiceableAlt();
			}

			if (location.hasBearing()) {
				//gps_course = filterGpsCourse.runningAverage(Math.toRadians(location.getBearing()));
				gps_course = (float) Math.toRadians(location.getBearing());
				gps_rateOfTurn = calculateRateOfTurn(gps_course);
				mGLView.setServiceableDi();
			}
			else {
				gps_rateOfTurn = 0;
				mGLView.setUnServiceableDi();
			}

			if (location.hasSpeed() && location.hasBearing() ) {
				mGLView.setServiceableAh();
			}
			else {
				mGLView.setUnServiceableAh();
			}
		}
		updateEFIS(/*event.values*/);
}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		if (!LocationManager.GPS_PROVIDER.equals(provider)) {
			return;
		}
		// do something
	}


	protected void setGpsStatus()
	{
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
			Iterable<GpsSatellite> sats = mGpsStatus.getSatellites();
			gps_insky = 0;
			gps_infix = 0;
			for (GpsSatellite s : sats) {
				gps_insky += 1;
				if (s.usedInFix()) gps_infix += 1;
			}
		}
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
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mGLView.setPrefs(prefs_t.TERRAIN, SP.getBoolean("displayTerrain", true));
        mGLView.setPrefs(prefs_t.DEM, SP.getBoolean("displayDEM", false));
        mGLView.setPrefs(prefs_t.TAPE, SP.getBoolean("displayTape", true));
        mGLView.setPrefs(prefs_t.MIRROR, SP.getBoolean("displayMirror", false));
        mGLView.setPrefs(prefs_t.INFO_PAGE, SP.getBoolean("infoPage", true));
        mGLView.setPrefs(prefs_t.FLIGHT_DIRECTOR, SP.getBoolean("displayFlightDirector", false));
        mGLView.setPrefs(prefs_t.REMOTE_INDICATOR, SP.getBoolean("displayRmi", false));
        mGLView.setPrefs(prefs_t.HITS, SP.getBoolean("displayHITS", false));


        bLockedMode = SP.getBoolean("lockedMode", false);
        sensorBias = Float.valueOf(SP.getString("sensorBias", "0.15f"));

        // If we changed to Demo mode, use the current GPS as seed
        if (bDemoMode != SP.getBoolean("demoMode", false)) {
            if (gps_lon != 0 && gps_lat != 0) {
                _gps_lon = gps_lon;
                _gps_lat = gps_lat;
            }
        }
        bDemoMode = SP.getBoolean("demoMode", false);

        // If we changed to or from HUD mode, a calibration is required
        if (bHudMode != SP.getBoolean("displayMirror", false)) calibrationCount = 0;
        bHudMode = SP.getBoolean("displayMirror", false);

        // If the aircraft is changed, update the paramaters
        String s = SP.getString("AircraftModel", "RV8");
        AircraftData.setAircraftData(s); //mGLView.mRenderer.setAircraftData(s);  // refactored  to static model

        // If the database changed it needs to be re-loaded.
        s = SP.getString("regionDatabase", "zar.aus");
        if (!mGpx.region.equals(s)) mGpx.loadDatabase(s);               // load the waypoints
        if (!mDemGTOPO30.region.equals(s)) {
            mDemGTOPO30.loadDatabase(s);                 // load the dem
            mDemGTOPO30.loadDemBuffer(gps_lat, gps_lon); // force an update
        }

        // landscape / porait mode toggle
        bLandscapeMode = SP.getBoolean("landscapeMode", false);
        if (bLandscapeMode) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mGLView.mRenderer.Layout = EFISRenderer.layout_t.LANDSCAPE;
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mGLView.mRenderer.Layout = EFISRenderer.layout_t.PORTRAIT;
        }
        bLandscapeMode = SP.getBoolean("landscapeMode", false);
    }


	//-------------------------------------------------------------------------
	// Utility function to normalize an angle from any angle to
	// 0 to +180 and 0 to -180
	//
	private float compassRose180(float angle)
	{
		angle = (angle) % 360;

		if (angle >  180) angle = angle - 360;
		if (angle < -180) angle = angle + 360;

		return angle;
	}


	//-------------------------------------------------------------------------
	// Utility function to normalize an angle from any angle to
	// 0 to +360
	//
	private float compassRose360(float angle)
	{
		angle = (angle) % 360;

		if (angle <  0) angle = angle + 360;

		return angle;
	}


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
	private static Time  time = new Time(); 	// Time class
	private static long   time_a, _time_a;
	private static float _altitude;   	// previous altitude
	private float calculateRateOfClimb(float altitude)
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
	private float calculateRateOfTurn(float course)
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
	float getRemainingBattery()
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
	boolean isGPSAvailable()
	{
		if (gps_infix > 3) return true;
		else return false;
	}


	//-------------------------------------------------------------------------
	// Utility function to do a simple simulation for demo mode
	// It acts like a very simple flight simulator
	//
    //float _gps_lat = -25.656874f; float _gps_lon =   28.221832f; // Wonderboom
    //float _gps_lat = -34.259918f; float _gps_lon = 115.45f; // South of Valsbaai -34.359918f
    //float _gps_lat = -31.9f;  float _gps_lon = 115.45f;  // Australia north of Rottnest
    //float _gps_lat = -33.0f;  float _gps_lon = 28; //-28;// = -33; // South Africa - East London
    //float _gps_lat = -33.2f;  float _gps_lon = 28; //-28;// = -33; // South Africa - South of East London
    //float _gps_lat =  40.7f;  float _gps_lon = -111.82f;  // Salt Lake City
    //float _gps_lat =  48.14f; float _gps_lon = 11.57f;   // Munich
    float _gps_lat = 47.26f; float _gps_lon = 11.34f;   //Innsbruck
    //float _gps_lat = -33.98f; float _gps_lon =   18.82f; // Stellenbosh
	float _gps_course = 0.96f; //1.74f;  //in radians
    float _gps_altitude = 1000; // meters

	float _gps_speed = 0;       // m/s
	long _sim_ms = 0, sim_ms;
    Random rand = new Random();

    private void Simulate()
	{
		pitchValue = -sensorComplementaryFilter.getPitch();
		rollValue = -sensorComplementaryFilter.getRoll();

		hasSpeed = true;
		hasGps = true;

 		final float setSpeed = 65; // m/s

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
        //deltaT = 0.00000124f; //  Warp Speed ~ 490m/s - mach 1.5
        deltaT = 0.000000224f; // Super Speed2

        Random rnd = new Random();
        gps_course = _gps_course = (float) Math.toRadians(2);// 50 // + (float) rnd.nextGaussian() / 200;
        gps_speed = _gps_speed = 125;//100;  // m/s
        gps_altitude = 270; //2048; //900; //3048; //meter
        rollValue = 0;// (float) rnd.nextGaussian() / 5;
        pitchValue = 0;//(float) rnd.nextGaussian() / 20;

        //gps_lat = -33f; _gps_lon = 28f;  // EL
        //gps_lat = -33.98f; gps_lon =   18.82f;  // Stellenbosh
        //gps_lat = -33.4f; gps_lon = 19f;  // Stellenbosh ++ somewhere ??possible hole??
        //gps_lat = -33.52f; gps_lon = 19f;  // Stellenbosh ++ somewhere
        //gps_lat = 0f; gps_lon = 0f; _gps_lat = 0f; _gps_lon = 0f;  // Trapped on Null Island
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
	}


	//for landscape mode
	// private float azimuthValue;
	private float rollValue;
	private float pitchValue;
	private float gyro_rateOfTurn;
	private float loadfactor;
	private float slipValue;


	//-------------------------------------------------------------------------
	// Effectively the main execution loop. updateEFIS will get called when
	// something changes, eg a sensor has new data or new gps fix becomes available.
	//
	private void updateEFIS()
	{
		float[] gyro =  new float[3]; // gyroscope vector
		float[] accel = new float[3]; // accelerometer vector

		//
		// Read the Sensors
		//
        if (bLandscapeMode) {
            if (bHudMode) sensorComplementaryFilter.setOrientation(orientation_t.HORIZONTAL_LANDSCAPE);
            else          sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_LANDSCAPE);
        }
        else {
            if (bHudMode) sensorComplementaryFilter.setOrientation(orientation_t.HORIZONTAL_PORTRAIT);
            else          sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_PORTRAIT);
        }

		sensorComplementaryFilter.getGyro(gyro); 	// Use the gyroscopes for the attitude
		sensorComplementaryFilter.getAccel(accel);	// Use the accelerometer for G and slip

		//pitchValue = -sensorComplementaryFilter.getPitch();
		//rollValue = -sensorComplementaryFilter.getRoll();
		pitchValue = -sensorComplementaryFilter.getPitchAcc();
		rollValue = -sensorComplementaryFilter.getRollAcc();

        if (bLandscapeMode) {
            gyro_rateOfTurn = (float) filterRateOfTurnGyro.runningAverage(-gyro[0]);
            slipValue  = filterSlip.runningAverage(accel[1]);
        }
        else {
            gyro_rateOfTurn = (float) filterRateOfTurnGyro.runningAverage(-gyro[1]);
            slipValue = filterSlip.runningAverage(-accel[0]);
        }

		loadfactor = sensorComplementaryFilter.getLoadFactor();
		loadfactor = filterG.runningAverage(loadfactor);

		//
		// Check if we have a valid GPS
		//
		hasGps = isGPSAvailable();

		// for debug
		if (false) {
			hasGps = true;          //debug
			hasSpeed = true;        //debug
			gps_speed = 3;//60;     //m/s debug
			gps_rateOfClimb = 1.0f; //m/s debug
		}
		// end debug

		//
		//Demo mode handler
		//
		if (bDemoMode) {
			mGLView.setDemoMode(true, "DEMO");
			Simulate();
			// Set the GPS flag to true and
			// make all the instruments serviceable
			hasGps = true;
			hasSpeed = true;
			mGLView.setServiceableDevice();
			mGLView.setServiceableDi();
			mGLView.setServiceableAsi();
			mGLView.setServiceableAlt();
			mGLView.setServiceableAh();
			mGLView.setDisplayAirport(true);
		}
		else {
            mGLView.setDemoMode(false, " ");
        }

        //
        // Calculate the augmented bank angle and also the flight path vector
        //
        float deltaA, fpvX = 0, fpvY = 0;
        //if (hasGps && hasSpeed) {
        if (hasGps && gps_speed > 5) {
            // Testing shows that reasonable value is sensorBias of 75% gps and 25% gyro on most older devices,
            // if the gyro and accelerometer are good quality and stable, use sensorBias of 100%
            rollValue = sensorComplementaryFilter.calculateBankAngle((sensorBias)*gyro_rateOfTurn + (1-sensorBias)*gps_rateOfTurn, gps_speed);
            pitchValue = sensorComplementaryFilter.calculatePitchAngle(gps_rateOfClimb, gps_speed);


            // the Flight Path Vector (FPV)
            deltaA = compassRose180(gps_course - orientationAzimuth);
            fpvX = (float) filterfpvX.runningAverage(Math.atan2(-gyro_rateOfTurn * 100.0f, gps_speed) * 180.0f / UTrig.M_PI); // a point 100m ahead of nose
            fpvY = (float) filterfpvY.runningAverage(Math.atan2(gps_rateOfClimb * 1.0f, gps_speed) * 180.0f / UTrig.M_PI);    // simple RA of the two velocities

            // Pitch and birdie
            mGLView.setDisplayAirport(true);
            mGLView.setFPV(fpvX, fpvY); // need to clean this up
        }
        else if (hasGps && gps_speed < 9) {  // m/s
            // taxi mode
            rollValue = 0;
            pitchValue = 0;
        }
        else {
            // No GPS no speed ... no idea what the AH is :-(
            fpvX = 0;
            fpvY = 0;

            // The dreaded red crosses are required
            mGLView.setDisplayAirport(false);
            mGLView.setUnServiceableAsi();
            mGLView.setUnServiceableAlt();
            mGLView.setUnServiceableDi();
            mGLView.setUnServiceableAh();

            // Force a blank screen and no birdie
            rollValue = 0;
            pitchValue = -270;
            mGLView.setFPV(180, 180);
        }


		//
		// Read and Set the user preferences
		//
		setUserPrefs();

		// Apply a little filtering to the pitch and bank
		pitchValue = filterPitch.runningAverage(pitchValue);
		rollValue = filterRoll.runningAverage(compassRose180(rollValue));

		//
		// Get the battery percentage
		//
		float batteryPct = getRemainingBattery();

        //-----------------------------
        // Handle the DEM buffer.
        // Load new data to the buffer when the horizon gets close to the edge or
        // if we have gone off the current tile.
        //
        float dem_dme = UNavigation.calcDme(mDemGTOPO30.lat0, mDemGTOPO30.lon0, gps_lat, gps_lon);

        if ((dem_dme != 0) && (mDemGTOPO30.isOnTile(gps_lat, gps_lon) == false)) {
            mDemGTOPO30.loadDemBuffer(gps_lat, gps_lon);
        }

        //
        // Load new data into the buffer when the horizon gets close to the edge
        //
        if (dem_dme + DemGTOPO30.DEM_HORIZON > DemGTOPO30.BUFX / 4) {
            mDemGTOPO30.loadDemBuffer(gps_lat, gps_lon);
        }


		//
		// Pass the values to mGLView for updating
		//
		String s; // general purpose string

		mGLView.setPitch(pitchValue);                             // in degrees
		mGLView.setRoll(rollValue);                               // in degrees
		mGLView.setBatteryPct(batteryPct);                        // in percentage
		mGLView.setGForce(loadfactor);                            // in gunits
		mGLView.setSlip(SLIP_SENS * slipValue);
		mGLView.setHeading((float) Math.toDegrees(gps_course));   // in degrees
		mGLView.setALT((int) (gps_altitude * 3.2808f)); 	      // in feet
		mGLView.setASI(gps_speed * 1.94384449f);            	  // in knots
		mGLView.setVSI((int) (gps_rateOfClimb * 196.8504f)); 	  // in fpm
		mGLView.setLatLon(gps_lat, gps_lon);
		mGLView.setTurn((sensorBias)*gyro_rateOfTurn + (1-sensorBias)*gps_rateOfTurn);

        s = String.format("GPS %d / %d", gps_infix, gps_insky);
        mGLView.setGpsStatus(s);

        //---------------------------------------------------------------------
        // Was used for debugging
        // s = String.format("%c%03.2f %c%03.2f",  (gps_lat < 0)?  'S':'N' , Math.abs(gps_lat), (gps_lon < 0)? 'W':'E' , Math.abs(gps_lon));
        // mGLView.setMSG(5, s);
        // s = String.format("RS:%3.0f RG:%3.0f ", gyro_rateOfTurn*1000, gps_rateOfTurn*1000);
        // mGLView.setMSG(3, s);
        // s = String.format("BIAS: %d", (int) (sensorBias*100));
        // mGLView.setMSG(1, s);

	}
}


/*
new AlertDialog.Builder(this)
                    .setMessage("Hello boys!!!")
                    .setPositiveButton("OK", null)
                    .show();
*/
