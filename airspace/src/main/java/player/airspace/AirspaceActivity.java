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


public class AirspaceActivity
{
/*
    private static class Days
  {
	public int sunday = 1;
	public int monday = 2;
	public int tuesday= 3;
	public int wednesday = 4;
	public int thursday  = 5;
	public int friday    = 6;
	public int saturday   =7;
  }

//C++ TO JAVA CONVERTER TODO TASK: Unions are not supported in Java:
//ORIGINAL LINE: union
//C++ TO JAVA CONVERTER NOTE: Classes must be named in Java, so the following class has been named AnonymousStruct:
  private final static class AnonymousStruct
  {
	public Days days = new Days();
//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: unsigned char value;
	public byte value;
  }
  private AnonymousStruct mask = new AnonymousStruct();

  public AirspaceActivity()
  {
	SetAll();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean equals(const AirspaceActivity _mask) const
  public final boolean equals(AirspaceActivity _mask)
  {
	return mask.value == _mask.mask.value;
  }

  public AirspaceActivity(byte day_of_week)
  {
	// setter from BrokenDate format day
	mask.value = 0;
	switch (day_of_week)
	{
	case 0:
	  mask.days.sunday = true;
	  break;
	case 1:
	  mask.days.monday = true;
	  break;
	case 2:
	  mask.days.tuesday = true;
	  break;
	case 3:
	  mask.days.wednesday = true;
	  break;
	case 4:
	  mask.days.thursday = true;
	  break;
	case 5:
	  mask.days.friday = true;
	  break;
	case 6:
	  mask.days.saturday = true;
	  break;
	default:
	  SetAll();
	  break;
	}
  }

  public final void SetAll()
  {
	mask.value = (byte)0xFF;
  }

  public final void SetWeekdays()
  {
	mask.value = 0;
	mask.days.monday = true;
	mask.days.tuesday = true;
	mask.days.wednesday = true;
	mask.days.thursday = true;
	mask.days.friday = true;
  }

  public final void SetWeekend()
  {
	mask.value = 0;
	mask.days.saturday = true;
	mask.days.sunday = true;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean Matches(AirspaceActivity _mask) const
  public final boolean Matches(AirspaceActivity _mask)
  {
	return mask.value & _mask.mask.value;
  }

  */
}
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to 'static_assert':
//static_assert(sizeof(AirspaceActivity) == 1, "Wrong size");

