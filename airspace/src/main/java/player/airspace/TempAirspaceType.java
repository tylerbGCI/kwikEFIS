import java.util.*;

// this wpt.north.west now be called multiple times to load several airspaces.

public class TempAirspaceType
{
    /*


  public TempAirspaceType()
  {
	points.ensureCapacity(256);
	Reset();
  }

  // General
  public tstring name = new tstring();
  public tstring radio = new tstring();
  public AirspaceClass type;
  public AirspaceAltitude base = new AirspaceAltitude();
  public AirspaceAltitude top = new AirspaceAltitude();
  public AirspaceActivity days_of_operation = new AirspaceActivity();

  // Polygon
  public ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();

  // Circle or Arc
  public GeoPoint center = new GeoPoint();
  public double radius;

  // Arc
  public int rotation;

  public final void Reset()
  {
	days_of_operation.SetAll();
	radio = "";
	type = AirspaceClass.OTHER;
	base = top = new AirspaceAltitude();
	points.clear();
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: center.longitude = Angle::Zero();
	center.longitude.copyFrom(Angle.Zero());
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: center.latitude = Angle::Zero();
	center.latitude.copyFrom(Angle.Zero());
	rotation = 1;
	radius = 0;
  }

  public final void ResetTNP()
  {
	// Preserve type, radio and days_of_operation for next airspace blocks
	points.clear();
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: center.longitude = Angle::Zero();
	center.longitude.copyFrom(Angle.Zero());
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: center.latitude = Angle::Zero();
	center.latitude.copyFrom(Angle.Zero());
	rotation = 1;
	radius = 0;
  }

  public final void AddPolygon(Airspaces airspace_database)
  {
	if (points.size() < 3)
	{
	  return;
	}

	AbstractAirspace as = new AirspacePolygon(points);
	as.SetProperties(std::move(name), type, base, top);
	as.SetRadio(radio);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: as->SetDays(days_of_operation);
	as.SetDays(new AirspaceActivity(days_of_operation));
	airspace_database.Add(as);
  }

  public final void AddCircle(Airspaces airspace_database)
  {
	AbstractAirspace as = new AirspaceCircle(center, radius);
	as.SetProperties(std::move(name), type, base, top);
	as.SetRadio(radio);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: as->SetDays(days_of_operation);
	as.SetDays(new AirspaceActivity(days_of_operation));
	airspace_database.Add(as);
  }

  public static int ArcStepWidth(double radius)
  {
	if (radius > 50000)
	{
	  return 1;
	}
	if (radius > 25000)
	{
	  return 2;
	}
	if (radius > 10000)
	{
	  return 3;
	}

	return 5;
  }

  public final void AppendArc(GeoPoint start, GeoPoint end)
  {

	// Determine start bearing and radius
	final GeoVector v = center.DistanceBearing(start);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: Angle start_bearing = v.bearing;
	Angle start_bearing = new Angle(v.bearing);
	final double radius = v.distance;

	// 5 or -5, depending on direction
	final int _step = ArcStepWidth(radius);
	final Angle step = Angle.Degrees(rotation * _step);
	final double threshold = _step * 1.5;

	// Determine end bearing
	Angle end_bearing = center.Bearing(end);

	if (rotation > 0)
	{
	  while (end_bearing.lessThan(start_bearing))
	  {
		end_bearing.addAssignment(Angle.FullCircle());
	  }
	}
	else if (rotation < 0)
	{
	  while (end_bearing.greaterThan(start_bearing))
	  {
		end_bearing.subtractAssignment(Angle.FullCircle());
	  }
	}

	// Add first polygon point
	points.add(start);

	// Add intermediate polygon points
	while (end_bearing.subtract(start_bearing).AbsoluteDegrees() > threshold)
	{
	  start_bearing.addAssignment(step);
	  points.add(FindLatitudeLongitude(center, start_bearing, radius));
	}

	// Add last polygon point
	points.add(end);
  }

  public final void AppendArc(Angle start, Angle end)
  {
	// 5 or -5, depending on direction
	final int _step = ArcStepWidth(radius);
	final Angle step = Angle.Degrees(rotation * _step);
	final double threshold = _step * 1.5;

	if (rotation > 0)
	{
	  while (end.lessThan(start))
	  {
		end.addAssignment(Angle.FullCircle());
	  }
	}
	else if (rotation < 0)
	{
	  while (end.greaterThan(start))
	  {
		end.subtractAssignment(Angle.FullCircle());
	  }
	}

	// Add first polygon point
	points.add(FindLatitudeLongitude(center, start, radius));

	// Add intermediate polygon points
	while (end.subtract(start).AbsoluteDegrees() > threshold)
	{
	  start.addAssignment(step);
	  points.add(FindLatitudeLongitude(center, start, radius));
	}

	// Add last polygon point
	points.add(FindLatitudeLongitude(center, end, radius));
  }


  */
}