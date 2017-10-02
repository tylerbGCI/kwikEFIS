/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;


//
// A view container where OpenGL ES graphics can be drawn on screen.
// This view can also be used to capture touch events, such as a user
// interacting with drawn objects.
//
public class EFISSurfaceView extends GLSurfaceView
{

    //private final EFISRenderer mRenderer;
    public final EFISRenderer mRenderer;  // normally this would be private but we want to access the sel wpt from main activity

    public EFISSurfaceView(Context context)
    {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new EFISRenderer(context); // = new MyGLRenderer();  --b2
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    //private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            /* We dont use movement messages, maybe later ...
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                requestRender();
                break;
            */

            case MotionEvent.ACTION_DOWN:
                mRenderer.setActionDown(x, y);
                requestRender();
                break;
        }
        mPreviousX = x;
        mPreviousY = y;

        requestRender();
        return true;
    }

    // Pitch
    public void setPitch(float degrees)
    {
        mRenderer.setPitch(degrees);
        requestRender();
    }

    // Roll / Bank
    public void setRoll(float degrees)
    {
        mRenderer.setRoll(degrees);
        requestRender();
    }

    // Heading / Course indicator
    public void setHeading(float degrees)
    {
        mRenderer.setHeading(degrees);
        requestRender();
    }

    // Altimeter
    public void setALT(int value)
    {
        mRenderer.setALT(value);
        requestRender();
    }

    // Radio Altimeter (agl)
    void setAGL(int value)
    {
        mRenderer.setAGL(value);
        requestRender();
    }


    // Air Speed Indicator
    public void setASI(float value)
    {
        mRenderer.setIAS(value);
        requestRender();
    }

    // Vertical Speed Indicator
    public void setVSI(int value)
    {
        mRenderer.setVSI(value);
        requestRender();
    }

    // FLight Path Vector
    void setFPV(float fpvX, float fpvY)
    {
        mRenderer.setFPV(fpvX, fpvY);
        requestRender();
    }

    // G force
    public void setGForce(float gunits)
    {
        mRenderer.setGForce(gunits);
        requestRender();
    }

    //Parentage battery remaining
    public void setBatteryPct(float value)
    {
        mRenderer.setBatteryPct(value);
        requestRender();
    }

    // The amount of Slip
    public void setSlip(float slip)
    {
        mRenderer.setSlip(slip);
        requestRender();
    }

    // Automatic Waypoint
    public void setWPT(String wpt)
    {
        mRenderer.setAutoWptValue(wpt);
        requestRender();
    }

    // The DME - Distance Measuring Equipment
    public void setDME(float dme)
    {
        mRenderer.setAutoWptDme(dme);
        requestRender();
    }

    // Ground Speed
    public void setGS(float gs)
    {
        //mRenderer.setGS(gs);
        //requestRender();
    }


    // Turn Rate
    public void setTurn(float rot)
    {
        mRenderer.setTurn(rot);
        requestRender();
    }

    //
    // Red X's
    //

    // The entire EFIS
    public void setUnServiceableDevice()
    {
        mRenderer.setUnServiceableDevice();
        requestRender();
    }

    public void setServiceableDevice()
    {
        mRenderer.setServiceableDevice();
        requestRender();
    }

    // Artificial Horizon
    public void setUnServiceableAh()
    {
        mRenderer.setUnServiceableAh();
        requestRender();
    }

    public void setServiceableAh()
    {
        mRenderer.setServiceableAh();
        requestRender();
    }

    // Altimeter
    public void setUnServiceableAlt()
    {
        mRenderer.setUnServiceableAlt();
        requestRender();
    }

    public void setServiceableAlt()
    {
        mRenderer.setServiceableAlt();
        requestRender();
    }

    // Airspeed
    public void setUnServiceableAsi()
    {
        mRenderer.setUnServiceableAsi();
        requestRender();
    }

    public void setServiceableAsi()
    {
        mRenderer.setServiceableAsi();
        requestRender();
    }

    // Direction Indicator
    public void setUnServiceableDi()
    {
        mRenderer.setUnServiceableDi();
        requestRender();
    }

    public void setServiceableDi()
    {
        mRenderer.setServiceableDi();
        requestRender();
    }

    void setLatLon(float lat, float lon)
    {
        mRenderer.setLatLon(lat, lon);
        requestRender();
    }

    void setGpsStatus(String gpsstatus)
    {
        mRenderer.setGpsStatus(gpsstatus);
        requestRender();
    }

    void setDisplayFPV(boolean display)
    {
        mRenderer.setDisplayFPV(display);
        requestRender();
    }

    void setDisplayAirport(boolean display)
    {
        mRenderer.setDisplayAirport(display);
        requestRender();
    }

    void setDisplayAirspace(boolean display)
    {
        mRenderer.setDisplayAirspace(display);
        requestRender();
    }

    /*
    void setCalibratingMsg(boolean cal, String msg)
    {
        mRenderer.setCalibrate(cal, msg);
        requestRender();
    }
    */

    public void setFlightDirector(boolean active, float pit, float rol)
    {
        mRenderer.setFlightDirector(active, pit, rol);
        requestRender();
    }


    void setDemoMode(boolean demo, String msg)
    {
        mRenderer.setDemoMode(demo, msg);
        requestRender();
    }


    public void setPrefs(prefs_t pref, boolean value)
    {
        mRenderer.setPrefs(pref, value);
        requestRender();
    }

    public void setMapZoom(float zoom)
    {
        mRenderer.setMapZoom(zoom);
        requestRender();
    }

}
