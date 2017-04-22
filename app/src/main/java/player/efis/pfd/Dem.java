
package player.efis.pfd;

// Standard imports
import android.content.Context;
import java.io.*;


public class Dem
{
    static final int BUFX = 400;
    static final int BUFY = 400;
    static short buff[][] = new short[BUFX][BUFY];

    static float demTopLeftLat =  -10;
    static float demTopLeftLon = +100;

    static private float lat0 = -32.0f;
    static private float lon0 = 116.0f;

    static private int x0 = (int) (Math.abs(lon0 - demTopLeftLon) * 60 *2) - BUFX/2;  //0;
    static private int y0 = (int) (Math.abs(lat0 - demTopLeftLat) * 60 *2) - BUFY/2;  //2750;

    //static private int x0 = (int) ((116.0 - 100) * 60 *2) - BUFX/2;  //0;
    //static private int y0 = (int) ((32.0 - 10) * 60 *2) - BUFY/2;  //2750;

    final int width = BUFX;
    final int height = BUFY;

    Context context;
    public String region = "zar.aus";
    //static ArrayList<Apt> aptList = null;

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
        // check if buff is valid -- todo

        int y = (int) (Math.abs(lat - demTopLeftLat) * 60 * 2) - y0;  // *60 > min * 2 > 30 arcsec = 1/2 a min
        int x = (int) (Math.abs(lon - demTopLeftLon) * 60 * 2) - x0;
        //int x = 0;

        if ((x < 0) || (y < 0) || (x > BUFX) || ( y > BUFY)) return -9999;
        else return buff[x][y];

        //return buff[x][y];
    }


    public void loadDEM(String database)
    {
        region = database;
        try {
            //InputStream in_s = context.getAssets().open(region + "/E100S10.DEM"); //AU-W
            //InputStream in_s = context.getAssets().open(region + "/E020S10.DEM"); //SA-E
            InputStream in_s = context.getAssets().open(region + "/TEST.DEM"); //SA-E
            InputStream input = new BufferedInputStream(in_s);
            readDEMBinaryFileToBuff(input);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }



    //public void readDEMBinaryFileToBuff(String aFileName)
    public void readDEMBinaryFileToBuff(InputStream inputStream)
    {
        DataInputStream in = null;

        try {
            // Now layer a DataInputStream on top of it.
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            short c;

            int x = 0;
            int y = 0;

            final int maxcol = 4800;
            final int maxrow = 6000;

            //0,2710 = mountains; // 0,2800 = south coast

            int x1 = 1800;
            int y1 = 0;
            int x2 = 3000;  //1000;
            int y2 = 5600;   //2000;

            x1 = x0;
            x2 = x1 + width;
            y1 = y0;
            y2 = y1 + height;

            int _x = 0;
            int _y = 0;
            int _c = 0;
            final int NUM_BYTES = 2;

            dataInputStream.skipBytes(NUM_BYTES  * (maxcol * y1));
            dataInputStream.skipBytes(NUM_BYTES * (x1));
            for (y = y1; y < y2; y++) {
                for (x = x1; x < x2; x++) {
                    c = dataInputStream.readShort();
                    //System.out.println(c);
                    if (c > 0) {
                        buff[x-x1][y-y1] = c;  /// fill in the buffer
                    }
                }
                dataInputStream.skipBytes(NUM_BYTES * (maxcol - x2));
                dataInputStream.skipBytes(NUM_BYTES * (x1));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






    //public void readDEMBinaryFile(String aFileName)
    public void readDEMBinaryFile(InputStream inputStream)
    {
        DataInputStream in = null;
        //Graphics g = getGraphics();
        //g.drawLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
        //lastPoint = new Point(e.getX(), e.getY());


        try {
            //in = new FileInputStream("xanadu.txt");
            //out = new FileOutputStream("outagain.txt");
            //in = new FileInputStream("h:\\home\\player\\TMP\\DTM\\E100S10.DEM");

            // First create a FileInputStream.
            //InputStream inputStream = new FileInputStream("h:\\home\\player\\TMP\\DTM\\W180N90.DEM"); //ALASKA
            //InputStream inputStream = new FileInputStream("h:\\home\\player\\TMP\\DTM\\E100S10.DEM"); //AUS-W
            //InputStream inputStream = new FileInputStream("h:\\home\\player\\TMP\\DTM\\E020S10.DEM"); //SA-E
            //InputStream inputStream = new FileInputStream("h:\\home\\player\\TMP\\DTM\\W020S10.DEM"); //SA-W


            // Now layer a DataInputStream on top of it.
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            //int c;
            short c;

            int x = 0;
            int y = 0;
            int div = 5;
            float r = 600;
            float r2 = r*2;

            int cmax = 0;
            //Color color = new Color(0, 1, 0);
            //g.setColor(color);

            //while ((c = in.read()) != -1) {
            float red = 0;
            float blue = 0;
            float green = 0;//(float) c / r;

            while ((c = dataInputStream.readShort()) != -1) {
                //System.out.println(c);

                /*if (c > 0) {
                    red = 0;
                    blue = 0;
                    green = (float) c / r;
                    if (green > 1) {
                        green = 1;
                        red = (float) (c - r) / r;
                        if (red > 1) {
                            red = 1;
                            blue = (float) (c - r2) / r;
                            if (blue > 1) {
                                blue = 1;
                            }
                        }
                    }*/


                    //Color color = new Color(red, green, blue);
                    //g.setColor(color);
                    //g.drawLine(x / div, y / div, x / div, y / div);
                }

                x++;
                if (x >= 4800) {
                    x = 0;
                    y++;
                }
        }
        catch (IOException e) {

        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
            }
        }
    }

}
