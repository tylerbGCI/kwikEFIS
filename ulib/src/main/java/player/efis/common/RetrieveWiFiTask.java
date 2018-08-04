package player.efis.common;

import android.os.AsyncTask;
import android.util.Log;

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

//class RetrieveFeedTask extends AsyncTask<String, Void, RSSFeed> {

public class RetrieveWiFiTask extends AsyncTask<String, Void, Void>
{

    private Exception exception;
    private boolean mRunning;
    private Thread mThread;

    DatagramSocket mSocket;
    private int mPort = 4000;

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

    public double GPSTrueCourse;
    public double GPSAltitudeMSL;
    public double GPSTurnRate;
    public double GPSGroundSpeed;

    //protected RSSFeed doInBackground(String... urls) {
    protected Void doInBackground(String... urls)
    {
        try {
            doRead();
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


    public BufferProcessor bp;// = new BufferProcessor();

    private void doRead()
    {
        //BufferProcessor bp = new BufferProcessor();
        bp = new BufferProcessor();

        byte[] buffer = new byte[8192];

        mRunning = true;
        //
        // This state machine will keep trying to connect to
        // ADBS/GPS receiver
        //
        //while (isRunning()) {
        while (mRunning) {

            int red = 0;

            //  Read.
            red = read(buffer);
            if (red <= 0) {
                //if (isStopped()) {
                if (!mRunning) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                }

                //
                //  Try to reconnect
                //
                //Logger.Logit("Listener error, re-starting listener");

                disconnect();
                connect(Integer.toString(mPort), false);
                continue;
            }

            //String traffic = readURL("ws://192.168.10.1/traffic");
            //Log.d("++trf: ", traffic);

            //Log.d("B2", "doRead: " + red);


            //
            //  Put both in Decode and ADSB buffers
            //
            bp.put(buffer, red);
            LinkedList<String> objs = bp.decode();
            for (String s : objs) {
                // sendDataToHelper(s);
                //Log.d("s", s);

                try {
                    JSONObject jObject = new JSONObject(s);
                    if (jObject.getString("type") == "traffic") {
                        String callsign = jObject.getString("callsign");
                        Log.d("callsign=", callsign);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //----------------------------------------
            String situation = getSituation(/*"http://192.168.10.1/getSituation"*/);
            //Log.d("++sit: ", situation);

            try {
                JSONObject jObject = new JSONObject(situation);

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

                GPSTrueCourse = jObject.getDouble("GPSTrueCourse");
                GPSAltitudeMSL = jObject.getDouble("GPSAltitudeMSL");
                GPSTurnRate = jObject.getDouble("GPSTurnRate");
                GPSGroundSpeed = jObject.getDouble("GPSGroundSpeed");



                //Log.d("pitch: ", String.valueOf(pitch));
                //Log.d("roll: ", String.valueOf(roll));
                //Log.d("debug ", String.valueOf(AHRSSlipSkid));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // return null;
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

    private String getSituation()
    {
        URL url;
        StringBuffer response = new StringBuffer();
        try {
            url = new URL("http://192.168.10.1/getSituation");
            //url = new URL(address);
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
            conn.setRequestMethod("GET");
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

        //Logger.Logit("Making socket to listen");

        try {
            mSocket = new DatagramSocket(mPort);
            mSocket.setBroadcast(true);
        }
        catch (Exception e) {
            //Logger.Logit("Failed! Connecting socket " + e.getMessage());
            return false;
        }

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
            //Logger.Logit("Error stream close");
        }

        //disconnectConnection();
    }


}


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