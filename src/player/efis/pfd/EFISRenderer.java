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
import java.util.Iterator;

import player.ulib.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import player.gles20.GLText;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

enum AircraftModel 
{
	GENERIC,
	AZTEC,
	CRICRI,
	CRUZ,
	J160,
	LGEZ,
	PA28,
	RV6,
	RV7,
	RV8,
	T18,
	W10
}
  

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class EFISRenderer implements GLSurfaceView.Renderer 
{
	//private final static AircraftModel mAcraftModel = AircraftModel.GENERIC; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.AZTEC; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.CRICRI; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.CRUZ; //done 
	//private final static AircraftModel mAcraftModel = AircraftModel.J160; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.LGEZ; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.PA28; //done
	//private final static AircraftModel mAcraftModel = AircraftModel.RV6;
	//private final static AircraftModel mAcraftModel = AircraftModel.RV7;
	private final static AircraftModel mAcraftModel = AircraftModel.RV8; //done 
	//private final static AircraftModel mAcraftModel = AircraftModel.T18;
	//private final static AircraftModel mAcraftModel = AircraftModel.W10; //done
	
	private static final String TAG = "EFISRenderer"; //"MyGLRenderer";
	 
	private Triangle mTriangle; 
	private Square   mSquare;
	private Line     mLine;
	private PolyLine mPolyLine;
	private Polygon  mPolygon;

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	private final float[] mFdRotationMatrix = new float[16];  // for Flight Director
	private final float[] mRmiRotationMatrix = new float[16]; // for RMI / Compass Rose

	private float mAngle; 

	//b2 start
	// OpenGL    
	private  int pixW, pixH;         // Width & Height of window in pixels
	private  int pixW2, pixH2;       // Half Width & Height of window in pixels
	private  int pixM;               // The smallest dimension of pixH or pixM
	private  int pixM2;              // The smallest dimension of pixH2 or pixM2
	private  float zfloat;           // A Z to use for layering of ortho projected markings*/

	//b2
	// Artificial Horizon
	private float pitchInView;      // The degrees pitch to display above and below the lubber line
	private float pitch, roll;      // Pitch and roll in degrees
	private float pitchTranslation;	// Pitch amplified by 1/2 window pixels for use by glTranslate
	private float rollRotation;     // Roll converted for glRotate
	// Airspeed Indicator
	private float IASInView;     	// The indicated units to display above the center line
	//private int   IASValue;         // Indicated Airspeed
	private float IASValue;         // Indicated Airspeed
	private float IASTranslation;	// Value amplified by 1/2 window pixels for use by glTranslate
	// The following should be read from a calibration file by an init routine
	private int Vs0, Vs1, Vfe, Vno;	// Basic Vspeeds
	private int Vne, Va, Vy, Vx;    // More Vspeeds
	private int IASMaxDisp;         // The highest speed to show on tape
	// Altimeter
	private float MSLInView;        // The indicated units to display above the center line
	private int MSLValue;           // Altitude MSL
	private float MSLTranslation;   // Value amplified by 1/2 window pixels for use by glTranslate
	private float baroPressure;     // Barometric pressure in in-Hg
	// The following should be read from a calibration file by an init routine
	private int MSLMinDisp;         // The lowest altitude to show on tape
	private int MSLMaxDisp;         // The highest altitude to show on tape
	// VSI
	private float VSIInView;        // Vertical speed to display above the centerline
	private int   VSIValue;         // Vertical speed in feet/minute
	private float VSINeedleAngle;   // The angle to set the VSI needle
	//DI
	private float DIInView;        	// The indicated units to display above the center line
	private float DIValue;          // Altitude MSL
	private float SlipValue;        // was int
	private float BatteryPct;
	private float GForceValue;
	private float GSValue;
	private float ROTValue;
	private float DITranslation;   	// Value amplified by 1/2 window pixels for use by glTranslate
	// Geographic Coordinates
	private float LatValue;  		// Latitude
	private float LonValue;  		// Longitude
	//FPV - Flight Path Vector
	private float fpvX; 			// Flight Path Vector X
	private float fpvY; 			// Flight Path Vector Y
	//Flight Director
	float FDTranslation;// = -6 / pitchInView  * pixM2;  // command 6 deg pitch up
	float FDRotation;// = 20;  // command 20 deg roll
	boolean displayInfoPage;
	boolean displayFlightDirector;
	//RMI
	boolean displayRMI;
	float RMIRotation;
	
	boolean displayAirport;

	private boolean displayTerrain;
	private boolean displayTape;
	private boolean displayMirror;
	private boolean displayFPV;

	private boolean ServiceableDevice;	
	private boolean ServiceableAh;	
	private boolean ServiceableAlt;	
	private boolean ServiceableAsi;	
	private boolean ServiceableDi;
	private boolean Calibrating;
	private String  CalibratingMsg;

	// kepress location
	private float mX, mY; 

	//Demo Modes
	private boolean bDemoMode;
	private String sDemoMsg;

	//b2 end

	private GLText glText;                             // A GLText Instance
	private Context context;                           // Context (from Activity)

	public EFISRenderer(Context context)  
	{
		super();
		this.context = context;                         // Save Specified Context

		// Initialisation of variables
		pitchTranslation = rollRotation = 0; // default object translation and rotation

		IASTranslation = 0; // default IAS tape translation
		IASValue = 0; 		// The default to show if no IAS calls come in
		MSLTranslation = 0; // default MSL tape translation
		MSLValue = 0; 		// The default to show if no MSL calls come in
		VSIValue = 0; 		// The default vertical speed

		IASMaxDisp = 200; // 400;
		MSLMinDisp = -1000;
		MSLMaxDisp =  20000;

		VSIInView = 2000;

		//displayTerrain = true;
		//displayTape    = true;
		//displayMirror  = false;
		displayFPV = true;

		// Generic - Ultralight
		Vs0 = 30;  // Stall, flap extended
		Vs1 = 40;  // Stall, flap retracted
		Vx  = 50;  // Best angle climb
		Vy  = 60;  // Best rate climb 
		Vfe = 70;  // Flaps extension
		Va  = 80;  // Maneuvering
		Vno = 100; // Max structural cruise
		Vne = 120; // Never exceed
		
		// White Arc  Vs0 - Vfe
		// Green Arc  Vs1 - Vno
		// Yellow Arc Vno - Vne
		
		// "Conditional" compiles for various aircraft models
		if (mAcraftModel == AircraftModel.GENERIC) {
			// Ultralight
			Vs0 = 30;
			Vs1 = 40;
			Vx  = 50;
			Vy  = 60;
			Vfe = 60;
			Va  = 80;
			Vno = 100;
			Vne = 120;
		}

		if (mAcraftModel == AircraftModel.AZTEC) {
			// Colomban CriCri
			Vs0 = 61;
			Vs1 = 66;
			Vx  = 93; 
			Vy  = 102; 
			Vfe = 140;
			Va  = 129;
			Vno = 172;
			Vne = 216;
		}
		
		if (mAcraftModel == AircraftModel.CRICRI) {
			// Colomban CriCri
			Vs0 = 39;
			Vs1 = 49;
			Vx  = 56; // 62mph
			Vy  = 68; // 75mph
			Vfe = 70;
			Va  = 85;
			Vno = 100;
			Vne = 140;
		}
		
		if (mAcraftModel == AircraftModel.CRUZ) {
			// PiperSport Cruzer
			Vs0 = 32;  // Stall, flap extended
			Vs1 = 39;  // Stall, flap retracted
			Vx  = 56;  // Best angle climb
			Vy  = 62;  // Best rate climb 
			Vfe = 75;  // Flaps extension
			Va  = 88;  // Maneuvering
			Vno = 108; // Max structural cruise
			Vne = 138; // Never exceed
		}
		
		if (mAcraftModel == AircraftModel.J160) {
			// Jabiru J160-C
			Vs0 = 40;
			Vs1 = 45;
			Vx  = 65; 
			Vy  = 68; 
			Vfe = 80;
			Va  = 90;
			Vno = 108;
			Vne = 140;
		}
		
		
		if (mAcraftModel == AircraftModel.LGEZ) {
			// RV-8A
			Vs0 = 56;
			Vs1 = 56;
			Vx  = 72;
			Vy  = 90;
			Vfe = 85;
			Va  = 120;
			Vno = 161;
			Vne = 200;
		}
			
		if (mAcraftModel == AircraftModel.PA28) {
			// Piper PA28 Archer II
			Vs0 = 49;
			Vs1 = 55;
			Vx  = 64;
			Vy  = 76;
			Vfe = 102;
			Va  = 89;
			Vno = 125;
			Vne = 154;
		}
			
		if (mAcraftModel == AircraftModel.RV8) {
			// RV-8A
			Vs0 = 51;
			Vs1 = 56;
			Vx  = 72;
			Vy  = 90;
			Vfe = 85;
			Va  = 120;
			Vno = 165;
			Vne = 200;
		}

		if (mAcraftModel == AircraftModel.W10) {
			// Witttman Tailwind
			Vs0 = 48;  // Stall, flap extended
			Vs1 = 55;  // Stall, flap retracted
			Vx  = 90;  // Best angle climb - tbd
			Vy  = 104;  // Best rate climb 
			Vfe = 91;  // Flaps extension
			Va  = 130;  // Maneuvering
			Vno = 155; // Max structural cruise - tbd
			Vne = 174; // Never exceed
		}
		
		
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) 
	{
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		mTriangle = new Triangle();
		mSquare   = new Square();
		mLine     = new Line();
		mPolyLine = new PolyLine();
		mPolygon  = new Polygon();

		// Create the GLText
		glText = new GLText(context.getAssets());
	}


	//	float[] scratch = new float[160];

	@Override
	public void onDrawFrame(GL10 gl) 
	{
		float[] scratch1 = new float[16]; // moved to class scope
		float[] scratch2 = new float[16]; // moved to class scope
		float[] altMatrix = new float[16]; // moved to class scope
		float[] iasMatrix = new float[16]; // moved to class scope
		float[] fdMatrix = new float[16];  // moved to class scope
		float[] rmiMatrix = new float[16]; // moved to class scope

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set the camera position (View matrix)
		if (displayMirror)
			Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);  // Mirrored View
		else	
			Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);   // Normal View

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		// Create a rotation for the horizon
		Matrix.setRotateM(mRotationMatrix, 0, rollRotation, 0, 0, 1.0f);


		// Combine the rotation matrix with the projection and camera view
		// Note that the mMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		Matrix.multiplyMM(scratch1, 0, mMVPMatrix, 0, mRotationMatrix, 0);
		Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, mRotationMatrix, 0);


		//??Matrix.multiplyMM(altMatrix, 0, mMVPMatrix, 0, mRotationMatrix, 0); ??

		// Slide pitch to current value
		Matrix.translateM(scratch1, 0, 0, pitchTranslation, 0); // apply the pitch

		// Slide ALT to current value
		Matrix.translateM(altMatrix, 0, mMVPMatrix, 0, 0, -MSLTranslation, 0); // apply the altitude

		// Slide IAS to current value
		Matrix.translateM(iasMatrix, 0, mMVPMatrix, 0, 0, -IASTranslation, 0); // apply the altitude


		zfloat = 0;

		if (displayTerrain == true) renderTerrain(scratch1);
		renderRollMarkers(scratch2);
		renderPitchMarkers(scratch1);

		// FPV only means anything if we have speed and rate of climb, ie altitude
		// displayFPV = displayFPV && ServiceableAlt && ServiceableAsi;
		if (displayFPV) renderFPV(scratch1);  // must be on the same matrix as the Pitch
		if (displayAirport) renderAPT(scratch1);  // must be on the same matrix as the Pitch		

		if (displayFlightDirector) {  
			// Create a rotation for the Flight director
			Matrix.setRotateM(mFdRotationMatrix, 0, rollRotation + FDRotation, 0, 0, 1.0f);  // fd rotation
			Matrix.multiplyMM(fdMatrix, 0, mMVPMatrix, 0, mFdRotationMatrix, 0);
			// Slide FD to current value
			Matrix.translateM(fdMatrix, 0, 0, pitchTranslation - FDTranslation, 0); // apply the altitude
			renderFlightDirector(fdMatrix);
			renderWptSelValue(mMVPMatrix);
			renderAltSelValue(mMVPMatrix);
		}
		
		// This will have to be on its own page or portrait mode like Garmin
		// Leave it out for now
		/*
		if (displayRMI) {
			// Create a rotation for the RMI
			Matrix.setRotateM(mRmiRotationMatrix, 0, rollRotation + FDRotation, 0, 0, 1.0f);  // compass rose rotation
			Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);
			// Slide FD to current value
			Matrix.translateM(rmiMatrix, 0, 0, pitchTranslation - FDTranslation, 0); // apply the altitude
			renderCompassRose(rmiMatrix);  
		}
		*/
		
		renderFixedHorizonMarkers();

		if (displayTape == true) renderALTMarkers(altMatrix);  		
		renderFixedALTMarkers(mMVPMatrix); // this could be empty argument

		//if (displayTape == true) renderFixedVSIMarkers(mMVPMatrix);
		renderVSIMarkers(mMVPMatrix); 

		if (displayTape == true) renderASIMarkers(iasMatrix);
		renderFixedASIMarkers(mMVPMatrix); // this could be empty argument 
		renderFixedDIMarkers(mMVPMatrix);  
		renderTurnMarkers(mMVPMatrix);
		renderHDGValue(mMVPMatrix);       
		renderSlipBall(mMVPMatrix);		
		renderBatteryPct(mMVPMatrix);
		renderGForceValue(mMVPMatrix);

		if (displayInfoPage) { 
			renderAutoWptValue(mMVPMatrix);
			renderAutoWptDme(mMVPMatrix);		
			//renderAutoWptRlb(mMVPMatrix);
			renderAutoWptBrg(mMVPMatrix);
			renderMSGValue(mMVPMatrix);
		}

		if (!ServiceableDevice) renderUnserviceableDevice(mMVPMatrix);
		if (!ServiceableAh)  renderUnserviceableAh(mMVPMatrix);		
		if (!ServiceableAlt) renderUnserviceableAlt(mMVPMatrix);		
		if (!ServiceableAsi) renderUnserviceableAsi(mMVPMatrix);		
		if (!ServiceableDi)  renderUnserviceableDi(mMVPMatrix);
		if (Calibrating)  renderCalibrate(mMVPMatrix);

		if (bDemoMode) renderDemoMode(mMVPMatrix);
	}


	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		// Adjust the viewport based on geometry changes, such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates in the onDrawFrame() method
		//b2 Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		//Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 7); // - this apparently fixed for the Samsung?		

		//b2 start
		// Capture the window scaling for use by the rendering functions
		pixW = width; //(int) (width*0.5);
		pixH = height;
		pixW2 = pixW/2;
		pixH2 = pixH/2;

		if (pixW < pixH) pixM = pixW; 
		else pixM = pixH;
		pixM2 = pixM/2;

		// Set the window size specific scales, positions and sizes (nothing dynamic yet...)
		pitchInView = 25;//25.0f;	// degrees to display from horizon to top of viewport
		IASInView   = 40;//25.0f;    // IAS units to display from center to top of viewport
		MSLInView   = 300;//250.0f;	// IAS units to display from center to top of viewport

		//Matrix.frustumM(mProjectionMatrix, 0, -ratio*pixH2, ratio*pixH2, -pixH2, pixH2, 3f, 7f); // all the rest  
		Matrix.frustumM(mProjectionMatrix, 0, -ratio*pixH2, ratio*pixH2, -pixH2, pixH2, 2.99f, 7f); //hack for Samsung G2

		// Create the GLText
		// --debug glText = new GLText(context.getAssets()); - moved to onsurfacecreated

		// Load the font from file (set size + padding), creates the texture
		// NOTE: after a successful call to this the font is ready for rendering!
		//glText.load( "Roboto-Regular.ttf", 14, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
		glText.load( "square721_cn_bt_roman.ttf", pixM * 14 / 734  , 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

		// enable texture + alpha blending
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	}



	/**
	 * Utility method for compiling a OpenGL shader.
	 *
	 * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.</p>
	 *
	 * @param type - Vertex or fragment shader type.
	 * @param shaderCode - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode)
	{

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 *
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
	 * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
	 *
	 * If the operation is not successful, the check throws an error.
	 *
	 * @param glOperation - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) 
	{
		///*  bugbug
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
		//*/
	}

	/**
	 * Returns the rotation angle of the triangle shape (mTriangle).
	 *
	 * @return - A float representing the rotation angle.
	 */
	public float getAngle() 
	{
		return mAngle;
	}

	/**
	 * Sets the rotation angle of the triangle shape (mTriangle).
	 */
	public void setAngle(float angle) 
	{
		mAngle = angle;
	}


	private void renderCalibrate(float[] matrix)
	{
		String t = CalibratingMsg; //"Calibrating...";
		glText.begin( 1.0f, 0f, 0f, 1.0f, matrix ); // Red
		glText.setScale(5.0f); 							// 
		glText.drawCX(t, 0, 0 );            // Draw  String
		glText.end();                                    
	}


	// calibrating flag
	void setCalibrate(boolean cal, String msg)
	{
		Calibrating = cal;
		CalibratingMsg = msg;
	}


	private void renderDemoMode(float[] matrix)
	{
		String t = sDemoMsg; //"demo mode";
		glText.begin( 1.0f, 0f, 0f, 1.0f, matrix ); // Red
		glText.setScale(5.0f); 							// 
		glText.drawCX(t, 0, - pixM2 / 2);            // Draw  String
		glText.end();                                    
	}


	void setDemoMode(boolean demo, String msg)
	{
		bDemoMode = demo;
		sDemoMsg = msg;
	}


	//-------------------------------------------------------------------------
	// Flight Director
	//
	private void renderFlightDirector(float[] matrix)  
	{	
		int i;
		float z, pixPerDegree;

		z = zfloat;
		pixPerDegree = pixM2 / pitchInView;

		// fwd triangles
		mTriangle.SetWidth(1); 
		mTriangle.SetColor(1f, 0.5f, 1f, 1);  //purple'ish
		mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
				10.0f * pixPerDegree, -3.0f * pixPerDegree, z,
				12.0f * pixPerDegree, -2.0f * pixPerDegree, z);
		mTriangle.draw(matrix);
		mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
				-12.0f * pixPerDegree, -2.0f * pixPerDegree, z,
				-10.0f * pixPerDegree, -3.0f * pixPerDegree, z);
		mTriangle.draw(matrix);

		// rear triangles
		mTriangle.SetColor(0.6f, 0.3f, 0.6f, 1);  //purple'ish
		mTriangle.SetVerts(10.0f * pixPerDegree, -3.0f * pixPerDegree, z,
				12.0f * pixPerDegree, -2.0f * pixPerDegree, z,
				12.0f * pixPerDegree, -3.0f * pixPerDegree, z);
		mTriangle.draw(matrix);
		mTriangle.SetVerts(-12.0f * pixPerDegree, -2.0f * pixPerDegree, z,
				-10.0f * pixPerDegree, -3.0f * pixPerDegree, z,
				-12.0f * pixPerDegree,  -3.0f * pixPerDegree, z);
		mTriangle.draw(matrix);
	}

	public void setFlightDirector(boolean active, float pit, float rol)
	{
		displayFlightDirector = active;
		FDTranslation = -pit / pitchInView  * pixM2;  // command 6 deg pitch up
		FDRotation = -rol;// = 20;  // command 20 deg roll
	}


	//-------------------------------------------------------------------------
	// Attitude Indicator
	//
	private void renderFixedHorizonMarkers()  
	{
		int i;
		float z, pixPerDegree, sinI, cosI;
		float _sinI, _cosI;

		z = zfloat;
		pixPerDegree = pixM2 / pitchInView;

		// The lubber line - W style
		if (false) {
			mPolyLine.SetColor(1, 1, 0, 1);
			mPolyLine.SetWidth(6); 

			float[] vertPoly = {
					// in counterclockwise order:
					-6.0f * pixPerDegree,  0.0f, z,
					-4.0f * pixPerDegree,  0.0f, z,
					-2.0f * pixPerDegree, -2.0f * pixPerDegree, z,
					0.0f * pixPerDegree,  0.0f, z,
					2.0f * pixPerDegree, -2.0f * pixPerDegree, z,
					4.0f * pixPerDegree,  0.0f, z,
					6.0f * pixPerDegree,  0.0f, z
			};
			mPolyLine.VertexCount = 7;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(mMVPMatrix);
		}
		else {
			// The lubber line - Flight Director style
			// side lines
			int B2 = 3;
			mLine.SetWidth(2*B2);
			mLine.SetColor(1, 1, 0, 1);  // light yellow
			mLine.SetVerts(11.0f * pixPerDegree,  B2, z,
					15.0f * pixPerDegree,  B2, z);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(-11.0f * pixPerDegree,  B2, z,
					-15.0f * pixPerDegree, B2, z);
			mLine.draw(mMVPMatrix);

			mLine.SetColor(0.6f, 0.6f, 0, 1);  // dark yellow
			mLine.SetVerts(11.0f * pixPerDegree, -B2, z,
					15.0f * pixPerDegree, -B2, z);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(-11.0f * pixPerDegree, -B2, z,
					-15.0f * pixPerDegree, -B2, z);
			mLine.draw(mMVPMatrix);

			// outer triangles
			mTriangle.SetWidth(1); 
			mTriangle.SetColor(1, 1, 0, 1);
			mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
					6.0f * pixPerDegree, -3.0f * pixPerDegree, z,
					10.0f * pixPerDegree, -3.0f * pixPerDegree, z);
			mTriangle.draw(mMVPMatrix);
			mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
					-10.0f * pixPerDegree, -3.0f * pixPerDegree, z,
					-6.0f * pixPerDegree, -3.0f * pixPerDegree, z);
			mTriangle.draw(mMVPMatrix);

			// inner triangle
			mTriangle.SetColor(0.6f, 0.6f, 0, 1);
			mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
					4.0f * pixPerDegree, -3.0f * pixPerDegree, z,
					6.0f * pixPerDegree, -3.0f * pixPerDegree, z);
			mTriangle.draw(mMVPMatrix);
			mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
					-6.0f * pixPerDegree, -3.0f * pixPerDegree, z,
					-4.0f * pixPerDegree, -3.0f * pixPerDegree, z);
			mTriangle.draw(mMVPMatrix);

			// center triangle - optional 
			//mTriangle.SetVerts(0.0f * pixPerDegree,  0.0f * pixPerDegree, z,
			//		-4.0f * pixPerDegree, -2.0f * pixPerDegree, z,
			//		 4.0f * pixPerDegree, -2.0f * pixPerDegree, z);
			//mTriangle.draw(mMVPMatrix);
		}

		// The fixed roll marker (roll circle marker radius is 15 degrees of pitch, with fixed markers on the outside)
		mTriangle.SetColor(0.9f, 0.9f, 0.0f, 1); //yellow
		mTriangle.SetVerts(0.035f * pixW2, 16.5f * pixPerDegree, z,
											-0.035f * pixW2, 16.5f * pixPerDegree, z,
											 0.0f, 15f * pixPerDegree, z);
		mTriangle.draw(mMVPMatrix);

		mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
		mLine.SetWidth(2); 
		// The lines
		for (i = 10; i <= 30; i=i+10) {
			sinI = UTrig.isin( i );  
			cosI = UTrig.icos( i );
			mLine.SetVerts(
					15 * pixPerDegree * sinI, 15 * pixPerDegree * cosI, z,
					16 * pixPerDegree * sinI, 16 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(
					15 * pixPerDegree * -sinI, 15 * pixPerDegree * cosI, z,
					16 * pixPerDegree * -sinI, 16 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
		}
		// 45 - even though it is only one number, leave the loop 
		// for consitency and possible changes
		for (i = 45; i <= 60; i=i+15) { 
			sinI = UTrig.isin( i );
			cosI = UTrig.icos( i );
			// The lines
			mLine.SetVerts(
					15 * pixPerDegree * sinI, 15 * pixPerDegree * cosI, z,
					16 * pixPerDegree * sinI, 16 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(
					15 * pixPerDegree * -sinI, 15 * pixPerDegree * cosI, z,
					16 * pixPerDegree * -sinI, 16 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
		}
		// 30 and 60
		for (i = 30; i <=60; i=i+30) { 
			sinI = UTrig.isin( i );
			cosI = UTrig.icos( i );

			mLine.SetVerts(
					15 * pixPerDegree * sinI, 15 * pixPerDegree * cosI, z,
					17 * pixPerDegree * sinI, 17 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(
					15 * pixPerDegree * -sinI, 15 * pixPerDegree * cosI, z,
					17 * pixPerDegree * -sinI, 17 * pixPerDegree * cosI, z
					);
			mLine.draw(mMVPMatrix);
		}

		// The arc
		_sinI = 0; _cosI = 1;
		for (i = 10; i <=60; i=i+5) { 
			sinI = UTrig.isin( i );  
			cosI = UTrig.icos( i );

			mLine.SetVerts(
					15 * pixPerDegree * _sinI, 15 * pixPerDegree * _cosI, z,
					15 * pixPerDegree *  sinI, 15 * pixPerDegree *  cosI, z
					);
			mLine.draw(mMVPMatrix);
			mLine.SetVerts(
					15 * pixPerDegree * -_sinI, 15 * pixPerDegree * _cosI, z,
					15 * pixPerDegree *  -sinI, 15 * pixPerDegree *  cosI, z
					);
			mLine.draw(mMVPMatrix);
			_sinI = sinI; _cosI = cosI;
		}
		
	} //renderFixedHorizonMarkers

	private void renderRollMarkers(float[] matrix)
	{
		float z, pixPerDegree;

		z = zfloat;
		pixPerDegree = pixM2 / pitchInView;							// Put the markers in open space at zero pitch

		mTriangle.SetColor(0.9f, 0.9f, 0.9f, 0);
		mTriangle.SetVerts(-0.02f  * pixW2, 14 * pixPerDegree, z,
				0.02f * pixW2, 14 * pixPerDegree, z,
				0.0f, 15 * pixPerDegree, z);
		mTriangle.draw(matrix);
	}

	private void renderPitchMarkers(float[] matrix)
	{
		int i;
		float innerTic, outerTic, z, pixPerDegree, iPix;

		pixPerDegree = pixM2 / pitchInView;
		z = zfloat;

		innerTic = 0.10f * pixW2; 
		outerTic = 0.13f * pixW2;


		//for (i = 270; i > 0; i=i-10) {
		for (i = 90 ; i > 0; i=i-10) {      
			iPix = (float) i * pixPerDegree;
			String t = Integer.toString(i); 
			{
				mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white 
				mPolyLine.SetWidth(2); 
				float[] vertPoly = {
						// in counterclockwise order:
						-innerTic, iPix, z,
						-outerTic, iPix, z,
						-outerTic, iPix - 0.03f * pixW2, z 
				};
				mPolyLine.VertexCount = 3;  	
				mPolyLine.SetVerts(vertPoly); 
				mPolyLine.draw(matrix); 
			}
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
			glText.setScale(2);
			glText.drawC(t, -0.2f * pixW2, iPix + glText.getCharHeight() / 2);           
			glText.end();                                   

			{
				mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
				mPolyLine.SetWidth(2); 
				float[] vertPoly = {
						// in counterclockwise order:
						0.1f * pixW2, iPix, z,
						outerTic, iPix, z,
						outerTic, iPix - 0.03f * pixW2, z
				};
				mPolyLine.VertexCount = 3;  	
				mPolyLine.SetVerts(vertPoly); 
				mPolyLine.draw(matrix);
			}
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
			glText.drawC(t, 0.2f * pixW2, iPix + glText.getCharHeight() / 2);             
			glText.end();                                   

		}

		mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
		mLine.SetWidth(2); 
		for (i = 9; i >= 6; i=i-1) {
			iPix = (float) i * pixPerDegree;
			mLine.SetVerts(-0.03f  * pixW2, iPix, z, 
					0.03f  * pixW2, iPix, z);
			mLine.draw(matrix);
		}

		mLine.SetVerts( -0.1f  * pixW2, 5.0f * pixPerDegree, z,
				0.1f  * pixW2, 5.0f * pixPerDegree, z);
		mLine.draw(matrix);

		for (i = 4; i >=1; i=i-1) {
			iPix = (float) i * pixPerDegree;

			mLine.SetVerts(-0.03f  * pixW2, iPix, z,
					0.03f  * pixW2, iPix, z);
			mLine.draw(matrix);
		}

		// horizon line - longer and thicker
		mLine.SetWidth(4);
		mLine.SetVerts(-0.95f  * pixW2, 0.0f, z,
				0.95f  * pixW2, 0.0f, z);
		mLine.draw(matrix);

		mLine.SetWidth(2);
		for (i = -1; i >=- 4; i = i - 1) {
			iPix = (float) i * pixPerDegree;
			mLine.SetVerts(-0.03f  * pixW2, iPix, z,
					0.03f  * pixW2, iPix, z);
			mLine.draw(matrix);
		}


		mLine.SetVerts(-0.1f  * pixW2, -5.0f * pixPerDegree, z,
				0.1f  * pixW2, -5.0f * pixPerDegree, z);
		mLine.draw(matrix);

		for (i = -6; i>=-9; i=i-1) {
			iPix = (float) i * pixPerDegree;
			mLine.SetVerts(-0.03f  * pixW2, iPix, z,
					0.03f  * pixW2, iPix, z);
			mLine.draw(matrix);
		}

		//for (i = -10; i>=-270; i=i-10) { 
		for (i = -10; i >= -90; i=i-10) {
			iPix = (float) i * pixPerDegree;
			String t = Integer.toString(i);

			{
				mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
				mPolyLine.SetWidth(2); 
				float[] vertPoly = {
						// in counterclockwise order:
						-0.10f * pixW2, iPix, z,
						-0.13f * pixW2, iPix, z,
						-0.13f * pixW2, iPix + 0.03f * pixW2, z
				};

				mPolyLine.VertexCount = 3;  	 
				mPolyLine.SetVerts(vertPoly);
				mPolyLine.draw(matrix);
			}
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
			glText.drawC(t, -0.2f * pixW2, iPix + glText.getCharHeight() / 2);           
			glText.end();                                   

			{
				mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
				mPolyLine.SetWidth(2); 
				float[] vertPoly = {
						0.10f * pixW2, iPix, z,
						0.13f * pixW2, iPix, z,
						0.13f * pixW2, iPix  + 0.03f * pixW2, z
				};

				mPolyLine.VertexCount = 3;  	
				mPolyLine.SetVerts(vertPoly);
				mPolyLine.draw(matrix);
			}
			glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
			glText.drawC(t, 0.2f * pixW2, iPix + glText.getCharHeight() / 2);            
			glText.end();                                   
		}
	}

	private void renderTerrain(float[] matrix)
	{
		float pixPitchViewMultiplier, pixOverWidth, z;

		/*!
			The ModelView has units of +/- 1 about the center.  In order to keep the gyro edges outside of the edges of
			the ViewPort, it is drawn wide to deal with affect of the aspect ratio scaling and the corners during roll

			The pitch range in degrees to be viewed must fit the ModelView units of 1. To accommodate this, the gyro must
			be ovesized, hence the multiplier 90/ pitchInView.
		 */

		pixPitchViewMultiplier = 90.0f / pitchInView * pixH2;
		pixOverWidth = pixW2 * 1.42f;
		z = zfloat;

		// Earth
		// Level to -90 pitch
		mSquare.SetColor(64f/255f, 50f/255f, 25f/255f, 0); //brown
		mSquare.SetWidth(1);
		{
			float[] squarePoly = {
					-pixOverWidth, -1.0f * pixPitchViewMultiplier, z,
					pixOverWidth, -1.0f * pixPitchViewMultiplier, z,
					pixOverWidth, 0.0f + 0.0f/pixH2, z,
					-pixOverWidth, 0.0f + 0.0f/pixH2, z
					/*pixOverWidth, 0.0f + 1.0f/pixH2, z,
					-pixOverWidth, 0.0f + 1.0f/pixH2, z*/
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}


		//  -90 pitch to -180 pitch 
		//mSquare.SetColor(194f/255f, 150f/255f, 75f/255f, 0); //brown
		mSquare.SetColor(64f/255f, 50f/255f, 25f/255f, 0); //brown
		mSquare.SetWidth(1);
		{
			float[] squarePoly = {
					-pixOverWidth, -2.0f * pixPitchViewMultiplier, z,
					pixOverWidth,  -2.0f * pixPitchViewMultiplier, z,
					pixOverWidth,  -1.0f * pixPitchViewMultiplier, z,
					-pixOverWidth, -1.0f * pixPitchViewMultiplier, z
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		} 

		/* 
   		b2 due to flip no longer needed
		glBegin(GL_POLYGON);
		qglColor( QColor( 194, 150, 75 ) );
		glVertex3f( -pixOverWidth, -2.0 * pixPitchViewMultiplier, z);
		glVertex3f( pixOverWidth, -2.0 * pixPitchViewMultiplier, z);
		qglColor( QColor( 64, 50, 25 ) );
		glVertex3f( pixOverWidth, -1.0 * pixPitchViewMultiplier, z);
		glVertex3f( -pixOverWidth, -1.0 * pixPitchViewMultiplier, z);
		glEnd();
		 */

		// Sky
		// Level to 90 pitch
		mSquare.SetColor(0f, 0f, 0.9f, 0); //blue
		mSquare.SetWidth(1);
		{
			float[] squarePoly = {
					-pixOverWidth, 0.0f + 1.0f/pixH2, z,
					pixOverWidth,  0.0f + 1.0f/pixH2, z,
					pixOverWidth,  1.0f * pixPitchViewMultiplier, z,
					-pixOverWidth, 1.0f * pixPitchViewMultiplier, z
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}


		// 90 pitch to 180 pitch
		mSquare.SetColor(0f, 0f, 0.9f, 0); //blue
		mSquare.SetWidth(1);
		{
			float[] squarePoly = {
					-pixOverWidth,  1.0f * pixPitchViewMultiplier, z,
					pixOverWidth,  1.0f * pixPitchViewMultiplier, z,
					pixOverWidth,  2.0f * pixPitchViewMultiplier, z,
					-pixOverWidth,  2.0f * pixPitchViewMultiplier, z
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}


		/*
		   b2 due to flip no longer needed
		  //Once more to fill the ViewPort when pitch approaches -180
			glBegin(GL_POLYGON);
			qglColor( QColor( 0, 0, 0 ) );
			glVertex3f( -pixOverWidth, -3.0 * pixPitchViewMultiplier, z);
			glVertex3f( pixOverWidth, -3.0 * pixPitchViewMultiplier, z);
			qglColor( QColor( 0, 0, 255 ) );
			glVertex3f( pixOverWidth, -2.0 * pixPitchViewMultiplier, z);
			glVertex3f( -pixOverWidth,  -2.0 * pixPitchViewMultiplier, z);
			glEnd();
		 */


	}


	/*!
	Set the pitch
	 */
	public void setPitch( float degrees )
	{
		pitch = (float) -degrees;

		pitchTranslation = pitch / pitchInView * pixH2;
		//updateGL();
	}

	/*!
	Set the roll angle
	 */
	void setRoll( float degrees )
	{
		//roll = (GLfloat)(degrees % 360);
		roll = (float) degrees;
		rollRotation = roll;
		//updateGL();
	}


	//-------------------------------------------------------------------------
	// Altimeter Indicator
	//

	void renderFixedALTMarkers(float[] matrix)
	{
		float z;
		String t;

		z = zfloat;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixM2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if required.

		// Altimeter Display

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(1.5f); 
		glText.end();                                

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
					1.15f * pixM2, -glText.getCharHeight(), z,//+0.1f,
					1.15f * pixM2,  glText.getCharHeight(), z,//+0.1f,
					0.80f * pixM2,  glText.getCharHeight(), z,//+0.1f,
					0.80f * pixM2, -glText.getCharHeight(), z,//+0.1f
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}

		int MSLValue =  (int) (Math.round((float) this.MSLValue / 10) * 10);  // round to 10
		// draw the tape text in mixed sizes
		// to clearly show the thousands
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		t = Integer.toString(MSLValue / 1000);
		float margin;

		// draw the thousands digits larger
		glText.setScale(3f);  //2.5 
		if (MSLValue > 1000) glText.draw(t, 0.84f * pixM2, -glText.getCharHeight() / 2);

		if (MSLValue < 10000) margin = 0.6f*glText.getCharWidthMax(); // because of the differing sizes
		else margin = 1.1f*glText.getCharWidthMax();                 	// we have to deal with the margin ourselves  

		//draw the hundreds digits smaller
		t = String.format("%03.0f",(float) MSLValue % 1000);
		glText.setScale(2.5f); // was 2.5 
		glText.draw(t, 0.84f * pixM2 + margin, -glText.getCharHeight() / 2);            

		glText.setScale(1.5f); 
		glText.end();                                   


		mTriangle.SetColor(0.0f, 0.0f, 0.0f, 1);  //black
		mTriangle.SetVerts(0.80f * pixM2, glText.getCharHeight()/2, z,//+.1,
				0.75f * pixM2, 0.0f, z,
				0.80f * pixM2, -glText.getCharHeight()/2, z///+.1
				);
		mTriangle.draw(mMVPMatrix);
		{
			mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
			mPolyLine.SetWidth(2); 
			float[] vertPoly = {
					1.15f * pixM2, -glText.getCharHeight(), z,//+.2);
					1.15f * pixM2,  glText.getCharHeight(), z,//+.2);
					0.80f * pixM2,  glText.getCharHeight(), z,//+.2);
					0.80f * pixM2,  glText.getCharHeight()/2, z,//+.2);
					0.75f * pixM2, 0.0f, z,//+.2);
					0.80f * pixM2, -glText.getCharHeight()/2, z,//+.2);
					0.80f * pixM2, -glText.getCharHeight(), z,//+.2);
					1.15f * pixM2, -glText.getCharHeight(), z //+.2);
			};

			mPolyLine.VertexCount = 8;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(matrix);
		}
	}


	void renderALTMarkers(float[] matrix)
	{
		float tapeShade = 0.6f; // for grey
		int i, j;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		pixPerUnit = pixM2/MSLInView ;
		z = zfloat;

		innerTic = 0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		midTic   = 0.75f * pixM2;
		outerTic = 0.80f * pixM2;

		// The numbers & tics for the tape
		for (i = MSLMaxDisp; i >=MSLMinDisp; i=i-100) {
			// Ugly hack but is does significantly improve performance.
			if (i > MSLValue+1.00*MSLInView) continue;
			if (i < MSLValue-1.50*MSLInView) continue;

			iPix = (float) i * pixPerUnit;

			mLine.SetColor(tapeShade, tapeShade, tapeShade, 1);  // grey
			mLine.SetWidth(3); 
			mLine.SetVerts(
					innerTic, iPix, z,
					outerTic, iPix, z 
					);
			mLine.draw(matrix);

			// draw the tape text in mixed sizes 
			// to clearly show the thousands
			glText.begin(tapeShade, tapeShade, tapeShade, 1.0f, matrix ); // grey
			String t = Integer.toString(i/1000);
			float margin;

			// draw the thousands digits larger
			glText.setScale(2.5f); 
			if (i >= 1000) glText.draw(t, outerTic, iPix - glText.getCharHeight() / 2);

			if (i < 10000) margin = 0.6f*glText.getCharWidthMax();  // because of the differing sizes
			else margin = 1.1f*glText.getCharWidthMax();            // we have to deal with the margin ourselves  

			//draw the hundreds digits smaller
			t = String.format("%03.0f",(float) i % 1000);
			glText.setScale(1.5f); // was 1.5 
			glText.draw(t, outerTic + margin, iPix - glText.getCharHeight() / 2);            
			glText.end();                                   

			for (j = i + 20; j < i+90; j=j+20) {
				iPix = (float) j * pixPerUnit;
				mLine.SetWidth(2); 
				mLine.SetVerts(
						innerTic, iPix, z,
						midTic, iPix, z
						);
				mLine.draw(matrix);
			}
		}

		// The vertical scale bar
		mLine.SetVerts(
				innerTic,  MSLMinDisp, z,                     
				innerTic, (MSLMaxDisp + 100) * pixPerUnit, z		
				);
		mLine.draw(matrix);
	}

	//
	//Set the altimeter - ft
	//
	void setALT(int feet)
	{
		MSLValue = feet;
		MSLTranslation = MSLValue / MSLInView  * pixH2;
	}

	//
	//Set the barometric pressure
	//
	void setBaro(float milliBar)
	{
		baroPressure = milliBar;
	}


	void renderFixedVSIMarkers(float[] matrix)
	{
		int i, j;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		pixPerUnit =  0.75f* pixM2 / VSIInView ;
		z = zfloat;

		innerTic = 1.20f * pixM2;	// inner & outer are relative to the vertical scale line
		midTic   = 1.23f * pixM2;
		outerTic = 1.26f * pixM2;

		for (i = 0; i <= VSIInView; i=i+500) {

			iPix = (float) i * pixPerUnit;

			String t = Float.toString((float)i/1000);

			mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
			mLine.SetWidth(2); 
			mLine.SetVerts(
					innerTic, iPix, z,
					outerTic, iPix, z
					);
			mLine.draw(matrix);

			glText.begin( 0.5f, 0.5f, 0.5f, 1.0f, matrix ); // white
			glText.setScale(1.5f); // was 1.2 
			glText.draw(t, outerTic + glText.getCharWidthMax()/2, iPix - glText.getCharHeight() / 2);   // Draw String
			glText.end();                                    

			if (i < VSIInView) { 
				for (j = i + 100; j < i+500; j=j+100) {
					iPix = (float) j * pixPerUnit;
					mLine.SetVerts(
							innerTic, iPix, z,
							midTic, iPix, z
							);
					mLine.draw(matrix);
				}
			}
		}

		// The vertical scale bar
		mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
		mLine.SetWidth(2); 
		mLine.SetVerts(
				innerTic,  -VSIInView, z,                     
				innerTic, (+VSIInView + 100) * pixPerUnit, z		
				);
		mLine.draw(matrix);
	}


	//-------------------------------------------------------------------------
	// VSI Indicator
	//
	void renderVSIMarkers(float[] matrix)
	{
		int i, j;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		pixPerUnit =  0.75f* pixM2 / VSIInView ;
		z = zfloat;

		innerTic = 0.64f * pixM2;	// inner & outer are relative to the vertical scale line
		outerTic = 0.70f * pixM2;
		midTic   = 0.67f * pixM2;


		// VSI box
		for (i = -2; i<=2; i += 1) { 
			mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
			mLine.SetWidth(4);
			mLine.SetVerts(
					0.64f*pixM2, i*1000*pixPerUnit, z,                     
					0.70f*pixM2, i*1000*pixPerUnit, z		
					);
			mLine.draw(matrix);

			if (i != 0) {
				String t = Integer.toString(Math.abs(i));
				glText.begin( 0.75f, 0.75f, 0.75f, 1.0f, matrix ); // light grey
				glText.setScale(1.75f);  
				glText.draw(t, innerTic - 1.5f * glText.getLength(t), i*1000*pixPerUnit - glText.getCharHeight() / 2);           
				glText.end();                                   
			}
		}

		// vertical speed  bar
		mLine.SetColor(0, 0.8f, 0, 1); // green
		mLine.SetWidth(16);
		mLine.SetVerts(
				0.67f*pixM2, 0.0f*pixH2, z,                     
				0.67f*pixM2, VSIValue*pixPerUnit, z		
				);
		mLine.draw(matrix);
	}


	//
	//Set the VSI - ft
	//
	void setVSI(int fpm)
	{
		VSIValue = fpm;
	}



	//-------------------------------------------------------------------------
	// Airspeed Indicator
	//
	void renderFixedASIMarkers(float[] matrix)
	{
		float z;
		String t;

		z = zfloat;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixH2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if reqd.

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(1.5f); // was 1.2
		glText.end();                                    

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
					-1.05f * pixM2, -glText.getCharHeight(), z,//+0.1f,
					-1.05f * pixM2,  glText.getCharHeight(), z,//+0.1f,
					-0.80f * pixM2,  glText.getCharHeight(), z,//+0.1f,
					-0.80f * pixM2, -glText.getCharHeight(), z,//+0.1f
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}

		t = Integer.toString(Math.round(IASValue));
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(3.0f); 							// was 2.5
		glText.drawC(t, -0.85f * pixM2, glText.getCharHeight()/2 );            
		glText.setScale(1.5f); 							// was 1.2
		glText.end();                                    

		mTriangle.SetColor(0.0f, 0.0f, 0.0f, 1);  //black
		mTriangle.SetVerts(
				-0.80f * pixM2, glText.getCharHeight()/2, z,//+.1,
				-0.75f * pixM2, 0.0f, z,
				-0.80f * pixM2, -glText.getCharHeight()/2, z///+.1
				);
		mTriangle.draw(mMVPMatrix);
		{
			mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
			mPolyLine.SetWidth(2); 
			float[] vertPoly = {
					-1.05f * pixM2, -glText.getCharHeight(), z,//+.2);
					-1.05f * pixM2,  glText.getCharHeight(), z,//+.2);
					-0.80f * pixM2,  glText.getCharHeight(), z,//+.2);
					-0.80f * pixM2,  glText.getCharHeight()/2, z,//+.2);
					-0.75f * pixM2, 0.0f, z,//+.2);
					-0.80f * pixM2, -glText.getCharHeight()/2, z,//+.2);
					-0.80f * pixM2, -glText.getCharHeight(), z,//+.2);
					-1.05f * pixM2, -glText.getCharHeight(), z //+.2);
			};

			mPolyLine.VertexCount = 8;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(matrix);
		}
	}


	void renderASIMarkers(float[] matrix)
	{
		float tapeShade = 0.6f; // for grey
		int i, j;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		//glLineWidth( 2 );
		z = zfloat;
		pixPerUnit = pixH2/IASInView ;

		innerTic = -0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		outerTic = -0.80f * pixM2;
		midTic   = -0.77f * pixM2;

		// The numbers & tics for the tape
		for (i = IASMaxDisp; i >= 0; i = i - 10) {
			// Ugly hack but helps with performance - Not needed and appears to case a bug 
			//if (i < IASValue - IASInView) continue;
			//if (i > IASValue + IASInView) continue; 

			iPix = (float) i * pixPerUnit;
			String t = Integer.toString(i);

			mLine.SetColor(tapeShade, tapeShade, tapeShade, 1);  // grey
			mLine.SetWidth(2); 
			mLine.SetVerts(
					innerTic, iPix, z,
					outerTic, iPix, z
					);
			mLine.draw(matrix);

			glText.begin(tapeShade, tapeShade, tapeShade, 1.0f, matrix ); // grey
			glText.setScale(2f); // was 1.5 
			glText.draw(t, outerTic - 1.5f * glText.getLength(t), iPix - glText.getCharHeight() / 2);            
			glText.end();                                    

			for (j = i + 2; j < i+9; j=j+2) {
				iPix = (float) j * pixPerUnit;
				mLine.SetVerts(
						innerTic, iPix, z,
						midTic, iPix, z
						);
				mLine.draw(matrix);
			}
		}

		// The vertical scale bar
		mLine.SetVerts(
				innerTic,  0 , z,  // IASMinDisp - no longer used, set to 0                     
				innerTic, (IASMaxDisp + 100) * pixPerUnit, z		
				);
		mLine.draw(matrix);

		// For monochrome display (displayTerrain false) do not use any color
		if (displayTerrain) {
			//
			// Special Vspeed markers
			//
			String t;

			// Vx
			iPix = (float) Vx * pixPerUnit;
			mLine.SetColor(0, 0, 0, 1);  // black
			mLine.SetVerts(outerTic, iPix, z,// - 0.1f,
					midTic, iPix, z// - 0.1f
					);
			mLine.draw(matrix);
			t = "Vx";
			glText.begin( 0.0f, 0.6f, 0.0f, 1.0f, matrix ); // Green
			glText.setScale(1.5f); 							// was 1.2
			glText.drawC(t, midTic, iPix);            
			glText.end();                                    

			// Vy
			iPix = (float) Vy * pixPerUnit; 
			mLine.SetColor(0, 0, 0, 1);  // black
			mLine.SetVerts(outerTic, iPix, z,// - 0.1f,
					midTic, iPix, z// - 0.1f
					);
			mLine.draw(matrix);
			t = "Vy";
			glText.begin( 0.0f, 0.6f, 0.0f, 1.0f, matrix ); // Green
			glText.setScale(1.5f); 							// was 1.2
			glText.drawC(t, midTic, iPix);            
			glText.end();                                    

			// Va
			iPix = (float) Va * pixPerUnit;
			mLine.SetColor(0, 0, 0, 1);  // black
			mLine.SetVerts(outerTic, iPix, z,// - 0.1f,
					midTic, iPix, z// - 0.1f
					);
			mLine.draw(matrix);
			t = "Va";
			glText.begin( 0.0f, 0.6f, 0.0f, 1.0f, matrix ); // Green
			glText.setScale(1.5f); 							// was 1.2
			glText.drawC(t, midTic, iPix);            
			glText.end();                                    

			// Tape markings for V speeds
			midTic = -0.75f * pixM2;													// Put tape under the tics, w/ VsO-Vfe bar narrower than minor tics
			mSquare.SetColor(0, 0.5f, 0, 1);  // dark green;
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vs1 * pixPerUnit, z, //-.2);
						innerTic, (float) Vno * pixPerUnit, z, //-.2);
						midTic, (float) Vno * pixPerUnit, z, //-.2);
						midTic, (float) Vs1 * pixPerUnit, z //-.2);
				};
				mSquare.SetVerts(squarePoly);
				mSquare.draw(matrix);
			}
			mSquare.SetColor(0.5f, 0.5f, 0.5f, 1);  // white
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vs0 * pixPerUnit, z, //-.2);
						innerTic, (float) Vfe * pixPerUnit, z, //-.2);
						midTic, (float) Vfe * pixPerUnit, z, //-.2);
						midTic, (float) Vs0 * pixPerUnit, z //-.2);
				};
				mSquare.SetVerts(squarePoly);
				mSquare.draw(matrix);
			}

			mSquare.SetColor(0.9f, 0.9f, 0, 1);  // yellow
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vno * pixPerUnit, z, //-.2);
						innerTic, (float) Vne * pixPerUnit, z, //-.2);
						midTic, (float) Vne * pixPerUnit, z, //-.2);
						midTic, (float) Vno * pixPerUnit, z //-.2);
				};
				mSquare.SetVerts(squarePoly);
				mSquare.draw(matrix);
			} 

			// Vne
			mSquare.SetColor(0.9f, 0, 0, 1);  // red
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vne * pixPerUnit, z, //-.2);
						innerTic, (float) (Vne + 1) * pixPerUnit, z, //-.2);
						outerTic, (float) (Vne + 1) * pixPerUnit, z, //-.2);
						outerTic, (float) Vne * pixPerUnit, z //-.2);
				};
				mSquare.SetVerts(squarePoly);
				mSquare.draw(matrix);

				float[] squarePoly2 = { 
						innerTic, (float) Vne * pixPerUnit, z, //-.2);
						innerTic, (float) (IASMaxDisp + 10) * pixPerUnit, z, //-.2);
						midTic, (float) (IASMaxDisp + 10) * pixPerUnit, z, //-.2);
						midTic, (float) Vne * pixPerUnit, z //-.2);
				};
				mSquare.SetVerts(squarePoly2);
				mSquare.draw(matrix);
			}
		}
	}

	//
	//Set the IAS indicator
	//
	void setIAS(float value )  
	{
		IASValue = value;
		IASTranslation = IASValue / IASInView  * pixH2;
	}

	//-------------------------------------------------------------------------
	// Direction Indicator
	//   Also contains the slip and turn indications
	//
	void renderFixedDIMarkers(float[] matrix)
	{
		float z;
		String t;

		z = zfloat;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixH2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if reqd.

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(1.5f); // was 1.2
		glText.end();                                    

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
					0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,
					0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
					-0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
					-0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix); 
		}


		/*mTriangle.SetColor(0.9f, 0.9f, 0.9f, 0);  //white
		mTriangle.SetVerts(
				-0.80f * pixH2, glText.getCharHeight()/2, z,//+.1,
				-0.75f * pixH2, 0.0f, z,
				-0.80f * pixH2, -glText.getCharHeight()/2, z///+.1
				);
		mTriangle.draw(mMVPMatrix);*/

		{
			mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
			mPolyLine.SetWidth(2); 
			float[] vertPoly = {
					0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
					-0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
					-0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
					0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,
					0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,

					// for some reason this causes a crash on restart if there are not 8 vertexes
					// most probably a a bug in PolyLine
					-0.175f * pixH2, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
					-0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
					0.175f * pixH2, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,

			};
			mPolyLine.VertexCount = 8;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(matrix);
		} 
	}


	//---------------------------------------------------------------------------
	// EFIS serviceability ... aka the Red X's
	// 
	void renderUnserviceableDevice(float[] matrix)
	{
		renderUnserviceableAh(matrix);
		renderUnserviceableDi(matrix);
		renderUnserviceableAlt(matrix);
		renderUnserviceableAsi(matrix);
	} 

	void renderUnserviceableAh(float[] matrix)
	{
		float z, pixPerUnit;
		z = zfloat;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(20);

		mLine.SetVerts(
				-0.7f * pixM2,  0.8f * pixH2, z,                     
				0.7f * pixH2, -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
		mLine.SetVerts(
				0.7f * pixM2,    0.8f * pixH2, z,                     
				-0.7f *pixM2,  -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
	} 

	void renderUnserviceableDi(float[] matrix)
	{
		float z, pixPerUnit;
		z = zfloat;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(8);

		mLine.SetVerts(
				-0.7f * pixM2,  0.95f * pixH2, z,                     
				0.7f * pixH2,  0.85f * pixH2, z                     
				);
		mLine.draw(matrix);
		mLine.SetVerts(
				0.7f * pixM2,    0.95f * pixH2, z,                     
				-0.7f *pixM2,   0.85f * pixH2, z                     
				);
		mLine.draw(matrix);
	} 


	void renderUnserviceableAlt(float[] matrix)
	{
		float z, pixPerUnit;
		z = zfloat;

		//innerTic = 0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		//outerTic = 0.80f * pixM2;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(8);

		mLine.SetVerts(
				0.7f * pixM2,   0.8f * pixH2, z,                     
				1.0f * pixM2, -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
		mLine.SetVerts(
				1.0f * pixM2,  0.8f * pixH2, z,                     
				0.7f * pixM2, -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
	} 

	void renderUnserviceableAsi(float[] matrix)
	{
		float z, pixPerUnit;
		z = zfloat;

		//innerTic = 0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		//outerTic = 0.80f * pixM2;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(8);

		mLine.SetVerts(
				-0.7f * pixM2,  0.8f * pixH2, z,                     
				-1.0f * pixM2, -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
		mLine.SetVerts(
				-1.0f * pixM2,  0.8f * pixH2, z,                     
				-0.7f * pixM2, -0.8f * pixH2, z                     
				);
		mLine.draw(matrix);
	} 


	// Overall PFD serviceability
	void setServiceableDevice()
	{
		ServiceableDevice = true;	
	}

	void setUnServiceableDevice()
	{
		ServiceableDevice = false;	
	}

	// Artificial Horizon serviceability
	void setServiceableAh()
	{
		ServiceableAh = true;	
	}

	void setUnServiceableAh()
	{
		ServiceableAh = false;	
	}

	// Altimeter serviceability
	void setServiceableAlt()
	{
		ServiceableAlt = true;	
	}

	void setUnServiceableAlt()
	{
		ServiceableAlt = false;	
	}


	// Airspeed Indicator serviceability
	void setServiceableAsi()
	{
		ServiceableAsi = true;	
	}

	void setUnServiceableAsi()
	{
		ServiceableAsi = false;	
	}

	// Direction Indicaotor serviceability
	void setServiceableDi()
	{
		ServiceableDi = true;	
	}

	void setUnServiceableDi()
	{
		ServiceableDi = false;	
	}

	// Display control for FPV
	void setDisplayFPV(boolean display)
	{
		displayFPV = display;
	}

	// Display control for Airports
	void setDisplayAirport(boolean display)
	{
		displayAirport = display;
	}



	void renderHDGValue(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		//int rd = (int) 5 * Math.round(2 * DIValue / 10); // round to nearest 5
		int rd = (int) Math.round(DIValue); // round to nearest integer

		String t = Integer.toString(rd); 
		glText.begin( 1, 1, 1, 1, matrix ); 			// white
		glText.setScale(2.5f); 							// was 1.2
		glText.drawCX(t, 0, 0.9f*pixH2 - glText.getCharHeight()/2 );   // Draw String
		glText.setScale(1.5f); 							// was 1.2
		glText.end();                                    
	}

	void setHeading(float value)
	{
		DIValue = value;	
	}


	void renderSlipBall(float[] matrix)
	{
		float z;

		z = zfloat;

		float radius = 10*pixH/736;
		float x1= SlipValue;
		float y1= -0.9f*pixH2;


		// slip box
		mLine.SetColor(1, 1, 0, 1);
		mLine.SetWidth(4);
		mLine.SetVerts(
				-0.07f * pixH2, -0.9f*pixH2 - 0.8f*glText.getCharHeight(), z,                     
				-0.07f * pixH2, -0.9f*pixH2 + 0.8f*glText.getCharHeight(), z		
				);
		mLine.draw(matrix);

		mLine.SetVerts(
				0.07f * pixH2, -0.9f*pixH2 - 0.8f*glText.getCharHeight(), z,                     
				0.07f * pixH2, -0.9f*pixH2 + 0.8f*glText.getCharHeight(), z		
				);
		mLine.draw(matrix);


		// slip ball
		mPolygon.SetWidth(1); 
		mPolygon.SetColor(0.9f, 0.9f, 0.9f, 0); //white
		{
			float[] vertPoly = {
					// some issue with draworder to figger out.
					x1 + 2.0f*radius, y1 + 0.8f*radius, z,
					x1 + 0.8f*radius, y1 + 2.0f*radius, z,
					x1 - 0.8f*radius, y1 + 2.0f*radius, z,
					x1 - 2.0f*radius, y1 + 0.8f*radius, z,
					x1 - 2.0f*radius, y1 - 0.8f*radius, z,
					x1 - 0.8f*radius, y1 - 2.0f*radius, z,
					x1 + 0.8f*radius, y1 - 2.0f*radius, z,
					x1 + 2.0f*radius, y1 - 0.8f*radius, z
			};
			mPolygon.VertexCount = 8;  	
			mPolygon.SetVerts(vertPoly);
			mPolygon.draw(matrix);
		}
	}

	void setSlip(float value)
	{
		SlipValue = value;
	}


	//-------------------------------------------------------------------------
	// Flight Path Vector
	//
	void renderFPV(float[] matrix)
	{
		float z, pixPerDegree;

		pixPerDegree = pixM2 / pitchInView;
		z = zfloat;

		float radius = 10*pixH/736;  //12

		float x1 = fpvX * pixPerDegree;
		float y1 = fpvY * pixPerDegree;

		mPolyLine.SetWidth(3); 
		mPolyLine.SetColor(0.0f, 0.9f, 0.0f, 1); //green
		{
			float[] vertPoly = {
					// some issue with draworder to figger out.
					x1 + 2.0f*radius, y1 + 0.8f*radius, z,
					x1 + 0.8f*radius, y1 + 2.0f*radius, z,
					x1 - 0.8f*radius, y1 + 2.0f*radius, z,
					x1 - 2.0f*radius, y1 + 0.8f*radius, z,
					x1 - 2.0f*radius, y1 - 0.8f*radius, z,
					x1 - 0.8f*radius, y1 - 2.0f*radius, z,
					x1 + 0.8f*radius, y1 - 2.0f*radius, z,
					x1 + 2.0f*radius, y1 - 0.8f*radius, z,
					x1 + 2.0f*radius, y1 + 0.8f*radius, z
			};
			mPolyLine.VertexCount = 9;  	
			mPolyLine.SetVerts(vertPoly);  //crash here
			mPolyLine.draw(matrix);
		}

		mLine.SetWidth(3); 
		mLine.SetColor(0.0f, 0.9f, 0.0f, 0); //green
		mLine.SetVerts(
				x1 + 2.0f*radius, y1 + 0.0f*radius, z,                     
				x1 + 4.0f*radius, y1 + 0.0f*radius, z		
				);
		mLine.draw(matrix);

		mLine.SetVerts(
				x1 - 2.0f*radius, y1 + 0.0f*radius, z,                     
				x1 - 4.0f*radius, y1 + 0.0f*radius, z		
				);
		mLine.draw(matrix);

		mLine.SetVerts(
				x1 + 0.0f*radius, y1 + 2.0f*radius, z,                     
				x1 + 0.0f*radius, y1 + 4.0f*radius, z		
				);
		mLine.draw(matrix);
	}

	void setFPV(float x, float y)
	{
		fpvX = x;
		fpvY = y;  
	}

	static final int MX_NR_APT = 10;
	static int MX_RANGE = 20;   //nm
	static int counter = 0;

	void renderAPT(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
		float radius = 5;

		pixPerDegree = pixM2 / pitchInView;
		z = zfloat;

		// 0.16667 deg lat  = 10 nm
		// 0.1 approx 5nm
		double d = 0;         // =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // ft 
		double _d = 6080000; // 1,000 nm 
		double relBrg = 0;    // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
		double _relBrg = 180; // Assume the worst
		int nrAptsFound = 0;

		Iterator <Apt> it = Gpx.aptList.iterator(); 
		while (it.hasNext()) {
			Apt currApt  = it.next(); 

			String wptId = currApt.name;

			double deltaLat = currApt.lat - LatValue;
			double deltaLon = currApt.lon - LonValue;
			//d =  364800 * Math.hypot(deltaLon, deltaLat);  // in ft, 1 deg of lat  6080 * 60 = 364,80 note hypot uses convergenge and is very slow.
			d =  364800 * Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);  // in ft, 1 deg of lat  6080 * 60 = 364,800 

			// Apply selection criteria
			if (d < 5*6080) nrAptsFound++;                                              // always show apts closer then 5nm
			else if ((nrAptsFound < MX_NR_APT) && (d < MX_RANGE*6080))  nrAptsFound++;  // show all others up to MX_NR_APT for MX_RANGE
			else continue;                                                              // we already have all the apts as we wish to display

			relBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat)) - DIValue) % 360;
			if (relBrg >  180) relBrg = relBrg - 360;
			if (relBrg < -180) relBrg = relBrg + 360;

			x1 = (float) ( relBrg * pixPerDegree);
			y1 = (float) (-Math.toDegrees(Math.atan2(MSLValue, d) )  * pixPerDegree);

			mPolyLine.SetWidth(3); 
			mPolyLine.SetColor(0.99f, 0.5f, 0.99f, 1); //purple'ish
			{
				float[] vertPoly = {
						x1 + 2.0f*radius, y1 + 0.0f*radius, z,
						x1 + 0.0f*radius, y1 + 2.0f*radius, z,
						x1 - 2.0f*radius, y1 + 0.0f*radius, z,
						x1 - 0.0f*radius, y1 - 2.0f*radius, z,
						x1 + 2.0f*radius, y1 + 0.0f*radius, z
				};
				mPolyLine.VertexCount = 5;  	
				mPolyLine.SetVerts(vertPoly);  //crash here 
				mPolyLine.draw(matrix);
			}

			String t = wptId;
			glText.begin( 1.0f, 0.5f, 1.0f , 0, matrix);  // purple
			glText.setScale(2.0f); 	
			glText.drawCY(t, x1, y1 + glText.getCharHeight()/2 );        
			glText.end(); 

			/*if (Math.abs(relBrg) < Math.abs(_relBrg)) {
					// closest on the nose bearing
					setWPTAutoValue(wptId); 
					setDME((float) d/6080);  // 1nm = 6080ft
					setRelBrg((float) relBrg);
					_relBrg = relBrg;
			}*/ 
			
			double absBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat))) % 360;
			while (absBrg < 0) absBrg += 360;
			if (Math.abs(d) < Math.abs(_d)) {
				// closest apt (dme)
				setAutoWptValue(wptId); 
				setAutoWptDme((float) d/6080);  // 1nm = 6080ft
				setAutoWptBrg((float) absBrg); 
				_d = d;
			}
		} 

		//
		// If we dont have the full compliment of apts expand the range incrementally
		// If do we have a full compliment start reducing the range
		// This also has the "useful" side effect of "flashing" new additions for a few cycles 
		//
		if ((nrAptsFound < MX_NR_APT-2) && (counter++ % 10 == 0)) MX_RANGE += 1; 
		else if ((nrAptsFound >= MX_NR_APT)) MX_RANGE -= 1;
		MX_RANGE = Math.min(MX_RANGE, 100);

		setMSG(6, String.format("RNG %d", MX_RANGE));     
		setMSG(5, String.format("#AP %d", nrAptsFound)); 
	}

	void setLatLon(float lat, float lon)
	{
		LatValue = lat;
		LonValue = lon;
	}

	//-------------------------------------------------------------------------
	// Turn Indicator
	//
	void renderTurnMarkers(float[] matrix)
	{
		final float STD_RATE = 0.0524f; // = rate 1 = 3deg/s
		float z;

		z = zfloat;

		// rate of turn box
		mLine.SetColor(0.7f, 0.7f, 0.7f, 1);  // grey
		mLine.SetWidth(4);
		mLine.SetVerts(
				-STD_RATE*4*pixM2, -0.8f*pixH2 - 10, z,                     
				-STD_RATE*4*pixM2, -0.8f*pixH2 + 10, z		
				);
		mLine.draw(matrix);

		mLine.SetVerts(
				STD_RATE*4*pixM2, -0.8f*pixH2 - 10, z,                     
				STD_RATE*4*pixM2, -0.8f*pixH2 + 10, z		
				);
		mLine.draw(matrix);

		// rate of turn bar
		mLine.SetColor(0, 0.8f, 0, 1); // green
		mLine.SetWidth(15);
		mLine.SetVerts(
				0,                -0.8f*pixH2, z,                     
				ROTValue*4*pixM2, -0.8f*pixH2, z		
				);
		mLine.draw(matrix);
	}


	void setTurn(float rot)
	{
		ROTValue = rot;
	}

	//
	// Percentage battery remaining
	//
	void renderBatteryPct(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("BAT %3.0f", BatteryPct*100) + "%";
		if (BatteryPct > 0.1) glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		else glText.begin( 0.0f, 1.0f, 1.0f, 1.0f, matrix ); // red
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, -0.7f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setBatteryPct(float value)
	{
		BatteryPct = value;
	}

	void renderGForceValue(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("G %03.1f", GForceValue);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(3.0f); 							// 
		glText.draw(t, -0.97f*pixW2, -0.9f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setGForce(float value)
	{
		GForceValue = value;
	}

	String mAutoWpt; 
	void renderAutoWptValue(float[] matrix)
	{
		float z, pixPerUnit;
		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("WPT %s", mAutoWpt);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.9f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setAutoWptValue(String wpt)
	{
		mAutoWpt = wpt;
	}

	float mAutoWptBrg;
	void renderAutoWptBrg(float[] matrix)
	{
		float z;
		z = zfloat;

		String t = String.format("BRG  %03.0f", mAutoWptBrg);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.7f*pixH2 - glText.getCharHeight()/2 );            // Draw  String
		glText.end();                                    
	}

	void setAutoWptBrg(float brg)
	{
		mAutoWptBrg = brg;
	}
	
	
	/*
	String mWptSelName = "YAAA";
	String mWptSelComment = "   ";
	float mWptSelLat = 0;
	float mWptSelLon = 0;
	*/
	
	/*
	<wpt lat="-32.395000" lon="115.871000">
		<ele>2.7432</ele>
		<magvar>1.8</magvar>
		<name>YSEN</name>
		<cmt>Serpentine,AS</cmt>
		<type>AIRPORT</type>
	</wpt>
	*/

	String mWptSelName = "YSEN";
	String mWptSelComment = "Serpentine";
	float mWptSelLat = -32.395000f;
	float mWptSelLon = 115.871000f;
	
	String mAltSelName = "00000";
	float mAltSelValue = 0;

	float leftC = 0.6f;
	void renderWptSelValue(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
		//double acLat, acLon, alt;
		//float radius = 5;

		pixPerDegree = pixM2 / pitchInView;

		z = zfloat;
		// Draw the selecting triangle spinner buttons
		mTriangle.SetColor(0.6f, 0.6f, 0.6f, 0);  // gray
		for (int i = 0; i < 4; i++) {
			float xPos = (leftC + (float) i/10f);
			mTriangle.SetVerts((xPos - 0.02f) * pixW2, 0.90f * pixM2, z,  //0.02
					(xPos + 0.02f) * pixW2, 0.90f * pixM2, z,
					(xPos + 0.00f) * pixW2, 0.94f * pixM2, z);
			mTriangle.draw(matrix);

			mTriangle.SetVerts((xPos - 0.02f) * pixW2, 0.74f * pixM2, z,  //0.02
					(xPos + 0.02f) * pixW2, 0.74f * pixM2, z,
					(xPos + 0.00f) * pixW2, 0.70f * pixM2, z);
			mTriangle.draw(matrix);

			// Draw the individual select characters  
			if (mWptSelName != null) { 
				glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
				glText.setScale(3f);
				String s = String.format("%c", mWptSelName.charAt(i));
				glText.drawCX(s, xPos * pixW2, 0.81f*pixH2 - glText.getCharHeight()/2 );            
				glText.end();
			} 
		}

		// draw the apt description string 
		glText.begin( 1.0f, 1f, 1.0f, 1.0f, matrix ); // 
		glText.setScale(2.1f);
		String s = mWptSelComment;
		glText.draw(s, leftC * pixW2, 0.5f*pixH2 - glText.getCharHeight()/2 );            
		glText.end(); 

		// Calculate the relative bearing to the selected wpt
		double deltaLat = mWptSelLat - LatValue;
		double deltaLon = mWptSelLon - LonValue;
		//double d =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // in ft  very slow see comment elsewhere
		double d =  364800 * Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);  //faster version see comment in renderApt

		double relBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat)) - DIValue) % 360;
		if (relBrg >  180) relBrg = relBrg - 360;
		if (relBrg < -180) relBrg = relBrg + 360;

		if (relBrg >  30) relBrg = 30;
		if (relBrg < -30) relBrg = -30;

		// Calculate how many degrees of pitch to command
		final float MAX_COMMAND = 15; // Garmin spec 15 deg pitch and 30 deg roll
		float deltaAlt = mAltSelValue - MSLValue;
		//float commandPitch =  (IASValue) / 5 * (deltaAlt / 1000);
		float commandPitch;		
		if (deltaAlt > 0) 
			commandPitch =  (IASValue - Vy) / 5 * (deltaAlt / 1000);
		else
			commandPitch =  (IASValue) / 5 * (deltaAlt / 1000);

		if (commandPitch >  MAX_COMMAND) commandPitch =  MAX_COMMAND;
		if (commandPitch < -MAX_COMMAND) commandPitch = -MAX_COMMAND;
		//if (IASValue < Vs0) commandPitch = -MAX_COMMAND;  

		// update the flight director data
		setFlightDirector(true, commandPitch, (float) relBrg); 

		// the next two will be moved to their own methods ... TODO
		// DME
		String t = String.format("DME %03.1f", d / 6080);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.5f); 							// 
		glText.draw(t, 0.6f*pixW2, 0.4f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    

		// BRG
		double absBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat))) % 360;
		while (absBrg < 0) absBrg += 360;

		t = String.format("BRG  %03.0f", absBrg);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.5f); 							// 
		glText.draw(t, 0.6f*pixW2, 0.3f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    

		// HWY
		/*
			x1 = (float) ( relBrg * pixPerDegree);
			y1 = (float) (-Math.toDegrees(Math.atan2(MSLValue, d) )  * pixPerDegree);

			mLine.SetWidth(15f);
			mLine.SetColor(0.1f, 0.1f, 0.1f, 0);  // black
			mLine.SetVerts(0,  -0.8f * pixH2, z,
					           x1,  y1, z);
			mLine.draw(mMVPMatrix);
		 */
	}


	void renderAltSelValue(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
		pixPerDegree = pixM2 / pitchInView;
		z = zfloat;

		// Draw the selecting triangle spinner buttons
		mTriangle.SetColor(0.6f, 0.6f, 0.6f, 0);  // gray
		for (int i = 0; i < 3; i++) {
			float xPos = (leftC + (float) i/10f);
			mTriangle.SetVerts((xPos - 0.02f) * pixW2, -0.90f * pixM2, z,  //0.02
					(xPos + 0.02f) * pixW2, -0.90f * pixM2, z,
					(xPos + 0.00f) * pixW2, -0.94f * pixM2, z);
			mTriangle.draw(matrix);

			mTriangle.SetVerts((xPos - 0.02f) * pixW2, -0.74f * pixM2, z,  //0.02
					(xPos + 0.02f) * pixW2, -0.74f * pixM2, z,
					(xPos + 0.00f) * pixW2, -0.70f * pixM2, z);
			mTriangle.draw(matrix);

			// Draw the individual select characters  
			if (mAltSelName != null) { 
				glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
				glText.setScale(3f);
				String s = String.format("%c", mAltSelName.charAt(i));
				glText.drawCX(s, xPos * pixW2, -0.83f*pixH2 - glText.getCharHeight()/2 );            
				glText.end();
			} 
		}

		/*
		// We will only go to 10 ft. Hardcode the last digit to '0'   
		float xPos = (leftC + 3.8f/10f);
		glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
		glText.setScale(3f);
		String s = "0";
		glText.drawCX(s, xPos * pixW2, -0.83f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();
		 */
		float xPos = (leftC + 2.6f/10f);
		glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
		glText.setScale(2.2f);
		//String s = "X100 ft";
		String s = "F L";
		glText.draw(s, xPos * pixW2, -0.83f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();


		// maybe draw some HITS boxes?
		/*
			// Calculate the relative bearing to the selected wpt
			double deltaLat = mWptSelLat - LatValue;
			double deltaLon = mWptSelLon - LonValue;
			double d =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // in ft 
			double relBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat)) - DIValue) % 360;
			if (relBrg >  180) relBrg = relBrg - 360;
			if (relBrg < -180) relBrg = relBrg + 360;

			if (relBrg >  30) relBrg = 30;
			if (relBrg < -30) relBrg = -30;

			// update the flight director data
			setFlightDirector(true, 2f, (float) relBrg);
		 */
	}



	void setActionDown(float x, float y)
	{
		// 0,0 is top left in landscape
		mX =  (x / pixW - 0.5f) * 2;
		mY = -(y / pixH - 0.5f) * 2;  
		int pos = -1; // set to invalid / no selection
		int inc = 0;  //initialise to 0, also acts as a flag 
		int ina = 0;  //initialise to 0, also acts as a flag
		char[] wpt = mWptSelName.toCharArray();
		char[] alt = mAltSelName.toCharArray();

		// Determine if we are counting up or down? 
		// wpt character
		if      (Math.abs(mY - 0.9) < 0.15) inc = -1;
		else if (Math.abs(mY - 0.6) < 0.15) inc = +1;

		// Determine if we are counting up or down?
		// altitude number
		else if (Math.abs(mY + 0.6) < 0.15) ina = -1;
		else if (Math.abs(mY + 0.9) < 0.15) ina = +1;

		// Determine which digit is changing
		for (int i = 0; i < 4; i++) {
			//float xPos = (leftC + (float) i/10f);
			if (Math.abs(mX - (leftC + 0.0f)) < 0.05) {  //0.6
				pos = 0;
			} 
			else if (Math.abs(mX - (leftC + 0.1f)) < 0.05) {  //0.7
				pos = 1;
			}
			else if (Math.abs(mX - (leftC + 0.2f)) < 0.05) {  //0.8
				pos = 2;
			}
			else if (Math.abs(mX - (leftC + 0.3f)) < 0.05) {  //0.9
				pos = 3;
			}
		}

		//
		// Increment the appropriate digit if we got a valid tap (pos != -1)
		//
		if (pos > -1) { 
			if (ina != 0) {
				alt[pos] += ina;
				if (alt[pos] < '0') alt[pos] = '9'; 
				if (alt[pos] > '9') alt[pos] = '0';
				if (alt[0] > '2') alt[pos] = '0'; 
				mAltSelName = String.valueOf(alt);
				mAltSelValue = Float.parseFloat(mAltSelName);
			}

			if (inc != 0) {
				wpt[pos] += inc;
				if (wpt[pos] < 'A') wpt[pos] = 'Z'; 
				if (wpt[pos] > 'Z') wpt[pos] = 'A';

				Iterator <Apt> it = Gpx.aptList.iterator();

				while (it.hasNext()) {
					Apt currApt  = it.next();

					// Look for a perfect match 
					if (currApt.name.equals(String.valueOf(wpt))) {
						mWptSelName = currApt.name; 
						mWptSelComment = currApt.cmt;
						mWptSelLat = currApt.lat;
						mWptSelLon = currApt.lon;
						break;
					}
					// Look for a partial match
					else if ((pos < 3) && currApt.name.startsWith(String.valueOf(wpt).substring(0, pos+1)  ))  {   
						// We found a partial match, fill in the rest for a first guess
						for (int i = pos; i < 4; i++) wpt[i] = currApt.name.charAt(i);
						mWptSelName = currApt.name;
						mWptSelComment = currApt.cmt;
						mWptSelLat = currApt.lat;
						mWptSelLon = currApt.lon;
						break;
					}
					// No match at all
					else {
						mWptSelName = String.valueOf(wpt);
						mWptSelComment = " ";
						mWptSelLat = 0;
						mWptSelLon = 0;
					}
				}
			}
		}
	}


	float mAutoWptDme; 
	void renderAutoWptDme(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("DME %03.1f", mAutoWptDme);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.8f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setAutoWptDme(float dme)
	{
		mAutoWptDme = dme;
	}

	float mAutoWptRlb; 
	void renderAutoWptRlb(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("RLB  %03.0f", mAutoWptRlb);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.7f*pixH2 - glText.getCharHeight()/2 );            // Draw  String
		glText.end();                                    
	}

	void setAutoWptRelBrg(float rlb)
	{
		mAutoWptRlb = rlb;
	}

	

	static String mMsg[] = new String[10]; 
	static float lineNr;
	void renderMSGValue(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		for (int i = 0; i  < mMsg.length;  i++) {  
			String t = mMsg[i];
			if (t != null) {
				glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix );
				glText.setScale(2.0f); 							 
				glText.draw(t, -0.97f*pixW2, (float) i / 10f * pixH2 - glText.getCharHeight()/2 );            
				glText.end();                                  
			}
		}
	}

	void setMSG(int line, String s)
	{
		lineNr = line;
		mMsg[line] = s;
	}


	/*	
 	String mMsg1; 
	void renderMSG1Value(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = mMsg1;
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.6f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setMSG1(String s)
	{
		mMsg1 = s;
	}

	String mMsg2; 
	void renderMSG2Value(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = mMsg2;
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							// 
		glText.draw(t, -0.97f*pixW2, 0.5f*pixH2 - glText.getCharHeight()/2 );            
		glText.end();                                    
	}

	void setMSG2(String s)
	{
		mMsg2 = s;
	}
	 */


	void setPrefs(prefs_t pref, boolean value)
	{
		switch (pref) {
		case TERRAIN:
			displayTerrain = value;
			break;
		case TAPE:
			displayTape = value;
			break;
		case MIRROR:
			displayMirror = value;
			break;
		case INFO_PAGE:
			displayInfoPage = value;
			break;
		case FLIGHT_DIRECTOR:
			displayFlightDirector = value;
			break;
		case AIRPORT:
			displayAirport = value;
			break;

		}
	}

	
	//-----------------------------------------------------------------------------
	//	
	//  HSI
	//
	//-----------------------------------------------------------------------------
	// todo - render fixed HSI markers
	
	private void renderCompassRose(float[] matrix)
	{
		float tapeShade = 0.6f;
		int i, j;
		float innerTic, outerTic, pixPerDegree, iPix;
		float z, sinI, cosI;
		String t;

		float roseRadius = 9.0f; 												
		float x1= -0.75f*pixW2;
		float y1=  0.60f*pixH2;

		pixPerDegree = pixM2 / pitchInView;
		z = zfloat;

		mLine.SetWidth(2);  //3
		mLine.SetColor(tapeShade, tapeShade, tapeShade, 1);  // grey
		
		// The rose degree tics
	  for (i = 0; i <= 330; i=i+30) {
			sinI = UTrig.isin( (450-i) % 360 );  
			cosI = UTrig.icos( (450-i) % 360 );
			mLine.SetVerts(
					x1 + 0.9f * pixPerDegree * roseRadius * cosI, y1 + 0.9f * pixPerDegree * roseRadius * sinI, z,
					x1 + 1.0f * pixPerDegree * roseRadius * cosI, y1 + 1.0f * pixPerDegree * roseRadius * sinI, z
					);
			mLine.draw(mMVPMatrix);
			
			switch( i ) {
				case 0 : t = "N";
				break;
				case 30 : t = "30";
				break;
				case 60 : t = "60";
				break;
				case 90 : t = "E";
				break;
				case 120 : t = "120";
				break;
				case 150 : t = "150";
				break;
				case 180 : t = "S";
				break;
				case 220 : t = "210";
				break;
				case 240 : t = "240";
				break;
				case 270 : t = "W";
				break;
				case 300 : t = "300";
				break;
				case 330 : t = "330";
				break;
				//t = String.format("%03.0f",(float) MSLValue % 1000);
				//default : t = (QString( "%1" ).arg( i/10, -2 ));
				//default : t = String.format("%03.0f", (float) i/10);
				default : t = "";
				break;
			}

			glText.begin( tapeShade, tapeShade, tapeShade, 1.0f, matrix ); // white
			glText.setScale(1.0f); // seems to have a weird effect here?
			glText.drawC(t, x1 + 0.75f * pixPerDegree * roseRadius * cosI, y1 + 0.75f * pixPerDegree * roseRadius * sinI);             
			glText.end();                                   
			for (j = 10; j <=20; j=j+10) {
	      sinI = UTrig.isin( (i+j) );
	      cosI = UTrig.icos( (i+j) );
				mLine.SetVerts(
						x1 + 0.93f * pixPerDegree * roseRadius * cosI, y1 + 0.93f * pixPerDegree * roseRadius * sinI, z,
						x1 + 1.00f * pixPerDegree * roseRadius * cosI, y1 + 1.00f * pixPerDegree * roseRadius * sinI, z
						);
				mLine.draw(mMVPMatrix);
			}
			for (j = 5; j <=25; j=j+10) {
	      sinI = UTrig.isin( (i+j) );
	      cosI = UTrig.icos( (i+j) );
				mLine.SetVerts(
						x1 + 0.96f * pixPerDegree * roseRadius * cosI, y1 + 0.96f * pixPerDegree * roseRadius * sinI, z,
						x1 + 1.00f * pixPerDegree * roseRadius * cosI, y1 + 1.00f * pixPerDegree * roseRadius * sinI, z
						);
				mLine.draw(mMVPMatrix);
			}
		}
	}
	
	


/*
//-------------------------------------------------
// todo move to individual and populate
t = (QString( "%1" ).sprintf("WPT: %s", "YABA")); // todo ... demo
QGLWidget::renderText (-0.97*pixW2, 0.90*pixH2, z+.1, t, font, 2000 ) ;

t = (QString( "%1" ).sprintf("BRG: %03d", 123)); // todo ... demo
QGLWidget::renderText (-0.97*pixW2, 0.80*pixH2, z+.1, t, font, 2000 ) ;


t = (QString( "%1" ).sprintf("GS: %03.1f", GSValue )); // todo ... demo
QGLWidget::renderText (-0.97*pixW2, 0.60*pixH2, z+.1, t, font, 2000 ) ;
	 */


	/*



void GLPFD::renderDIMarkers()
{
	GLint i, j;
	GLfloat innerTic, midTic, outerTic, z, pixPerUnit, iPix;

	glLineWidth( 2 );
  pixPerUnit = pixH2/DIInView;
  z = zfloat;

  font = QFont("Fixed", 10, QFont::Normal);
	QFontMetrics fm = fontMetrics();

  innerTic = 0.80 * pixH2;												// inner & outer are relative to the vertical scale line
  outerTic = 0.90 * pixH2;
  midTic = 0.87 * pixH2;

	// The numbers & tics for the tape
	qglColor( QColor( "white" ) );

  for (i = 0; i < 360; i=i+30) {
    iPix = (float) i * pixPerUnit;
    t = QString( "%1" ).arg( i );

    glBegin(GL_LINE_STRIP);
      glVertex3f( iPix, innerTic, z);
      glVertex3f( iPix, outerTic, z);
    glEnd();
    QGLWidget::renderText (iPix - fm.width(t)/2 , outerTic  + fm.ascent() / 2 , z, t, font, 2000 ) ;

    //for (j = i + 20; j < i+90; j=j+20) {
    for (j = i + 10; j < i+90; j=j+10) {
      iPix = (float) j * pixPerUnit;
			glBegin(GL_LINE_STRIP);
        glVertex3f( iPix, innerTic,z);
        glVertex3f( iPix, midTic,z);
			glEnd();
    }


    iPix = (float) (360-i) * pixPerUnit;
    t = QString( "%1" ).arg( i );
    glBegin(GL_LINE_STRIP);
      glVertex3f( -iPix, innerTic, z);
      glVertex3f( -iPix, outerTic, z);
    glEnd();
    QGLWidget::renderText (-iPix - fm.width(t)/2 , outerTic  + fm.ascent() / 2 , z, t, font, 2000 ) ;

    //for (j = i + 20; j < i+90; j=j+20) {
    for (j = i + 10; j < i+90; j=j+10) {
      iPix = (float) j * pixPerUnit;
      glBegin(GL_LINE_STRIP);
        glVertex3f( -iPix, innerTic,z);
        glVertex3f( -iPix, midTic,z);
      glEnd();
    }
	}

  // The horizontal scale bar
	glBegin(GL_LINE_STRIP);
    glVertex3f(-180 * pixPerUnit , innerTic, z);
    glVertex3f(180 * pixPerUnit, innerTic, z);
  glEnd();
}


//
//	Set the indicator
//

void GLPFD::setHeading(int degrees)
{
  DIValue = degrees;
  while (DIValue < 0) DIValue += 360;
  DITranslation = DIValue / DIInView  * pixH2;
	updateGL();
}

void GLPFD::setSlip(int slip)
{
  SlipValue = slip;
  updateGL();
}


void GLPFD::setGForce(float gforce)
{
  GForceValue = gforce;
  updateGL();
}

void GLPFD::setGS(float gs)
{
  GSValue = gs;
  updateGL();
}

void GLPFD::setROT(float rot)
{
  ROTValue = rot;
  updateGL();
}

//
//	Set the bearing (to steer) 
//

void GLPFD::setBearing(int degrees)
{
  baroPressure = degrees;
	updateGL();
}

	 */



}




