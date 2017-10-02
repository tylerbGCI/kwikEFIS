package player.airspace;

/// Abstract base class for airspace regions

public abstract class AbstractAirspace implements java.io.Closeable
{
/*
  public enum Shape
  {
	CIRCLE,
	POLYGON;
  }

  private final Shape shape = null;

  ////Airspace class///
  private AirspaceClass type;

  //protected TriState is_convex;
  protected boolean active;

  ////Base of airspace///
  protected AirspaceAltitude altitude_base = new AirspaceAltitude();

  ////Top of airspace///
  protected AirspaceAltitude altitude_top = new AirspaceAltitude();

  ////Airspace name (identifier)///
  protected String name;

  ////Radio frequency (optional)///
  protected String radio;

  ////Actual border///
  protected SearchPointVector m_border = new SearchPointVector();

  ////Convex clearance border///
  protected SearchPointVector m_clearance = new SearchPointVector();

  protected AirspaceActivity days_of_operation = new AirspaceActivity();

  public AbstractAirspace(Shape _shape)
  {
	  this.shape = new AbstractAirspace.Shape(_shape);
	  this.active = true;
  }
  public void close()
  {
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: Shape GetShape() const
  public final Shape GetShape()
  {
	return shape;
  }

  ////
  //Compute bounding box enclosing the airspace.  Rounds up/down
  //so discretisation ensures bounding box is indeed enclosing.
  //
  //@param projection Projection used for flat-earth representation
  //
  //@return Enclosing bounding box
  ///
  public final FlatBoundingBox GetBoundingBox(FlatProjection projection)
  {
	Project(projection);
	return m_border.CalculateBoundingbox();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: GeoBounds GetGeoBounds() const
  public final GeoBounds GetGeoBounds()
  {
	return m_border.CalculateGeoBounds();
  }

  ///
  //Get arbitrary center or reference point for use in determining
  //overall center location of all airspaces
  //
  //@return Location of reference point
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure virtual const GeoPoint GetReferenceLocation() const = 0;
  public abstract gcc_pure GeoPoint GetReferenceLocation();

  ///
  //Get geometric center of airspace.
  //
  //@return center
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure virtual const GeoPoint GetCenter() const = 0;
  public abstract gcc_pure GeoPoint GetCenter();

  ///
  //Checks whether an observer is inside the airspace (no altitude taken into account)
  //This is slow because it uses geodesic calculations
  //
  //@param loc State about which to test inclusion
  //
  //@return true if observer is inside airspace boundary
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure virtual boolean Inside(const GeoPoint &loc) const = 0;
  public abstract gcc_pure boolean Inside(GeoPoint loc);

  ///
  //Checks whether an observer is inside the airspace altitude range.
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean Inside(const AltitudeState &state) const
  public final boolean Inside(AltitudeState state)
  {
	return altitude_base.IsBelow(state) && altitude_top.IsAbove(state);
  }

  ///
  //Checks whether an observer is inside the airspace (altitude is taken into account)
  //This calls inside(state.location) so wpt.north.west be slow.
  //
  //@param state State about which to test inclusion
  //
  //@return true if aircraft is inside airspace boundaries
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean Inside(const AircraftState &state) const
  public final boolean Inside(AircraftState state)
  {
	return altitude_base.IsBelow(state) && altitude_top.IsAbove(state) && Inside(state.location);
  }

  ///
  //Checks whether a line intersects with the airspace.
  //Can be approximate by using flat-earth representation internally.
  //
  //@param g1 Location of origin of search vector
  //@param end the end of the search vector
  //
  //@return Vector of intersection pairs if the line intersects the airspace
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure virtual AirspaceIntersectionVector Intersects(const GeoPoint &g1, const GeoPoint &end, const FlatProjection &projection) const = 0;
  public abstract gcc_pure AirspaceIntersectionVector Intersects(GeoPoint g1, GeoPoint end, FlatProjection projection);

  ///
  //Find location of closest point on boundary to a reference
  //
  //@param loc Reference location of observer
  //
  //@return Location of closest point of boundary to reference
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure virtual GeoPoint ClosestPoint(const GeoPoint &loc, const FlatProjection &projection) const = 0;
  public abstract gcc_pure GeoPoint ClosestPoint(GeoPoint loc, FlatProjection projection);

  ///
  //Set terrain altitude for AGL-referenced airspace altitudes
  //
  //@param alt Height above MSL of terrain (m) at center
  ///
  public final void SetGroundLevel(double alt)
  {
	altitude_base.SetGroundLevel(alt);
	altitude_top.SetGroundLevel(alt);
  }

  ///
  //Is it necessary to call SetGroundLevel() for this AbstractAirspace?
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean NeedGroundLevel() const
  public final boolean NeedGroundLevel()
  {
	return altitude_base.NeedGroundLevel() || altitude_top.NeedGroundLevel();
  }

  ///
  //Set QNH pressure for FL-referenced airspace altitudes
  //
  //@param press Atmospheric pressure model and QNH
  ///
  public final void SetFlightLevel(AtmosphericPressure press)
  {
	altitude_base.SetFlightLevel(press);
	altitude_top.SetFlightLevel(press);
  }

  ///
  //Set activity based on day mask
  //
  //@param days Mask of activity
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: void SetActivity(const AirspaceActivity mask) const
  public final void SetActivity(AirspaceActivity mask)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: active = days_of_operation.Matches(mask);
	active = days_of_operation.Matches(new AirspaceActivity(mask));
  }

  ///
  //Set fundamental properties of airspace
  //
  //@param _Name Name of airspace
  //@param _Type Type/class
  //@param _base Lower limit
  //@param _top Upper limit
  ///
  public final void SetProperties(String & _name, AirspaceClass _Type, AirspaceAltitude _base, AirspaceAltitude _top)
  {
	name = std::move(_name);
	type = _Type;
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: altitude_base = _base;
	altitude_base.copyFrom(_base);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: altitude_top = _top;
	altitude_top.copyFrom(_top);
  }

  ///
  //Set radio frequency of airspace
  //
  //@param _Radio Radio frequency of airspace
  ///
  public final void SetRadio(String _Radio)
  {
	radio = _Radio;
  }

  ///
  //Set activation setting of the airspace
  //
  //@param _active New activation setting of airspace
  ///
  public final void SetDays(AirspaceActivity mask)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: days_of_operation = mask;
	days_of_operation.copyFrom(mask);
  }

  ///
  //Get type of airspace
  //
  //@return Type/class of airspace
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AirspaceClass GetType() const
  public final AirspaceClass GetType()
  {
	return type;
  }

  ///
  //Test whether base is at terrain level
  //
  //@return True if base is 0 AGL
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean IsBaseTerrain() const
  public final boolean IsBaseTerrain()
  {
	return altitude_base.IsTerrain();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const AirspaceAltitude &GetBase() const
  public final AirspaceAltitude GetBase()
  {
	  return altitude_base;
  }
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const AirspaceAltitude &GetTop() const
  public final AirspaceAltitude GetTop()
  {
	  return altitude_top;
  }

  ///
  //Get base altitude
  //
  //@return Altitude AMSL (m) of base
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double GetBaseAltitude(const AltitudeState &state) const
  public final double GetBaseAltitude(AltitudeState state)
  {
	return altitude_base.GetAltitude(state);
  }

  ///
  //Get top altitude
  //
  //@return Altitude AMSL (m) of top
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double GetTopAltitude(const AltitudeState &state) const
  public final double GetTopAltitude(AltitudeState state)
  {
	return altitude_top.GetAltitude(state);
  }

  ///
  //Find time/distance/height to airspace from an observer given a
  //simplified performance model and the boundary start/end points.  If
  //inside the airspace, this will give the time etc to exit (it cares
  //not about interior/exterior, only minimum time to reach the
  //specified location)
  //
  //@param state Aircraft state
  //@param perf Aircraft performance model
  //@param solution Solution of intercept (set if intercept possible, else untouched)
  //@param loc_start Location of first point on/in airspace to query (if provided)
  //@param loc_end Location of last point on/in airspace to query (if provided)
  //@return True if intercept found
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AirspaceInterceptSolution Intercept(const AircraftState &state, const AirspaceAircraftPerformance &perf, const GeoPoint &loc_start, const GeoPoint &loc_end) const
  public final AirspaceInterceptSolution Intercept(AircraftState state, AirspaceAircraftPerformance perf, GeoPoint loc_start, GeoPoint loc_end)
  {
	final boolean only_vertical = loc_start.equalsTo(loc_end) && loc_start.equalsTo(state.location);

	final double distance_start = only_vertical ? (double)0 : state.location.Distance(loc_start);

	final boolean distance_end = loc_start.equalsTo(loc_end) ? distance_start : (only_vertical ? (double)0 : state.location.Distance(loc_end));

	AirspaceInterceptSolution solution = AirspaceInterceptSolution.Invalid();

	// need to scan at least three sides, top, far, bottom (if not terrain)

	AirspaceInterceptSolution solution_candidate = AirspaceInterceptSolution.Invalid();

	if (!only_vertical)
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution_candidate = InterceptVertical(state, perf, distance_start);
	  solution_candidate.copyFrom(InterceptVertical(state, perf, distance_start));
	  // search near wall
	  if (solution_candidate.IsEarlierThan(solution))
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution = solution_candidate;
		solution.copyFrom(solution_candidate);
	  }

	  if (distance_end != distance_start)
	  {
		// need to search far wall also
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution_candidate = InterceptVertical(state, perf, distance_end);
		solution_candidate.copyFrom(InterceptVertical(state, perf, distance_end));
		if (solution_candidate.IsEarlierThan(solution))
		{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution = solution_candidate;
		  solution.copyFrom(solution_candidate);
		}
	  }
	}

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution_candidate = InterceptHorizontal(state, perf, distance_start, distance_end, false);
	solution_candidate.copyFrom(InterceptHorizontal(state, perf, distance_start, distance_end, false));
	// search top wall
	if (solution_candidate.IsEarlierThan(solution))
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution = solution_candidate;
	  solution.copyFrom(solution_candidate);
	}

	// search bottom wall
	if (!altitude_base.IsTerrain())
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution_candidate = InterceptHorizontal(state, perf, distance_start, distance_end, true);
	  solution_candidate.copyFrom(InterceptHorizontal(state, perf, distance_start, distance_end, true));
	  if (solution_candidate.IsEarlierThan(solution))
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution = solution_candidate;
		solution.copyFrom(solution_candidate);
	  }
	}

	if (solution.IsValid())
	{
	  if (solution.distance == distance_start)
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution.location = loc_start;
		solution.location.copyFrom(loc_start);
	  }
	  else if (solution.distance == distance_end)
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution.location = loc_end;
		solution.location.copyFrom(loc_end);
	  }
	  else if (distance_end > false)
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution.location = state.location.Interpolate(loc_end, solution.distance / distance_end);
		solution.location.copyFrom(state.location.Interpolate(loc_end, solution.distance / distance_end));
	  }
	  else
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution.location = loc_start;
		solution.location.copyFrom(loc_start);
	  }

	  assert solution.distance >= 0;
	}

	return solution;
  }

  ///
  //Find time/distance/height to airspace from an observer given a
  //simplified performance model and the aircraft path vector.  If
  //inside the airspace, this will give the time etc to exit (it cares
  //not about interior/exterior, only minimum time to reach the
  //specified location)
  //
  //@param state Aircraft state
  //@param end end point of aircraft path vector
  //@param perf Aircraft performance model
  //@param solution Solution of intercept (set if intercept possible, else untouched)
  //@return True if intercept found
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AirspaceInterceptSolution Intercept(const AircraftState &state, const GeoPoint &end, const FlatProjection &projection, const AirspaceAircraftPerformance &perf) const
  public final AirspaceInterceptSolution Intercept(AircraftState state, GeoPoint end, FlatProjection projection, AirspaceAircraftPerformance perf)
  {
	AirspaceInterceptSolution solution = AirspaceInterceptSolution.Invalid();
//C++ TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java:
	for (auto i : Intersects(state.location, end, projection))
	{
	  AirspaceInterceptSolution new_solution = Intercept(state, perf, i.first, i.second);
	  if (new_solution.IsEarlierThan(solution))
	  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: solution = new_solution;
		solution.copyFrom(new_solution);
	  }
	}

	return solution;
  }

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
///#if DO_PRINT
//C++ TO JAVA CONVERTER TODO TASK: Java has no concept of a 'friend' function:
//ORIGINAL LINE: friend std::ostream &operator <<(std::ostream &f, const AbstractAirspace &as);
//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//  std::ostream operator <<(std::ostream f, AbstractAirspace as);
///#endif

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure const sbyte//GetName() const
  public final gcc_pure String GetName()
  {
	return name.c_str();
  }

  ///
  //Returns true if the name begins with the specified string.
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean MatchNamePrefix(const sbyte//prefix) const
  public final boolean MatchNamePrefix(String prefix)
  {
	size_t prefix_length = _tcslen(prefix);
	return GlobalMembers.StringIsEqualIgnoreCase(name.c_str(), prefix, prefix_length);
  }

  ///
  //Produce text version of radio frequency.
  //
  //@return Text version of radio frequency
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure const String &GetRadioText() const
  public final gcc_pure String GetRadioText()
  {
	return radio;
  }

  ///
  //Accessor for airspace shape
  //
  //For polygon airspaces, this is the actual boundary,
  //for circle airspaces, this is a simplified shape
  //
  //@return border of airspace
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const SearchPointVector &GetPoints() const
  public final SearchPointVector GetPoints()
  {
	return m_border;
  }

  ///
  //On-demand access of clearance border.  Generated on call,
  //to deallocate, call clear_clearance().  Uses mutable object
  //and const methods to allow visitors to generate them on demand
  //from within a visit method.
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const SearchPointVector& GetClearance(const FlatProjection &projection) const
  public final SearchPointVector GetClearance(FlatProjection projection)
  {

	if (!m_clearance.isEmpty())
	{
	  return m_clearance;
	}

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: m_clearance = m_border;
	m_clearance.copyFrom(m_border);
	if (is_convex != TriState.FALSE)
	{
	  is_convex = m_clearance.PruneInterior() ? TriState.FALSE : TriState.TRUE;
	}

	FlatBoundingBox bb = m_clearance.CalculateBoundingbox();
	FlatGeoPoint center = bb.GetCenter();

	for (SearchPoint i : m_clearance)
	{
	  FlatGeoPoint p = i.GetFlatLocation();
	  FlatRay r = new FlatRay(center, p);
	  int mag = r.Magnitude();
	  int mag_new = mag + DefineConstants.RADIUS;
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: p = r.Parametric((double)mag_new / mag);
	  p.copyFrom(r.Parametric((double)mag_new / mag));
	  i = new SearchPoint(projection.Unproject(p), p);
	}

	return m_clearance;
  }
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: void ClearClearance() const
  public final void ClearClearance()
  {
	m_clearance.clear();
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: gcc_pure boolean IsActive() const
  public final gcc_pure boolean IsActive()
  {
	return active;
  }

  /// Project border///
  protected final void Project(FlatProjection projection)
  {
	m_border.Project(projection);
  }

  ///
  //Find time/distance to specified point on the boundary from an observer
  //given a simplified performance model.  If inside the airspace, this will
  //give the time etc to exit (it cares not about interior/exterior, only minimum
  //time to reach the specified location)
  //
  //@param state Aircraft state
  //@param perf Aircraft performance model
  //@param distance Distance from aircraft to boundary
  //@return Solution of intercept
  ///
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AirspaceInterceptSolution InterceptVertical(const AircraftState &state, const AirspaceAircraftPerformance &perf, double distance) const
  private AirspaceInterceptSolution InterceptVertical(AircraftState state, AirspaceAircraftPerformance perf, double distance)
  {
	AirspaceInterceptSolution solution = new AirspaceInterceptSolution();
	solution.distance = distance;
	tangible.RefObject<Double> tempRef_altitude = new tangible.RefObject<Double>(solution.altitude);
	solution.elapsed_time = perf.SolutionVertical(solution.distance, state.altitude, altitude_base.GetAltitude(state), altitude_top.GetAltitude(state), tempRef_altitude);
	solution.altitude = tempRef_altitude.argValue;
	return solution;
  }

  ///
  //Find time/distance to specified horizontal boundary from an observer
  //given a simplified performance model.  If inside the airspace, this will
  //give the time etc to exit (it cares not about interior/exterior, only minimum
  //time to reach the specified location)
  //
  //@param state Aircraft state
  //@param perf Aircraft performance model
  //@param distance_start Distance from aircraft to start boundary
  //@param distance_end Distance from aircraft to end boundary
  //@param lower If true, examines lower boundary, otherwise upper boundary
  //@return Solution of intercept
  ///
  private AirspaceInterceptSolution InterceptHorizontal(AircraftState state, AirspaceAircraftPerformance perf, double distance_start, double distance_end)
  {
	  return InterceptHorizontal(state, perf, distance_start, distance_end, true);
  }
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: AirspaceInterceptSolution InterceptHorizontal(const AircraftState &state, const AirspaceAircraftPerformance &perf, double distance_start, double distance_end, const boolean lower = true) const
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above:
  private AirspaceInterceptSolution InterceptHorizontal(AircraftState state, AirspaceAircraftPerformance perf, double distance_start, double distance_end, boolean lower)
  {
	if (lower && altitude_base.IsTerrain())
	{
	  // impossible to be lower than terrain
	  return AirspaceInterceptSolution.Invalid();
	}

	AirspaceInterceptSolution solution = new AirspaceInterceptSolution();
	solution.altitude = lower ? altitude_base.GetAltitude(state) : altitude_top.GetAltitude(state);
	tangible.RefObject<Double> tempRef_distance = new tangible.RefObject<Double>(solution.distance);
	solution.elapsed_time = perf.SolutionHorizontal(distance_start, distance_end, state.altitude, solution.altitude, tempRef_distance);
	solution.distance = tempRef_distance.argValue;
	return solution;
  }


  */
}