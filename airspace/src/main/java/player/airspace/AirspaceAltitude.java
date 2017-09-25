/* Copyright_License {

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

/* Copyright_License {

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



/* Copyright_License {

  XCSoar Glide Computer - http://www.xcsoar.org/
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
 * This enum specifies the reference for altitude specifications.
 */
enum AltitudeReference
{
     // No reference set, the altitude value is invalid.
    NONE (-1),

     // Altitude is measured above ground level (AGL).
     // Note: the integer value is important because it is stored in the profile.
    AGL (0),

     // Altitude is measured above mean sea level (MSL).
     // Note: the integer value is important because it is stored in the profile.
    MSL (1),

     // Altitude is measured above the standard pressure (1013.25 hPa).
     // This is used for flight levels (FL).
    STD (2);

    private final int i;
    AltitudeReference(int i)
    {
        this.i = i;
    }
}


/**
 * Structure for altitude-related state data
 */
class AltitudeState
{
    ///##############
    //   Altitude
    ///##############

    /** Altitude used for navigation (GPS or Baro) */
    public double altitude;

    /** Fraction of working band height */
    public double working_band_fraction;

    /** Altitude over terrain */
    public double altitude_agl;

//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  void Reset();
}





//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class AtmosphericPressure;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//struct AltitudeState;

/** Structure to hold airspace altitude boundary data */
public class AirspaceAltitude
{
  /** Altitude AMSL (m) resolved from type */
  public double altitude;
  /** Flight level (100ft) for FL-referenced boundary */
  public double flight_level;
  /** Height above terrain (m) for ground-referenced boundary */
  public double altitude_above_terrain;

  /** Type of airspace boundary */
  public AltitudeReference reference;

  /**
   * Constructor.  Initialises to zero.
   *
   * @return Initialised blank object
   */
  public AirspaceAltitude()
  {
	  this.altitude = 0;
	  this.flight_level = 0;
	  this.altitude_above_terrain = 0;
	  this.reference = AltitudeReference.NONE;
  }

  /**
   * Get Altitude AMSL (m) resolved from type.
   * For AGL types, this assumes the terrain height
   * is the terrain height at the aircraft.
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double GetAltitude(const AltitudeState &state) const
  public final double GetAltitude(AltitudeState state)
  {
	// TODO: check if state.altitude_agl is valid
	return reference == AltitudeReference.AGL ? altitude_above_terrain + (state.altitude - state.altitude_agl) : altitude;
  }

  /** Is this altitude reference at or above the aircraft state? */
  public final boolean IsAbove(AltitudeState state)
  {
	  return IsAbove(state, 0);
  }
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean IsAbove(const AltitudeState &state, const double margin = 0) const
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
  public final boolean IsAbove(AltitudeState state, double margin)
  {
	return GetAltitude(state) >= state.altitude - margin;
  }

  /** Is this altitude reference at or below the aircraft state? */
  public final boolean IsBelow(AltitudeState state)
  {
	  return IsBelow(state, 0);
  }
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean IsBelow(const AltitudeState &state, const double margin = 0) const
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
  public final boolean IsBelow(AltitudeState state, double margin)
  {
	return GetAltitude(state) <= state.altitude + margin || IsTerrain();
	  /* special case: GND is always "below" the aircraft, even if the
	     aircraft's AGL altitude turns out to be negative due to terrain
	     file inaccuracies */
  }

  /**
   * Test whether airspace boundary is the terrain
   *
   * @return True if this altitude limit is the terrain
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean IsTerrain() const
  public final boolean IsTerrain()
  {
	return altitude_above_terrain <= 0 && reference == AltitudeReference.AGL;
  }

  /**
   * Set height of terrain for AGL-referenced airspace;
   * this sets Altitude and must be called before AGL-referenced
   * airspace is considered initialised.
   *
   * @param alt Height of terrain at airspace center
   */
  public final void SetGroundLevel(double alt)
  {
	if (reference == AltitudeReference.AGL)
	{
	  altitude = altitude_above_terrain + alt;
	}
  }

  /**
   * Is it necessary to call SetGroundLevel() for this AirspaceAltitude?
   */
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean NeedGroundLevel() const
  public final boolean NeedGroundLevel()
  {
	return reference == AltitudeReference.AGL;
  }

  /**
   * Set atmospheric pressure (QNH) for flight-level based
   * airspace.  This sets Altitude and must be called before FL-referenced
   * airspace is considered initialised.
   *
   * @param press Atmospheric pressure model (to obtain QNH)
   */
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
  private final double fl_feet_to_m = 30.48;
  public final void SetFlightLevel(AtmosphericPressure press)
  {
//C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
//	static constexpr double fl_feet_to_m(30.48);
	if (reference == AltitudeReference.STD)
	{
	  altitude = press.PressureAltitudeToQNHAltitude(flight_level * fl_feet_to_m);
	}
  }

  public static boolean SortHighest(AirspaceAltitude a, AirspaceAltitude b)
  {
	return a.altitude > b.altitude;
  }
}