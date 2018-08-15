package player.efis.common;

import android.os.AsyncTask;
import android.util.Log;

import com.stratux.stratuvare.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import player.ulib.UTime;
import player.ulib.Unit;

//class RetrieveFeedTask extends AsyncTask<String, Void, RSSFeed> {

public class StratuxWiFiTask extends AsyncTask<String, Void, Void>
{
    protected static final int CONNECTED = 1;
    protected static final int CONNECTING = 2;
    protected static final int DISCONNECTED = 0;

    private Exception exception;
    private boolean mRunning;
    private boolean mCancel;
    //private Thread mThread;

    DatagramSocket mSocket;
    private int mPort = 4000;
    private int mState;

    // Public Stratux vars
    public int GPSSatellites;
    public int GPSSatellitesTracked;
    public int GPSSatellitesSeen;

    public double AHRSPitch;
    public double AHRSRoll;
    public double AHRSGyroHeading;
    public double AHRSMagHeading;
    public double AHRSSlipSkid;
    public double AHRSTurnRate;
    public double AHRSGLoad;
    public double AHRSGLoadMin;
    public double AHRSGLoadMax;

    public double GPSLongitude;
    public double GPSLatitude;
    public double GPSAltitudeMSL;
    public double GPSTrueCourse;
    public double GPSTurnRate;
    public double GPSGroundSpeed;
    private String id;

    public StratuxWiFiTask(String id)
    {
        this.id = id;
        mState = DISCONNECTED;
        mRunning = false;
        mCancel = false;

        //calibrateAhrs();
        //cageAhrs();
    }


    //protected RSSFeed doInBackground(String... urls) {
    protected Void doInBackground(String... urls)
    {
        try {
            mRunning = true;
            mainExecutionLoop();
        }
        catch (Exception e) {
        }
        finally {
        }
        return null;
    }

    protected void onPostExecute()
    {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    static Semaphore mutex = new Semaphore(1, true);
    private LinkedList<String> trafficList = new LinkedList<String>();

    LinkedList<String> getAcList()
    {

        //if (trafficList == null) return null;
        //return new LinkedList<String>(trafficList);
        try {
            mutex.acquire();
            try {
                return new LinkedList<String>(trafficList);
            }
            finally {
                mutex.release();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    //private LinkedList<String> objs;// = bp.decode();
    float a, b;
    long pt1 = 0, pt2 = 0;

    private void mainExecutionLoop()
    {
        byte[] buffer = new byte[8192];
        BufferProcessor bp = new BufferProcessor();

        //
        // This state machine will keep trying to connect to
        // ADBS/GPS receiver
        //
        //while (mRunning == true) {
        while (mCancel == false) {
            if (!mRunning) {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {}
                continue;
            }

            //  Read
            int nrBytesRead = read(buffer);
            if (nrBytesRead <= 0) {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {}
                disconnect();
                connect(Integer.toString(mPort), false);
                /*if (mRunning) {
                    //  Try to reconnect
                    Logger.Logit(id + "Listener error, re-starting listener");
                    disconnect();
                    if (connect(Integer.toString(mPort), false)) start();
                    else stop();
                }*/
                continue;
            }

            //  Put both in Decode and ADSB buffers
            bp.put(buffer, nrBytesRead);

            try {
                mutex.acquire();
                try {

                    LinkedList<String> objs = bp.decode();
                    long unixTime = UTime.getUtcTimeMillis();

                    /*-------------------------------------
                    // < debug - add ghost AC traffic

                    // fixed target
                    JSONObject object = new JSONObject();

                    //LongReportMessage tm = (LongReportMessage) m;
                    try {
                        object.put("type", "traffic");
                        object.put("longitude", (double) 115.83);
                        object.put("latitude", (double) -31.80);
                        object.put("speed", (double) 123.0);
                        object.put("bearing", (double) 348.7);
                        //object.put("altitude", (double) Unit.Meter.toFeet(75));
                        object.put("altitude", 4321);
                        object.put("callsign", (String) "GHOST-0");
                        object.put("address", (int) 777);
                        object.put("time", (long) unixTime);
                    }
                    catch (JSONException e1) {
                        continue;
                    }
                    objs.add(object.toString());


                    // 2 moving targets
                    if (unixTime - pt1 > 4000) {
                        pt1 = unixTime;
                        b = b + 0.001f;

                        object = new JSONObject();
                        //LongReportMessage tm = (LongReportMessage) m;
                        try {
                            object.put("type", "traffic");
                            object.put("longitude", (double) 115.9 - b / 5);
                            object.put("latitude", (double) -32.2 + b);
                            object.put("speed", (double) 123.0);
                            object.put("bearing", (double) 348.7);
                            object.put("altitude", (double) 4321);
                            object.put("callsign", (String) "GHOST-1");
                            object.put("address", (int) 555);
                            object.put("time", (long) unixTime);
                        }
                        catch (JSONException e1) {
                            continue;
                        }
                        objs.add(object.toString());
                    }

                    if (unixTime - pt2 > 1000) {
                        pt2 = unixTime;
                        a = a + 0.001f;

                        object = new JSONObject();
                        //LongReportMessage tm = (LongReportMessage) m;
                        try {
                            object.put("type", "traffic");
                            object.put("longitude", (double) 115.7 + a / 5);
                            object.put("latitude", (double) -32.2 + a);
                            object.put("speed", (double) 246.0);
                            object.put("bearing", (double) 11.3);
                            object.put("altitude", (double) 7654);
                            object.put("callsign", (String) "GHOST-2");
                            object.put("address", (int) 666);
                            object.put("time", (long) unixTime);
                        }
                        catch (JSONException e1) {
                            continue;
                        }
                        objs.add(object.toString());
                    }
                    // debug >
                    //-------------------------------------*/


                    // Extract traffic
                    for (String s : objs) {
                        try {
                            JSONObject js = new JSONObject(s);
                            if (js.getString("type").contains("traffic")) {
                                for (int i = 0; i < trafficList.size(); i++) {
                                    String t = trafficList.get(i);
                                    JSONObject jt = new JSONObject(t);
                                    if (jt.getInt("address") == js.getInt("address")) {
                                        trafficList.remove(i);
                                    }
                                    // If a target is older then 20 seconds remove it from the list
                                    long deltaT = unixTime - jt.getLong("time");
                                    if (deltaT > 20 * 1000) {
                                        trafficList.remove(i);
                                    }
                                }
                                trafficList.add(s);
                            }


                        }
                        catch (JSONException e) {
                        }
                    }

                    //----------------------------------------------
                    // use the Http
                    try {
                        // Situation
                        String situation = getSituation();

                        JSONObject jObject;
                        jObject = new JSONObject(situation);

                        GPSSatellites = jObject.getInt("GPSSatellites");
                        GPSSatellitesTracked = jObject.getInt("GPSSatellitesTracked");
                        GPSSatellitesSeen = jObject.getInt("GPSSatellitesSeen");

                        AHRSPitch = jObject.getDouble("AHRSPitch");
                        AHRSRoll = jObject.getDouble("AHRSRoll");
                        AHRSGyroHeading = jObject.getDouble("AHRSGyroHeading");
                        AHRSMagHeading = jObject.getDouble("AHRSMagHeading");
                        AHRSSlipSkid = jObject.getDouble("AHRSSlipSkid");
                        AHRSTurnRate = jObject.getDouble("AHRSTurnRate");
                        AHRSGLoad = jObject.getDouble("AHRSGLoad");
                        AHRSGLoadMin = jObject.getDouble("AHRSGLoadMin");
                        AHRSGLoadMax = jObject.getDouble("AHRSGLoadMax");

                        GPSLatitude = jObject.getDouble("GPSLatitude");
                        GPSLongitude = jObject.getDouble("GPSLongitude");
                        GPSAltitudeMSL = jObject.getDouble("GPSAltitudeMSL");
                        GPSTrueCourse = jObject.getDouble("GPSTrueCourse");
                        GPSTurnRate = jObject.getDouble("GPSTurnRate");
                        GPSGroundSpeed = jObject.getDouble("GPSGroundSpeed");

                        // Status
                        String status = getDeviceStatus();
                        jObject = new JSONObject(status);

                        String rv = jObject.getString("Ping_connected");
                        //Log.v("bugbug", rv);

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                finally {
                    mutex.release();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private int read(byte[] buffer)
    {
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
        try {
            mSocket.receive(pkt);
        }
        catch (Exception e) {
            return -1;
        }

        //saveToFile(pkt.getLength(), buffer);
        return pkt.getLength();
    }


    //
    // Stratux getSituation post
    //
    private String getSituation()
    {
        return getHttp("http://192.168.10.1/getSituation");
    }

    //
    // Stratux getSituation post
    //
    private String getDeviceStatus()
    {
        return getHttp("http://192.168.10.1/getStatus");
    }

    //
    // Stratux "level" attitude display. Submit a blank POST to this URL.
    //
    private String cageAhrs()
    {
        return postHttp("http://192.168.10.1/cageAHRS");
    }

    //
    // Stratux sensor calibration. Submit a blank POST to this URL.
    //
    private String calibrateAhrs()
    {
        return postHttp("http://192.168.10.1/calibrateAHRS");
    }



    private String getHttp(String addr)
    {
        return doHttp(addr, "GET");
    }

    private String postHttp(String addr)
    {
        return doHttp(addr, "POST");
    }


    private String doHttp(String addr, String method)
    {
        URL url;
        StringBuffer response = new StringBuffer();
        try {
            //url = new URL("http://192.168.10.1/getSituation");
            url = new URL(addr);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url");
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(method); //"GET"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
            //Here is your json in string format
            String responseJSON = response.toString();
            return responseJSON;
        }
    }


    public boolean connect(String to, boolean secure)
    {
        try {
            mPort = Integer.parseInt(to);
        }
        catch (Exception e) {
            return false;
        }

        Logger.Logit(id + "Making socket to listen");

        try {
            mSocket = new DatagramSocket(mPort);
            mSocket.setBroadcast(true);
        }
        catch (Exception e) {
            Logger.Logit(id + "Failed! Connecting socket " + e.getMessage());
            return false;
        }
        mState = CONNECTED;
        return true; //connectConnection();
    }

    public String getParam()
    {
        return Integer.toString(mPort);
    }

    public void disconnect()
    {
        try {
            mSocket.close();
        }
        catch (Exception e2) {
            Logger.Logit(id + "Error stream close");
        }
        mState = DISCONNECTED;
    }

    public void finish()
    {
        mCancel = true;
    }

    public void stop()
    {
        if (mState != CONNECTED) {
            Logger.Logit(id + ": Stop failed because already stopped");
            return;
        }
        mRunning = false;
        disconnect();
        Logger.Logit(id + "Stopped");
    }


    public void start()
    {
        if (mState != DISCONNECTED) {
            Logger.Logit(id + ": Starting failed because already started");
            return;
        }
        mRunning = true;
        disconnect();
        connect(Integer.toString(mPort), false);
        Logger.Logit(id + "Started");
    }

    protected boolean isRunning()
    {
        return mRunning;
    }

    protected boolean isStopped()
    {
        return !mRunning;
    }



}


    /*{
        "GPSLastFixSinceMidnightUTC": 67337.6,
            "GPSLatitude": 39.108533,
            "GPSLongitude": -76.770862,
            "GPSFixQuality": 2,
            "GPSHeightAboveEllipsoid": 115.51,
            "GPSGeoidSep": -17.523,
            "GPSSatellites": 5,
            "GPSSatellitesTracked": 11,
            "GPSSatellitesSeen": 8,
            "GPSHorizontalAccuracy": 10.2,
            "GPSNACp": 9,
            "GPSAltitudeMSL": 170.10767,
            "GPSVerticalAccuracy": 8,
            "GPSVerticalSpeed": -0.6135171,
            "GPSLastFixLocalTime": "0001-01-01T00:06:44.24Z",
            "GPSTrueCourse": 0,
            "GPSTurnRate": 0,
            "GPSGroundSpeed": 0.77598433056951,
            "GPSLastGroundTrackTime": "0001-01-01T00:06:44.24Z",
            "GPSTime": "2017-09-26T18:42:17Z",
            "GPSLastGPSTimeStratuxTime": "0001-01-01T00:06:43.65Z",
            "GPSLastValidNMEAMessageTime": "0001-01-01T00:06:44.24Z",
            "GPSLastValidNMEAMessage": "$PUBX,04,184426.00,260917,240266.00,1968,18,-177618,-952.368,21*1A",
            "GPSPositionSampleRate": 0,
            "BaroTemperature": 37.02,
            "BaroPressureAltitude": 153.32,
            "BaroVerticalSpeed": 1.3123479,
            "BaroLastMeasurementTime": "0001-01-01T00:06:44.23Z",
            "AHRSPitch": -0.97934145732801,
            "AHRSRoll": -2.2013729217108,
            "AHRSGyroHeading": 187741.08073052,
            "AHRSMagHeading": 3276.7,
            "AHRSSlipSkid": 0.52267604604907,
            "AHRSTurnRate": 3276.7,
            "AHRSGLoad": 0.99847599584255,
            "AHRSGLoadMin": 0.99815989027411,
            "AHRSGLoadMax": 1.0043409597397,
            "AHRSLastAttitudeTime": "0001-01-01T00:06:44.28Z",
            "AHRSStatus": 7
    }*/



/*
    protected RSSFeed doInBackground(String... urls) {
    {
        try {
            URL url = new URL(urls[0]);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlreader = parser.getXMLReader();
            RssHandler theRSSHandler = new RssHandler();
            xmlreader.setContentHandler(theRSSHandler);
            InputSource is = new InputSource(url.openStream());
            xmlreader.parse(is);

            return theRSSHandler.getFeed();
        } catch (Exception e) {
            this.exception = e;

            return null;
        } finally {
            is.close();
        }
    }

    protected void onPostExecute(RSSFeed feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
*/


