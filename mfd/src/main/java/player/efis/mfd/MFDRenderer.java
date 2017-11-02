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

package player.efis.mfd;

import java.util.Iterator;

import player.efis.common.AirspaceClass;
import player.efis.common.DemColor;
import player.efis.common.DemGTOPO30;
import player.efis.common.AircraftData;
import player.efis.common.Apt;
import player.efis.common.EFISRenderer;
import player.efis.common.Gpx;
import player.efis.common.OpenAir;
import player.efis.common.OpenAirPoint;
import player.efis.common.OpenAirRec;
import player.efis.common.prefs_t;
import player.gles20.Line;
import player.gles20.PolyLine;
import player.gles20.Polygon;
import player.gles20.Square;
import player.gles20.Triangle;
import player.ulib.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import player.gles20.GLText;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MFDRenderer extends EFISRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = "MFDRenderer";
    public MFDRenderer(Context context)
    {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle();
        mSquare = new Square();
        mLine = new Line();
        mPolyLine = new PolyLine();
        mPolygon = new Polygon();

        // Create the GLText
        glText = new GLText(context.getAssets());
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        if (displayMirror)
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);  // Mirrored View
        else
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);   // Normal View

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        zfloat = 0;

        if (displayDEM && !fatFingerActive) renderDEMTerrain(mMVPMatrix);  // fatFingerActive just for perfromance

        // Remote Magnetic Inidicator - RMI
        if (displayRMI) {
            float xlx;
            float xly;

            // Add switch for orientation
            if (Layout == layout_t.LANDSCAPE) {
                // Landscape
                xlx = 0; //-0.00f * pixW2;
                xly = -1.80f * pixH2;  //0.45f
                roseScale = 1.9f;
                GLES20.glViewport(0, pixH2, pixW, pixH); 
            }
            else {
                //Portrait
                xlx = 0; //-0.00f * pixW2;
                xly = -0.20f * pixH2;  //0.45f
                roseScale = 1.9f; //0.45f; //0.50f;
            }

            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            // Create a rotation for the RMI
            Matrix.setRotateM(mRmiRotationMatrix, 0, DIValue, 0, 0, 1);  // compass rose rotation
            Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);
            renderFixedCompassMarkers(mMVPMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            renderCompassRose(rmiMatrix);
            GLES20.glViewport(0, 0, pixW, pixH);  // fullscreen
        }

        if (displayAirspace) renderAirspace(mMVPMatrix);
        if (displayAirport) renderAPT(mMVPMatrix);  // must be on the same matrix as the Pitch

        //-----------------------------
        if (displayFlightDirector) renderDctTrack(mMVPMatrix);
        renderMapScale(mMVPMatrix);  // do before the DI

        if (displayTape == true) {
            renderFixedALTMarkers(mMVPMatrix);
            renderFixedRADALTMarkers(mMVPMatrix); // AGL
            renderFixedASIMarkers(mMVPMatrix);
            //renderVSIMarkers(mMVPMatrix);
            renderFixedDIMarkers(mMVPMatrix);
            renderHDGValue(mMVPMatrix);
        }

        //-----------------------------
        if (displayInfoPage) {
            renderAncillaryDetails(mMVPMatrix);
            renderBatteryPct(mMVPMatrix);

            // North Que
            {
                float xlx = -0.82f * pixW2;
                float xly = (lineAncillaryDetails +0.10f) * pixM2;

                Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
                Matrix.setRotateM(mRmiRotationMatrix, 0, DIValue, 0, 0, 1);  // compass rose rotation
                Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);
                Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);
                renderNorthQue(rmiMatrix);
            }
        }
        if (bDemoMode) renderDemoMode(mMVPMatrix);

        renderACSymbol(mMVPMatrix);

        // Do this last so that every else wil be dimmed for fatfinger entry
        if (displayFlightDirector) {
            //renderDctTrack(mMVPMatrix);
            renderSelWptDetails(mMVPMatrix);
            renderSelWptValue(mMVPMatrix);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        // Adjust the viewport based on geometry changes, such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        // this projection matrix is applied to  object coordinates in the onDrawFrame() method
        //b2 Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 7); // - this apparently fixed for the Samsung S2?

        //b2 start
        // Capture the window scaling for use by the rendering functions
        pixW = width;
        pixH = height;
        pixW2 = pixW / 2;
        pixH2 = pixH / 2;

        if (pixW < pixH) pixM = pixW;
        else pixM = pixH;

        // because the ascpect ratio is different in landscape and portrait (due to menu bar)
        // we just fudge it as 85% throughout,  looks OK in landscape as well
        pixM = pixM * 88 / 100;
        pixM2 = pixM / 2;

        setSpinnerParams(); // Set up the spinner locations and SelWpt display

        // Set the window size specific scales, positions and sizes (nothing dynamic yet...)
        pitchInView = 25.0f;      // degrees to display from horizon to top of viewport
        IASInView = 40.0f;      // IAS units to display from center to top of viewport
        MSLInView = 300.0f;      // IAS units to display from center to top of viewport

        // this projection matrix is applied to  object coordinates in the onDrawFrame() method
        float ratio = (float) width / height;
        //Matrix.frustumM(mProjectionMatrix, 0, -ratio*pixH2, ratio*pixH2, -pixH2, pixH2, 3f, 7f); // all the rest
        Matrix.frustumM(mProjectionMatrix, 0, -ratio * pixH2, ratio * pixH2, -pixH2, pixH2, 2.99f, 75f); //hack for Samsung G2

        // Create the GLText
        // --debug glText = new GLText(context.getAssets()); - moved to onsurfacecreated

        // Load the font from file (set size + padding), creates the texture
        // NOTE: after a successful call to this the font is ready for rendering!
        //glText.load( "Roboto-Regular.ttf", 14, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
        glText.load("square721_cn_bt_roman.ttf", pixM * 14 / 734, 2, 2);  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }



    //-------------------------------------------------------------------------
    //
    //
    public void setSpinnerParams()
    {
        // This code determines where the spinner control
        // elements are displayed. Used by WPT and ALT
        if (Layout == layout_t.LANDSCAPE) {
            // Landscape --------------
            lineAutoWptDetails = 0.00f;
            lineAncillaryDetails = -0.30f;

            if (fatFingerActive) {
                selWptDec = 0.75f * pixH2;
                selWptInc = 0.45f * pixH2;
                selAltDec = -0.45f * pixH2;
                selAltInc = -0.75f * pixH2;

                lineC = 0.2f;
                leftC = -0.55f;
                spinnerStep = 0.25f;
                spinnerTextScale = 2.0f;
            }
            else {
                // Top
                selWptDec = 0.90f * pixH2;
                selWptInc = 0.74f * pixH2;
                selAltDec = -0.74f * pixH2;
                selAltInc = -0.90f * pixH2;

                lineC = 0.50f;
                leftC = 0.6f;
                spinnerStep = 0.1f;
                spinnerTextScale = 1f;
            }
        }
        else {
            // Portrait ---------------
            lineAutoWptDetails = -0.60f;
            lineAncillaryDetails = -0.85f;

            if (fatFingerActive) {
                selWptDec = 0.7f * pixH2;
                selWptInc = 0.4f * pixH2;
                selAltDec = -0.4f * pixH2;
                selAltInc = -0.7f * pixH2;

                lineC = 0.15f;
                leftC = -0.75f;
                spinnerStep = 0.5f;
                spinnerTextScale = 2f;
            }
            else {
                selWptDec = -0.60f * pixH2; //-0.30f * pixH2;
                selWptInc = -0.71f * pixH2; //-0.41f * pixH2;
                selAltDec = -0.80f * pixH2;
                selAltInc = -0.91f * pixH2;

                lineC = -0.82f; // lineC = -0.55f;
                leftC = 0.6f;
                spinnerStep = 0.1f;
                spinnerTextScale = 1f;
            }
        }
    }

	// TODO: use slide to position and get rid of the top/right vars
    //-------------------------------------------------------------------------
    // RadAlt Indicator (AGL)
    //
    protected void renderFixedRADALTMarkers(float[] matrix)
    {
        float z = zfloat;
        String t;

        float top = -0.3f * pixM2;
        float right = 0.99f * pixW2;
        float left = right - 0.35f * pixM2;


        // The tapes are positioned left & right of the roll circle, occupying the space based
        // on the vertical dimension, from .6 to 1.0 pixM2.  This makes the basic display
        // square, leaving extra space outside the edges for terrain which can be clipped if required.

        // Radio Altimeter (AGL) Display

        // Do a dummy glText so that the Heights are correct for the masking box
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix); // white
        glText.setScale(2.5f);  //was 1.5
        glText.end();

        // Mask over the moving tape for the value display box
        mSquare.SetColor(backShade, backShade, backShade, 1); //black
        mSquare.SetWidth(2);
        {
            float[] squarePoly = {
                    right, top - glText.getCharHeight(), z,
                    right, top + glText.getCharHeight(), z,
                    left, top + glText.getCharHeight(), z,
                    left, top - glText.getCharHeight(), z,
            };
            mSquare.SetVerts(squarePoly);
            mSquare.draw(matrix);
        }

        // if we are below the preset, show the warning chevrons
        final float CevronAGL = 1000.0f;
        if (AGLValue < CevronAGL) {
            float slant = 0.06f * pixM2;
            float step = 0.04f * pixM2;
            float i;
            // moving yellow chevrons
            mLine.SetColor(0.4f, 0.4f, 0.0f, 0.5f); //yellow
            mLine.SetWidth(8); //4
            for (i = left; i < right - (float)AGLValue/CevronAGL*(right-left) - step; i = i + step) {
                mLine.SetVerts(
                        i, top + glText.getCharHeight(), z,
                        slant + i, top - glText.getCharHeight(), z
                );
                mLine.draw(matrix);
            }

            // left filler
            mLine.SetVerts(
                    left, top, z,
                    left + step/2, top - glText.getCharHeight(), z
            );
            mLine.draw(matrix);

            // right filler
            mLine.SetVerts(
                    i, top + glText.getCharHeight(), z,
                    slant/2 + i-2, top, z
            );
            mLine.draw(matrix);

            /* this seems a little bit over the top
            // bar
            mLine.SetVerts(
                    spinnerStep + i, top + glText.getCharHeight(), z,
                    spinnerStep + i, top - glText.getCharHeight(), z
            );
            mLine.draw(matrix);
            */
        }

        int aglAlt = Math.round((float) this.AGLValue / 10) * 10;  // round to 10
        // draw the tape text in mixed sizes
        // to clearly show the thousands
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix); // white
        t = Integer.toString(aglAlt / 1000);
        float margin;

        // draw the thousands digits larger
        glText.setScale(3.5f);  //3  2.5
        if (aglAlt >= 1000) glText.draw(t, left + 0.03f*pixM2, top - glText.getCharHeight() / 2);
        if (aglAlt < 10000)
            margin = 0.6f * glText.getCharWidthMax(); // because of the differing sizes
        else
            margin = 1.1f * glText.getCharWidthMax();                    // we have to deal with the margin ourselves

        // draw the hundreds digits smaller
        t = String.format("%03.0f", (float) aglAlt % 1000);
        glText.setScale(2.5f); // was 2.5
        glText.draw(t, left + 0.03f*pixM2 + margin, top - glText.getCharHeight() / 2);
        glText.end();

        {
            mPolyLine.SetColor(foreShade, foreShade, foreShade, 1); //white
            mPolyLine.SetWidth(2);
            float[] vertPoly = {
                    right, top - glText.getCharHeight(), z,
                    right, top + glText.getCharHeight(), z,
                    left, top + glText.getCharHeight(), z,
                    left, top - glText.getCharHeight(), z,
                    right, top - glText.getCharHeight(), z
            };

            mPolyLine.VertexCount = 5;
            mPolyLine.SetVerts(vertPoly);
            mPolyLine.draw(matrix);
        }

    }

	// TODO: use slide to position and get rid of the top/right vars
    //-------------------------------------------------------------------------
    // Altimeter Indicator
    //
    protected void renderFixedALTMarkers(float[] matrix)
    {
        float z = zfloat;
        String t;

        float right = 0.99f * pixW2;
        float left = right - 0.35f * pixM2;
        float apex = left - 0.05f * pixM2;

        // The tapes are positioned left & right of the roll circle, occupying the space based
        // on the vertical dimension, from .6 to 1.0 pixM2.  This makes the basic display
        // square, leaving extra space outside the edges for terrain which can be clipped if required.

        // Altimeter Display

        // Do a dummy glText so that the Heights are correct for the masking box
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix); // white
        glText.setScale(2.5f);  //was 1.5
        glText.end();

        // Mask over the moving tape for the value display box
        mSquare.SetColor(backShade, backShade, backShade, 1); //black
        mSquare.SetWidth(2);
        {
            float[] squarePoly = {
                    right, -glText.getCharHeight(), z,//+0.1f,
                    right,  glText.getCharHeight(), z,//+0.1f,
                    left,   glText.getCharHeight(), z,//+0.1f,
                    left,  -glText.getCharHeight(), z,//+0.1f
            };
            mSquare.SetVerts(squarePoly);
            mSquare.draw(matrix);
        }

        int mslAlt = Math.round((float) this.MSLValue / 10) * 10;  // round to 10
        // draw the tape text in mixed sizes
        // to clearly show the thousands
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix); // white
        t = Integer.toString(mslAlt / 1000);
        float margin;

        // draw the thousands digits larger
        glText.setScale(3.5f);  //3  2.5
        if (mslAlt >= 1000) glText.draw(t, left + 0.03f*pixM2, -glText.getCharHeight() / 2);
        if (mslAlt < 10000)
            margin = 0.6f * glText.getCharWidthMax(); // because of the differing sizes
        else
            margin = 1.1f * glText.getCharWidthMax(); // we have to deal with the margin ourselves

        // draw the hundreds digits smaller
        t = String.format("%03.0f", (float) mslAlt % 1000);
        glText.setScale(2.5f); // was 2.5
        glText.draw(t, left + 0.03f*pixM2 + margin, -glText.getCharHeight() / 2);
        glText.end();

        mTriangle.SetColor(backShade, backShade, backShade, 1);  //black
        mTriangle.SetVerts(
                left, glText.getCharHeight() / 2, z,
                apex, 0.0f, z,
                left, -glText.getCharHeight() / 2, z
        );
        mTriangle.draw(mMVPMatrix);

        {
            mPolyLine.SetColor(foreShade, foreShade, foreShade, 1); //white
            mPolyLine.SetWidth(2);
            float[] vertPoly = {
                    right, -glText.getCharHeight(), z,
                    right, glText.getCharHeight(), z,
                    left, glText.getCharHeight(), z,
                    left, glText.getCharHeight() / 2, z,
                    apex, 0.0f, z,
                    left, -glText.getCharHeight() / 2, z,
                    left, -glText.getCharHeight(), z,
                    right, -glText.getCharHeight(), z
            };

            mPolyLine.VertexCount = 8;
            mPolyLine.SetVerts(vertPoly);
            mPolyLine.draw(matrix);
        }
    }

    protected void renderALTMarkers(float[] matrix)
    {
        int i, j;
        float innerTic, midTic, outerTic, z, pixPerUnit, iPix;

        //pixPerUnit = pixM2 / MSLInView; //b2 landscape
        pixPerUnit = pixH2 / MSLInView; //portrait
        z = zfloat;

        innerTic = 0.70f * pixM2;    // inner & outer are relative to the vertical scale line
        midTic = 0.75f * pixM2;
        outerTic = 0.80f * pixM2;

        // The numbers & tics for the tape
        for (i = MSLMaxDisp; i >= MSLMinDisp; i = i - 100) {
            // Ugly hack but is does significantly improve performance.
            if (i > MSLValue + 1.00 * MSLInView) continue;
            if (i < MSLValue - 1.50 * MSLInView) continue;

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
            glText.begin(tapeShade, tapeShade, tapeShade, 1.0f, matrix); // grey
            String t = Integer.toString(i / 1000);
            float margin;

            // draw the thousands digits larger
            glText.setScale(3.0f);
            //glText.setScale(4.0f, 2.5f);
            if (i >= 1000) glText.draw(t, outerTic, iPix - glText.getCharHeight() / 2);

            if (i < 10000)
                margin = 0.6f * glText.getCharWidthMax();  // because of the differing sizes
            else
                margin = 1.1f * glText.getCharWidthMax();            // we have to deal with the margin ourselves

            // draw the hundreds digits smaller
            t = String.format("%03.0f", (float) i % 1000);
            glText.setScale(2.0f); // was 1.5
            glText.draw(t, outerTic + margin, iPix - glText.getCharHeight() / 2);
            glText.end();

            for (j = i + 20; j < i + 90; j = j + 20) {
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
                innerTic, MSLMinDisp, z,
                innerTic, (MSLMaxDisp + 100) * pixPerUnit, z
        );
        mLine.draw(matrix);
    }

	// TODO: use slide to position and get rid of the top/right vars
    //-------------------------------------------------------------------------
    // Airspeed Indicator
    //
    protected void renderFixedASIMarkers(float[] matrix)
    {
        float z = zfloat;
        String t;

        float left = -0.99f * pixW2;
        float right = left + 0.3f * pixM2;
        float apex = right + 0.05f * pixM2;

        // The tapes are positioned left & right of the roll circle, occupying the space based
        // on the vertical dimension, from .6 to 1.0 pixH2.  This makes the basic display
        // square, leaving extra space outside the edges for terrain which can be clipped if reqd.

        // Do a dummy glText so that the Heights are correct for the masking box
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix); // white
        glText.setScale(2.5f); // was 1.5
        glText.end();

        // Mask over the moving tape for the value display box
        mSquare.SetColor(backShade, backShade, backShade, 1); //black
        mSquare.SetWidth(2);
        {
            float[] squarePoly = {
                    left, -glText.getCharHeight(), z,
                    left,  glText.getCharHeight(), z,
                    right,  glText.getCharHeight(), z,
                    right, -glText.getCharHeight(), z,
            };
            mSquare.SetVerts(squarePoly);
            mSquare.draw(matrix);
        }

        mTriangle.SetColor(backShade, backShade, backShade, 1);  //black
        mTriangle.SetVerts(
                right, glText.getCharHeight() / 2, z,
                apex, 0.0f, z,
                right, -glText.getCharHeight() / 2, z
        );
        mTriangle.draw(mMVPMatrix);

        {
            mPolyLine.SetColor(foreShade, foreShade, foreShade, 1); //white
            mPolyLine.SetWidth(2);
            float[] vertPoly = {
                    left, -glText.getCharHeight(), z,
                    left,  glText.getCharHeight(), z,
                    right, glText.getCharHeight(), z,
                    right, glText.getCharHeight() / 2, z,
                    apex, 0.0f, z,
                    right, -glText.getCharHeight() / 2, z,
                    right, -glText.getCharHeight(), z,
                    left,  -glText.getCharHeight(), z
            };

            mPolyLine.VertexCount = 8;
            mPolyLine.SetVerts(vertPoly);
            mPolyLine.draw(matrix);
        }
        t = Integer.toString(Math.round(IASValue));
        glText.begin(foreShade, foreShade, foreShade, 1.0f, matrix);     // white
        glText.setScale(3.5f);                            // was 2.5
        glText.drawC(t, left + 0.25f*pixM2, glText.getCharHeight() / 2);
        glText.end();
    }




    // This may be a differnt name?
    //-------------------------------------------------------------------------
    // Airports / Waypoints
    //

    //
    // Variables specific to render APT
    //
    private final int MX_NR_APT = 20;
    private int MX_RANGE = 200;    //nm
    private int Aptscounter = 0;
    private int nrAptsFound;
    public float mMapZoom = 20; // Zoom multiplier for map. 1 (max out) to 200 (max in)


    private int Airspacecounter = 0;
    private int nrAirspaceFound;

    protected void renderAPT(float[] matrix)
    {
        float z, pixPerDegree, x1, y1;
        float radius = 5;

        pixPerDegree = pixM / pitchInView;
        z = zfloat;

        // 0.16667 deg lat  = 10 nm
        // 0.1 approx 5nm
        float dme;         
        float _dme = 1000;  
        float aptRelBrg;   // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));

        nrAptsFound = 0;
        Iterator<Apt> it = Gpx.aptList.iterator();
        while (it.hasNext()) {
            Apt currApt;//  = it.next();
            try {
                currApt = it.next();
            }
            //catch (ConcurrentModificationException e) {
            catch (Exception e) {
                break;
            }

            String wptId = currApt.name;
            dme = UNavigation.calcDme(LatValue, LonValue, currApt.lat, currApt.lon); // in ft

            // Apply selection criteria
            if (dme < 5)
                nrAptsFound++;                                              // always show apts closer then 5nm
            else if ((nrAptsFound < MX_NR_APT) && (dme < MX_RANGE))
                nrAptsFound++;  // show all others up to MX_NR_APT for MX_RANGE
            else
                continue;  // we already have all the apts as we wish to display

            aptRelBrg = UNavigation.calcRelBrg(LatValue, LonValue, currApt.lat, currApt.lon, DIValue);
            x1 = mMapZoom * (dme * UTrig.icos(90-(int)aptRelBrg));
            y1 = mMapZoom * (dme * UTrig.isin(90-(int)aptRelBrg));

            mPolyLine.SetWidth(3);
            mPolyLine.SetColor(0.99f, 0.50f, 0.99f, 1); //purple'ish
            {
                float[] vertPoly = {
                        x1 + 2.0f * radius, y1, z,
                        x1, y1 + 2.0f * radius, z,
                        x1 - 2.0f * radius, y1, z,
                        x1, y1 - 2.0f * radius, z,
                        x1 + 2.0f * radius, y1, z
                };
                mPolyLine.VertexCount = 5;
                mPolyLine.SetVerts(vertPoly);  //crash here
                mPolyLine.draw(matrix);
            }

            glText.begin(1.0f, 0.5f, 1.0f, 0, matrix);  // purple
            glText.setScale(2.0f);
            glText.drawCY(wptId, x1, y1 + glText.getCharHeight() / 2);
            glText.end();


            if (Math.abs(dme) < Math.abs(_dme)) {
                // closest apt (dme)
                float absBrg = UNavigation.calcAbsBrg(LatValue, LonValue, currApt.lat, currApt.lon);
                float relBrg = UNavigation.calcRelBrg(LatValue, LonValue, currApt.lat, currApt.lon, DIValue);

                setAutoWptValue(wptId);
                setAutoWptDme(dme);
                setAutoWptBrg(absBrg);
                setAutoWptRelBrg(relBrg);
                _dme = dme;
            }
        }

        //
        // If we dont have the full compliment of apts expand the range incrementally
        // If do we have a full compliment start reducing the range
        // This also has the "useful" side effect of "flashing" new additions for a few cycles
        //
        if ((nrAptsFound < MX_NR_APT - 2) && (Aptscounter++ % 10 == 0)) MX_RANGE += 1;
        else if ((nrAptsFound >= MX_NR_APT)) MX_RANGE -= 1;
        MX_RANGE = Math.min(MX_RANGE, 99);
    }

    //-------------------------------------------------------------------------
    // Synthetic Vision
    //
    protected void renderAirspace(float[] matrix)
    {
        float z, pixPerDegree;
        float x1, y1;
        float _x1, _y1;

        //pixPerDegree = pixM / pitchInView;
        z = zfloat;

        // 0.16667 deg lat  = 10 nm
        // 0.1 approx 5nm
        float dme = 0;         // =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // ft
        float _dme = 6080000;  // 1,000 nm in ft
        float airspacepntRelBrg;   // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
        DemColor color;

        nrAirspaceFound = 0;
        Iterator<OpenAirRec> it = OpenAir.airspacelst.iterator();
        while (it.hasNext()) {
            OpenAirRec currAirspace;
            try {
                currAirspace = it.next();
            }
            //catch (ConcurrentModificationException e) {
            catch (Exception e) {
                break;
            }
            _x1 = 0; _y1 = 0;
            String airspaceDesc = String.format("%s LL FL%d", currAirspace.ac, currAirspace.al);

            // Set the individual airspace colors
            if      (currAirspace.ac.equals("A") && AirspaceClass.A) color = new DemColor(0.37f, 0.62f, 0.42f); // ?
            else if (currAirspace.ac.equals("B") && AirspaceClass.B) color = new DemColor(0.37f, 0.42f, 0.62f); // Dk mod Powder blue 0.6
            else if (currAirspace.ac.equals("C") && AirspaceClass.C) color = new DemColor(0.37f, 0.42f, 0.62f); // Dk mod Powder blue 0.6
            else if (currAirspace.ac.equals("P") && AirspaceClass.P) color = new DemColor(0.45f, 0.20f, 0.20f);
            else if (currAirspace.ac.equals("R") && AirspaceClass.R) color = new DemColor(0.45f, 0.20f, 0.20f);
            else if (currAirspace.ac.equals("Q") && AirspaceClass.Q) color = new DemColor(0.25f, 0.10f, 0.10f);
            else if (currAirspace.ac.equals("CTR") && AirspaceClass.CTR) color = new DemColor(0.4f, 0.4f, 0.4f); // grey
            else continue; //color = new DemColor(0.4f, 0.4f, 0.4f);

            Iterator<OpenAirPoint> it2 = currAirspace.pointList.iterator();
            while (it2.hasNext()) {
                OpenAirPoint currAirPoint;
                try {
                    currAirPoint = it2.next();
                }
                //catch (ConcurrentModificationException e) {
                catch (Exception e) {
                    break;
                }

                dme = UNavigation.calcDme(LatValue, LonValue, currAirPoint.lat, currAirPoint.lon); // in ft

                // Apply selection criteria
                if (dme > MX_RANGE*2) //200
                    break;//continue;

                airspacepntRelBrg = UNavigation.calcRelBrg(LatValue, LonValue, currAirPoint.lat, currAirPoint.lon, DIValue);
                x1 = mMapZoom * (dme * UTrig.icos(90 - (int) airspacepntRelBrg));
                y1 = mMapZoom * (dme * UTrig.isin(90 - (int) airspacepntRelBrg));

                if (_x1 != 0 || _y1 != 0) {
                    mLine.SetWidth(8);
                    mLine.SetColor(color.red, color.green, color.blue, 0.85f);
                    mLine.SetVerts(
                            _x1, _y1, z,
                             x1, y1,  z
                    );
                    mLine.draw(matrix);
                }
                else {
                    // Draw the airspace description at the first coordinate
                    glText.begin(color.red, color.green, color.blue, 0.95f, matrix);
                    glText.setScale(1.5f);
                    glText.drawCY(airspaceDesc, x1, y1 + glText.getCharHeight() / 2);
                    glText.end();
                }
                _x1 = x1;
                _y1 = y1;

                if (Math.abs(dme) < Math.abs(_dme)) {
                    // closest apt (dme)
                    float absBrg = UNavigation.calcAbsBrg(LatValue, LonValue, currAirPoint.lat, currAirPoint.lon);
                    float relBrg = UNavigation.calcRelBrg(LatValue, LonValue, currAirPoint.lat, currAirPoint.lon, DIValue);

                    setAutoWptValue(airspaceDesc);
                    setAutoWptDme(dme);  // 1nm = 6080ft
                    setAutoWptBrg(absBrg);
                    setAutoWptRelBrg(relBrg);
                    _dme = dme;
                }
            }
        }

        //
        // If we dont have the full compliment of apts expand the range incrementally
        // If do we have a full compliment start reducing the range
        // This also has the "useful" side effect of "flashing" new additions for a few cycles
        //
        /*if ((nrAirspaceFound < MX_NR_APT - 2) && (nrAirspaceFound++ % 10 == 0)) MX_RANGE += 1;
        else if ((nrAirspaceFound >= MX_NR_APT)) MX_RANGE -= 1;
        MX_RANGE = Math.min(MX_RANGE, 99);*/

    }

    //-------------------------------------------------------------------------
    // North Que
    //
    protected void renderNorthQue(float[] matrix)
    {
        float  z = zfloat;

        mTriangle.SetWidth(1);
        // Right triangle
        mTriangle.SetColor(0.7f, 0.7f, 0.7f, 1);
        mTriangle.SetVerts(0,           -0.08f*pixM2, z,
                           0,            0.10f*pixM2, z,
                           0.03f*pixM2, -0.12f*pixM2,z);
        mTriangle.draw(matrix);

        // left triangle
        mTriangle.SetColor(0.5f, 0.5f, 0.5f, 1);
        mTriangle.SetVerts(0,           -0.08f*pixM2, z,
                           0,            0.10f*pixM2, z,
                          -0.03f*pixM2, -0.12f*pixM2,z);
        mTriangle.draw(matrix);

        glText.begin(0.6f, 0.6f, 0.6f, 1, matrix);
        glText.setScale(1.5f); // 2 seems full size
        glText.drawCX("N", 0, 0.12f*pixM2);
        glText.end();
    }


    //-------------------------------------------------------------------------
    // Render the Digital Elevation Model (DEM).
    //
    // This is the meat and potatoes of the synthetic vision implementation
    // The loops are very performance intensive, therefore all the hardcoded
    // magic numbers
    //
    protected void renderDEMTerrain(float[] matrix)
    {
        float z, pixPerDegree, x1, y1, z1;
        float lat, lon;
        z = zfloat;

        float dme;             //in nm
        float step = 0.50f;    //in nm, normally this should be = gridy
        float agl_ft;          //in Feet

        float demRelBrg;         // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
        float caution;
        final float cautionMin = 0.2f;
        final float IASValueThreshold = AircraftData.Vx; //1.5f * Vs0;

        float m = 1;  // 1 = normal
        if (mMapZoom < 10) m = 2;
        if (mMapZoom < 5) m = 4;

        for (dme = 0; dme <= 700f / mMapZoom; dme = dme + m*step) { // DEM_HORIZON=20, was 30
            float _x1=0, _y1=0;
            for (demRelBrg = -180; demRelBrg <= 180; demRelBrg = demRelBrg + 2*m*step) { //1
                lat = LatValue + dme / 60 * UTrig.icos((int) (DIValue + demRelBrg));
                lon = LonValue + dme / 60 * UTrig.isin((int) (DIValue + demRelBrg));
                z1 = DemGTOPO30.getElev(lat, lon);

                x1 = mMapZoom * (dme * UTrig.icos(90-(int)demRelBrg));
                y1 = mMapZoom * (dme * UTrig.isin(90-(int)demRelBrg));
                if ((_x1 != 0) || (_y1 != 0)) {
                    // float wid = mMapZoom * ((1.4148f*m*step) * UTrig.isin(90 - 0)); // simplified below
                    float wid = mMapZoom * ((1.4148f*m*step)); // simplified version, sin(90) = 1
                    DemColor color = DemGTOPO30.getColor((short) z1);
                    caution = cautionMin + (color.red + color.green + color.blue);
                    agl_ft = MSLValue - z1 * 3.28084f;  // in ft
                    if (agl_ft > 1000) mLine.SetColor(color.red, color.green, color.blue, 1);   // Enroute
                    else if (IASValue < IASValueThreshold) mLine.SetColor(color.red, color.green, color.blue, 1); // Taxi or  approach
                    else if (agl_ft > 200) mLine.SetColor(caution, caution, 0, 1f);             // Proximity notification
                    else mLine.SetColor(caution, 0, 0, 1f);                                     // Proximity warning

                    mLine.SetWidth(wid);
                    mLine.SetVerts(
                            _x1, _y1, z,
                             x1,  y1, z
                    );
                    mLine.draw(matrix);
                }
                _x1 = x1;
                _y1 = y1;
            }
        }
    }
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    //
    // Auto Waypoint handlers
    //
    //-------------------------------------------------------------------------


    private float roseTextScale = 1.9f;
    //protected void renderCompassRose(float[] matrix)
    //{
    //}



    //-------------------------------------------------------------------------
    // Render the Direct To bearing line
    //
    protected void renderDctTrack(float[] matrix)
    {
        float z, x1, y1; //sinI, cosI, _sinI, _cosI;
        z = zfloat;

        //
        // Direct Track to Selected Waypoint
        //
        mLine.SetWidth(20); //8
        //mLine.SetColor(0.5f, 0.250f, 0.5f, 0.125f); // purple'ish
        mLine.SetColor(0.45f, 0.45f, 0.10f, 0.125f); // yellow'ish

        x1 = mMapZoom * (mSelWptDme * UTrig.icos(90-(int)mSelWptRlb));
        y1 = mMapZoom * (mSelWptDme * UTrig.isin(90-(int)mSelWptRlb));
        mLine.SetVerts(
                0, 0, z,
                x1, y1, z
        );
        mLine.draw(matrix);
        // Skunk stripe
        mLine.SetWidth(4); //8
        mLine.SetColor(0.0f, 0.0f, 0.0f, 1); // black
        mLine.draw(matrix);

        //
        // Direct Track to Automatic Waypoint
        //
        /* Not sure I like this feature ...
        mLine.SetWidth(2); //8
        mLine.SetColor(0.7f, 0.7f, 0, 1.0f); // yellow

        x1 = mMapZoom * (mAutoWptDme * UTrig.icos(90-(int)mAutoWptRlb));
        y1 = mMapZoom * (mAutoWptDme * UTrig.isin(90-(int)mAutoWptRlb));
        mLine.SetVerts(
                0, 0, z,
                x1, y1, z
        );
        mLine.draw(matrix);
        */

    }

    //-------------------------------------------------------------------------
    // Render a little airplane symbol
    //
    protected void renderACSymbol(float[] matrix)
    {
        float z;
        z = zfloat;

        // Wings
        mLine.SetWidth(8);
        mLine.SetColor(1, 1, 1, 1); // white
        mLine.SetVerts(
                -0.10f*pixM2, 0, z,
                 0.10f*pixM2, 0, z
        );
        mLine.draw(matrix);
        // L
        mLine.SetVerts(
                -0.10f*pixM2, 0, z,
                0,  0.025f*pixM2, z
        );
        mLine.draw(matrix);
        // R
        mLine.SetVerts(
                0.10f*pixM2, 0, z,
                0,  0.025f*pixM2, z
        );
        mLine.draw(matrix);

        // Fuselage
        mLine.SetVerts(
                0, -0.10f*pixM2, z,
                0,  0.075f*pixM2, z
        );
        mLine.draw(matrix);
        // Tail
        mLine.SetVerts(
                -0.05f*pixM2, -0.10f*pixM2, z,
                 0.05f*pixM2, -0.10f*pixM2, z
        );
        mLine.draw(matrix);
    }

    //-------------------------------------------------------------------------
    // Render map scale ruler
    //
    protected void renderMapScale(float[] matrix)
    {
        float z;
        z = zfloat;

        //mMapZoom;
        // 700/mMapZoom = 35nm
        //x1 = mMapZoom * (dme * UTrig.icos(90-(int)demRelBrg));
        //y1 = mMapZoom * (dme * UTrig.isin(90-(int)demRelBrg));

        float len = 20;//20;
        float x1 = mMapZoom*len;// * (5 * UTrig.icos(90-(int)90));

        while (x1 > pixW2/2) {
            if (len > 5) len = len - 5;
            else len = 1;

            x1 = mMapZoom*len;// * (5 * UTrig.icos(90-(int)90));
        }

        // Scale line
        mLine.SetWidth(1);
        mLine.SetColor(1, 1, 1, 1); // white
        mLine.SetVerts(
                -0.95f*pixW2 + 0,  -0.95f*pixH2, z,
                -0.95f*pixW2 + x1, -0.95f*pixH2, z
        );
        mLine.draw(matrix);
        mLine.SetVerts(
                -0.95f*pixW2 + 0, -0.95f*pixH2, z,
                -0.95f*pixW2 + 0, -0.92f*pixH2, z
        );
        mLine.draw(matrix);
        mLine.SetVerts(
                -0.95f*pixW2 + x1, -0.95f*pixH2, z,
                -0.95f*pixW2 + x1, -0.92f*pixH2, z
        );
        mLine.draw(matrix);

        String t = String.format("%3.0f nm", len);
        glText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); // White
        glText.setScale(1.5f);
        glText.draw(t, -0.90f*pixW2, -0.95f*pixH2);            // Draw  String
        glText.end();

        // leader line
        mLine.SetVerts(
                0, 0, z,
                0, x1, z
        );
        mLine.draw(matrix);
        mLine.SetVerts(
                -0.025f*pixM2, x1, z,
                +0.025f*pixM2, x1, z
        );
        mLine.draw(matrix);

    }

    //-------------------------------------------------------------------------
    // Map Zooming
    //
    void setMapZoom(float zoom)
    {
        mMapZoom = zoom;
    }

    public void zoomIn()
    {
        if (mMapZoom < 5) mMapZoom += 1;
        else if (mMapZoom < 120) mMapZoom += 5;
    }

    public void zoomOut()
    {
        if (mMapZoom > 5) mMapZoom -= 5;
        else if (mMapZoom > 2) mMapZoom -= 1;
    }



}
//-------------
// END OF CLASS
//-------------


