package player.airspace;

import java.util.*;


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class RasterTerrain;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class AirspaceIntersectionVisitor;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class AirspacePredicate;


/*

///
 // Container for airspaces using kd-tree representation internally for
 // fast geospatial lookups.
 ///
public class Airspaces extends AirspacesInterface implements java.io.Closeable
{
  private AtmosphericPressure qnh = new AtmosphericPressure();
  private AirspaceActivity activity_mask = new AirspaceActivity();

  private final boolean owns_children;

//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
  private AirspaceTree airspace_tree = new AirspaceTree();
  private TaskProjection task_projection = new TaskProjection();

  private LinkedList<AbstractAirspace > tmp_as = new LinkedList<AbstractAirspace >();

   // This attribute keeps track of changes to this project.  It is
   // used by the renderer cache.
  private Serial serial = new Serial();

   // Constructor.
   // Note this class can't safely be copied (yet)
   //
   // If m_owner, this instance will be responsible for deleting objects
   // on destruction.
   //
   // @return empty Airspaces class.
   //
  public Airspaces()
  {
	  this(true);
  }
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
//ORIGINAL LINE: Airspaces(boolean _owns_children=true) :qnh(AtmosphericPressure::Zero()), owns_children(_owns_children)
  public Airspaces(boolean _owns_children)
  {
	  this.qnh = new AtmosphericPressure(AtmosphericPressure.Zero());
	  this.owns_children = _owns_children;
  }

//C++ TO JAVA CONVERTER TODO TASK: Java has no equivalent to ' = delete':
//ORIGINAL LINE: Airspaces(const Airspaces &) = delete;
//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  Airspaces(Airspaces NamelessParameter);

  ///
   // Destructor.
   // This also destroys Airspace objects contained in the tree or temporary buffer
   ///
  public final void close()
  {
	Clear();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const Serial &GetSerial() const
  public final Serial GetSerial()
  {
	return serial;
  }

  ///
   // Add airspace to the internal airspace tree.
   // The airspace is not copied; ownership is transferred to this class if
   // m_owner is true
   //
   // @param asp New airspace to be added.
   ///
  public final void Add(AbstractAirspace airspace)
  {
	if (airspace == null)
	{
	  // nothing to add
	  return;
	}

	// reset QNH to zero so set_pressure_levels will be triggered next update
	// this allows for airspaces to be add at any time
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: qnh = AtmosphericPressure::Zero();
	qnh.copyFrom(AtmosphericPressure.Zero());

	// reset day to all so set_activity will be triggered next update
	// this allows for airspaces to be add at any time
	activity_mask.SetAll();

	if (owns_children)
	{
	  if (IsEmpty())
	  {
		task_projection.Reset(airspace.GetReferenceLocation());
	  }

	  task_projection.Scan(airspace.GetReferenceLocation());
	}

	tmp_as.addLast(airspace);
  }

  ///
   // Re-organise the internal airspace tree after inserting/deleting.
   // Should be called after inserting/deleting airspaces prior to performing
   // any searches, but can be done once after a batch insert/delete.
   ///
  public final void Optimise()
  {
	if (IsEmpty())
	{
	  // avoid assertion failure in uninitialised task_projection
	  return;
	}

	if (!owns_children || task_projection.Update())
	{
	  // dont update task_projection if not owner!

	  // task projection changed, so need to push items back onto stack
	  // to re-build airspace envelopes

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	  for (auto i : QueryAll())
	  {
		tmp_as.addLast(i.GetAirspace());
	  }

	  airspace_tree.clear();
	}

	for (AbstractAirspace i : tmp_as)
	{
	  Airspace as = new Airspace(i, task_projection);
	  airspace_tree.insert(as);
	}

	tmp_as.clear();

	serial.increment();
  }

  ///
   // Clear the airspace store, deleting airspace objects if m_owner is true
   ///
  public final void Clear()
  {
	// delete temporaries in case they were added without an optimise() call
	while (!tmp_as.isEmpty())
	{
	  if (owns_children)
	  {
		AbstractAirspace aa = tmp_as.getFirst();
		if (aa != null)
		{
			aa.close();
		}
	  }
	  tmp_as.removeFirst();
	}

	// delete items in the tree
	if (owns_children)
	{
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	  for (auto i : QueryAll())
	  {
		Airspace a = i;
		a.Destroy();
	  }
	}

	// then delete the tree
	airspace_tree.clear();
  }

  ///
   // Size of airspace (in tree, not in temporary store) ---
   // must call optimise() before this for it to be accurate.
   //
   // @return Number of airspaces in tree
   ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: uint GetSize() const
  public final int GetSize()
  {
	return airspace_tree.size();
  }

  ///
   // Whether airspace store is empty
   //
   // @return True if no airspace stored
   ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean IsEmpty() const
  public final boolean IsEmpty()
  {
	return airspace_tree.empty() && tmp_as.isEmpty();
  }

  ///
   // Set terrain altitude for all AGL-referenced airspace altitudes
   //
   // @param terrain Terrain model for lookup
   ///
//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  void SetGroundLevels(RasterTerrain terrain);

  ///
   // Set QNH pressure for all FL-referenced airspace altitudes.
   // Doesn't do anything if QNH is unchanged
   //
   // @param press Atmospheric pressure model and QNH
   ///
  public final void SetFlightLevels(AtmosphericPressure press)
  {
	if ((int)press.GetHectoPascal() != (int)qnh.GetHectoPascal())
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: qnh = press;
	  qnh.copyFrom(press);

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	  for (auto v : QueryAll())
	  {
		v.SetFlightLevel(press);
	  }
	}
  }

  ///
   // Set activity based on day mask
   //
   // @param days Mask of activity (a particular or range of days matching this day)
   ///
  public final void SetActivity(AirspaceActivity mask)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: if (!mask.equals(activity_mask))
	if (!mask.equals(new AirspaceActivity(activity_mask)))
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: activity_mask = mask;
	  activity_mask.copyFrom(mask);

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	  for (auto v : QueryAll())
	  {
		v.SetActivity(mask);
	  }
	}
  }

//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure const_iterator_range QueryAll() const
  public final gcc_pure const_iterator_range QueryAll()
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	auto predicate = boost::geometry.index.satisfies((Airspace UnnamedParameter1) ->
	{
		return true;
	});
	return
	{
		airspace_tree.qbegin(predicate), airspace_tree.qend()
	};
  }

  ///
   // Query airspaces within range of location.
   //
   // @param loc location of origin of search
   // @param range distance in meters of search radius
   ///
//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Airspaces::const_iterator_range QueryWithinRange(const GeoPoint &location, double range) const
  public final Airspaces.const_iterator_range QueryWithinRange(GeoPoint location, double range)
  {
	if (IsEmpty())
	{
	  // nothing to do
	  return
	  {
		  airspace_tree.qend(), airspace_tree.qend()
	  };
	}

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: const FlatBoundingBox box = task_projection.ProjectSquare(location, range);
	final FlatBoundingBox box = task_projection.ProjectSquare(new GeoPoint(location), range);
	return
	{
		airspace_tree.qbegin(bgi.intersects(box)), airspace_tree.qend()
	};
  }

  ///
   // Query airspaces intersecting the vector (bounding box check
   // only).  The result is in no specific order.
   ///
//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Airspaces::const_iterator_range QueryIntersecting(const GeoPoint &a, const GeoPoint &b) const
  public final Airspaces.const_iterator_range QueryIntersecting(GeoPoint a, GeoPoint b)
  {
	if (IsEmpty())
	{
	  // nothing to do
	  return
	  {
		  airspace_tree.qend(), airspace_tree.qend()
	  };
	}

	// TODO: use StaticArray instead of std::vector
	boost::geometry.model.linestring<FlatGeoPoint> line = new boost::geometry.model.linestring<FlatGeoPoint>();
	line.push_back(task_projection.ProjectInteger(a));
	line.push_back(task_projection.ProjectInteger(b));

	return
	{
		airspace_tree.qbegin(bgi.intersects(line)), airspace_tree.qend()
	};
  }

  ///
   // Call visitor class on airspaces intersected by vector.
   // Note that the visitor is not instantiated separately for each match
   //
   // @param loc location of origin of search
   // @param end end of line along with to search for intersections
   // @param include_inside visit airspaces if the vector is completely
   // inside (i.e. no intersection with airspace outline)
   // @param visitor visitor class to call on airspaces intersected by line
   ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: void VisitIntersecting(const GeoPoint &loc, const GeoPoint &end, boolean include_inside, AirspaceIntersectionVisitor &visitor) const
  public final void VisitIntersecting(GeoPoint loc, GeoPoint end, boolean include_inside, AirspaceIntersectionVisitor visitor)
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : QueryIntersecting(loc, end))
	{
	  if (visitor.SetIntersections(i.Intersects(loc, end, task_projection)))
	  {
		visitor.Visit(i.GetAirspace());
	  }
	}

	if (include_inside)
	{
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	  for (auto i : QueryInside(loc))
	  {
		if (i.IsInside(end))
		{
		  // the vector is completely inside the airspace, and thus does
		  //   not intersect with airspace's outline: on caller's request,
		  //   report an intersection
		  AirspaceIntersectionVector v = new AirspaceIntersectionVector();
		  v.ensureCapacity(1);
		  v.emplace_back(loc, end);
		  visitor.SetIntersections(std::move(v));
		  visitor.Visit(i.GetAirspace());
		}
	  }
	}
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: void VisitIntersecting(const GeoPoint &location, const GeoPoint &end, AirspaceIntersectionVisitor &visitor) const
  public final void VisitIntersecting(GeoPoint location, GeoPoint end, AirspaceIntersectionVisitor visitor)
  {
	VisitIntersecting(location, end, false, visitor);
  }

  ///
   // Query airspaces this location is inside.
   //
   // @param loc location of origin of search
   ///
//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Airspaces::const_iterator_range QueryInside(const GeoPoint &loc) const
  public final Airspaces.const_iterator_range QueryInside(GeoPoint loc)
  {
	if (IsEmpty())
	{
	  // nothing to do
	  return
	  {
		  airspace_tree.qend(), airspace_tree.qend()
	  };
	}

	final FlatGeoPoint flat_location = task_projection.ProjectInteger(loc);
	FlatBoundingBox box = new FlatBoundingBox(flat_location, flat_location);

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	final auto _begin = airspace_tree.qbegin(bgi.intersects(box) && bgi.satisfies((Airspace as) ->
	{
							   return as.IsInside(loc);
	}));

	return
	{
		_begin, airspace_tree.qend()
	};
  }

  ///
   // Query airspaces the aircraft is inside (taking altitude into
   // account).
   //
   // @param loc location of origin of search
   ///
//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Airspaces::const_iterator_range QueryInside(const AircraftState &aircraft) const
  public final Airspaces.const_iterator_range QueryInside(AircraftState aircraft)
  {
	if (IsEmpty())
	{
	  // nothing to do
	  return
	  {
		  airspace_tree.qend(), airspace_tree.qend()
	  };
	}

	final FlatGeoPoint flat_location = task_projection.ProjectInteger(aircraft.location);
	FlatBoundingBox box = new FlatBoundingBox(flat_location, flat_location);

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	final auto _begin = airspace_tree.qbegin(bgi.intersects(box) && bgi.satisfies((Airspace as) ->
	{
							   return as.IsInside(aircraft);
	}));

	return
	{
		_begin, airspace_tree.qend()
	};
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const FlatProjection &GetProjection() const
  public final FlatProjection GetProjection()
  {
	return task_projection;
  }

  ///
   // Empty clearance polygons of all airspaces in this database
   ///
  public final void ClearClearances()
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto v : QueryAll())
	{
	  v.ClearClearance();
	}
  }

  ///
   // Copy/delete objects in this database based on query of master
   //
   // @param master Airspaces object to copy from
   // @param location location of aircraft, from which to search
   // @param range distance in meters of search radius
   // @param condition condition to be applied to matches
   //
   // @return True on change
   ///
  public final boolean SynchroniseInRange(Airspaces master, GeoPoint location, double range, AirspacePredicate condition)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: qnh = master.qnh;
	qnh.copyFrom(master.qnh);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: activity_mask = master.activity_mask;
	activity_mask.copyFrom(master.activity_mask);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: task_projection = master.task_projection;
	task_projection.copyFrom(master.task_projection);

  //C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
	AirspaceVector contents_master = new AirspaceVector();
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : master.QueryWithinRange(location, range))
	{
	  if (condition(i.GetAirspace()))
	  {
		contents_master.push_back(i);
	  }
	}

	if (GlobalMembers.CompareAirspaceVectors(contents_master, AsVector()))
	{
	  return false;
	}

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : QueryAll())
	{
	  i.ClearClearance();
	}
	airspace_tree.clear();

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : contents_master)
	{
	  airspace_tree.insert(i);
	}

	serial.increment();

	return true;
  }

//C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: inline AirspacesInterface::AirspaceVector AsVector() const
  private AirspacesInterface.AirspaceVector AsVector()
  {
  //C++ TO JAVA CONVERTER TODO TASK: The following line could not be converted:
	AirspaceVector v = new AirspaceVector();
	v.reserve(airspace_tree.size());

//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : QueryAll())
	{
	  v.push_back(i);
	}

	return v;
  }
}
//C++ TO JAVA CONVERTER TODO TASK: There is no Java equivalent to C++ namespace aliases:
//namespace bgi = boost::geometry::index;

*/