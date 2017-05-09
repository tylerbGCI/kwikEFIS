/*
 * Copyright (C) 2017 Player One
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


package player.efis.data;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//------------------------------
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
//------------------------------


//public class EFISDataPac extends AppCompatActivity
//public class DataPac extends Activity
public class EFISDataPac extends Activity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kwik_efisdata);

        ///*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button gobutton = (Button) findViewById(R.id.button_go );
        gobutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DoSomething();
            }
        });
        //*/

        // disable and hide the buttons
        // -- we may use this again in future
        fab.setVisibility(View.GONE);
        gobutton.setVisibility(View.GONE);

        listAssetFiles("terrain");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kwik_efisdata, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void listAssets()
    {
        System.out.println("Hello World");
        TextView tv = new TextView(this);
        for (int i = 0; i < 10; i++) {
            tv.setText("Hello\nWorld");
            this.setContentView(tv);
        }

        //this.getAssets()

    }

    private boolean listAssetFiles(String path)
    {

        String [] list;
        try {
            list = getAssets().list(path);
        }
        catch (IOException e) {
            return false;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gtopo30_index);
        bitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true);
        int twidth = bitmap.getWidth() / 9;
        int theight = bitmap.getHeight() / 3;

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(173, 214, 255)); // cyanish
        paint.setAlpha(128);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Get the bitmap to draw on
        ImageView imgView = new ImageView(this);
        imgView.setImageBitmap(bitmap);

        // Get the version number of the app
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;

        TextView txtView = new TextView(this);
        String buff = "\nKwik EFIS Terrain data. Version " + version + "\n\n";
        for (int i = 0; i < list.length; i++) {
            buff += list[i] + "\t";

            int x1=0, y1=0, x2, y2;
            switch (list[i]) {
                // top row
                case "W180N90.DEM":
                    x1 = twidth * 0;
                    y1 = theight * 0;
                    break;
                case "W140N90.DEM":
                    x1 = twidth * 1;
                    y1 = theight * 0;
                    break;
                case "W100N90.DEM":
                    x1 = twidth * 2;
                    y1 = theight * 0;
                    break;
                case "W060N90.DEM":
                    x1 = twidth * 3;
                    y1 = theight * 0;
                    break;
                case "W020N90.DEM":
                    x1 = twidth * 4;
                    y1 = theight * 0;
                    break;
                case "E020N90.DEM":
                    x1 = twidth * 5;
                    y1 = theight * 0;
                    break;
                case "E020S90.DEM":
                    x1 = twidth * 5;
                    y1 = theight * 0;
                    break;
                case "E060S90.DEM":
                    x1 = twidth * 6;
                    y1 = theight * 0;
                    break;
                case "E100S90.DEM":
                    x1 = twidth * 7;
                    y1 = theight * 0;
                    break;
                case "E140S90.DEM":
                    x1 = twidth * 8;
                    y1 = theight * 0;
                    break;

                // middle row
                case "W180N40.DEM":
                    x1 = twidth * 0;
                    y1 = theight * 1;
                    break;
                case "W140N40.DEM":
                    x1 = twidth * 1;
                    y1 = theight * 1;
                    break;
                case "W100N40.DEM":
                    x1 = twidth * 2;
                    y1 = theight * 1;
                    break;
                case "W060N40.DEM":
                    x1 = twidth * 3;
                    y1 = theight * 1;
                    break;
                case "W020N40.DEM":
                    x1 = twidth * 4;
                    y1 = theight * 1;
                    break;
                case "E020N40.DEM":
                    x1 = twidth * 5;
                    y1 = theight * 1;
                    break;
                case "E020S40.DEM":
                    x1 = twidth * 5;
                    y1 = theight * 1;
                    break;
                case "E060S40.DEM":
                    x1 = twidth * 6;
                    y1 = theight * 1;
                    break;
                case "E100S40.DEM":
                    x1 = twidth * 7;
                    y1 = theight * 1;
                    break;
                case "E140S40.DEM":
                    x1 = twidth * 8;
                    y1 = theight * 1;
                    break;

                // bottom row
                case "W180N1.DEM":
                    x1 = twidth * 0;
                    y1 = theight * 2;
                    break;
                case "W140N10.DEM":
                    x1 = twidth * 1;
                    y1 = theight * 2;
                    break;
                case "W100N10.DEM":
                    x1 = twidth * 2;
                    y1 = theight * 2;
                    break;
                case "W060N10.DEM":
                    x1 = twidth * 3;
                    y1 = theight * 2;
                    break;
                case "W020S10.DEM":
                    x1 = twidth * 4;
                    y1 = theight * 2;
                    break;
                case "E020S10.DEM":
                    x1 = twidth * 5;
                    y1 = theight * 2;
                    break;
                case "E060S10.DEM":
                    x1 = twidth * 6;
                    y1 = theight * 2;
                    break;
                case "E100S10.DEM":
                    x1 = twidth * 7;
                    y1 = theight * 2;
                    break;
                case "E140S10.DEM":
                    x1 = twidth * 8;
                    y1 = theight * 2;
                    break;
                default:
                    // invalid - pick coordinates off the bitmap
                    x1 = twidth * 10;
                    y1 = theight * 10;
            }
            canvas.drawRect(x1, y1, x1+twidth, y1+theight, paint); // test

        }
        buff += "\n";
        txtView.setText(buff);
        layout.addView(txtView);
        layout.addView(imgView);
        setContentView(layout);
        return true;
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        //byte[] buffer = new byte[1024];
        byte[] buffer = new byte[16384];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void DoSomething()
    {
        //CopyAssets();
        Toast.makeText(this, "Something starts", Toast.LENGTH_SHORT).show();

        //String DemFilename = "W020S10"; // Stellenbiosch // todo: need to finesse this
        String DemFilename = "E100S10"; // Serpentine // todo: need to finesse this

        try {
            // read from "assets"
            InputStream inp = this.getAssets().open("terrain/" + DemFilename + ".DEM");
            DataInputStream demFile = new DataInputStream(inp);

            // write from local directory "/data/ ...
            File storage = Environment.getExternalStorageDirectory();
            //File file = new File(storage + "/data/player.efis.pfd/terrain/" + DemFilename + ".DEM");

            File dir = new File(storage + "/data/player.efis.pfd/terrain");
            dir.mkdirs();

            File file = new File(storage + "/data/player.efis.pfd/terrain/" + DemFilename + ".DEM");

            FileOutputStream outp = new FileOutputStream(file);
            //DataInputStream demFile = new DataInputStream(inp);

            copyFile(inp, outp);

            outp.flush();
            outp.close();
            inp.close();

        }
        catch (IOException e) {
            Toast.makeText(this, "DEM copy error: " + DemFilename, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        Toast.makeText(this, "Something is done", Toast.LENGTH_SHORT).show();

    }


}
