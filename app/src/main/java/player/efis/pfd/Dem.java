
package player.efis.pfd;

// Standard imports
import android.content.Context;
import java.io.*;

import player.ulib.UNavigation;



/*

a
 +------------------------------------+
 |                                    |     a = demTopLeftLat /  demTopLeftLon
 |             +---------+            |     b = lat0 / lon0 and x0 / y 0
 |             |         |            |
 |             |  b +    |            |
 |             |         |            |
 |             +---------+            |
 |                                    |
 |                                    |
 +------------------------------------+
*/


public class Dem
{
    final int maxcol = 4800;
    final int maxrow = 6000;

    static final int BUFX = 600;  //800 = 400nm square ie at least  200nm in each direction
    static final int BUFY = BUFX; // 400;
    static short buff[][] = new short[BUFX][BUFY];

    static float demTopLeftLat =  -10;
    static float demTopLeftLon = +100;

    /*
    static public float lat0 = -32.0f;
    static public float lon0 = 116.0f;
    static private int x0 = (int) (Math.abs(lon0 - demTopLeftLon) * 60 *2) - BUFX/2;  //0;
    static private int y0 = (int) (Math.abs(lat0 - demTopLeftLat) * 60 *2) - BUFY/2;  //2750;
    */

    static public float lat0;
    static public float lon0;
    static private int x0;   // center of the BUFX tile ??
    static private int y0;   // center of the BUFX tile


    //static private int x0 = (int) ((116.0 - 100) * 60 *2) - BUFX/2;  //0;
    //static private int y0 = (int) ((32.0 - 10) * 60 *2) - BUFY/2;  //2750;

    final int width = BUFX;
    final int height = BUFY;

    Context context;
    public String region = "zar.aus";

    public static boolean demDataValid = false;

    /**
     * Construct a new default loader with no flags set
     */
    public Dem()
    {
    }

    public Dem(Context context)
    {
        this.context = context;
        //aptList = new ArrayList();
    }


    public static short getElev(float lat, float lon)
    {
        // Do we return bad data and let the program continue
        // or do we wait for valid data ?
        while (!demDataValid)
          ; // do nothing and wait for valid data

        // check if buff is valid -- todo
        // do the test here ... if not in buff ... do a reload centered here.
        //float center_dme = UNavigation.calcDme(lat, lon, lat0, lon0);

        int y = (int) (Math.abs(lat - demTopLeftLat) * 60 * 2) - y0;  // *60 > min * 2 > 30 arcsec = 1/2 a min
        int x = (int) (Math.abs(lon - demTopLeftLon) * 60 * 2) - x0;
        //int x = 0;

        if ((x < 0) || (y < 0) || (x >= BUFX) || ( y >= BUFY))
            return -8888;
        else return buff[x][y];

        //return buff[x][y];
    }

    public void setTile(float lat, float lon)
    {
        lat0 = lat;
        lon0 = lon;
        x0 = (int) (Math.abs(lon0 - demTopLeftLon) * 60 *2) - BUFX/2;
        y0 = (int) (Math.abs(lat0 - demTopLeftLat) * 60 *2) - BUFY/2;
    }


    //-------------------------------------------------------------------------
    // use the lat lon to determine with region file is active
    //
    String DemFilename;
    public void setDEMRegionFileName(float lat, float lon)
    {
        setTile(lat, lon);  // set the buffer tile as well

        demTopLeftLat =  (int) (lat+10) / 50 * 50 - 10; //-10;
        demTopLeftLon =  (int) (lon-20) / 40 * 40 + 20; //+100;

        String s = String.format("%c%03d%c%02d", demTopLeftLon<0?'W':'E', (int)Math.abs(demTopLeftLon),
                                                 demTopLeftLat<0?'S':'N', (int)Math.abs(demTopLeftLat));
        DemFilename = s;

    }


    // assume a setTile was done...
    public void loadDemBuffer(String database)
    {
        demDataValid = false;
        region = database;

        try {

            InputStream inp  = context.getAssets().open(region + "/" + DemFilename + ".DEM"); //SA-E
            DataInputStream demFile = new DataInputStream(inp);

            short c;

            int x = 0;
            int y = 0;

            int x1; //= 1800;
            int y1; //= 0;
            int x2; //= 3000;  //1000;
            int y2; //= 5600;   //2000;

            x1 = x0;
            x2 = x1 + width;
            y1 = y0;
            y2 = y1 + height;

            int _x = 0;
            int _y = 0;
            int _c = 0;
            final int NUM_BYTES_IN_SHORT = 2;

            //demFile.seek(0);
            demFile.skipBytes(NUM_BYTES_IN_SHORT  * (maxcol * y1));
            demFile.skipBytes(NUM_BYTES_IN_SHORT * (x1));
            for (y = y1; y < y2; y++) {
                for (x = x1; x < x2; x++) {
                    c = demFile.readShort();
                    //System.out.println(c);
                    if (c > 0) {
                        buff[x-x1][y-y1] = c;  // fill in the buffer
                    }
                }
                demFile.skipBytes(NUM_BYTES_IN_SHORT * (maxcol - x2));
                demFile.skipBytes(NUM_BYTES_IN_SHORT * (x1));
            }
            demFile.close();
            //inp.close();
            demDataValid = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }






}
