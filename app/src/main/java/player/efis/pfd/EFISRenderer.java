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
    M20J,
	PA28,
	RV6,
	RV7,
	RV8,
	// --Commented out by Inspection (2017-02-08 12:56):T18,
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
	//private final static AircraftModel mAircraftModel = AircraftModel.GENERIC; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.AZTEC; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.CRICRI; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.CRUZ; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.J160; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.LGEZ; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.PA28; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.RV6; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.RV7; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.RV8; //done
	//private final static AircraftModel mAircraftModel = AircraftModel.T18; // removed
	//private final static AircraftModel mAircraftModel = AircraftModel.W10; //done

	private static AircraftModel mAircraftModel = AircraftModel.RV8;
	
	
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
	private float BatteryPct;       // Battery usage
	private float GForceValue;      // G force
	private float GSValue;
	private float ROTValue;         // Rate Of Turn
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
	boolean displayRMI;  // debug
	float RMIRotation;
	//3D map display
	boolean displayAirport;
	private boolean displayTerrain;
	private boolean displayTape;
	private boolean displayMirror;
	private boolean displayFPV;

	private boolean ServiceableDevice;	// Flag to indicate no faults
	private boolean ServiceableAh;	    // Flag to indicate AH failure
	private boolean ServiceableAlt;	    // Flag to indicate Altimeter failure
	private boolean ServiceableAsi;   	// Flag to indicate Airspeed failure
	private boolean ServiceableDi;      // Flag to indicate DI failure
	private boolean Calibrating;
	private String  CalibratingMsg;

	// kepress location
	private float mX, mY;

    // the magic number for portrait offset
    float portraitOffset = 0.40f; // 40f / 100f  aka 40%

	//Demo Modes
	private boolean bDemoMode;
	private String sDemoMsg;

	//b2 end

	private GLText glText;                             // A GLText Instance
	private Context context;                           // Context (from Activity)

	public enum layout_t
	{
		PORTRAIT,
		LANDSCAPE
	}
	//layout_t Layout = layout_t.PORTRAIT;
	layout_t Layout = layout_t.LANDSCAPE;

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

        // Pitch
        if (Layout == layout_t.LANDSCAPE) {
            // Slide pitch to current value
            Matrix.translateM(scratch1, 0, 0, pitchTranslation, 0); // apply the pitch
        }
        else {
            // Slide pitch to current value adj for portrait
            float Adjust = pixH2 * portraitOffset;
            Matrix.translateM(scratch1, 0, 0, pitchTranslation + Adjust, 0); // apply the pitch
        }

		// Slide ALT to current value
		Matrix.translateM(altMatrix, 0, mMVPMatrix, 0, 0, -MSLTranslation, 0); // apply the altitude

		// Slide IAS to current value
		Matrix.translateM(iasMatrix, 0, mMVPMatrix, 0, 0, -IASTranslation, 0); // apply the altitude

		zfloat = 0;

        if (displayTerrain == true) renderTerrain(scratch1);
        renderPitchMarkers(scratch1);

		// FPV only means anything if we have speed and rate of climb, ie altitude
		if (displayFPV) renderFPV(scratch1);      // must be on the same matrix as the Pitch
		if (displayAirport) renderAPT(scratch1);  // must be on the same matrix as the Pitch


        // Flight Director - FD
		if (displayFlightDirector) {  
			// Create a rotation for the Flight director
			Matrix.setRotateM(mFdRotationMatrix, 0, rollRotation + FDRotation, 0, 0, 1.0f);  // fd rotation
			Matrix.multiplyMM(fdMatrix, 0, mMVPMatrix, 0, mFdRotationMatrix, 0);

            if (Layout == layout_t.LANDSCAPE) {
                // Slide FD to current value
                Matrix.translateM(fdMatrix, 0, 0, pitchTranslation - FDTranslation, 0); // apply the altitude
            }
            else {
                //Matrix.translateM(scratch1, 0, 0, pitchTranslation + Adjust, 0); // apply the pitch
                // Slide pitch to current value adj for portrait
                float Adjust = pixH2 * portraitOffset;
                // Slide FD to current value
                Matrix.translateM(fdMatrix, 0, 0, pitchTranslation - FDTranslation + Adjust, 0); // apply the altitude
            }
			renderFlightDirector(fdMatrix);
			renderSelWptValue(mMVPMatrix);
            renderSelWptDetails(mMVPMatrix);
			renderSelAltValue(mMVPMatrix);
		}
		
		// Remote Magnetic Inidicator - RMI
		if (displayRMI) {
			float xlx; // = -0.78f*pixW2;
			float xly; // = -0.40f*pixH2;

			// Add switch for orientation
			if (Layout == layout_t.LANDSCAPE) {
				// Landscape
                xlx = -0.74f * pixW2; // top left -0.75
                xly =  0.50f * pixH2; // top left  0.55
				roseScale = 0.44f;
			}
			else {
				//Portrait
				xlx = -0.00f * pixW2;
				xly = -0.44f * pixH2;  //0.45f
				roseScale = 0.52f; //0.45f; //0.50f;
			}

			Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0); 
			// Create a rotation for the RMI
			Matrix.setRotateM(mRmiRotationMatrix, 0, DIValue, 0, 0, 1);  // compass rose rotation
			Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);
			renderBearingTxt(mMVPMatrix);
            renderFixedCompassMarkers(mMVPMatrix);
			Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            renderCompassRose(rmiMatrix);
			renderBearing(rmiMatrix);
            renderAutoWptDetails(mMVPMatrix);
		}

		if (displayFlightDirector || displayRMI) {
            renderSelWptValue(mMVPMatrix);
            renderSelWptDetails(mMVPMatrix);
        }


        if (Layout == layout_t.PORTRAIT) {
            // Slide pitch to current value adj for portrait
            float Adjust = pixH2 * portraitOffset;
            GLES20.glViewport(0, (int) Adjust, pixW, pixH); // Portrait //
        }
        renderFixedHorizonMarkers();
        renderRollMarkers(scratch2);

        //-----------------------------
        {
            if (Layout == layout_t.LANDSCAPE)
                GLES20.glViewport(pixW / 30, pixH / 30, pixW - pixW / 15, pixH - pixH / 15); //Landscape
            else
                GLES20.glViewport(pixW / 100, pixH * 40 / 100, pixW - pixW / 50, pixH - pixH * 42 / 100); // Portrait

            if (displayTape == true) {
                renderALTMarkers(altMatrix);
                renderASIMarkers(iasMatrix);
            }

            //if (displayTape == true) renderFixedVSIMarkers(mMVPMatrix);
            renderFixedALTMarkers(mMVPMatrix); // this could be empty argument
            renderFixedASIMarkers(mMVPMatrix); // this could be empty argument
            renderVSIMarkers(mMVPMatrix);

            renderFixedDIMarkers(mMVPMatrix);
            renderHDGValue(mMVPMatrix);

            GLES20.glViewport(0, 0, pixW, pixH);  // fullscreen
        }
        //-----------------------------

        //renderFixedDIMarkers(mMVPMatrix);
        //renderHDGValue(mMVPMatrix);


        renderTurnMarkers(mMVPMatrix);
		renderSlipBall(mMVPMatrix);
		renderGForceValue(mMVPMatrix);

		if (displayInfoPage) { 
			/*renderAutoWptValue(mMVPMatrix);
			renderAutoWptDme(mMVPMatrix);		
			//renderAutoWptRlb(mMVPMatrix);
			renderAutoWptBrg(mMVPMatrix);
			*/
			//renderMSGValue(mMVPMatrix); // need to phase this out, causes problems with portrait/landscape
            renderAncillaryDetails(mMVPMatrix);
            renderBatteryPct(mMVPMatrix);
            //renderAutoWptDetails(mMVPMatrix);
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

		// this projection matrix is applied to  object coordinates in the onDrawFrame() method
		//b2 Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		//Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 7); // - this apparently fixed for the Samsung?		

		//b2 start
		// Capture the window scaling for use by the rendering functions
		pixW = width; 
		pixH = height;
		pixW2 = pixW/2;
		pixH2 = pixH/2;

        if (pixW < pixH) pixM = pixW;
		else pixM = pixH;

        // because the ascpect ratio is different in landscape and portrait (due to menu bar)
        // we just fudge it as 85% throughout,  looks OK in landscape as well
        pixM = pixM * 88 / 100;
        pixM2 = pixM/2;

        // This code determines where the spinner control
        // elements are displayed. Used by WPT and ALT
        if (Layout == layout_t.LANDSCAPE) {
            // Landscape
            lineC =  0.50f;
            lineB =  0.00f;

            // Top
            selWptDec =  0.90f * pixH2;
            selWptInc =  0.74f * pixH2;

            // Bottom
            //selWptDec =  -0.00f * pixH2;
            //selWptInc =  -0.16f * pixH2;

            selAltDec = -0.74f * pixH2;
            selAltInc = -0.90f * pixH2;
        }
        else {
            // Portrait
            lineC = -0.90f;
            lineB = -0.90f;

            selWptDec = -0.30f * pixH2;
            selWptInc = -0.41f * pixH2;

            selAltDec = -0.80f * pixH2;
            selAltInc = -0.91f * pixH2;
        }


		// Set the window size specific scales, positions and sizes (nothing dynamic yet...)
		pitchInView = 25.0f;  //12.5f;//25.0f;	// degrees to display from horizon to top of viewport
		IASInView   = 40.0f;  //25.0f;    // IAS units to display from center to top of viewport
		MSLInView   = 300.0f; //250.0f;	  // IAS units to display from center to top of viewport

        float ratio = (float) width / height;
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
		/*  bugbug
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
		*/
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
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

	
	//-------------------------------------------------------------------------
  // Define the various built-in arcraft definitions
  //
	public void setAircraftData(String model)
	{
		try {
			mAircraftModel = AircraftModel.valueOf(model);
		}
		//catch (IllegalArgumentException e) {
		catch (Exception e) {
			mAircraftModel = AircraftModel.RV8;
		}


		// Vs0  Stall, flap extended
		// Vs1  Stall, flap retracted
		// Vx   Best angle climb
		// Vy   Best rate climb
		// Vfe  Flaps extension
		// Va   Maneuvering
		// Vno  Max structural cruise
		// Vne  Never exceed
		//
		// White Arc  Vs0 - Vfe
		// Green Arc  Vs1 - Vno
		// Yellow Arc Vno - Vne
		
		switch (mAircraftModel) {
		// V Speeds for various aircraft models
		case GENERIC: 
			// Ultralight
			Vs0 = 30;  // Stall, flap extended 
			Vs1 = 40;  // Stall, flap retracted
			Vx  = 50;  // Best angle climb     
			Vy  = 60;  // Best rate climb      
			Vfe = 60;  // Flaps extension      
			Va  = 80;  // Maneuvering          
			Vno = 90;  // Max structural cruise
			Vne = 120; // Never exceed         
			break;

		case AZTEC:
			// Colomban CriCri
			Vs0 = 61;   // Stall, flap extended 
			Vs1 = 66;   // Stall, flap retracted
			Vx  = 93;   // Best angle climb     
			Vy  = 102;  // Best rate climb      
			Vfe = 140;  // Flaps extension      
			Va  = 129;  // Maneuvering          
			Vno = 172;  // Max structural cruise
			Vne = 216;  // Never exceed         
			break;
		
		case CRICRI:
			// Colomban CriCri
			Vs0 = 39;  // Stall, flap extended
			Vs1 = 49;  // Stall, flap retracted
			Vx  = 56;  // Best angle climb
			Vy  = 68;  // Best rate climb
			Vfe = 70;  // Flaps extension
			Va  = 85;  // Maneuvering
			Vno = 100; // Max structural cruise
			Vne = 140; // Never exceed  
			break;
		
		case CRUZ:
			// PiperSport Cruzer
			Vs0 = 32;  // Stall, flap extended
			Vs1 = 39;  // Stall, flap retracted
			Vx  = 56;  // Best angle climb
			Vy  = 62;  // Best rate climb 
			Vfe = 75;  // Flaps extension
			Va  = 88;  // Maneuvering
			Vno = 108; // Max structural cruise
			Vne = 138; // Never exceed
			break;
		
		case J160:
			// Jabiru J160-C
			Vs0 = 40;   // Stall, flap extended 
			Vs1 = 45;   // Stall, flap retracted
			Vx  = 65;   // Best angle climb     
			Vy  = 68;   // Best rate climb      
			Vfe = 80;   // Flaps extension      
			Va  = 90;   // Maneuvering          
			Vno = 108;  // Max structural cruise
			Vne = 140;  // Never exceed         
			break;
		
		case LGEZ:
			// RV-8A
			Vs0 = 56;   // Stall, flap extended 
			Vs1 = 56;   // Stall, flap retracted
			Vx  = 72;   // Best angle climb     
			Vy  = 90;   // Best rate climb      
			Vfe = 85;   // Flaps extension      
			Va  = 120;  // Maneuvering          
			Vno = 161;  // Max structural cruise
			Vne = 200;  // Never exceed         
			break;

        case M20J:
            Vs0 = 53;   // Stall, flap extended
            Vs1 = 53;   // Stall, flap retracted
            Vx  = 66;   // Best angle climb
            Vy  = 85;   // Best rate climb
            Vfe = 115;  // Flaps extension
            Va  = 120;  // Maneuvering
            Vno = 152;  // Max structural cruise
            Vne = 174;  // Never exceed
            break;

			
		case PA28:
			// Piper PA28 Archer II
			Vs0 = 49;   // Stall, flap extended 
			Vs1 = 55;   // Stall, flap retracted
			Vx  = 64;   // Best angle climb     
			Vy  = 76;   // Best rate climb      
			Vfe = 102;  // Flaps extension      
			Va  = 89;   // Maneuvering          
			Vno = 125;  // Max structural cruise
			Vne = 154;  // Never exceed         
			break;
			
		case RV6:
		case RV7: 
		case RV8:
			// RV-6,7,8
			Vs0 = 51;    // Stall, flap extended 
			Vs1 = 56;    // Stall, flap retracted
			Vx  = 72;    // Best angle climb     
			Vy  = 90;    // Best rate climb      
			Vfe = 85;    // Flaps extension      
			Va  = 120;   // Maneuvering          
			Vno = 165;   // Max structural cruise
			Vne = 200;   // Never exceed         
			break;
	
		case W10:
			// Witttman Tailwind
			Vs0 = 48;  // Stall, flap extended
			Vs1 = 55;  // Stall, flap retracted
			Vx  = 90;  // Best angle climb - tbd
			Vy  = 104; // Best rate climb
			Vfe = 91;  // Flaps extension
			Va  = 130; // Maneuvering
			Vno = 155; // Max structural cruise - tbd
			Vne = 174; // Never exceed
			break;
		
		default:
			// RV-8A
			Vs0 = 51;    // Stall, flap extended 
			Vs1 = 56;    // Stall, flap retracted
			Vx  = 72;    // Best angle climb     
			Vy  = 90;    // Best rate climb      
			Vfe = 85;    // Flaps extension      
			Va  = 120;   // Maneuvering          
			Vno = 165;   // Max structural cruise
			Vne = 200;   // Never exceed         
			break;
		}
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
		String s = sDemoMsg; //"demo mode";
		glText.begin( 1.0f, 0f, 0f, 1.0f, matrix ); // Red
		glText.setScale(9.0f);
        glText.drawCX(s, 0, 0);
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

	float PPD_DIV = 30; // for landscape
	//float PPD_DIV = 25; // for landscape
	//float PPD_DIV = 50; // for portrait

	private void renderFlightDirector(float[] matrix)
	{	
		//int i;
		float z, pixPerDegree;

		z = zfloat;
		//pixPerDegree = pixM2 / pitchInView;
		pixPerDegree = pixM2 / PPD_DIV;

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
		//pixPerDegree = pixM2 / pitchInView;
        pixPerDegree = pixM2 / PPD_DIV;

        // We might make this configurable in future
        // for now force it to false
		if (false) {
            // The lubber line - W style
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
		mTriangle.SetVerts(0.035f * pixM2, 16.5f * pixPerDegree, z,
  						  -0.035f * pixM2, 16.5f * pixPerDegree, z,
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
		//pixPerDegree = pixM2 / pitchInView;					// Put the markers in open space at zero pitch
		pixPerDegree = pixM2 / PPD_DIV;							// Put the markers in open space at zero pitch

		mTriangle.SetColor(0.9f, 0.9f, 0.9f, 0);
		/*mTriangle.SetVerts(
		       -0.02f  * pixM, 14 * pixPerDegree, z,
				0.02f * pixM, 14 * pixPerDegree, z,
				0.0f, 15 * pixPerDegree, z);*/
        mTriangle.SetVerts(
                 0.035f * pixM2, 13.5f * pixPerDegree, z,
                -0.035f * pixM2, 13.5f * pixPerDegree, z,
                 0.0f, 15f * pixPerDegree, z);
		mTriangle.draw(matrix);
	}

	private void renderPitchMarkers(float[] matrix)
	{
		int i;
		float innerTic, outerTic, z, pixPerDegree, iPix;
        //pixPerDegree = pixH / pitchInView;
		z = zfloat;

        //pixPerDegree = pixM / pitchInView;
        if (Layout == layout_t.LANDSCAPE) {
            pixPerDegree = pixM / pitchInView; //pixH
        }
        else {
            pixPerDegree = pixM / pitchInView * 100 / 60;
        }

		innerTic = 0.10f * pixW2; 
		outerTic = 0.13f * pixW2;

		// top
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
		mLine.SetWidth(6); //4
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

		// bottom
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

		pixPitchViewMultiplier = 90.0f / pitchInView * pixH;
		pixOverWidth = pixW2 * 1.80f; //1.42f;
		z = zfloat;

		// Earth
		// Level to -90 pitch
		mSquare.SetColor(64f/255f, 50f/255f, 25f/255f, 0); //brown
		mSquare.SetWidth(1);
		{
			float[] squarePoly = {
					-pixOverWidth, -1.0f * pixPitchViewMultiplier, z,
					 pixOverWidth, -1.0f * pixPitchViewMultiplier, z,
					 pixOverWidth,  0.0f + 0.0f/pixH2, z,
					-pixOverWidth,  0.0f + 0.0f/pixH2, z
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
					 pixOverWidth, -2.0f * pixPitchViewMultiplier, z,
					 pixOverWidth, -1.0f * pixPitchViewMultiplier, z,
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
					 pixOverWidth, 0.0f + 1.0f/pixH2, z,
					 pixOverWidth, 1.0f * pixPitchViewMultiplier, z,
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
	Set the pitch angle
	 */
	public void setPitch( float degrees )
	{
		pitch = (float) -degrees;
		pitchTranslation = pitch / pitchInView * pixH;
	}

	/*!
	Set the roll angle
	 */
	void setRoll( float degrees )
	{
		roll = (float) degrees;
		rollRotation = roll;
	}


/*
    //innerTic = 0.70f * pixM2;	// inner & outer are relative to the vertical scale line
    //midTic   = 0.75f * pixM2;
    //outerTic = 0.80f * pixM2;
    innerTic = 1.10f * pixM2;	// inner & outer are relative to the vertical scale line
    midTic   = 1.20f * pixM2;
    outerTic = 1.26f * pixM2;
*/

	//-------------------------------------------------------------------------
	// Altimeter Indicator
	//
	void renderFixedALTMarkers(float[] matrix)
	{
		float z = zfloat;
		String t;

        float left =  0.80f * pixM2;
        float right = 1.14f * pixM2;
        float apex =  0.75f * pixM2;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixM2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if required.

		// Altimeter Display

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.5f);  //was 1.5
		glText.end();                                

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
                    right,-glText.getCharHeight(), z,//+0.1f,
                    right, glText.getCharHeight(), z,//+0.1f,
                    left,  glText.getCharHeight(), z,//+0.1f,
                    left, -glText.getCharHeight(), z,//+0.1f
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
	    float colom = 0.83f;

		// draw the thousands digits larger
		glText.setScale(3.5f);  //3  2.5
        if (MSLValue > 1000) glText.draw(t, colom * pixM2, -glText.getCharHeight() / 2);
		if (MSLValue < 10000) margin = 0.6f*glText.getCharWidthMax(); // because of the differing sizes
		else margin = 1.1f*glText.getCharWidthMax();                 	// we have to deal with the margin ourselves  

		//draw the hundreds digits smaller
		t = String.format("%03.0f",(float) MSLValue % 1000);
		glText.setScale(2.5f); // was 2.5
        glText.draw(t, colom * pixM2 + margin, -glText.getCharHeight() / 2);
		glText.end();

		mTriangle.SetColor(0.0f, 0.0f, 0.0f, 1);  //black
		mTriangle.SetVerts(
                left, glText.getCharHeight()/2, z,//+.1,
				apex, 0.0f, z,
                left, -glText.getCharHeight()/2, z///+.1
				);
		mTriangle.draw(mMVPMatrix);

        {
			mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
			mPolyLine.SetWidth(2); 
			float[] vertPoly = {
                    right, -glText.getCharHeight(), z,//+.2);
                    right,  glText.getCharHeight(), z,//+.2);
                    left,   glText.getCharHeight(), z,//+.2);
                    left,   glText.getCharHeight()/2, z,//+.2);
					apex,   0.0f, z,//+.2);
                    left,  -glText.getCharHeight()/2, z,//+.2);
                    left,  -glText.getCharHeight(), z,//+.2);
                    right, -glText.getCharHeight(), z //+.2);
			};

			mPolyLine.VertexCount = 8;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(matrix);
		}
	}


/* saved
    innerTic = -0.70f * pixM2;	// inner & outer are relative to the vertical scale line
    outerTic = -0.80f * pixM2;
    midTic   = -0.77f * pixM2;
    topTic =  0.9f * pixM2;
    botTic = -0.9f * pixM2;
*/
/*
    // Landscape
    innerTic = -1.10f * pixM2;	// inner & outer are relative to the vertical scale line
    midTic   = -1.20f * pixM2;
    outerTic = -1.26f * pixM2;
*/


	private void renderALTMarkers(float[] matrix)
	{
		float tapeShade = 0.6f; // for grey
		int i, j;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		//pixPerUnit = pixM2 / MSLInView; //b2 landscape
		pixPerUnit = pixH2 / MSLInView; //portrait
		z = zfloat;

		//innerTic = 0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		//midTic   = 0.75f * pixM2;
		//outerTic = 0.80f * pixM2;
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
			glText.setScale(3.0f);
            //glText.setScale(4.0f, 2.5f);
            if (i >= 1000) glText.draw(t, outerTic, iPix - glText.getCharHeight() / 2);

            if (i < 10000) margin = 0.6f*glText.getCharWidthMax();  // because of the differing sizes
			else margin = 1.1f*glText.getCharWidthMax();            // we have to deal with the margin ourselves

			//draw the hundreds digits smaller
			t = String.format("%03.0f",(float) i % 1000);
			glText.setScale(2.0f); // was 1.5
            //glText.setScale(2.4f, 1.5f); // was 1.5
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
		int i;
		float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

		pixPerUnit =  1.0f* pixM2 / VSIInView ;
		z = zfloat;

		innerTic = 0.64f * pixM2;	// inner & outer are relative to the vertical scale line
		outerTic = 0.70f * pixM2;
		midTic   = 0.67f * pixM2;


		// VSI box
		for (i = -2; i <= 2; i += 1) {
			mLine.SetColor(0.5f, 0.5f, 0.5f, 1);  // grey
			mLine.SetWidth(4);
			mLine.SetVerts(
					0.64f*pixM2, i*1000*pixPerUnit, z,                     
					0.70f*pixM2, i*1000*pixPerUnit, z		
					);
			mLine.draw(matrix);

			if (i != 0) {
				String s = Integer.toString(Math.abs(i));
				glText.begin( 0.75f, 0.75f, 0.75f, 1.0f, matrix ); // light grey
				glText.setScale(3.0f);   //1.75f
				glText.draw(s, innerTic - 1.5f * glText.getLength(s), i*1000*pixPerUnit - glText.getCharHeight() / 2);
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
		float z = zfloat;
		String t;

        float left  = -1.10f * pixM2;
        float right = -0.80f * pixM2;
        float apex  = -0.75f * pixM2;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixH2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if reqd.

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.5f); // was 1.5
		glText.end();

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
					left,  -glText.getCharHeight(), z,//+0.1f,
                    left,   glText.getCharHeight(), z,//+0.1f,
                    right,  glText.getCharHeight(), z,//+0.1f,
                    right, -glText.getCharHeight(), z,//+0.1f
			};
			mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
		}

		mTriangle.SetColor(0.0f, 0.0f, 0.0f, 1);  //black
		mTriangle.SetVerts(
                right, glText.getCharHeight()/2, z,//+.1,
                apex,  0.0f, z,
                right, -glText.getCharHeight()/2, z///+.1
				);
		mTriangle.draw(mMVPMatrix);

		{
			mPolyLine.SetColor(0.9f, 0.9f, 0.9f, 0); //white
			mPolyLine.SetWidth(2); 
			float[] vertPoly = {
                    left, -glText.getCharHeight(), z,//+.2);
                    left,  glText.getCharHeight(), z,//+.2);
                    right, glText.getCharHeight(), z,//+.2);
                    right, glText.getCharHeight()/2, z,//+.2);
                    apex,  0.0f, z,//+.2);
                    right, -glText.getCharHeight()/2, z,//+.2);
                    right, -glText.getCharHeight(), z,//+.2);
                    left,  -glText.getCharHeight(), z //+.2);
			};

			mPolyLine.VertexCount = 8;  	
			mPolyLine.SetVerts(vertPoly);
			mPolyLine.draw(matrix);
		}
        t = Integer.toString(Math.round(IASValue));
        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
        glText.setScale(3.5f); 							// was 2.5
        glText.drawC(t, -0.85f * pixM2, glText.getCharHeight()/2 );
        glText.end();
	}


	private void renderASIMarkers(float[] matrix)
	{
		float tapeShade = 0.6f; // for grey
		int i, j;
		float innerTic, midTic, outerTic, topTic, botTic;
		float z, pixPerUnit, iPix;

		//glLineWidth( 2 );
		z = zfloat;
		pixPerUnit = pixH2/IASInView ;

//        /* saved
		innerTic = -0.70f * pixM2;	// inner & outer are relative to the vertical scale line
		outerTic = -0.80f * pixM2;
		midTic   = -0.77f * pixM2;
		//topTic =  0.9f * pixM2;
		//botTic = -0.9f * pixM2;
//		*/
/*
        if (Layout == layout_t.LANDSCAPE) {
            // Landscape
            pixPerUnit = pixM2 / IASInView;
            innerTic = -1.10f * pixM2;    // inner & outer are relative to the vertical scale line
            outerTic = -1.26f * pixM2;
            midTic = -1.20f * pixM2;
        }
        else {
            // Portrait
            pixPerUnit = pixM2 / (IASInView / 2);
            innerTic = -0.60f * pixM2;    // inner & outer are relative to the vertical scale line
            outerTic = -0.70f * pixM2;
            midTic = -0.67f * pixM2;
        }
*/

		// The numbers & tics for the tape
		for (i = IASMaxDisp; i >= 0; i = i - 10) {
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
			glText.setScale(2.5f); // was 2
            //glText.setScale(3.2f, 2f);  // screen ratio is 1.6 on Nexus 2 x 1.6 = 3.2
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
		///*
		mLine.SetVerts(
				innerTic, 0, z,  // IASMinDisp - no longer used, set to 0
				innerTic, (IASMaxDisp + 100) * pixPerUnit, z
		);
		mLine.draw(matrix);
		//*/

		// For monochrome display (displayTerrain false) do not use any color
        if (displayTerrain) {
            //
			// Special Vspeed markers
			//

            // Simplified V Speeds

            //glText.begin( 0.0f, 0.6f, 0.0f, 1.0f, matrix); // Green
            //glText.begin( 0.9f, 0.9f, 0.0f, 1.0f, matrix); // yellow
            //glText.begin( 0.0f, 0.9f, 0.9f, 1.0f, matrix); // cyan
            glText.begin( 0.7f, 0.7f, 0.7f, 1.0f, matrix); // grey
            glText.setScale(2.0f); 	// was 1.5
            glText.draw(" Vx", innerTic, (float) Vx * pixPerUnit); // Vx
            glText.draw(" Vy", innerTic, (float) Vy * pixPerUnit); // Vy
            glText.draw(" Va", innerTic, (float) Va * pixPerUnit); // Va
            glText.end();


			// Tape markings for V speeds
            // Re use midTic ... maybe not such a good idea ...
			midTic = -0.75f * pixM2;					// Put tape under the tics, w/ VsO-Vfe bar narrower than minor tics
			//midTic = -1.17f * pixM2;	// Put tape under the tics, w/ VsO-Vfe bar narrower than minor tics
			mSquare.SetColor(0, 0.5f, 0, 1);  // dark green arc
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vs1 * pixPerUnit, z, //-.2);
						innerTic, (float) Vno * pixPerUnit, z, //-.2);
						midTic,   (float) Vno * pixPerUnit, z, //-.2);
						midTic,   (float) Vs1 * pixPerUnit, z  //-.2);
				};
				mSquare.SetVerts(squarePoly);
			mSquare.draw(matrix);
			}
			mSquare.SetColor(0.5f, 0.5f, 0.5f, 1);  // white arc
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vs0 * pixPerUnit, z, //-.2);
						innerTic, (float) Vfe * pixPerUnit, z, //-.2);
						midTic,   (float) Vfe * pixPerUnit, z, //-.2);
						midTic,   (float) Vs0 * pixPerUnit, z  //-.2);
				};
				mSquare.SetVerts(squarePoly);
				mSquare.draw(matrix);
			}

			mSquare.SetColor(0.9f, 0.9f, 0, 1);  // yellow arc
			mSquare.SetWidth(1);
			{
				float[] squarePoly = {
						innerTic, (float) Vno * pixPerUnit, z, //-.2);
						innerTic, (float) Vne * pixPerUnit, z, //-.2);
						midTic,   (float) Vne * pixPerUnit, z, //-.2);
						midTic,   (float) Vno * pixPerUnit, z  //-.2);
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
	//   Just a simple text box
	//
	void renderFixedDIMarkers(float[] matrix)
	{
		float z = zfloat;
		//String t;

        float left =  -0.15f * pixM2;
        float right =  0.15f * pixM2;
        //float apex =   0.00f * pixM2;

		// The tapes are positioned left & right of the roll circle, occupying the space based
		// on the vertical dimension, from .6 to 1.0 pixH2.  This makes the basic display
		// square, leaving extra space outside the edges for terrain which can be clipped if reqd.

		// Do a dummy glText so that the Heights are correct for the masking box
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.5f); // was 1.5
		glText.end();                                    

		// Mask over the moving tape for the value display box
		mSquare.SetColor(0.0f, 0.0f, 0.0f, 1); //black 
		mSquare.SetWidth(2);
		{
			float[] squarePoly = {
                    right, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,
                    right, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
                    left, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
                    left, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
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
                    right, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
                    left,  0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
                    left,  0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
                    right, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,
                    right, 0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,

					// for some reason this causes a crash on restart if there are not 8 vertexes
					// most probably a a bug in PolyLine
                    left,  0.9f * pixH2 + glText.getCharHeight(), z,//+0.1f,
                    left,  0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f
                    right, 0.9f * pixH2 - glText.getCharHeight(), z,//+0.1f,

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
		float z;
		z = zfloat;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(20);

		mLine.SetVerts(
				-0.7f * pixM2,  0.8f * pixH2, z,                     
				 0.7f * pixM2, -0.8f * pixH2, z
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
		float z;
		z = zfloat;

		mLine.SetColor(1, 0, 0, 1);  // red
		mLine.SetWidth(8);

		mLine.SetVerts(
				-0.7f * pixM2,  0.95f * pixH2, z,                     
				0.7f * pixM2,  0.85f * pixH2, z
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
		float z;
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
		float z;
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
		float z;//, pixPerUnit;

		//pixPerUnit = pixH2/DIInView;
		z = zfloat;

		//int rd = (int) 5 * Math.round(2 * DIValue / 10); // round to nearest 5
		int rd = (int) Math.round(DIValue); // round to nearest integer

		String t = Integer.toString(rd);
		glText.begin( 1, 1, 1, 1, matrix ); 			// white
		glText.setScale(3.5f); //2.5f); 							// was 1.2
		glText.drawCX(t, 0, 0.9f*pixH2 - glText.getCharHeight()/2 );   // Draw String
		//glText.setScale(1.5f); 							// was 1.2
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

		float radius = 10*pixM/736;
		float x1= SlipValue;
		float y1= -0.9f*pixH2;


		// slip box
		mLine.SetColor(1, 1, 0, 1);
		mLine.SetWidth(4);
		mLine.SetVerts(
				-0.07f * pixM2, y1 - 0.4f*glText.getCharHeight(), z,
				-0.07f * pixM2, y1 + 0.4f*glText.getCharHeight(), z
				);
		mLine.draw(matrix);

		mLine.SetVerts(
				0.07f * pixM2, y1 - 0.4f*glText.getCharHeight(), z,
				0.07f * pixM2, y1 + 0.4f*glText.getCharHeight(), z
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

		//pixPerDegree = pixM2 / pitchInView;
		pixPerDegree = pixM2 / PPD_DIV;
		z = zfloat;

		float radius = 10*pixM/736;  //12

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
    static int nrAptsFound;
	private void renderAPT(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
		float radius = 5;

		pixPerDegree = pixM / pitchInView;
		z = zfloat;

		// 0.16667 deg lat  = 10 nm
		// 0.1 approx 5nm
		double d = 0;         // =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // ft 
		double _d = 6080000;  // 1,000 nm
		double relBrg = 0;    // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
		double _relBrg = 180; // Assume the worst

        nrAptsFound = 0;
		Iterator <Apt> it = Gpx.aptList.iterator();
		while (it.hasNext()) {
			Apt currApt;//  = it.next();
			try {
				currApt  = it.next();
			}
		  //catch (ConcurrentModificationException e) {
		  catch (Exception e) {
		  	break;
			}
			
			String wptId = currApt.name;

			double deltaLat = currApt.lat - LatValue;
			double deltaLon = currApt.lon - LonValue;
			//d =  364800 * Math.hypot(deltaLon, deltaLat);  // in ft, 1 deg of lat  6080 * 60 = 364,80 note hypot uses convergenge and is very slow.
			d =  364800 * Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);  // in ft, 1 deg of lat  6080 * 60 = 364,800 

			// Apply selection criteria
			if (d < 5*6080) nrAptsFound++;                                              // always show apts closer then 5nm
			else if ((nrAptsFound < MX_NR_APT) && (d < MX_RANGE*6080))  nrAptsFound++;  // show all others up to MX_NR_APT for MX_RANGE
			else continue;                                                              // we already have all the apts as we wish to display

			relBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat)) - DIValue) % 360;  // the relative bearing to the apt
			if (relBrg >  180) relBrg = relBrg - 360;
			if (relBrg < -180) relBrg = relBrg + 360;

			x1 = (float) ( relBrg * pixPerDegree);
			y1 = (float) (-Math.toDegrees(Math.atan2(MSLValue, d) ) * pixPerDegree);    // we do not take apt elevation into account

			mPolyLine.SetWidth(3); 
			mPolyLine.SetColor(0.99f, 0.50f, 0.99f, 1); //purple'ish
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

			String s = wptId;
			glText.begin( 1.0f, 0.5f, 1.0f , 0, matrix);  // purple
			glText.setScale(2.0f); 	
			glText.drawCY(s, x1, y1 + glText.getCharHeight()/2 );
			glText.end(); 

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
		MX_RANGE = Math.min(MX_RANGE, 99);

        //setMSG(8, String.format("RNG %d   #AP %d", MX_RANGE, nrAptsFound)); // // TODO: 2017-02-07 move to own handlers
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
		String s = String.format("BAT %3.0f", BatteryPct*100) + "%";
		if (BatteryPct > 0.1) glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white

		else glText.begin( 0.0f, 1.0f, 1.0f, 1.0f, matrix ); // red
		glText.setScale(2.0f); 							// 
		//glText.draw(t, -0.97f*pixW2, -0.8f*pixH2 - glText.getCharHeight()/2 );
		//glText.draw(t, -0.97f*pixW2, 0.2f*pixH2 - glText.getCharHeight()/2 );
        //glText.draw(t, -0.70f*pixW2, -0.9f*pixH2 - glText.getCharHeight()/2 );  // next to the G meter
        //glText.draw(s, -0.97f*pixW2, 0.2f*pixH2 - glText.getCharHeight()/2 );
        glText.draw(s, -0.97f * pixW2, (lineA-0.2f)*pixM2 - glText.getCharHeight()/2 ); // as part of the ancillaray group
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

	String mAutoWpt = "YSEN";
    void setAutoWptValue(String wpt)
	{
		mAutoWpt = wpt;
	}

	float mAutoWptBrg;
	void setAutoWptBrg(float brg)
	{
		mAutoWptBrg = brg;
	}
	
	float mSelWptBrg;
	void setSelWptBrg(float brg)
	{
		mSelWptBrg = brg;
	}

    float mSelWptDme;
    void setSelWptDme(float dme)
    {
        mSelWptDme = dme;
    }


    //
    // Display all the relevant auto wpt information with
    // A combo function to replace the individual ones
    //
    float lineB;  // Auto Wpt - Set in onSurfaceChanged
    private void renderAutoWptDetails(float[] matrix)
    {
        //float z, pixPerUnit;
        //pixPerUnit = pixH2/DIInView;
        //z = zfloat;

        String s;
        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
        glText.setScale(2.0f);

        s = String.format("%s", mAutoWpt);
        glText.draw(s, -0.97f * pixW2, (lineB-0.0f)*pixM2 - glText.getCharHeight()/2 );

        s = String.format("BRG  %03.0f", mAutoWptBrg);
        glText.draw(s, -0.97f * pixW2, (lineB-0.1f)*pixM2 - glText.getCharHeight()/2 );

        s = String.format("DME %03.1f", mAutoWptDme);
        glText.draw(s, -0.97f * pixW2, (lineB-0.2f)*pixM2 - glText.getCharHeight()/2 );
        glText.end();
    }

    //-------------------------------------------------------------------------
    // Display all the relevant ancillary device information with
    // A combo function to replace the individual ones
    //
    float lineA = -0.30f;  // Ancillary
    private void renderAncillaryDetails(float[] matrix)
    {
        String s;

        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
        glText.setScale(2.0f);

        s = mGpsStatus; //String.format("%c%03.2f %c%03.2f",  (gps_lat < 0)?  'S':'N' , Math.abs(gps_lat), (gps_lon < 0)? 'W':'E' , Math.abs(gps_lon));
        glText.draw(s, -0.97f * pixW2, (lineA-0.3f)*pixM2 - glText.getCharHeight()/2 );

        /*
        s = String.format("%c%03.2f %c%03.2f",  (LatValue < 0)?  'S':'N' , Math.abs(LatValue), (LonValue < 0)? 'W':'E' , Math.abs(LonValue));
        glText.draw(s, -0.97f * pixW2, (lineA-0.4f)*pixM2 - glText.getCharHeight()/2 );
         */

        s = String.format("RNG %d   #AP %d", MX_RANGE, nrAptsFound);
        glText.draw(s, -0.97f * pixW2, (lineA-0.4f)*pixM2 - glText.getCharHeight()/2 );

        glText.end();

        //setMSG(8, String.format("RNG %d   #AP %d", MX_RANGE, nrAptsFound));
    }


    String mGpsStatus; // = "GPS: 10 / 11";
    public void setGpsStatus(String gpsstatus)
    {
        mGpsStatus = gpsstatus;
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
	float leftC = 0.6f;   // Selected Wpt
    float lineC;          // Selected Wpt - Set in onSurfaceChanged
    float selWptDec;      // = 0.90f * pixH2;
    float selWptInc;      // = 0.74f * pixH2;


	private void renderSelWptValue(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
        pixPerDegree = pixH2 / PPD_DIV;

		z = zfloat;
		// Draw the selecting triangle spinner buttons
		mTriangle.SetColor(0.6f, 0.6f, 0.6f, 0);  // gray
		for (int i = 0; i < 4; i++) {
			float xPos = (leftC + (float) i/10f);
			mTriangle.SetVerts((xPos - 0.02f) * pixW2, selWptDec, z,  //0.02
                               (xPos + 0.02f) * pixW2, selWptDec, z,
                               (xPos + 0.00f) * pixW2, selWptDec + 0.04f * pixM2, z);
                               //(xPos + 0.00f) * pixW2, 0.94f * pixH2, z);
			mTriangle.draw(matrix);

			mTriangle.SetVerts((xPos - 0.02f) * pixW2, selWptInc, z,  //0.02
					           (xPos + 0.02f) * pixW2, selWptInc, z,
                               (xPos + 0.00f) * pixW2, selWptInc - 0.04f * pixM2, z);
         	    			   //(xPos + 0.00f) * pixW2, 0.70f * pixH2, z);
			mTriangle.draw(matrix);

			// Draw the individual select characters  
			if (mWptSelName != null) {
				glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
				glText.setScale(3f);
				String s = String.format("%c", mWptSelName.charAt(i));
				//glText.drawCX(s, xPos * pixW2, 0.81f*pixH2 - glText.getCharHeight()/2 );
                glText.drawCX(s, xPos * pixW2, ((selWptInc + selWptDec) / 2) - (glText.getCharHeight() / 2));
                glText.end();
			} 
		}
		
		// Calculate the relative bearing to the selected wpt
		double deltaLat = mWptSelLat - LatValue;
		double deltaLon = mWptSelLon - LonValue;
		//double d =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // in ft  very slow see comment elsewhere
		double dme =  364800 * Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);  //faster version see comment in renderApt
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
		setFlightDirector(displayFlightDirector, commandPitch, (float) relBrg);

        // BRG
        double absBrg = (Math.toDegrees(Math.atan2(deltaLon, deltaLat))) % 360;
        while (absBrg < 0) absBrg += 360;

        // Setting data in this renderer does not make
        // much logical sense. This could be re-factored
        // Perhaps introduce a new function to explicitly
        // handle "navigation"?
        setSelWptBrg((float) absBrg);
        setSelWptDme((float) dme);

        // Display the Name details and also BRG and DME
        renderSelWptDetails(matrix);


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

    private void renderSelWptDetails(float[] matrix)
    {
        //float lineC =  0.35f;  // landscape
        //float lineC =  -0.50f;  // Also OK for Landscape
        //float lineC =  selWptInc / pixM2 - 0.35f;  //

        glText.begin( 1.0f, 1f, 1.0f, 1.0f, matrix ); //
        glText.setScale(2.1f);
        String s = mWptSelComment;
        //glText.draw(s, leftC * pixW2, 0.5f*pixH2 - glText.getCharHeight()/2 );
        //glText.draw(s, leftC * pixW2, (lineC+0.15f)*pixM2 - glText.getCharHeight()/2 );
        glText.draw(s, leftC * pixW2, (lineC+0.0f)*pixM2 - glText.getCharHeight()/2 );
        glText.end();

        // DME
        String t = String.format("DME %03.1f", mSelWptDme / 6080);
        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
        glText.setScale(2.5f); 							//
        //glText.draw(t, leftC*pixW2, (lineC-0.05f)*pixH2 - glText.getCharHeight()/2 );
        glText.draw(t, leftC*pixW2, (lineC-0.2f)*pixM2 - glText.getCharHeight()/2 );
        glText.end();

        // BRG
        t = String.format("BRG  %03.0f", mSelWptBrg);
        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
        glText.setScale(2.5f); 							//
        //glText.draw(t, leftC*pixW2, (lineC+0.05f)*pixH2 - glText.getCharHeight()/2 );
        glText.draw(t, leftC*pixW2, (lineC-0.1f)*pixM2 - glText.getCharHeight()/2 );
        glText.end();
    }



    float selAltInc; // = -0.90f * pixH2;
    float selAltDec; // = -0.74f * pixH2;
	private void renderSelAltValue(float[] matrix)
	{
		float z, pixPerDegree, x1, y1;
		//pixPerDegree = pixM2 / pitchInView;
		//pixPerDegree = pixM2 / PPD_DIV;
		z = zfloat;

		// Draw the selecting triangle spinner buttons
		mTriangle.SetColor(0.6f, 0.6f, 0.6f, 0);  // gray
		for (int i = 0; i < 3; i++) {
			float xPos = (leftC + (float) i/10f);
			mTriangle.SetVerts((xPos - 0.02f) * pixW2, selAltDec, z,  //0.02
					           (xPos + 0.02f) * pixW2, selAltDec, z,
                               (xPos + 0.00f) * pixW2, selAltDec + 0.04f * pixM2, z);
					           //(xPos + 0.00f) * pixW2, -0.94f * pixH2, z);
			mTriangle.draw(matrix);

			mTriangle.SetVerts((xPos - 0.02f) * pixW2, selAltInc, z,  //0.02
					           (xPos + 0.02f) * pixW2, selAltInc, z,
                               (xPos + 0.00f) * pixW2, selAltInc - 0.04f * pixM2, z);
					           //(xPos + 0.00f) * pixW2, -0.70f * pixH2, z);
			mTriangle.draw(matrix);

			// Draw the individual select characters  
			if (mAltSelName != null) { 
				glText.begin( 1.0f, 0.8f, 1.0f, 1.0f, matrix ); //
				glText.setScale(3f);
				String s = String.format("%c", mAltSelName.charAt(i));
				//glText.drawCX(s, xPos * pixW2, -0.83f*pixH2 - glText.getCharHeight()/2 );
                glText.drawCX(s, xPos * pixW2, ((selAltInc + selAltDec) / 2) - glText.getCharHeight()/2 );
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


	//---------------------------------------------------------------------------
	// Handle the tap events
	//
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
        //selWptDec
        if      (Math.abs(mY - selWptDec / pixH2) < 0.10) inc = -1;
        else if (Math.abs(mY - selWptInc / pixH2) < 0.10) inc = +1;
		//if      (Math.abs(mY - 0.9) < 0.15) inc = -1;
		//else if (Math.abs(mY - 0.6) < 0.15) inc = +1;

		// Determine if we are counting up or down?
		// altitude number
        else if (Math.abs(mY - selAltDec / pixH2) < 0.10) ina = -1;
        else if (Math.abs(mY - selAltInc / pixH2) < 0.10) ina = +1;
		//else if (Math.abs(mY + 0.6) < 0.15) ina = -1;
		//else if (Math.abs(mY + 0.9) < 0.15) ina = +1;

		// Determine which digit is changing
		for (int i = 0; i < 4; i++) {
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

	void setAutoWptDme(float dme)
	{
		mAutoWptDme = dme;
	}

	float mAutoWptRlb; 
	private void renderAutoWptRlb(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		String t = String.format("RLB  %03.0f", mAutoWptRlb);
		glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix ); // white
		glText.setScale(2.0f); 							//
        glText.draw(t, -0.97f*pixW2, -0.7f*pixM2 - glText.getCharHeight()/2 );            // Draw  String
		glText.end();
	}

	void setAutoWptRelBrg(float rlb)
	{
		mAutoWptRlb = rlb;
	}






	static String mMsg[] = new String[20]; //10
	static float lineNr;
	void renderMSGValue(float[] matrix)
	{
		float z, pixPerUnit;

		pixPerUnit = pixH2/DIInView;
		z = zfloat;

		//for (int i = 0; i  < mMsg.length;  i++) {
        for (int i = 0; i  < mMsg.length;  i++) {
            String t = mMsg[i];
			if (t != null) {
				glText.begin( 1.0f, 1.0f, 1.0f, 1.0f, matrix );
				glText.setScale(2.0f); 							 
				glText.draw(t, -0.97f*pixW2, (float) (i-10) / 10f * pixM2 - glText.getCharHeight()/2 );
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
		case REMOTE_INDICATOR:
			displayRMI = value;
			break;
		case AIRPORT:
			displayAirport = value;
			break;

		}
	}

	
	//-----------------------------------------------------------------------------
	//	
	//  Remote Indicator
	//
	//-----------------------------------------------------------------------------
	// todo - render fixed RMI markers
	float roseScale = 0.34f; //0.30f; //0.33f; //0.5f

    private void renderFixedCompassMarkers(float[] matrix)
    {
        float tapeShade = 0.6f;
        int i;
        //float innerTic, outerTic, iPix;
        float z, sinI, cosI;
        float roseRadius = roseScale * pixM2;

        z = zfloat;

        mLine.SetWidth(2);  //3
        //mLine.SetColor(tapeShade, tapeShade, tapeShade, 1);  // grey
        //mLine.SetColor(1, 1, 0, 1);  // light yellow
        mLine.SetColor(0.9f, 0.9f, 0.9f, 1);
        for (i = 0; i <= 315; i=i+45) {

            if (i % 90 == 0) mLine.SetWidth(4);
            else mLine.SetWidth(2);

            sinI = UTrig.isin((450 - i) % 360);
            cosI = UTrig.icos((450 - i) % 360);
            mLine.SetVerts(
                    1.005f * roseRadius * cosI, 1.005f * roseRadius * sinI, z,
                    1.120f * roseRadius * cosI, 1.120f * roseRadius * sinI, z
            );
            mLine.draw(matrix);
        }

        // Apex marker
        float pixPerDegree;
        //pixPerDegree = pixM2 / pitchInView;
        pixPerDegree = pixM2 / PPD_DIV;

        mTriangle.SetColor(0.9f, 0.9f, 0.9f, 0);
        mTriangle.SetVerts(
                0.035f * pixM2, 1.120f * roseRadius, z,
               -0.035f * pixM2, 1.120f * roseRadius, z,
                0.0f,    1.00f * roseRadius, z);
        mTriangle.draw(matrix);


    }

    /*
		float z, pixPerDegree;
		z = zfloat;
		//pixPerDegree = pixM2 / pitchInView;					// Put the markers in open space at zero pitch
		pixPerDegree = pixM2 / PPD_DIV;							// Put the markers in open space at zero pitch

		mTriangle.SetColor(0.9f, 0.9f, 0.9f, 0);
        mTriangle.SetVerts(
            0.035f * pixM2, 13.5f * pixPerDegree, z,
        -0.035f * pixM2, 13.5f * pixPerDegree, z,
        0.0f, 15f * pixPerDegree, z);
        mTriangle.draw(matrix);
    */

	
	private void renderCompassRose(float[] matrix)
	{
		float tapeShade = 0.6f;
		int i, j;
		//float innerTic, outerTic, iPix;
		float z, sinI, cosI;
		String t;
		float roseRadius = roseScale * pixM2;

		z = zfloat;

		mLine.SetWidth(2);  //3
		mLine.SetColor(tapeShade, tapeShade, tapeShade, 1);  // grey
		
		// The rose degree tics
        for (i = 0; i <= 330; i=i+30) {
			sinI = UTrig.isin( (450-i) % 360 );  
			cosI = UTrig.icos( (450-i) % 360 );
			mLine.SetVerts(
					0.9f * roseRadius * cosI, 0.9f * roseRadius * sinI, z,
					1.0f * roseRadius * cosI, 1.0f * roseRadius * sinI, z
					);
			mLine.draw(matrix);
			
			glText.begin( tapeShade, tapeShade, tapeShade, 1.0f, matrix ); // grey
			glText.setScale(1.5f); 
			float angleDeg = 90-i;
			switch( i ) {
				case 0 :  
					t = "N";
					angleDeg = -i;
					glText.begin( 1, 1, 1, 1.0f, matrix ); // white
					glText.setScale(2.0f); 
					break;
				case 30 :	t = "3";	break;
				case 60 : t = "6";	break;
				case 90 : 
					t = "E"; 
					angleDeg = -i;
					glText.begin( 1, 1, 1, 1.0f, matrix ); // white
					glText.setScale(1.5f); 
					break;
				case 120 : t = "12";	break;
				case 150 : t = "15";	break;
				case 180 : 
					t = "S";	
					angleDeg = -i;
					glText.begin( 1, 1, 1, 1.0f, matrix ); // white
					glText.setScale(1.5f); 
					break;
				case 220 : t = "21";	break;
				case 240 : t = "24";	break;
				case 270 : 
					t = "W";	
					angleDeg = -i;
					glText.begin( 1, 1, 1, 1.0f, matrix ); // white
					glText.setScale(1.5f); 
					break;
				case 300 : t = "30";	break;
				case 330 : t = "33";	break;
				default : t = ""; break;
			}

			//glText.begin( tapeShade, tapeShade, tapeShade, 1.0f, matrix ); // white
			//glText.setScale(1.5f); // seems to have a weird effect here?
			glText.drawC(t, 0.75f * roseRadius * cosI, 0.75f * roseRadius * sinI, angleDeg); //90-i             
			glText.end();                                   
			for (j = 10; j <=20; j=j+10) {
                sinI = UTrig.isin( (i+j) );
	            cosI = UTrig.icos( (i+j) );
				mLine.SetVerts(
						0.93f * roseRadius * cosI, 0.93f * roseRadius * sinI, z,
						1.00f * roseRadius * cosI, 1.00f * roseRadius * sinI, z
						);
				mLine.draw(matrix);
			}
			for (j = 5; j <=25; j=j+10) {
	            sinI = UTrig.isin( (i+j) );
	            cosI = UTrig.icos( (i+j) );
				mLine.SetVerts(
						0.96f * roseRadius * cosI, 0.96f * roseRadius * sinI, z,
						1.00f * roseRadius * cosI, 1.00f * roseRadius * sinI, z
						);
				mLine.draw(matrix);
			}
		}
	}

    //-------------------------------------------------------------------------
    // Render the two RMI needles
    //
	void renderBearing(float[] matrix)
	{
		float z, sinI, cosI, _sinI, _cosI;
		float roseRadius = roseScale * pixM2;

		z = zfloat;
		
		//
		// Bearing to Automatic Waypoint
		//
		mLine.SetWidth(5); //3);
		mLine.SetColor(0.7f, 0.7f, 0.0f, 1);  // yellow 

        sinI = 0.9f*UTrig.isin(90-(int)mAutoWptBrg);
        cosI = 0.9f*UTrig.icos(90-(int)mAutoWptBrg);
        _sinI = 0.5f*UTrig.isin(90-(int)mAutoWptBrg);
        _cosI = 0.5f*UTrig.icos(90-(int)mAutoWptBrg);
    
        //tail
		mLine.SetVerts(
				-roseRadius * _cosI, -roseRadius * _sinI, z,
				-roseRadius * cosI,  -roseRadius * sinI, z
				);
		mLine.draw(matrix);
        // head
		// point L
        _sinI = 0.80f * UTrig.isin(90-(int)mAutoWptBrg + 6);
        _cosI = 0.80f * UTrig.icos(90-(int)mAutoWptBrg + 6);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * cosI,  roseRadius * sinI, z
				);
		mLine.draw(matrix);
		
		// prallel
		float parr = 0.40f;
		
        float __sinI = parr*UTrig.isin(90-(int)mAutoWptBrg + 12);
        float __cosI = parr*UTrig.icos(90-(int)mAutoWptBrg + 12);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * __cosI,  roseRadius * __sinI, z
				);
		mLine.draw(matrix);
    
		// point R
		_sinI = 0.80f * UTrig.isin(90-(int)mAutoWptBrg - 5);
        _cosI = 0.80f * UTrig.icos(90-(int)mAutoWptBrg - 5);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * cosI,  roseRadius * sinI, z
				);
		mLine.draw(matrix);

		// prallel
         __sinI = parr*UTrig.isin(90-(int)mAutoWptBrg - 12);
         __cosI = parr*UTrig.icos(90-(int)mAutoWptBrg - 12);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * __cosI,  roseRadius * __sinI, z
				);
		mLine.draw(matrix);

		//
		// Bearing to Selected Waypoint
		//
		mLine.SetWidth(8); //6
		//mLine.SetColor(0, 0.7f, 0, 1);  // green
		mLine.SetColor(0.99f, 0.5f, 0.99f, 1); // purple'ish

        sinI = 0.9f*UTrig.isin(90-(int)mSelWptBrg);
        cosI = 0.9f*UTrig.icos(90-(int)mSelWptBrg);
        _sinI = 0.5f*UTrig.isin(90-(int)mSelWptBrg);
        _cosI = 0.5f*UTrig.icos(90-(int)mSelWptBrg);

        //tail
		mLine.SetVerts(
				-roseRadius * _cosI, -roseRadius * _sinI, z,
				-roseRadius * cosI,  -roseRadius * sinI, z
				);
		mLine.draw(matrix);
		//head
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * cosI,  roseRadius * sinI, z
				);
		mLine.draw(matrix);

		mLine.SetWidth(6); //3
		// point
        _sinI = 0.80f * UTrig.isin(90-(int)mSelWptBrg + 9); //6
        _cosI = 0.80f * UTrig.icos(90-(int)mSelWptBrg + 9);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * cosI,  roseRadius * sinI, z
				);
		mLine.draw(matrix);
		_sinI = 0.80f * UTrig.isin(90-(int)mSelWptBrg - 8);  //5
        _cosI = 0.80f * UTrig.icos(90-(int)mSelWptBrg - 8);
		mLine.SetVerts(
				roseRadius * _cosI, roseRadius * _sinI, z,
				roseRadius * cosI,  roseRadius * sinI, z
				);
		mLine.draw(matrix);
	}
	

	void renderBearingTxt(float[] matrix)
	{
		float roseRadius = roseScale * pixM2;
		float scale = 1.6f;  // not sure why this is > 1.0 Does not really make sense
                             // it is somehow related to which matrix it is drawn on
		
		//
		// Bearing to Selected Waypoint
		//
		glText.begin( 0.99f, 0.5f, 0.99f, 1.0f, matrix ); // purple'ish
		glText.setScale(scale);
		glText.drawC(mWptSelName , 0, 0.12f*roseRadius, 0);             
		glText.end();                                   
		
		//
		// Bearing to Automatic Waypoint
		//
		glText.begin( 0.7f, 0.7f, 0, 1.0f, matrix ); // yellow
		glText.setScale(scale);
		glText.drawC(mAutoWpt , 0, -0.12f*roseRadius, 0);             
		glText.end();                                   
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



