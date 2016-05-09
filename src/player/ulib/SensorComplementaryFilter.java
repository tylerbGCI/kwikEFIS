package player.ulib;

import java.util.Timer;
import java.util.TimerTask;

import android.hardware.SensorManager;


public class SensorComplementaryFilter
{
	private final float ALPHA = 0.998f; //0.9998f; //0.98f is the default 
	orientation_t orientation = orientation_t.VERTICAL_LANDSCAPE; 

	private final float ACCELEROMETER_SENSITIVITY = 1.0f; //8192.0f; 
	private final float GYROSCOPE_SENSITIVITY = 1.0f;  //65.536f;
	 
	private float M_PI = 3.14159265359f;	    
	 
	private final static float dt = 0.01f;	// 10 ms sample rate!    
	public static final int TIME_CONSTANT = (int) (dt*1000); //30;
	
	private float pitch; 
	private float roll;
	
	private float pitchAcc, rollAcc; 
	private float loadfactor;             
	
	float[] accData = {0,0,0}; 
	float[] gyrData = {0,0,0};
	
	private Timer complTimer = new Timer(); 
	
	//public SensorComplementaryFilter(float[] accData, float[] gyrData )
	public SensorComplementaryFilter()
	{
		// wait for two second until gyroscope and magnetometer/accelerometer
		// data is initialised then schedule the complementary filter task
		complTimer.scheduleAtFixedRate(new calculateFilterTask(), 2000, TIME_CONSTANT);
	}

	public void setOrientation(orientation_t ori)
	{
		orientation = ori;
	}
	
	public void setAccel(float[] sensorValues)
	{
		System.arraycopy(sensorValues, 0, accData, 0, 3);
		loadfactor = (float) Math.sqrt(accData[0]*accData[0] + accData[1]*accData[1] + accData[2]*accData[2]) / SensorManager.GRAVITY_EARTH;
	}

	public void getAccel(float[] sensorValues)
	{
		System.arraycopy(accData, 0, sensorValues, 0, 3);
	}

	public void setGyro(float[] sensorValues)
	{
		System.arraycopy(sensorValues, 0, gyrData, 0, 3);
	}
	
	public void getGyro(float[] sensorValues)
	{
		System.arraycopy(gyrData, 0, sensorValues, 0, 3);
	}
	
	
	public float getPitch()
	{
		return pitch;
	}
	
	public float getRoll()
	{
		return roll;
	}

	public float getPitchAcc()
	{
		return pitchAcc;
	}

	public float getRollAcc()
	{
		return rollAcc;
	}
	
	public float getLoadFactor()
	{
		return loadfactor;
	}
	
	
	public void primePitch()
	{
		pitch = pitchAcc;
	}

	public void primeRoll()
	{
		roll = rollAcc;
	}

	//
	// Calculate augmented bank angle given rate of turn and velocity
	//
	DigitalFilter filterRollAcc = new DigitalFilter(16); //64
	
	public float calculateBankAngle(float rot, float gps_speed)
	{ 
		float bank = 0;
		
		if (gps_speed > 2.0) {  
			// Bank angle using the speed
			// For a coordinated turn:
			//   tan (b) = w * v / g 
			float roll_centripetal = (float) (Math.atan2(rot*gps_speed, SensorManager.GRAVITY_EARTH)* 180 / Math.PI);

			// Apply in a correction for any slip / skid
			float roll_accel = filterRollAcc.runningAverage(this.getRollAcc());
			
			//if (loadfactor > 1.02)
			if (Math.abs(roll_accel) > 20)
			  bank = (roll_centripetal - roll_accel);  // correct slip with acceleration sensor value
			else
				bank = roll_centripetal;  // no corrections
				
		} 
		return bank;
	}
	
	
	class calculateFilterTask extends TimerTask
	{
		public void run()
		{

			switch (orientation) {
			case HORIZONTAL_LANDSCAPE:  
				// Integrate the gyroscope data -> int(angularSpeed) = angle
				 roll += ((float)gyrData[0] * 180 / M_PI) * dt; // Angle around the X-axis
				 pitch -= ((float)gyrData[1] * 180 / M_PI) * dt;  // Angle around the Y-axis

				// Turning around the X axis results in a vector on the Y-axis
				rollAcc = (float) (Math.atan2((float)accData[1], (float)accData[2]) * 180 / M_PI);
				roll = roll * ALPHA +  rollAcc * (1 - ALPHA); 

				// Turning around the Y axis results in a vector on the X-axis
				pitchAcc = (float) (Math.atan2((float)accData[0], (float)accData[2]) * 180 / M_PI);
				pitch = pitch * ALPHA + pitchAcc * (1 - ALPHA);
				break;

			case VERTICAL_LANDSCAPE: 
				// Integrate the gyroscope data -> int(angularSpeed) = angle
				roll  += (gyrData[2] * 180 / M_PI) * dt;  // Angle around the Z-axis
				pitch += (gyrData[1] * 180 / M_PI) * dt; // Angle around the X-axis
				
				//String s = String.format("pitch:%3.4f g[0]:%3.4f", pitch, gyrData[0]);
				//System.out.println(s); 

				// Turning around the Z axis results in a vector on the X-axis
				rollAcc = (float) - (Math.atan2((float)accData[1], (float)accData[0]) * 180 / M_PI);
				roll = roll * ALPHA + rollAcc * (1 - ALPHA);
				
				// Turning around the X axis results in a vector on the Y-axis
				pitchAcc = (float) (Math.atan2((float)accData[2], (float)accData[0]) * 180 / M_PI);
				pitch = pitch * ALPHA + pitchAcc * (1 - ALPHA);
				break;

			case VERTICAL_PORTRAIT: 
				// TODO
				break;
				
			case HORIZONTAL_PORTRAIT:
			default:
				// Integrate the gyroscope data -> int(angularSpeed) = angle
				pitch += ((float)gyrData[0] * 180 / M_PI) * dt; // Angle around the X-axis
				roll -= ((float)gyrData[1] * 180 / M_PI) * dt;  // Angle around the Y-axis

				// Turning around the X axis results in a vector on the Y-axis
				pitchAcc = (float) (Math.atan2((float)accData[1], (float)accData[2]) * 180 / M_PI);
				pitch = pitch * ALPHA + pitchAcc * (1 - ALPHA);

				// Turning around the Y axis results in a vector on the X-axis
				rollAcc = (float) (Math.atan2((float)accData[0], (float)accData[2]) * 180 / M_PI);
				roll = roll * ALPHA + rollAcc * (1 - ALPHA);
				break; 

			} 
		}
	}

}
