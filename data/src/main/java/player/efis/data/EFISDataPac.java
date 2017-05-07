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

import android.content.res.AssetManager;
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


    private boolean listAssetFiles(String path)
    {

        String [] list;
        try {
            list = getAssets().list(path);
        }
        catch (IOException e) {
            return false;
        }


        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        ImageView imgView = new ImageView(this);
        imgView.setImageResource(R.drawable.gtopo30_index);

        TextView txtView = new TextView(this);
        String buff = "\nKwik EFIS Terrain data\n\n";
        for (int i = 0; i < list.length; i++) {
            buff += list[i] + "\t";
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
