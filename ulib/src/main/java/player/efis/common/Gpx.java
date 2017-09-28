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

package player.efis.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;

public class Gpx
{
	private Context context;
	public String region = "gpx.south.east";
	public static ArrayList<Apt> aptList = null;
	
	/*
	// Replace with individual methods for instantiate and loading
	
	public Gpx(Context context) 
	{
		XmlPullParserFactory pullParserFactory; 
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			    //InputStream in_s = context.getAssets().open("airport.gpx.xml");
			    InputStream in_s = context.getAssets().open(region + "/airport.gpx.xml");
		        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in_s, null);
	            parseXML(parser);
		} 
        catch (XmlPullParserException e) {
			e.printStackTrace();
		} 
        catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	
	public Gpx(Context context) 
	{
		this.context = context;
	  aptList = new ArrayList();
	}
	
	
	public void loadDatabase(String database)
	{
		region = database;
		aptList.clear();
		
		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			InputStream in_s = context.getAssets().open(region + "/airport.gpx.xml");
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in_s, null);
			parseXML(parser);
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		int eventType = parser.getEventType();
		Apt currentWpt = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String txt = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
                // aptList = new ArrayList();
				// To help avoid the ConcurrentModificationException
				aptList.clear();
				break;

			case XmlPullParser.START_TAG:
				txt = parser.getName();
				if (txt.equals("wpt")) {
					currentWpt = new Apt();
					if (parser.getAttributeCount() == 2) {
						String sLat = parser.getAttributeValue(0);
						String sLon = parser.getAttributeValue(1);
						currentWpt.lat = Float.valueOf(parser.getAttributeValue(0));
						currentWpt.lon = Float.valueOf(parser.getAttributeValue(1));
					}
				}
				else if (currentWpt != null) {
					if (txt.equals("name")) {
						currentWpt.name = parser.nextText();
					}
					else if (txt.equals("cmt")) {
						currentWpt.cmt = parser.nextText();
					}
				}
				break;

			case XmlPullParser.END_TAG:
				txt = parser.getName();
				// Only add non null wpt's that contain exactly 4 upper-case letters
				if (txt.equalsIgnoreCase("wpt") && currentWpt != null && currentWpt.name.length() == 4 && currentWpt.name.matches("[A-Z]+")) {
					aptList.add(currentWpt);
				}
			}
			eventType = parser.next();
		}
		// printProducts(aptList); // only used for debugging
	}
	
	public static ArrayList<Apt> getAptSelect(float lat, float lon, int range, int nr) 
	{
		//ArrayList<Apt> nearestAptList = null;
		ArrayList<Apt> nearestAptList = new ArrayList();

		Iterator <Apt> it = aptList.iterator(); 
		while (it.hasNext())
		{
			Apt currProduct  = it.next();
			
			// add code to determine  the <nr> apts in range 
			double deltaLat = lat - currProduct.lat;
			double deltaLon = lon - currProduct.lon;
			//double d =  Math.hypot(deltaLon, deltaLat);  // in degree, 1 deg = 60 nm 
			double d =  Math.sqrt(deltaLon*deltaLon + deltaLat*deltaLat);  // faster then hypot, see www 
			
			if (d < range) {
				nearestAptList.add(currProduct);
			}
		}
		return nearestAptList;
	}

	private void printProducts(ArrayList<Apt> list)
	{
		String content = "";
		Iterator <Apt> it = list.iterator(); 
		while (it.hasNext())  
		{
			Apt currProduct  = it.next();
			content = content + "\nName :" +  currProduct.name + "\n";
			content = content + "Cmt :" +  currProduct.cmt + "\n";
			//content = content + "Color :" +  currProduct.wpt + "n";
			System.out.println(content); 
		}
		//Log.v("b2", "b2 - " + content);
		//TextView display = (TextView)findViewById(R.id.info);
		//display.setText(content);
	}
}

