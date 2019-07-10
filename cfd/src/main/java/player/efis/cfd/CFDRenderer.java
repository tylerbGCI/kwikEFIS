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

package player.efis.cfd;

import java.nio.IntBuffer;
import java.util.Iterator;
import player.efis.common.AirspaceClass;
import player.efis.common.Apt;
import player.efis.common.DemColor;
import player.efis.common.DemGTOPO30;
import player.efis.common.AircraftData;
import player.efis.common.EFISRenderer;
import player.efis.common.Gpx;
import player.efis.common.OpenAir;
import player.efis.common.OpenAirPoint;
import player.efis.common.OpenAirRec;
import player.efis.common.Point;
import player.gles20.GLBitmap;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class CFDRenderer extends EFISRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = "CFDRenderer";
    protected boolean ServiceableAh;      // Flag to indicate AH failure
    protected boolean ServiceableMap;      // Flag to indicate Map failure


    protected GLBitmap glBitmap;


    public CFDRenderer(Context context)
    {
        super(context);
    }

    // Sprite code >>>> https://gamedev.stackexchange.com/questions/98767/opengl-es-2-0-2d-image-displaying
    // https://stackoverflow.com/questions/47280918/opengl-es-draw-bitmap

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        // Set the background frame color
        GLES20.glClearColor(backShadeR, backShadeG, backShadeB, 1.0f);

        mTriangle = new Triangle();
        mSquare = new Square();
        mLine = new Line();
        mPolyLine = new PolyLine();
        mPolygon = new Polygon();

        // Create the GLText
        glText = new GLText(context.getAssets());

        //glBitmap = new GLBitmap();  // We want the app to crash if glBitmap is used.

        roseTextScale = 1f;
    }

    private int ctr;
    @Override
    public void onDrawFrame(GL10 gl)
    {
        ctr++;

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        onDrawFramePfd(gl);
        onDrawFrameMfd(gl);
    }


    private void onDrawFramePfd(GL10 gl)
    {
        GLES20.glViewport(0, pixH2, pixW, pixH);

        // Draw background color
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        if (displayMirror)
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);  // Mirrored View
        else
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, +3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);   // Normal View

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation for the horizon
        Matrix.setRotateM(mRotationMatrix, 0, rollRotation, 0, 0, 1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch1, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Pitch
        if (Layout == layout_t.LANDSCAPE) {
            portraitOffset = 0;
            // Slide pitch to current value
            Matrix.translateM(scratch1, 0, 0, pitchTranslation, 0); // apply the pitch
        }
        else {
            portraitOffset = -0.5f;  // the magic number for portrait offset

            // Slide pitch to current value adj for portrait
            float Adjust = pixH2 * portraitOffset;                           //portraitOffset set to 0.4
            Matrix.translateM(scratch1, 0, 0, pitchTranslation + Adjust, 0); // apply the pitch and offset
        }

        // Slide ALT to current value
        Matrix.translateM(altMatrix, 0, mMVPMatrix, 0, 0, -MSLTranslation, 0); // apply the altitude

        // Slide IAS to current value
        Matrix.translateM(iasMatrix, 0, mMVPMatrix, 0, 0, -IASTranslation, 0); // apply the altitude

        zfloat = 0;

        if (displayDEM && !fatFingerActive) {
            // Make the blue sky for the DEM.
            // Note: it extends a little below the horizon when AGL is positive
            renderDEMSky(scratch1);
            // underground is not valid
            if ((AGLValue > 0) && (DemGTOPO30.demDataValid)) renderDEMTerrainPfd(scratch1);
        }
        else if (displayAHColors) renderAHColors(scratch1);

        renderPitchMarkers(scratch1);

        //GLES20.glViewport(0, pixH2, pixW, pixH2);

        // FPV only means anything if we have speed and rate of climb, ie altitude
        if (displayFPV) renderFPV(scratch1);      // must be on the same matrix as the Pitch
        if (displayAirport) renderAPT(scratch1);  // must be on the same matrix as the Pitch
        if (true) renderTargets(scratch1);        // Add control tof targets sometime ...
        if (displayHITS) renderHITS(scratch1);    // will not keep in the viewport

        //GLES20.glViewport(0, 0, pixW, pixH);


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
                // Slide pitch to current value adj for portrait
                float Adjust = -pixH2/2;
                // Slide FD to current value
                Matrix.translateM(fdMatrix, 0, 0, pitchTranslation - FDTranslation + Adjust, 0); // apply the altitude
            }
            renderFlightDirector(fdMatrix);
        }


        if (Layout == layout_t.PORTRAIT) {
            // Slide pitch to current value adj for portrait
            int Adjust = (int) (pixH2 * portraitOffset);
            GLES20.glViewport(0,  Adjust, pixW, pixH); // Portrait //
        }

        GLES20.glViewport(0, pixH/4, pixW, pixH);
        renderFixedHorizonMarkers();
        renderRollMarkers(scratch2);

        //-----------------------------
        if (Layout == layout_t.LANDSCAPE)
            GLES20.glViewport(pixW / 30, pixH / 30, pixW - pixW / 15, pixH - pixH / 15); //Landscape
        else
            //GLES20.glViewport(pixW / 100, pixH * 40 / 100, pixW - pixW / 50, pixH - pixH * 42 / 100); // Portrait
              GLES20.glViewport(pixW / 100, pixH2*105/100, pixW - pixW / 50, pixH2*90/100); // Portrait

        if (displayTape) {
            renderALTMarkers(altMatrix);
            renderASIMarkers(iasMatrix);
            renderVSIMarkers(mMVPMatrix);
        }

        {
            int Adjust = (int) (pixH2 * portraitOffset);
            GLES20.glViewport(0, 0, pixW, pixH); // Portrait //

            float xlx;
            float xly;

            //if (displayTape == true) renderFixedVSIMarkers(mMVPMatrix); // todo: maybe later

            xlx = 1.14f * pixM2;
            xly = pixH2/2; // half of tape viewport //-0.7f * pixH2;
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            renderFixedALTMarkers(mMVPMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            xly = 0.1f * pixH2;
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            renderFixedRADALTMarkers(mMVPMatrix);   // AGL
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            xlx = -1.10f * pixM2;
            xly = pixH2/2; // half of tape viewport //-0.7f * pixH2;
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            renderFixedASIMarkers(mMVPMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            //GLES20.glViewport(0, 0, pixW, pixH);  // fullscreen

            xlx = 0;
            xly = +0.90f * pixH2;
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            renderFixedDIMarkers(mMVPMatrix);
            renderHDGValue(mMVPMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);
        }


        //-----------------------------
        /*b1
        renderTurnMarkers(mMVPMatrix);
        renderSlipBall(mMVPMatrix);
        renderGForceValue(mMVPMatrix);

        if (displayInfoPage) {
            renderAncillaryDetails(mMVPMatrix);
            renderBatteryPct(mMVPMatrix);
        }
        */

        ///* b1
        //if (!ServiceableDevice) renderUnserviceableDevice(mMVPMatrix);
        //if (!ServiceableAh) renderUnserviceablePage(mMVPMatrix);

        // exploit the spacing hack from pfd to position
        if (bSimulatorActive) renderSimulatorActive(mMVPMatrix);
        if (!ServiceableAh) renderUnserviceableAh(mMVPMatrix);
        if (bBannerActive) renderBannerMsg(mMVPMatrix);

        GLES20.glViewport(0, pixH2, pixW, pixH2);  //
        if (!ServiceableAlt) renderUnserviceableAlt(mMVPMatrix);
        if (!ServiceableAsi) renderUnserviceableAsi(mMVPMatrix);
        if (!ServiceableDi) {
            renderUnserviceableDi(mMVPMatrix);
            //renderUnserviceableCompassRose(mMVPMatrix);
        }
        //*/
        //GLES20.glViewport(0, 0, pixW, pixH);  // fullscreen

        //if (bBannerActive) renderBannerMsg(mMVPMatrix);
        //if (bSimulatorActive) renderSimulatorActive(mMVPMatrix);

        /*b1
        // Do this last so that every else wil be dimmed for fatfinger entry
        if (displayFlightDirector || displayRMI || displayHITS) {
            renderSelWptDetails(mMVPMatrix);
            renderSelWptValue(mMVPMatrix);
            renderSelAltValue(mMVPMatrix);
        }
        */
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0, 0, width, height/2);

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
        pitchInView = 25.0f;     // degrees to display from horizon to top of viewport
        IASInView = 40.0f;       // IAS units to display from center to top of viewport
        MSLInView = 300.0f;      // IAS units to display from center to top of viewport

        // this projection matrix is applied to  object coordinates in the onDrawFrame() method
        float ratio = (float) width / height;
        //Matrix.frustumM(mProjectionMatrix, 0, -ratio*pixH2, ratio*pixH2, -pixH2, pixH2, 3f, 7f); // all the rest
        Matrix.frustumM(mProjectionMatrix, 0, -ratio * pixH2, ratio * pixH2, -pixH2, pixH2, 2.99f, 75f); //hack for Samsung G2

        // Create the GLText
        // --debug glText = new GLText(context.getAssets()); - moved to onsurfacecreated

        // Load the font from file (set size + padding), creates the texture
        // NOTE: after a successful call to this the font is ready for rendering!
        // glText.load( "Roboto-Regular.ttf", 14, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)
        glText.load("square721_cn_bt_roman.ttf", pixM * 14 / 734, 2, 2);  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    int mTextureDataHandle;

    @Override
    protected void renderUnserviceableDevice(float[] matrix)
    {
        renderUnserviceableAh(matrix);
        renderUnserviceableCompassRose(matrix);
        renderUnserviceableDi(matrix);
        renderUnserviceableAlt(matrix);
        renderUnserviceableAsi(matrix);
    }

    //
    // project
    //
    // relbrg in degrees
    // dme in nm
    // elev in m
    @Override
    protected Point project(float relbrg, float dme)
    {
        float pixPerDegree = pixM / pitchInView;
        // note: we do not take apt elevation into account
        return new Point(
            (float) (+pixPerDegree * relbrg),
            (float) (-pixPerDegree * Math.toDegrees(Math.atan2(MSLValue, Unit.NauticalMile.toFeet(dme))))
        );
    }


    @Override
    protected Point project(float relbrg, float dme, float elev)
    {
        float pixPerDegree = pixM / pitchInView;

        // note: we take apt elevation into account
        //float y1 = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - z1 * 3.28084f, dme_ft)) * pixPerDegree);

        //float dme_ft = dme * 6080;
        //float y = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - elev * 3.28084f, dme_ft)) * pixPerDegree);
        float y =  (float) (-pixPerDegree * Math.toDegrees(Math.atan2(MSLValue - Unit.Meter.toFeet(elev), Unit.NauticalMile.toFeet(dme))));

        return new Point(
                (float) (+pixPerDegree * relbrg),
                (float) y
        );
    }

    //-------------------------------------------------------------------------
    // Set the spinner control parameters
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
                selWptDec = -0.30f * pixH2;
                selWptInc = -0.41f * pixH2;
                selAltDec = -0.80f * pixH2;
                selAltInc = -0.91f * pixH2;

                lineC = -0.55f;
                leftC = 0.6f;
                spinnerStep = 0.1f;
                spinnerTextScale = 1f;
            }
        }
    }

    //-------------------------------------------------------------------------
    // Render the Digital Elevation Model (DEM).
    //
    // This is the meat and potatoes of the synthetic vision implementation
    // The loops are very performance intensive, therefore all the hardcoded
    // magic numbers
    //
    int frameSkipPfd = 4; 
    int textureHandlePfd;
    protected void renderDEMTerrainPfdCache(GL10 gl, float[] matrix)
    {
        /*
        if (ctr % frameSkipPfd == 0) {
            renderDEMTerrainPfd(matrix);
            Bitmap bm = saveScreen(gl, pixH2, pixH2);
            textureHandlePfd = loadTexture(bm);
            bm.recycle();
        }

        {
            float x = 1.4f * pixW2;
            float y = 1.4f * pixH2/2;
            float[] squarePoly = {
                    -x, -y , 0,
                    +x, -y , 0,
                    +x, +y, 0,
                    -x, +y, 0
            };
            glBitmap.SetVerts(squarePoly);
            glBitmap.textureDataHandle =  textureHandlePfd;
            glBitmap.draw(matrix);
        }
        */
    }


    protected void renderDEMTerrainPfd(float[] matrix)
    {
        float z, pixPerDegree, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, zav;
        float lat, lon;
        pixPerDegree = pixM / pitchInView;
        z = zfloat;

        float dme;             //in nm
        float step = 0.50f;    //in nm, normally this should be = gridy
        float agl_ft;          //in Feet

        // oversize 20% a little to help with
        // bleed through caused by itrig truncating
        float gridy = 0.5f;    //in nm
        float gridx = 1.0f;    //in degree

        float dme_ft;            // =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // ft
        float demRelBrg;         // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
        float caution;
        final float cautionMin = 0.2f;
        final float IASValueThreshold = AircraftData.Vx; //1.5f * Vs0;
        final float viewCone = 20; //25;

        mSquare.SetWidth(1);

        for (dme = 0; dme <= DemGTOPO30.DEM_HORIZON; dme += step) {
            for (demRelBrg = -viewCone; demRelBrg < viewCone; demRelBrg = demRelBrg + 1) {

                dme_ft = dme * 6080;
                lat = LatValue + dme / 60 * UTrig.icos((int) (DIValue + demRelBrg));
                lon = LonValue + dme / 60 * UTrig.isin((int) (DIValue + demRelBrg));
                z1 = DemGTOPO30.getElev(lat, lon);
                x1 = demRelBrg * pixPerDegree;
                y1 = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - z1 * 3.28084f, dme_ft)) * pixPerDegree);

                lat = LatValue + dme / 60 * UTrig.icos((int) (DIValue + demRelBrg + gridx));
                lon = LonValue + dme / 60 * UTrig.isin((int) (DIValue + demRelBrg + gridx));
                z2 = DemGTOPO30.getElev(lat, lon);
                x2 = (demRelBrg + gridx) * pixPerDegree;
                y2 = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - z2 * 3.28084f, dme_ft)) * pixPerDegree);

                dme_ft = (dme + gridy) * 6080;
                lat = LatValue + (dme + gridy) / 60 * UTrig.icos((int) (DIValue + demRelBrg + gridx));
                lon = LonValue + (dme + gridy) / 60 * UTrig.isin((int) (DIValue + demRelBrg + gridx));
                z3 = DemGTOPO30.getElev(lat, lon);
                x3 = (demRelBrg + gridx) * pixPerDegree;
                y3 = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - z3 * 3.28084f, dme_ft)) * pixPerDegree);

                lat = LatValue + (dme + gridy) / 60 * UTrig.icos((int) (DIValue + demRelBrg));
                lon = LonValue + (dme + gridy) / 60 * UTrig.isin((int) (DIValue + demRelBrg));
                z4 = DemGTOPO30.getElev(lat, lon);
                x4 = (demRelBrg) * pixPerDegree;
                y4 = (float) (-Math.toDegrees(UTrig.fastArcTan2(MSLValue - z4 * 3.28084f, dme_ft)) * pixPerDegree);

                //
                //  77%
                //
                //   Triangle #2   Triangle #1
                //    +             +--+
                //    |\             \ |
                //    | \             \|
                //    +--+             +
                //

                // Triangle #1 --------------
                zav = z1;  // in m asml
                DemColor color = DemGTOPO30.getColor((short) zav);

                // Handle Monochrome
                if (colorTheme == 2) {
                    color.red = 0;
                    color.blue = 0;
                }

                caution = cautionMin + (color.red + color.green + color.blue);
                agl_ft = MSLValue - zav * 3.28084f;  // in ft

                if (agl_ft > 1000) mTriangle.SetColor(color.red, color.green, color.blue, 1);                     // Enroute
                else if (IASValue < IASValueThreshold) mTriangle.SetColor(color.red, color.green, color.blue, 1); // Taxi or approach
                else if (agl_ft > 200) mTriangle.SetColor(caution, caution, 0, 1f);                               // Proximity notification (yellow)
                else mTriangle.SetColor(caution, 0, 0, 1f);                                                       // Proximity warning (red)

                mTriangle.SetVerts(
                        x1, y1, z,
                        x2, y2, z,
                        x4, y4, z);
                mTriangle.draw(matrix);

                // Triangle #2 --------------
                zav = (z1 + z2) / 2; // take the simple average
                color = DemGTOPO30.getColor((short) zav);

                // Handle Monochrome
                if (colorTheme == 2) {
                    color.red = 0;
                    color.blue = 0;
                }

                caution = cautionMin + (color.red + color.green + color.blue);
                agl_ft = MSLValue - zav * 3.28084f;  // in ft

                if (agl_ft > 1000) mTriangle.SetColor(color.red, color.green, color.blue, 1);                     // Enroute
                else if (IASValue < IASValueThreshold) mTriangle.SetColor(color.red, color.green, color.blue, 1); // Taxi or  approach
                else if (agl_ft > 200) mTriangle.SetColor(caution, caution, 0, 1f);  // Proximity notification
                else mTriangle.SetColor(caution, 0, 0, 1f);                          // Proximity warning

                mTriangle.SetVerts(
                        x2, y2, z,
                        x3, y3, z,
                        x4, y4, z);
                mTriangle.draw(matrix);

                /*
                //
                //  69%
                //
                //   Square
                //   4    3
                //    +--+
                //    |  |
                //    |  |
                //    +--+
                //   1    2

                    zav = z1;  // use the
                    getColor((short) zav);
                    agl_ft = MSLValue - zav*3.28084f;  // in ft

                    if (agl_ft > 100) mSquare.SetColor(red, green, blue, 1);                      // Enroute
                    else if (IASValue < IASValueThreshold) mTriangle.SetColor(red, green, blue, 1); // Taxi or  apporach
                    else mSquare.SetColor(caution, 0, 0, 1f);                                     // Proximity warning

                    float[] squarePoly = {
                            x1, y1, z,
                            x2, y2, z,
                            x3, y3, z,
                            x4, y4, z
                    };
                    mSquare.SetVerts(squarePoly);
                    mSquare.draw(matrix);
                */

            }
        }
    }

    //-------------------------------------------------------------------------
    // Airspace
    //
    protected void renderAirspacePfd(float[] matrix)
    {
        // Maybe later
    }


    //---------------------------------------------------------------------------
    // EFIS serviceability ... aka the Red X's
    //

    // Artificial Horizon serviceability
    public void setServiceableAh()
    {
        ServiceableAh = true;
    }
    public void setUnServiceableAh()
    {
        ServiceableAh = false;
    }


    //===========================================================================
    //---------------------------------------------------------------------------
    // DMAP routines
    //

    //---------------------------------------------------------------
    // Multi-Function-Display Drawing (DMAP)
    //
    private void onDrawFrameMfd(GL10 gl)
    {
        GLES20.glViewport(0, -101/100*pixH2, pixW, pixH);

        // Set the camera position (View matrix)
        if (displayMirror)
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);  // Mirrored View
        else
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, +3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);  // Normal View

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        zfloat = 0;

        float xlx;
        float xly;
        // Add switch for orientation
        if (Layout == layout_t.LANDSCAPE) {
            // TODO: implement a suitable landscape view
            // For now - Do nothing
            /*
            // Landscape
            xlx = -0.74f * pixW2; // top left
            xly = 0.50f * pixH2;  // top left
            roseScale = 0.44f;
            GLES20.glViewport(0, 0, pixW, pixH);
            */
            // Temp ... keep Portrait layout
            xlx = 0;
            xly = pixH2/2;
            roseScale = 0.52f;
        }
        else {
            //Portrait
            xlx = 0;
            xly = pixH2/2;
            roseScale = 0.52f;
        }

        Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
        // fatFingerActive just for performance
        if (displayDEM && !fatFingerActive) renderDEMTerrainMfd(mMVPMatrix);
        if (displayAirspace) renderAirspaceMfd(mMVPMatrix);
        if (displayAirport) renderAPTMfd(mMVPMatrix);  // must be on the same matrix as the Pitch
        if (true) renderTargets(mMVPMatrix);           // TODO: 2018-08-31 Add control of targets
        Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

        // use RMI from PFD
        // Remote Magnetic Inidicator - RMI
        if (displayRMI) {
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            // Create a rotation for the RMI
            Matrix.setRotateM(mRmiRotationMatrix, 0, DIValue, 0, 0, 1);  // compass rose rotation
            Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);

            //renderBearingTxt(mMVPMatrix);  // maybe too busy? b2
            renderFixedCompassMarkers(mMVPMatrix);
            renderACSymbol(mMVPMatrix);

            if (autoZoomActive) setAutoZoom();
            renderDctTrack(mMVPMatrix);

            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            renderCompassRose(rmiMatrix);
            //renderBearing(rmiMatrix);
            //GLES20.glViewport(0, 0, pixW, pixH);  // fullscreen
        }



        //-----------------------------
        if (displayInfoPage) {
            xlx = 0;
            xly = pixH2;
            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            renderAncillaryDetails(mMVPMatrix);
            renderBatteryPct(mMVPMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);

            // North Que
            xlx = -0.84f * pixW2;
            xly = 0.88f * pixH2;

            Matrix.translateM(mMVPMatrix, 0, xlx, xly, 0);
            Matrix.setRotateM(mRmiRotationMatrix, 0, DIValue, 0, 0, 1);  // compass rose rotation
            Matrix.multiplyMM(rmiMatrix, 0, mMVPMatrix, 0, mRmiRotationMatrix, 0);
            renderNorthQue(rmiMatrix);
            Matrix.translateM(mMVPMatrix, 0, -xlx, -xly, 0);
        }

        GLES20.glViewport(0, 0, pixW, pixH2);
        ///*
        //if (!ServiceableDevice) renderUnserviceableDevice(mMVPMatrix);
        if (!ServiceableMap) renderUnserviceablePage(mMVPMatrix);
        /*if (!ServiceableAlt) renderUnserviceableAlt(mMVPMatrix);
        if (!ServiceableAsi) renderUnserviceableAsi(mMVPMatrix);
        if (!ServiceableDi) renderUnserviceableDi(mMVPMatrix);
        if (bBannerActive) renderBannerMsg(mMVPMatrix);
        if (bSimulatorActive) renderSimulatorActive(mMVPMatrix);*/
        //*/

        //renderACSymbol(mMVPMatrix);

        /*
        // Do this last so that every else wil be dimmed for fatfinger entry
        if (displayFlightDirector) {
            renderSelWptDetails(mMVPMatrix);
            renderSelWptValue(mMVPMatrix);
        }
        //*/
        // Use the PFD version
        // Do this last so that every else wil be dimmed for fatfinger entry
        GLES20.glViewport(0, 0, pixW, pixH);  // use the whole screen to accomodate fatfinger
        if (displayFlightDirector || displayRMI || displayHITS) {
            renderSelWptDetails(mMVPMatrix);
            renderSelWptValue(mMVPMatrix);
            renderSelAltValue(mMVPMatrix);

            renderAutoWptDetails(mMVPMatrix);

        }
    }


    //-------------------------------------------------------------------------
    // Render the Digital Elevation Model (DEM) - DMAP.
    //
    // This is the meat and potatoes of the synthetic vision implementation
    // The loops are very performance intensive, therefore all the hardcoded
    // magic numbers
    //
    boolean bCacheActive = false;
    int frameSkipMfd = 5;
    int textureHandleMfd;
    protected void renderDEMTerrainMfdCache(GL10 gl, float[] matrix)
    {
		/*
        if (ctr % frameSkipMfd == 0) {
            renderDEMTerrainMfd(matrix);
            Bitmap bm = saveScreen(gl, 0, pixH2);
            textureHandleMfd = loadTexture(bm);
            bm.recycle();
            bm = null;
        }

        GLES20.glViewport(0, -pixH2*101/100, pixW, pixH);
        {
            float y = pixH2/2;
            float[] squarePoly = {
                    -pixW2, -y , 0,
                    +pixW2, -y , 0,
                    +pixW2, +y, 0,
                    -pixW2, +y, 0
            };
            glBitmap.SetVerts(squarePoly);
            glBitmap.textureDataHandle =  textureHandleMfd;
            glBitmap.draw(matrix);
        }
		*/
    }

    protected void renderDEMTerrainMfd(float[] matrix)
    {
        float z, x1, y1, z1;
        float lat, lon;
        z = zfloat;

        float dme;             //in nm
        float step = 0.50f;    //in nm, normally this should be = gridy
                               // 0.5 nm is appox 1km which is the size of the DEM tiles.
        float agl_ft;          //in Feet
        float demRelBrg;       // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
        float caution;
        final float cautionMin = 0.2f;
        final float IASValueThreshold = AircraftData.Vx; //1.5f * Vs0;
        float range = 1.4f * pixM2 / mMapZoom ;

        if (mMapZoom < 16) step *= 2;
        if (mMapZoom < 8) step *= 2;

        float wid = mMapZoom * step * 0.7071f; // optional  * 0.7071f;  // 1/sqrt(2)
        float hgt = 2*wid;

        for (dme = 0; dme <= range; dme = dme + step) { // DEM_HORIZON=20, was 30
            float _x1=0, _y1=0;
            for (demRelBrg = -180; demRelBrg <= 180; demRelBrg = demRelBrg + 1) { //1
                lat = LatValue + dme / 60 * UTrig.icos((int) (DIValue + demRelBrg));
                lon = LonValue + dme / 60 * UTrig.isin((int) (DIValue + demRelBrg));
                z1 = DemGTOPO30.getElev(lat, lon);

                x1 = mMapZoom * (dme * UTrig.icos(90-(int)demRelBrg));
                y1 = mMapZoom * (dme * UTrig.isin(90-(int)demRelBrg));

                if ((_x1 != 0) || (_y1 != 0)) {

                    DemColor color = DemGTOPO30.getColor((short) z1);
                    // Handle Monochrome
                    if (colorTheme == 2) {
                        color.red = 0;
                        color.blue = 0;
                    }
                    caution = cautionMin + (color.red + color.green + color.blue);
                    agl_ft = MSLValue - z1 * 3.28084f;  // in ft

                    //float wid = mMapZoom * step * 0.7071f; // optional  * 0.7071f;  // 1/sqrt(2)

                    if (agl_ft > 1000) mSquare.SetColor(color.red, color.green, color.blue, 1);                     // Enroute
                    else if (IASValue < IASValueThreshold) mSquare.SetColor(color.red, color.green, color.blue, 1); // Taxi or  approach
                    else if (agl_ft > 200) mSquare.SetColor(caution, caution, 0, 1f);  // Proximity notification
                    else mSquare.SetColor(caution, 0, 0, 1f);                          // Proximity warning
                    float[] squarePoly = {
                            x1-wid, y1-hgt, z,
                            x1-wid, y1+hgt, z,
                            x1+wid, y1+hgt, z,
                            x1+wid, y1-hgt, z
                    };
                    mSquare.SetVerts(squarePoly);
                    mSquare.draw(matrix);
                }
                _x1 = x1;
                _y1 = y1;
            }
        }
    }

    //-------------------------------------------------------------------------
    // Airspace
    //
    protected void renderAirspaceMfd(float[] matrix)
    {
        float z;
        float x1, y1;
        float _x1, _y1;

        z = zfloat;

        // 0.16667 deg lat  = 10 nm
        // 0.1 approx 5nm
        float dme = 0;           // =  60 * 6080 * Math.hypot(deltaLon, deltaLat);  // ft
        float _dme = 6080000;    // 1,000 nm in ft
        float airspacepntRelBrg; // = DIValue + Math.toDegrees(Math.atan2(deltaLon, deltaLat));
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

            // Handle Monochrome
            if (colorTheme == 2) {
                color.red = 0;
                color.blue = 0;
            }

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
                if (dme > AptSeekRange *2)
                    break;

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
                    setAutoWptDme(dme);
                    setAutoWptBrg(absBrg);
                    setAutoWptRelBrg(relBrg);
                    _dme = dme;
                }
            }
        }
    }


    protected Point projectMfd(float relbrg, float dme)
    {
        return new Point(
                mMapZoom * dme * UTrig.icos(90-(int)relbrg),
                mMapZoom * dme * UTrig.isin(90-(int)relbrg)
        );
    } // end of project

    protected Point projectMfd(float relbrg, float dme, float elev)
    {
        return new Point(
                mMapZoom * dme * UTrig.icos(90-(int)relbrg),
                mMapZoom * dme * UTrig.isin(90-(int)relbrg)
        );
    } // end of project


    //
    // Variables specific to render APT
    //
    protected void renderAPTMfd(float[] matrix)
    {
        float z, x1, y1;

        z = zfloat;

        // 0.16667 deg lat  = 10 nm
        // 0.1 approx 5nm
        float dme;
        float _dme = 1000;
        float aptRelBrg;
        String wptId = mWptSelName;
        float elev;

        // Aways draw at least the selected waypoint
        // TODO: 2018-08-12 Add elev to selected WPT
        wptId = mWptSelName;
        dme = UNavigation.calcDme(LatValue, LonValue, mWptSelLat, mWptSelLon); // in nm
        aptRelBrg = UNavigation.calcRelBrg(LatValue, LonValue, mWptSelLat, mWptSelLon, DIValue);
        x1 = projectMfd(aptRelBrg, dme).x;
        y1 = projectMfd(aptRelBrg, dme).y;
        renderAPTSymbol(matrix, x1, y1, wptId);

        // draw all the other waypoints that fit the criteria
        nrAptsFound = 0;
        Iterator<Apt> it = Gpx.aptList.iterator();
        while (it.hasNext()) {
            Apt currApt;
            try {
                currApt = it.next();
            }
            //catch (ConcurrentModificationException e) {
            catch (Exception e) {
                break;
            }

            if (currApt.name == null) break;

            wptId = currApt.name;
            dme = UNavigation.calcDme(LatValue, LonValue, currApt.lat, currApt.lon); // in nm


            // Apply selection criteria
            if (dme < 5) nrAptsFound++;                                                // always show apts closer then 5nm
            else if ((nrAptsFound < MX_NR_APT) && (dme < AptSeekRange)) nrAptsFound++; // show all others up to MX_NR_APT for AptSeekRange
            else continue;  // we already have all the apts as we wish to display

            aptRelBrg = UNavigation.calcRelBrg(LatValue, LonValue, currApt.lat, currApt.lon, DIValue);

            /*x1 = project(aptRelBrg, dme).x;
            y1 = project(aptRelBrg, dme).y;
            renderAPTSymbol(matrix, x1, y1, wptId);*/

            x1 = projectMfd(aptRelBrg, dme, currApt.elev).x;
            y1 = projectMfd(aptRelBrg, dme, currApt.elev).y;
            renderAPTSymbol(matrix, x1, y1, wptId);


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
        if ((nrAptsFound < MX_NR_APT - 2) && (Aptscounter++ % 10 == 0)) AptSeekRange += 1;
        else if ((nrAptsFound >= MX_NR_APT)) AptSeekRange -= 1;
        AptSeekRange = Math.min(AptSeekRange, MX_APT_SEEK_RNG);
    }



    //---------------------------------------------------------------------------
    // DMAP serviceability ... aka the Red X's
    //

    // Artificial Horizon serviceability
    public void setServiceableMap()
    {
        ServiceableMap = true;
    }

    public void setUnServiceableMap()
    {
        ServiceableMap = false;
    }


    public Bitmap saveScreen(GL10 mGL, int offset, int height)
    {
        final int mWidth = pixW;
        final int mHeight = height;

        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        //b2- IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        mGL.glReadPixels(0, offset, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        // b2
        //for (int i = 0; i < mHeight; i++) {
        //    for (int j = 0; j < mWidth; j++) {
        //        ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
        //    }
        //}

        Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.argb(0, 255, 255, 255));
        //b2 - mBitmap.copyPixelsFromBuffer(ibt);
        mBitmap.copyPixelsFromBuffer(ib);
        return mBitmap;
    }


    //public static int loadTexture(final Context context, final int resourceId)
    //public static int loadTexture(final Context context, Bitmap bitmap)
    // could be moved to EFISRenderer
    public static int loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inScaled = false;   // No pre-scaling

            // Read in the resource
            //final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            // bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

}


/*
moved to EFIS renderer (parent)

    public Bitmap saveScreen(GL10 mGL, int offset, int height)
    {
        final int mWidth = pixW;
        final int mHeight = height;

        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        //b2- IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        mGL.glReadPixels(0, offset, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        // b2
        //for (int i = 0; i < mHeight; i++) {
        //    for (int j = 0; j < mWidth; j++) {
        //        ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
        //    }
        //}

        Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.argb(0, 255, 255, 255));
        //b2 - mBitmap.copyPixelsFromBuffer(ibt);
        mBitmap.copyPixelsFromBuffer(ib);
        return mBitmap;
    }


    //public static int loadTexture(final Context context, final int resourceId)
    //public static int loadTexture(final Context context, Bitmap bitmap)
    // could be moved to EFISRenderer
    public static int loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inScaled = false;   // No pre-scaling

            // Read in the resource
            //final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            // bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
*/
