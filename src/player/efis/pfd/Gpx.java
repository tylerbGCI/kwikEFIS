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


package player.efis.pfd;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

class Apt
{
	public String name;
	public String cmt;
	public  float lat;
	public  float lon;
	public  float elev;
}

class Gpx
{
	static ArrayList<Apt> aptList = null;
	
	public Gpx(Context context) 
	{
		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			    InputStream in_s = context.getAssets().open("airport.gpx.xml");
		        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in_s, null);
	            parseXML(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		//ArrayList<Wpt> gpx = null;
        int eventType = parser.getEventType();
        Apt currentWpt = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
        	String txt = null;
        	switch (eventType) {
        	case XmlPullParser.START_DOCUMENT:
        		aptList = new ArrayList();
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
        		} else if (currentWpt != null) {
        			if (txt.equals("name")) {
        				currentWpt.name = parser.nextText(); 
        			} else if (txt.equals("cmt")) {
        				currentWpt.cmt = parser.nextText();
        			} 
        		}
        		break;
        	
        	case XmlPullParser.END_TAG: 
        		txt = parser.getName();
        		if (txt.equalsIgnoreCase("wpt") && currentWpt != null) {
        			aptList.add(currentWpt);
        		} 
        	}
        	eventType = parser.next();
        }
        //printProducts(aptList);
	}
	
	public static ArrayList<Apt> getAptSelect(float lat, float lon, int range, int nr) 
	{
		//ArrayList<Apt> nearestAptList = null;
		ArrayList<Apt> nearestAptList = new ArrayList();

		Iterator <Apt> it = aptList.iterator(); 
		while (it.hasNext())
		{
			Apt currProduct  = it.next();
			
			// TODO
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



/*

<?xml version="1.0" encoding="UTF-8"?>
<products>
	<product>     
		<productname>Jeans</productname>
		<productcolor>red</productcolor>
		<productquantity>5</productquantity>
	</product>
	<product>     
		<productname>Tshirt</productname>
		<productcolor>blue</productcolor>
		<productquantity>3</productquantity>
	</product>
	<product>     
		<productname>shorts</productname>
		<productcolor>green</productcolor>
		<productquantity>4</productquantity>
	</product>
</products>


class Product
{

	public String name;
	public String quantity;
	public String color;

}


public class XMLDemo extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		XmlPullParserFactory pullParserFactory;
		try {
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			    InputStream in_s = getApplicationContext().getAssets().open("apt-b86d7e37-mapsource.gpx.xml");
		        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in_s, null);

	            parseXML(parser);

		} catch (XmlPullParserException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		ArrayList<product> products = null;
        int eventType = parser.getEventType();
        Product currentProduct = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	products = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name == "product"){
                        currentProduct = new Product();
                    } else if (currentProduct != null){
                        if (name == "productname"){
                            currentProduct.name = parser.nextText();
                        } else if (name == "productcolor"){
                        	currentProduct.color = parser.nextText();
                        } else if (name == "productquantity"){
                            currentProduct.quantity= parser.nextText();
                        }  
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("product") && currentProduct != null) {
                    	products.add(currentProduct);
                    } 
            }
            eventType = parser.next();
        }

        printProducts(products);
	}

	private void printProducts(ArrayList<product> products)
	{
		String content = "";
		Iterator</product><product> it = products.iterator();
		while(it.hasNext())
		{
			Product currProduct  = it.next();
			content = content + "nnnProduct :" +  currProduct.name + "n";
			content = content + "Quantity :" +  currProduct.quantity + "n";
			content = content + "Color :" +  currProduct.color + "n";

		}

		TextView display = (TextView)findViewById(R.id.info);
		display.setText(content);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

ArrayList<String> vars = new ArrayList<String(9);
for (int i = 1; i <= 9; i++)
{
    vars.add("hi" + i);
    Toast.makeText(this, vars.get(i), Toast.LENGTH_SHORT).show();
}

*/