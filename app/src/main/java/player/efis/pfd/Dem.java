
package player.efis.pfd;

// Standard imports
import android.content.Context;
import java.io.*;



/*

a
 +------------------------------------+
 |            b                       |     a = demTopLeftLat,  demTopLeftLon
 |             +---------+            |     b = x0, y0
 |             |         |            |     c = lat0, lon0
 |             |  c +    |            |
 |             |         |            |
 |             +---------+            |
 |                                    |
 |             |< BUFX  >|            |
 |                                    |
 +------------------------------------+

 Note: BUFX should fit completely in the DEM tile
       Consider this when choosing size
*/


public class Dem
{
    Context context;

    final static float DEM_HORIZON = 30; // nm

    final  int maxcol = 4800;
    final  int maxrow = 6000;

    static final int BUFX = 400;  //600;  //800 = 400nm square ie at least  200nm in each direction
    static final int BUFY = BUFX; // 400;
    static short buff[][] = new short[BUFX][BUFY];

    static float demTopLeftLat =  -10;
    static float demTopLeftLon = +100;

    static public float lat0;
    static public float lon0;
    static private int x0;   // center of the BUFX tile ??
    static private int y0;   // center of the BUFX tile

    public static boolean demDataValid = false;

    //-------------------------------------------------------------------------
    // Construct a new default loader with no flags set
    //
    public Dem()
    {
    }

    public Dem(Context context)
    {
        this.context = context;
    }


    public static short getElev(float lat, float lon)
    {
        // Do we return bad data and let the program continue
        // or do we wait for valid data ?
        //while (!demDataValid)
        //  ; // do nothing and wait for valid data

        // check if buff is valid -- todo
        // do the test here ... if not in buff ... do a reload centered here.
        //float center_dme = UNavigation.calcDme(lat, lon, lat0, lon0);

        // *60 > min * 2 > 30 arcsec = 1/2 a min
        int y = (int) (Math.abs(lat - demTopLeftLat) * 60 * 2) - y0; //+ BUFX/2;
        int x = (int) (Math.abs(lon - demTopLeftLon) * 60 * 2) - x0; // + BUFY/2;
        //int x = 0;

        if ((x < 0) || (y < 0) || (x >= BUFX) || ( y >= BUFY))
            return -8888;
        else return buff[x][y];

    }

    public void setBufferCenter(float lat, float lon)
    {
        lat0 = lat;
        lon0 = lon;
        x0 = (int) (Math.abs(lon0 - demTopLeftLon) * 60 *2) -  BUFX/2;
        y0 = (int) (Math.abs(lat0 - demTopLeftLat) * 60 *2) -  BUFY/2;
    }


    //-------------------------------------------------------------------------
    // use the lat lon to determine with region file is active
    //
    String DemFilename;
    public void setDEMRegionFileName(float lat, float lon)
    {
        setBufferCenter(lat, lon);  // set the buffer tile as well

        demTopLeftLat =  (int) (lat+10) / 50 * 50 - 10; //-10;
        demTopLeftLon =  (int) (lon-20) / 40 * 40 + 20; //+100;

        String s = String.format("%c%03d%c%02d", demTopLeftLon<0?'W':'E', (int)Math.abs(demTopLeftLon),
                                                 demTopLeftLat<0?'S':'N', (int)Math.abs(demTopLeftLat));
        DemFilename = s;

    }


    // assume a setBufferCenter was done...
    // assume a setDEMRegionFileName was done...
    public void loadDemBuffer(/*String database*/)
    {
        demDataValid = false;

        try {

            InputStream inp  = context.getAssets().open("terrain/" + DemFilename + ".DEM"); //SA-E
            DataInputStream demFile = new DataInputStream(inp);

            final int NUM_BYTES_IN_SHORT = 2;
            short c;

            int x, y;
            int x1, y1, x2, y2;

            x1 = x0;
            x2 = x0 + BUFX;
            y1 = y0;
            y2 = y0 + BUFY;

            demFile.skipBytes(NUM_BYTES_IN_SHORT  * (maxcol * y1));
            demFile.skipBytes(NUM_BYTES_IN_SHORT * (x1));
            for (y = y1; y < y2; y++) {
                for (x = x1; x < x2; x++) {

                    /*if ((x-x1 > 0)
                       && (y-y1 > 0)
                       && (x-x1 < maxcol)
                       && (y-y1 < maxrow)
                       ) {*/

                    c = demFile.readShort();
                    if (c > 0) {
                        buff[x-x1][y-y1] = c;  // fill in the buffer
                    }
                }
                demFile.skipBytes(NUM_BYTES_IN_SHORT * (maxcol - x2));
                demFile.skipBytes(NUM_BYTES_IN_SHORT * (x1));
            }
            demFile.close();
            demDataValid = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
