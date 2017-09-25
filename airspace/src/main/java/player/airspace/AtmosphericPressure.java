/*
Copyright_License {

  XCSoar Glide Computer - http: //www.xcsoar.org/
  Copyright (C) 2000-2016 The XCSoar Project
  A detailed list of copyright holders can be found in the file "AUTHORS".

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
}
*/

/*
Copyright_License {

  XCSoar Glide Computer - http: //www.xcsoar.org/
  Copyright (C) 2000-2016 The XCSoar Project
  A detailed list of copyright holders can be found in the file "AUTHORS".

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
}
*/

package player.airspace;


/**
 * ICAO Standard Atmosphere calculations (valid in Troposphere, alt<11000m)
 *
 */
public class AtmosphericPressure
{
  /** Pressure in hPa */
  private double value;

  /**
   * @param value the pressure in hPa
   */
  private AtmosphericPressure(double _value)
  {
	  this.value = _value;
  }

  /**
   * Non-initialising constructor.
   */
//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  AtmosphericPressure();

  /**
   * Returns an object representing zero pressure.  This value doesn't
   * make a lot of practical sense (unless you're an astronaut), but
   * it may be used internally to mark an instance of this class
   * "invalid" (IsPlausible() returns false).
   */
  public static AtmosphericPressure Zero()
  {
	return new AtmosphericPressure(0);
  }

  /**
   * Returns an object representing the standard pressure (1013.25
   * hPa).
   */
  public static AtmosphericPressure Standard()
  {
	return new AtmosphericPressure(1013.25);
  }

  public static AtmosphericPressure Pascal(double value)
  {
	return new AtmosphericPressure(value / 100);
  }

  public static AtmosphericPressure HectoPascal(double value)
  {
	return new AtmosphericPressure(value);
  }

  /**
   * Is this a plausible value?
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean IsPlausible() const
  public final boolean IsPlausible()
  {
	return value > 100 && value < 1200;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double GetPascal() const
  public final double GetPascal()
  {
	return GetHectoPascal() * 100;
  }

  /**
   * Access QNH value
   *
   * @return QNH value (hPa)
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double GetHectoPascal() const
  public final double GetHectoPascal()
  {
	return value;
  }

  /**
   * Calculates the current QNH by comparing a pressure value to a
   * known altitude of a certain location
   *
   * @param pressure Current pressure
   * @param alt_known Altitude of a known location (m)
   */
  public static AtmosphericPressure FindQNHFromPressure(AtmosphericPressure pressure, double alt_known)
  {
	return pressure.QNHAltitudeToStaticPressure(-alt_known);
  }

  /**
   * Converts altitude with QNH=1013.25 reference to QNH adjusted altitude
   * @param alt 1013.25-based altitude (m)
   * @return QNH-based altitude (m)
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double PressureAltitudeToQNHAltitude(const double alt) const
  public final double PressureAltitudeToQNHAltitude(double alt)
  {
	return StaticPressureToQNHAltitude(PressureAltitudeToStaticPressure(alt));
  }

  /**
   * Converts QNH adjusted altitude to pressure altitude (with QNH=1013.25 as reference)
   * @param alt QNH-based altitude(m)
   * @return pressure altitude (m)
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double QNHAltitudeToPressureAltitude(const double alt) const
  public final double QNHAltitudeToPressureAltitude(double alt)
  {
	return StaticPressureToPressureAltitude(QNHAltitudeToStaticPressure(alt));
  }

  /**
   * Converts a pressure value to the corresponding QNH-based altitude
   *
   * See http://wahiduddin.net/calc/density_altitude.htm
   *
   * Example:
   * QNH=1014, ps=100203 => alt = 100
   * @see QNHAltitudeToStaticPressure
   * @param ps Air pressure
   * @return Altitude over QNH-based zero (m)
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double StaticPressureToQNHAltitude(const AtmosphericPressure ps) const
  public final double StaticPressureToQNHAltitude(AtmosphericPressure ps)
  {
	return (Math.pow(GetHectoPascal(), DefineConstants.k1) - Math.pow(ps.GetHectoPascal(), DefineConstants.k1)) * 1.0 / 8.417286e-5;
  }

  /**
   * Converts a QNH-based altitude to the corresponding pressure
   *
   * See http://wahiduddin.net/calc/density_altitude.htm
   *
   * Example:
   * alt= 100, QNH=1014 => ps = 100203 Pa
   * @see StaticPressureToAltitude
   * @param alt Altitude over QNH-based zero (m)
   * @return Air pressure at given altitude
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AtmosphericPressure QNHAltitudeToStaticPressure(const double alt) const
  public final AtmosphericPressure QNHAltitudeToStaticPressure(double alt)
  {
	return HectoPascal(Math.pow((Math.pow(GetHectoPascal(), DefineConstants.k1) - DefineConstants.k2 * alt), DefineConstants.inv_k1));
  }

  /**
   * Converts a pressure value to pressure altitude (with QNH=1013.25 as reference)
   * @param ps Air pressure
   * @return pressure altitude (m)
   */
  public static double StaticPressureToPressureAltitude(AtmosphericPressure ps)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: return Standard().StaticPressureToQNHAltitude(ps);
	//return Standard().StaticPressureToQNHAltitude(new AtmosphericPressure(ps));
      return 0;
  }

  /**
   * Converts a 1013.25 hPa based altitude to the corresponding pressure
   *
   * @see StaticPressureToAltitude
   * @param alt Altitude over 1013.25 hPa based zero(m)
   * @return Air pressure at given altitude
   */
  public static AtmosphericPressure PressureAltitudeToStaticPressure(double alt)
  {
	return Standard().QNHAltitudeToStaticPressure(alt);
  }
}
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
//ORIGINAL LINE: #define inv_k2 1.0 / 8.417286e-5
