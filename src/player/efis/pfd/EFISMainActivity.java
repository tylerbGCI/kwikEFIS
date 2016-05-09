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
//b2b2 import player.ulib.SensorFusion;
import player.ulib.DigitalFilter;
import player.ulib.orientation_t;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater; 
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


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
	
	private static final int CAL_MAX = 6;//500;//350;
	private static final int SLIP_SENS = 25; //50;	// Arbitrary choice
	private static final float STD_RATE = 0.0524f;	// = rate 1 = 3deg/s

	int mode = 0;
	float PitchOffset = 0; 
	float RollOffset = 0; 
	float RollSensorOffset = 0; 
	float SlipOffset = 0;
	float GmeterOffset = 0; 
	float loadfactorCal = 0;
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
	
	// Digital filters
	DigitalFilter filterRotGyro = new DigitalFilter(64);   //64
	DigitalFilter filterSlip = new DigitalFilter(128);  //32
	DigitalFilter filterRoll = new DigitalFilter(16);  //32 
	DigitalFilter filterPitch = new DigitalFilter(32); //64 
	DigitalFilter filterRateOfClimb = new DigitalFilter(4); //8
	DigitalFilter filterRateOfTurn = new DigitalFilter(4); //8
	DigitalFilter filterfpvX = new DigitalFilter(128); //32
	DigitalFilter filterfpvY = new DigitalFilter(32); //32
	DigitalFilter filterG = new DigitalFilter(32); //32
	DigitalFilter filterGpsSpeed = new DigitalFilter(6); //4
	DigitalFilter filterGpsAltitude = new DigitalFilter(6); //4
	DigitalFilter filterGpsCourse = new DigitalFilter(6); //4

	//
	//  Add the action bar buttons 
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.main_activity_actions, menu);
		inflater.inflate(R.menu.main_menu, menu);
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
		switch (item.getItemId()) {
		case R.id.preferences:
			// Launch settings activity
			Intent i = new Intent(this, AppPreferences.class);
			startActivity(i); 
			break;
			// more code...
		}
		return true; 
	}  

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

		try {
			mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
			registerSensorManagerListeners();
		} 
		catch (Exception e) {
			Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
		}

		//b2b2 sensorFusion = new SensorFusion();
		//b2b2 sensorFusion.setMode(SensorFusion.Mode.FUSION); //Mode.FUSION); //Mode.GYRO); //Mode.ACC_MAG);  //bad jitter   
		
		// testing for lightweight -- may or may not use
		sensorComplementaryFilter = new SensorComplementaryFilter();

		// Location 
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		// Define the criteria how to select the location provider -> use default
		//Criteria criteria = new Criteria();
		//provider = locationManager.getBestProvider(criteria, false);
		provider = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(provider, 400, 1, this);  // 400ms or 1m
		locationManager.addGpsStatusListener(this);
		Location location = locationManager.getLastKnownLocation(provider);
		// end location

		// Preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// Set the window to be full brightness
		WindowManager.LayoutParams layout = getWindow().getAttributes();
	    layout.screenBrightness = -1f;  // 1f = full bright 0 = selected
	    getWindow().setAttributes(layout);		
		
		// Instantiate a new apts gpx/xml
		Gpx gpx = new Gpx(this);
		
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    mGLView.mRenderer.mWptSelName = settings.getString("WptSelName", "YSEN");
    mGLView.mRenderer.mWptSelComment = settings.getString("WptSelComment", "Serpentine");
    mGLView.mRenderer.mWptSelLat = settings.getFloat("WptSelLat", -32.395000f);
    mGLView.mRenderer.mWptSelLon = settings.getFloat("WptSelLon", 115.871000f);
	}
	
	
	@Override
  protected void onStop(){
     super.onStop();

    // We need an Editor object to make preference changes.
    // All objects are from android.context.Context
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString("WptSelName", mGLView.mRenderer.mWptSelName);
    editor.putString("WptSelComment", mGLView.mRenderer.mWptSelComment);
    editor.putFloat("WptSelLat", mGLView.mRenderer.mWptSelLat);
    editor.putFloat("WptSelLon", mGLView.mRenderer.mWptSelLon);
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

		locationManager.requestLocationUpdates(provider, 400, 1, this);  // 400ms or 1m
		//locationManager .addNmeaListener(this);
		
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
		update(event.values);
	}
	

	@Override
	public void onLocationChanged(Location location)
	{
		gps_lat =  (float) location.getLatitude();
		gps_lon = (float) location.getLongitude();
		
		if (location.hasSpeed()) { 
			gps_speed = filterGpsSpeed.runningAverage(location.getSpeed());
			if (gps_speed == 0) gps_speed = 0.01f;  // nip div zero issues in the bud
			mGLView.setServiceableAsi();
			hasSpeed = true;
		}
		else {
			mGLView.setUnServiceableAsi();
			hasSpeed = false;
		}

		if (location.hasAltitude()) {
			gps_altitude = filterGpsAltitude.runningAverage(location.getAltitude());
			gps_rateOfClimb = calculateRateOfClimb(gps_altitude);
			mGLView.setServiceableAlt();
		}
		else {
			mGLView.setUnServiceableAlt();
		}

		if (location.hasBearing()) {
			gps_course = filterGpsCourse.runningAverage(Math.toRadians(location.getBearing()));
			gps_rateOfTurn = calculateRateOfTurn(gps_course);
			mGLView.setServiceableDi();
		}
		else {
			gps_rateOfTurn = 0;
			mGLView.setUnServiceableDi();
		}
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
		mGLView.setPrefs(prefs_t.TAPE,    SP.getBoolean("displayTape", true));
		mGLView.setPrefs(prefs_t.MIRROR,  SP.getBoolean("displayMirror", false));
		mGLView.setPrefs(prefs_t.INFO_PAGE, SP.getBoolean("infoPage", true));
		mGLView.setPrefs(prefs_t.FLIGHT_DIRECTOR, SP.getBoolean("flightDirector", false));
		bDemoMode = SP.getBoolean("demoMode", false);
		bLockedMode = SP.getBoolean("lockedMode", false);
		
		// If we changed to or from HUD mode, a calibration is required
		if (bHudMode != SP.getBoolean("displayMirror", false)) calibrationCount = 0;
		bHudMode = SP.getBoolean("displayMirror", false);
	}

 
	private void intro()  
	{
		mGLView.setGS((float) calibrationCount);
		mGLView.setRoll(calibrationCount*360/CAL_MAX);
		mGLView.setPitch(-90 + calibrationCount*90/CAL_MAX);
		mGLView.setASI((int) (calibrationCount*200/CAL_MAX));
		mGLView.setALT((int) (calibrationCount*20000/CAL_MAX));
		mGLView.setHeading((int) (calibrationCount*360/CAL_MAX));
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
	private static  Time  time = new Time(); 	// Time class 
	private static  long   time_a, _time_a;			
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
	private static float _course;   	// previous altitude
	private static  long   time_c, _time_c;			
	private float calculateRateOfTurn(float course)
	{
		float rateOfTurn = 0;
		long deltaT;
		float deltaCrs = course - _course;  
		
		// Handle the case around 0
		if (deltaCrs > Math.PI) deltaCrs -= (2*Math.PI);  
		if (deltaCrs < -Math.PI) deltaCrs += (2*Math.PI);  

		time.setToNow();
		time_c = time.toMillis(true); 
		deltaT = time_c - _time_c; 
		if (deltaT > 0) { 
			rateOfTurn = 1000* filterRateOfTurn.runningAverage((float)(deltaCrs) / deltaT); // rad/s
			_time_c = time_c; 
			_course = course; // save the previous course
		}
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
	// We assume no GPS is availaible if there has not been a valid
	// altitude update in 10 seconds
	//
	boolean isGPSAvailable() 
	{
			//gps_insky = 0;
			if (gps_infix > 3) return true;
			else return false;
		/*
		time.setToNow();
		long time_g = time.toMillis(true); 
		if (time_g - time_a > 9999) {
			hasSpeed = false;
			gps_rateOfTurn = 0;
			gps_rateOfClimb = 0;
			return false;
		}
		else 
			return true;
		*/
	}
	
	/*
	//-------------------------------------------------------------------------
	// Utility function to calculate the bank angle
	// Rate of climb in m/s.
	// The input paramaters are the fused sensor values
	//
	// This function has been moved to SensorComplementaryFilter
	// Left here for a while in case needed again, but will eventually be removed.
	//
	float calculateBankAngle(float roll, float rot, float accel)
	// if we have some speed and/or G we can calculate bank
	{
		// There is some G therefore the accelerometer cannot be trusted 
		// 10 m/s = 20kts. We assume that no flying is being done.
		if ( hasSpeed && (gps_speed > 2.0)  ) {  
			// Bank angle using the GPS derived speed
			// For a coordinated turn:
			//   tan (b) = w * v / g 
			mGLView.setServiceableAh();
			float roll_centripetal = (float) (Math.atan2(rot*gps_speed, SensorManager.GRAVITY_EARTH)* 180 / Math.PI);
			mode = 2;

			// Apply in a correction for any slip / skid
			float roll_accel = sensorComplementaryFilter.getRollAcc();
			//roll = compassRose180(roll_centripetal);  // no corrections
			roll = compassRose180(roll_centripetal - roll_accel);  // correct slip with acceleration sensor value
		}
		else  {
			// We take the values from the complementary filter
			// They are heavily biased to the gyros and will give
			// a reasonably accurate result for a while.
			mGLView.setServiceableAh();
			roll = compassRose180(roll);
			mode = 0;  
		}
//		else if (Math.abs(accel - 1.0f) < 0.05f) {
//			// If there there small or no G, the accelerometer will give 
//			// an fairly accurate result
//			mGLView.setServiceableAh();
//			roll = compassRose180(roll);
//			mode = 0;  
//		}
//		else {
//			// Too much G and no speed
//			// nothing we can do
//			mGLView.setUnServiceableAh();
//			roll = 0;
//			mode = -1;
//		}
		return roll; 
	}
*/	
	
	//-------------------------------------------------------------------------
	// Utility function to 
	// Perform a simple calibration
	//
	float _RollOffset = Float.MAX_VALUE;
	float _PitchOffset = Float.MAX_VALUE;
	long _cal_ms, cal_ms;
	boolean Calibrate(float pitch, float roll, float slip)
	{
		if (calibrationCount < CAL_MAX) { 
			time.setToNow();
			cal_ms = time.toMillis(true);
		  if ((double) cal_ms - (double) _cal_ms < 2000) {
		  	return false;
		  }
		  _cal_ms = cal_ms;
  		
			mGLView.setUnServiceable(); 
			//PitchOffset = -pitch; 
			//RollOffset  = -roll;
			
			PitchOffset = -sensorComplementaryFilter.getPitchAcc();
			sensorComplementaryFilter.primePitch();
			RollOffset = 0; //RollOffset  = sensorComplementaryFilter.getRollAcc(); 
			SlipOffset  = -slip;
			
			if (Math.abs(PitchOffset) < 45) {  
				mGLView.setCalibratingMsg(true, String.format("CALIBRATE %d", CAL_MAX-calibrationCount));  
				calibrationCount++;  

				if ((Math.abs(RollOffset - _RollOffset) < 0.1) 
						&& (Math.abs(PitchOffset - _PitchOffset) < 0.1))  {
					calibrationCount = CAL_MAX;
				}
			}  
			else  { 
				mGLView.setCalibratingMsg(true, "CHECK MOUNT ANGLE");
				calibrationCount = 0;
			}
			
			_RollOffset = RollOffset; 
			_PitchOffset = PitchOffset; 
			return false; 
		}
		else if (calibrationCount == CAL_MAX) {     
			sensorComplementaryFilter.primePitch();
			hasSpeed = false;
			gps_speed = 0; 

			calibrationCount++;
			mGLView.setServiceable();
			mGLView.setServiceableAh();
			mGLView.setMSG(0, null);
			mGLView.setCalibratingMsg(false, "DONE");  
			return true;
		} 
		else {
			return false;
		}
	}
	

	//-------------------------------------------------------------------------
	// Utility function to 
	// do a simple simulation for demo mode
	//
	static int counter;
	DigitalFilter filterTestAlt = new DigitalFilter(128); //32
	float _gps_lon = 0; 
	float	_gps_lat = 0;
	float _gps_course = 0;
	float _gps_altitude = 0;
	float _gps_speed = 0;
	long _sim_ms = 0, sim_ms;

	private void Simulate()
	{
		float pitchValue = this.pitchValue + this.PitchOffset;  // hack to work around 
		
		counter++; 
		hasSpeed = false; 
		//hasSpeed = true; 
		hasGps = true;

	
/*		
    // Free running speed - 0 to 200kts
		float _gps_speed = ((float) counter/40f) % 200;  
		if (_gps_speed < 100 ) gps_speed = _gps_speed;
		else gps_speed = 200 - _gps_speed;
*/
			
 		final float setSpeed = 65; //m/s
		if (Math.abs(pitchValue) > 5) {
			_gps_speed += 0.01f * pitchValue;
			if (_gps_speed > setSpeed) _gps_speed =  setSpeed;
			if (_gps_speed < -setSpeed) _gps_speed = -setSpeed;
		}
		else {
			_gps_speed *= 0.9998;  // decay to zero
		}
		gps_speed = setSpeed + _gps_speed;
		
		gps_rateOfClimb = (-pitchValue * gps_speed / 50 );
		//gps_rateOfClimb = (-(pitchValue + PitchOffset) * gps_speed / 50 );
		
		_gps_altitude += (gps_rateOfClimb / 10);  
		if (_gps_altitude < -100) _gps_altitude = -100;   //m
		if (_gps_altitude > 10000) _gps_altitude = 10000; //m 
		gps_altitude = _gps_altitude;
		
		
		if (gps_speed != 0) {
			_gps_course += (rollValue * gps_speed / 1e6f ); 	
			while (_gps_course > (2*Math.PI)) _gps_course %= (2*Math.PI) ;  
			while (_gps_course < 0) _gps_course += (2*Math.PI);  
		}
		gps_course = _gps_course;
		
		// Grab the first valid gps fix we get and use
		// that as the initial point for the simulation 
		if ((gps_lat == 0) 	&& (_gps_lat == 0) && 
		    (gps_lon == 0) && (_gps_lon == 0)) {
			return;
		}
		else if ((_gps_lon == 0) && (_gps_lat == 0)) {
			_gps_lon = gps_lon;
			_gps_lat = gps_lat;
		}

		time.setToNow();
		sim_ms = time.toMillis(true);
		float deltaT = (float) (sim_ms - _sim_ms) / 1000f / 3600f / 1.85f / 60f;  // in sec and scaled from meters to nm to degree
		_sim_ms = sim_ms;
		
		//deltaT = 0.00000125f; // for debugging
		//gps_course = (float) Math.PI / 2; // for debugging
		
  	if (deltaT < 0.0000125) {
			// gps_lon = _gps_lon += 0.00000125 * gps_speed * Math.sin(gps_course);
			// gps_lat = _gps_lat += 0.00000125 * gps_speed * Math.cos(gps_course);
			gps_lon = _gps_lon += deltaT * gps_speed * Math.sin(gps_course);
			gps_lat = _gps_lat += deltaT * gps_speed * Math.cos(gps_course);
			
			if (gps_lon > 180) gps_lon = -180; if (gps_lon < -180) gps_lon = 180;
			if (gps_lat > 90) gps_lat = -90;   if (gps_lat < -90) gps_lat = 90;
		}
	}


	//for landscape mode       
	private float azimuthValue;
	private float rollValue;   
	private float pitchValue;
	private float gyro_rateOfTurn;  
	private float loadfactor; 
	private float slipValue;
	private void update(float[] vectors)  
	{
		float[] gyro =  new float[3]; // gyroscope vector
		float[] accel = new float[3]; // accelerometer vector

		//
		// Read the Sensors
		//
		//sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_LANDSCAPE);
		if (bHudMode) sensorComplementaryFilter.setOrientation(orientation_t.HORIZONTAL_LANDSCAPE); 
		else sensorComplementaryFilter.setOrientation(orientation_t.VERTICAL_LANDSCAPE); 
		
		sensorComplementaryFilter.getGyro(gyro); 		// Use the gyroscopes for the attitude 
		sensorComplementaryFilter.getAccel(accel);		// Use the accelerometer for G and slip

		//float cfPitch = sensorComplementaryFilter.getPitch();
		//float cfRoll = sensorComplementaryFilter.getRoll();
		//pitchValue =  cfPitch; 
		//rollValue =  -cfRoll;
		pitchValue = sensorComplementaryFilter.getPitch();
		rollValue = -sensorComplementaryFilter.getRoll();
		
		gyro_rateOfTurn = (float) filterRotGyro.runningAverage(-gyro[0]);  
		slipValue  = filterSlip.runningAverage(accel[1]);
		//loadfactor = (float) Math.sqrt(accel[0]*accel[0] + accel[1]*accel[1] + accel[2]*accel[2]) / SensorManager.GRAVITY_EARTH;
		//loadfactor = filterG.runningAverage(loadfactor);
		loadfactor = sensorComplementaryFilter.getLoadFactor();
		loadfactor = filterG.runningAverage(loadfactor);
		
		// 
		//Demo mode handler  
		//
		if (bDemoMode) {
			mGLView.setDemoMode(true, "DEMO");
			Simulate();
			// Set the GPS flag and 
			// make all the instruments serviceable
			hasGps = true;
			mGLView.setServiceableDi();
			mGLView.setServiceableAsi();
			mGLView.setServiceableAlt();
		}
		else { 
			mGLView.setDemoMode(false, " ");
			hasGps = isGPSAvailable();  
		}

		
		//
		// Set the user preferences
		//
		setUserPrefs(); 					
		if (hasGps == false)	{
			//mGLView.setFlightDirector(false, 0, 0);
			mGLView.setDisplayAirport(false);		
			mGLView.setUnServiceableAsi();
			mGLView.setUnServiceableAlt();
			mGLView.setUnServiceableDi();
		}
		else {
			mGLView.setDisplayAirport(true);
			//mGLView.setServiceableAsi();
			//mGLView.setServiceableAlt();
			//mGLView.setServiceableDi();
		}
		
		//
		// Calculate the augmented bank angle and also the flight path vector 
		//
		float deltaA, fpvX = 0, fpvY = 0; 
		if ( hasSpeed && (gps_speed > 2.0)  ) {  
			mode = 2;
			rollValue = sensorComplementaryFilter.calculateBankAngle(0.95f*gyro_rateOfTurn + 0.05f*gps_rateOfTurn, gps_speed);

			// the Flight Path Vector (FPV)
			deltaA = compassRose180(gps_course - orientationAzimuth); 
			fpvX = (float) filterfpvX.runningAverage(Math.atan2(-gyro_rateOfTurn * 100.0f, gps_speed) * 180.0f / Math.PI); // velocity of a point 100m ahead of nose 
			fpvY = (float) filterfpvY.runningAverage(Math.atan2(gps_rateOfClimb, gps_speed) * 180.0f / Math.PI);
		}
		else {
			mode = 0;
			fpvX = 0;//180; 
			fpvY = 0;//180;
		}

		//
		// Perform a simple calibration
		// 
		Calibrate(pitchValue, rollValue, slipValue); 

		// 
		// Correct the pitch and G for the mounting angle of the device
		//
		rollValue  += RollOffset; // correct for mounting angle; 
		pitchValue += PitchOffset;  
		slipValue +=  SlipOffset;
	
		// Apply a little filtering to the bank
		rollValue = filterRoll.runningAverage(compassRose180(rollValue));  

		// Apply a little filtering to the pitch
		pitchValue 	 = filterPitch.runningAverage(pitchValue); 

		//
		// Get the battery percentage
		//
		float batteryPct = getRemainingBattery();
		
		//
		// Pass the values to mGLView for updating 
		//
		String s; // general purpose string    

		if (mode > 0) {
	  	// We have GPS augmentation. Use the fpvY for the pitch
			// and the sensor for the birdie 
			mGLView.setPitch(fpvY);  
			mGLView.setFPV(fpvX, -pitchValue);
		}
		else { 
			mGLView.setPitch(-pitchValue);  
			//mGLView.setFPV(fpvX, fpvY);
			mGLView.setFPV(180, 180);  // no birdie
		}
		
		mGLView.setRoll(rollValue); 
		mGLView.setBatteryPct(batteryPct); 
		mGLView.setGForce(loadfactor);  
		mGLView.setSlip(SLIP_SENS * slipValue); 
		mGLView.setHeading((float) Math.toDegrees(gps_course)); // in degrees
		mGLView.setALT((int) (gps_altitude * 3.2808f)); 	    // in feet
		mGLView.setASI(gps_speed * 1.94384449f);            	// in knots
		mGLView.setVSI((int) (gps_rateOfClimb * 196.8504f)); 	// in fpm
		mGLView.setLatLon(gps_lat, gps_lon);		
		mGLView.setTurn(0.95f*gyro_rateOfTurn + 0.05f*gps_rateOfTurn); 
		
		s = String.format("GPS: %d / %d", gps_infix, gps_insky);
		mGLView.setMSG(4, s);
		
		//s = String.format("ROL:%3.0f COR:%3.0f ", rollValue, RollOffset);
		//s = String.format("ROL:%3.0f PIT:%3.0f ", rollValue, pitchValue);
		//s = String.format("RS:%3.0f RG:%3.0f ", gyro_rateOfTurn*1000, gps_rateOfTurn*1000);
		//mGLView.setMSG(3, s);

		s = String.format("M: %d", mode);
		mGLView.setMSG(2, s);
		
		s = String.format("%c%03.2f %c%03.2f",  (gps_lat < 0)?  'S':'N' , Math.abs(gps_lat), (gps_lon < 0)? 'W':'E' , Math.abs(gps_lon));
		mGLView.setMSG(1, s);
	} 
}


/*
new AlertDialog.Builder(this)
                    .setMessage("Hello boys!!!")
                    .setPositiveButton("OK", null)
                    .show();
 */
