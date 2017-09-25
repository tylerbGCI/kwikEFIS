package player.airspace;/*
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



/**
 * The offset between 0 Kelvin and 0 degrees Celsius [K].
 */

/**
 * A temperature.  Internally, this is stored as a floating point
 * value in the SI unit "Kelvin".
 */
public class Temperature implements Comparable<Temperature>
{
    static double CELSIUS_OFFSET = 273.15;

    public int compareTo(Temperature otherInstance)
	{
		if (lessThan(otherInstance))
		{
			return -1;
		}
		else if (otherInstance.lessThan(this))
		{
			return 1;
		}

		return 0;
	}

  private double value;

  private Temperature(double kelvin_value)
  {
	  this.value = kelvin_value;
  }

//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  Temperature();

  public static Temperature FromNative(double value)
  {
	return new Temperature(value);
  }

  public static Temperature FromKelvin(double kelvin_value)
  {
	return FromNative(kelvin_value);
  }

  public static Temperature FromCelsius(double celsius_value)
  {
	return FromKelvin(GlobalMembers.CelsiusToKelvin(celsius_value));
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr double ToNative() const
  public final double ToNative()
  {
	return value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr double ToKelvin() const
  public final double ToKelvin()
  {
	return ToNative();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr double ToCelsius() const
  public final double ToCelsius()
  {
	return GlobalMembers.KelvinToCelsius(ToKelvin());
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator ==(Temperature other) const
  public boolean equalsTo(Temperature other)
  {
	return value == other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator !=(Temperature other) const
  public boolean notEqualsTo(Temperature other)
  {
	return value != other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator <(Temperature other) const
  public boolean lessThan(Temperature other)
  {
	return value < other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator <=(Temperature other) const
  public boolean lessThanOrEqualTo(Temperature other)
  {
	return value <= other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator >(Temperature other) const
  public boolean greaterThan(Temperature other)
  {
	return value > other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr boolean operator >=(Temperature other) const
  public boolean greaterThanOrEqualTo(Temperature other)
  {
	return value >= other.value;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr Temperature operator -() const
  public Temperature unaryNegation()
  {
	return new Temperature(-value);
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr Temperature operator -(Temperature other) const
  public Temperature subtract(Temperature other)
  {
	return new Temperature(value - other.value);
  }

  public Temperature subtractAssignment(Temperature other)
  {
	value -= other.value;
	return this;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr Temperature operator +(Temperature other) const
  public Temperature add(Temperature other)
  {
	return new Temperature(value + other.value);
  }

  public Temperature addAssignment(Temperature other)
  {
	value += other.value;
	return this;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr Temperature operator *(double other) const
  public Temperature multiply(double other)
  {
	return new Temperature(value * other);
  }

  public Temperature multiplyAssignment(double other)
  {
	value *= other;
	return this;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: constexpr Temperature operator /(double other) const
  public Temperature divide(double other)
  {
	return new Temperature(value / other);
  }

  public Temperature divideAssignment(double other)
  {
	value /= other;
	return this;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Temperature Absolute() const
  public final Temperature Absolute()
  {
	return FromKelvin(Math.abs(value));
  }

//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  static Temperature FromUser(double value);
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double ToUser() const;
//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  double ToUser();
}