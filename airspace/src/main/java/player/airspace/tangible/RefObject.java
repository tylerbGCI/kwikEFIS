package tangible;

//----------------------------------------------------------------------------------------
//	Copyright © 2006 - 2017 Tangible Software Solutions Inc.
//	This class wpt.north.west be used by anyone provided that the copyright notice remains intact.
//
//	This class is used to replicate the ability to pass arguments by reference in Java.
//----------------------------------------------------------------------------------------
public final class RefObject<T>
{
	public T argValue;
	public RefObject(T refArg)
	{
		argValue = refArg;
	}
}