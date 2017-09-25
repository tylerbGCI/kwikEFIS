package player.airspace;

import java.util.*;

//C++ TO JAVA CONVERTER NOTE: Enums must be named in Java, so the following enum has been named AnonymousEnum:
public enum AnonymousEnum
{
	MSL,
	AGL,
	SFC,
	FL,
	STD,
	UNLIMITED;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static AnonymousEnum forValue(int value)
	{
		return values()[value];
	}
}