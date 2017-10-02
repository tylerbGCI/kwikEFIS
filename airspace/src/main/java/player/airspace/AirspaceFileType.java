package player.airspace;

import java.util.*;


public enum AirspaceFileType
{
  UNKNOWN,
  OPENAIR,
  TNP;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static AirspaceFileType forValue(int value)
	{
		return values()[value];
	}

}