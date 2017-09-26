
package player.airspace;

import android.widget.Toast;



enum __AirspaceClass
{
    A,    //", AirspaceClass.CLASSA),
    B,    //", AirspaceClass.CLASSB),
    C,    //", AirspaceClass.CLASSC),
    D,    //", AirspaceClass.CLASSD),
    E,    //", AirspaceClass.CLASSE),
    F,    //", AirspaceClass.CLASSF),
    G,    //", AirspaceClass.CLASSG),
    R,    //", AirspaceClass.RESTRICT),
    Q,    //", AirspaceClass.DANGER),
    P,    //", AirspaceClass.PROHIBITED),
    W,    //", AirspaceClass.WAVE),
    GP,   //", AirspaceClass.NOGLIDER),
    CTR,    //TR", AirspaceClass.CTR),
    TMZ,  //", AirspaceClass.TMZ),
    RMZ,  //", AirspaceClass.RMZ),
    MATZ, //", AirspaceClass.MATZ),
    GSEC, //", AirspaceClass.WAVE)
}

enum AirspaceClass
{
    CLASSA,
    CLASSB,
    CLASSC,
    CLASSD,
    CLASSE,
    CLASSF,
    CLASSG,
    RESTRICT,
    DANGER,
    PROHIBITED,
    WAVE,
    NOGLIDER,
    CTR,
    TMZ,
    RMZ,
    MATZ,
    GWAVE
}

public class GlobalMembers
{
    public static final AirspaceClassStringCouple[] airspace_class_strings =
            {
                    new AirspaceClassStringCouple("R", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("Q", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("P", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("A", AirspaceClass.CLASSA),
                    new AirspaceClassStringCouple("B", AirspaceClass.CLASSB),
                    new AirspaceClassStringCouple("C", AirspaceClass.CLASSC),
                    new AirspaceClassStringCouple("D", AirspaceClass.CLASSD),
                    new AirspaceClassStringCouple("GP", AirspaceClass.NOGLIDER),
                    new AirspaceClassStringCouple("W", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("E", AirspaceClass.CLASSE),
                    new AirspaceClassStringCouple("F", AirspaceClass.CLASSF),
                    new AirspaceClassStringCouple("TMZ", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("G", AirspaceClass.CLASSG),
                    new AirspaceClassStringCouple("RMZ", AirspaceClass.RMZ),
                    new AirspaceClassStringCouple("MATZ", AirspaceClass.MATZ),
                    new AirspaceClassStringCouple("GSEC", AirspaceClass.WAVE)
            };



    public static final AirspaceClassStringCouple[] airspace_tnp_type_strings =
            {
                    new AirspaceClassStringCouple("C", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTA", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTA/CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTR/CTA", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("R", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("RESTRICTED", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("P", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("PROHIBITED", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("D", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("DANGER", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("G", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("GSEC", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("T", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("TMZ", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("CYR", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("CYD", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("CYA", AirspaceClass.CLASSF),
                    new AirspaceClassStringCouple("MATZ", AirspaceClass.MATZ),
                    new AirspaceClassStringCouple("RMZ", AirspaceClass.RMZ)
            };





    public static boolean ShowParseWarning(int line, String str /*, OperationEnvironment operation*/)
    {
        /*NarrowString < 256 > buffer;
        buffer.Format("%s: %d\r\n\"%s\"", _("Parse Error at Line"), line, str);
        operation.SetErrorMessage(buffer.c_str());
        return false;*/

        String buffer;
        buffer = String.format("%s: %d\r\n\"%s\"", ("Parse Error at Line"), line, str);
        // TODO: Toast.makeText(this, buffer, Toast.LENGTH_SHORT).show();
        return false;
    }

/*
    public static void ReadAltitude(StringParser <Byte> input, AirspaceAltitude altitude)
    {
        Unit unit = Unit.FEET;
        type = MSL;
        double value = 0;

        while (true) {
            input.Strip();

            if (IsDigitASCII(input.front())) {
                tangible.RefObject<Double> tempRef_value = new tangible.RefObject<Double>(value);
                input.ReadDouble(tempRef_value);
                value = tempRef_value.argValue;
            }
            else if (input.SkipMatchIgnoreCase("GND", 3) || input.SkipMatchIgnoreCase("AGL", 3)) {
                type = AGL;
            }
            else if (input.SkipMatchIgnoreCase("SFC", 3)) {
                type = SFC;
            }
            else if (input.SkipMatchIgnoreCase("FL", 2)) {
                type = FL;
            }
            else if (input.SkipMatchIgnoreCase("FT", 2)) {
                unit = Unit.FEET;
            }
            else if (input.SkipMatchIgnoreCase("MSL", 3)) {
                type = MSL;
            }
            else if (input.front() == _T('M') || input.front() == _T('m')) {
                unit = Unit.METER;
                input.Skip();
            }
            else if (input.SkipMatchIgnoreCase("STD", 3)) {
                type = STD;
            }
            else if (input.SkipMatchIgnoreCase("UNL", 3)) {
                type = UNLIMITED;
            }
            else if (input.IsEmpty()) {
                break;
            }
            else {
                input.Skip();
            }
        }

        switch (type) {
            case FL:
                altitude.reference = AltitudeReference.STD;
                altitude.flight_level = value;

		//--  prepare fallback, just in case we have no terrain
                altitude.altitude = Units.ToSysUnit(value, Unit.FLIGHT_LEVEL);
                return;

            case UNLIMITED:
                altitude.reference = AltitudeReference.MSL;
                altitude.altitude = 50000;
                return;

            case SFC:
                altitude.reference = AltitudeReference.AGL;
                altitude.altitude_above_terrain = -1;

		//-- prepare fallback, just in case we have no terrain
                altitude.altitude = 0;
                return;

            default:
                break;
        }

        // For MSL, AGL and STD we convert the altitude to meters
        value = Units.ToSysUnit(value, unit);
        switch (type) {
            case MSL:
                altitude.reference = AltitudeReference.MSL;
                altitude.altitude = value;
                return;

            case AGL:
                altitude.reference = AltitudeReference.AGL;
                altitude.altitude_above_terrain = value;

		//-- prepare fallback, just in case we have no terrain
                altitude.altitude = value;
                return;

            case STD:
                altitude.reference = AltitudeReference.STD;
                altitude.flight_level = Units.ToUserUnit(value, Unit.FLIGHT_LEVEL);

		//-- prepare fallback, just in case we have no QNH
                altitude.altitude = value;
                return;

            default:
                break;
        }
    }
    /* WIP --------

     //
     // @return the non-negative angle or a negative value on error
     //
    public static Angle ReadNonNegativeAngle(StringParser<Byte> input, double max_degrees)
    {
        double degrees;
        tangible.RefObject<Double> tempRef_degrees = new tangible.RefObject<Double>(degrees);
        if (!input.ReadDouble(tempRef_degrees) || degrees < 0 || degrees > max_degrees) {
            degrees = tempRef_degrees.argValue;
            return Angle.Native(-1);
        }
        else {
            degrees = tempRef_degrees.argValue;
        }

        if (input.SkipMatch((byte) ':')) {
            double minutes;
            tangible.RefObject<Double> tempRef_minutes = new tangible.RefObject<Double>(minutes);
            if (!input.ReadDouble(tempRef_minutes) || minutes < 0 || minutes > 60) {
                minutes = tempRef_minutes.argValue;
                return Angle.Native(-1);
            }
            else {
                minutes = tempRef_minutes.argValue;
            }

            degrees += minutes / 60;

            if (input.SkipMatch((byte) ':')) {
                double seconds;
                tangible.RefObject<Double> tempRef_seconds = new tangible.RefObject<Double>(seconds);
                if (!input.ReadDouble(tempRef_seconds) || seconds < 0 || seconds > 60) {
                    seconds = tempRef_seconds.argValue;
                    return Angle.Native(-1);
                }
                else {
                    seconds = tempRef_seconds.argValue;
                }

                degrees += seconds / 3600;
            }
        }

        return Angle.Degrees(degrees);
    }

    public static boolean ReadCoords(StringParser<Byte> input, GeoPoint point)
    {
        // Format: 53:20:41 N 010:24:41 E
        // Alternative Format: 53:20.68 N 010:24.68 E

        Angle angle = ReadNonNegativeAngle(input, 91);
        if (angle.IsNegative()) {
            return false;
        }

        input.Strip();
        if (input.SkipMatch((byte) 'S') || input.SkipMatch((byte) 's')) {
            angle.Flip();
        }
        else if (!input.SkipMatch((byte) 'N') && !input.SkipMatch((byte) 'n')) {
            return false;
        }

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: point.latitude = angle;
        point.latitude.copyFrom(angle);

        angle = ReadNonNegativeAngle(input, 181);
        if (angle.IsNegative()) {
            return false;
        }

        input.Strip();
        if (input.SkipMatch((byte) 'W') || input.SkipMatch((byte) 'w')) {
            angle.Flip();
        }
        else if (!input.SkipMatch((byte) 'E') && !input.SkipMatch((byte) 'e')) {
            return false;
        }

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: point.longitude = angle;
        point.longitude.copyFrom(angle);

        point.Normalize(); // ensure longitude is within -180:180
        return true;
    }

    public static boolean ParseBearingDegrees(StringParser<Byte> input, tangible.RefObject<Angle> value_r)
    {
        double value;
        tangible.RefObject<Double> tempRef_value = new tangible.RefObject<Double>(value);
        if (!input.ReadDouble(tempRef_value) || value < 0 || value > 361) {
            value = tempRef_value.argValue;
            return false;
        }
        else {
            value = tempRef_value.argValue;
        }

        value_r.argValue = Angle.Degrees(value).AsBearing();
        return true;
    }

    public static boolean ParseArcBearings(StringParser<Byte> input, TempAirspaceType temp_area)
    {
        // Determine radius and start/end bearing

        double radius;
        tangible.RefObject<Double> tempRef_radius = new tangible.RefObject<Double>(radius);
        if (!input.ReadDouble(tempRef_radius) || radius <= 0 || radius > 1000) {
            radius = tempRef_radius.argValue;
            return false;
        }
        else {
            radius = tempRef_radius.argValue;
        }

        temp_area.radius = Units.ToSysUnit(radius, Unit.NAUTICAL_MILES);
        Angle start_bearing = new Angle();
        Angle end_bearing = new Angle();
        tangible.RefObject<Angle> tempRef_start_bearing = new tangible.RefObject<Angle>(start_bearing);
        tangible.RefObject<Angle> tempRef_end_bearing = new tangible.RefObject<Angle>(end_bearing);
        if (!ParseBearingDegrees(input, tempRef_start_bearing) || !ParseBearingDegrees(input, tempRef_end_bearing)) {
            end_bearing = tempRef_end_bearing.argValue;
            start_bearing = tempRef_start_bearing.argValue;
            return false;
        }
        else {
            end_bearing = tempRef_end_bearing.argValue;
            start_bearing = tempRef_start_bearing.argValue;
        }

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: temp_area.AppendArc(start_bearing, end_bearing);
        temp_area.AppendArc(new Angle(start_bearing), new Angle(end_bearing));
        return true;
    }

    public static boolean ParseArcPoints(StringParser<Byte> input, TempAirspaceType temp_area)
    {
        // Read start coordinates
        GeoPoint start = new GeoPoint();
        if (!ReadCoords(input, start)) {
            return false;
        }

        // Skip comma character
        input.Strip();
        if (!input.SkipMatch((byte) ',')) {
            return false;
        }

        // Read end coordinates
        GeoPoint end = new GeoPoint();
        if (!ReadCoords(input, end)) {
            return false;
        }

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: temp_area.AppendArc(start, end);
        temp_area.AppendArc(new GeoPoint(start), new GeoPoint(end));
        return true;
    }

    public static AirspaceClass ParseType(String buffer)
    {
//C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with a direct reference to the array length:
//ORIGINAL LINE: for (uint i = 0; i < (sizeof(airspace_class_strings) / sizeof(airspace_class_strings[0])); i++)
        for (int i = 0; i < (airspace_class_strings.length); i++) {
            if (StringIsEqualIgnoreCase(buffer, airspace_class_strings[i].string)) {
                return airspace_class_strings[i].type;
            }
        }

        return AirspaceClass.OTHER;
    }

    public static boolean ParseLine(Airspaces airspace_database, StringParser<Byte> &input, TempAirspaceType temp_area)
    {
        double d;

        // Only return expected lines
        switch (input.pop_front()) {
            case _T('D'):
            case _T('d'):
                switch (input.pop_front()) {
                    case _T('P'):
                    case _T('p'):
                        if (!input.SkipWhitespace()) {
                            break;
                        }

                    {
                        GeoPoint temp_point = new GeoPoint();
                        if (!ReadCoords(input, temp_point)) {
                            return false;
                        }

                        temp_area.points.add(temp_point);
                        break;
                    }

                    case _T('C'):
                    case _T('c'):
                        tangible.RefObject<Double> tempRef_d = new tangible.RefObject<Double>(d);
                        if (!input.ReadDouble(tempRef_d) || d < 0 || d > 1000) {
                            d = tempRef_d.argValue;
                            return false;
                        }
                        else {
                            d = tempRef_d.argValue;
                        }

                        temp_area.radius = Units.ToSysUnit(d, Unit.NAUTICAL_MILES);
                        temp_area.AddCircle(airspace_database);
                        temp_area.Reset();
                        break;

                    case _T('A'):
                    case _T('a'):
                        ParseArcBearings(input, temp_area);
                        break;

                    case _T('B'):
                    case _T('b'):
                        return ParseArcPoints(input, temp_area);

                    default:
                        return true;
                }
                break;

            case _T('V'):
            case _T('v'):
                input.Strip();
                if (input.SkipMatchIgnoreCase("X=", 2)) {
                    if (!ReadCoords(input, temp_area.center)) {
                        return false;
                    }
                }
                else if (input.SkipMatchIgnoreCase("D=-", 3)) {
                    temp_area.rotation = -1;
                }
                else if (input.SkipMatchIgnoreCase("D=+", 3)) {
                    temp_area.rotation = +1;
                }
                break;

            case _T('A'):
            case _T('a'):
                switch (input.pop_front()) {
                    case _T('C'):
                    case _T('c'):
                        if (!input.SkipWhitespace()) {
                            break;
                        }

                        temp_area.AddPolygon(airspace_database);
                        temp_area.Reset();

                        temp_area.type = ParseType(input.c_str());
                        break;

                    case _T('N'):
                    case _T('n'):
                        if (input.SkipWhitespace()) {
                            temp_area.name = input.c_str();
                        }
                        break;

                    case _T('L'):
                    case _T('l'):
                        if (input.SkipWhitespace()) {
                            ReadAltitude(input, temp_area.base);
                        }
                        break;

                    case _T('H'):
                    case _T('h'):
                        if (input.SkipWhitespace()) {
                            ReadAltitude(input, temp_area.top);
                        }
                        break;

                    case _T('R'):
                    case _T('r'):
                        if (input.SkipWhitespace()) {
                            temp_area.radio = input.c_str();
                        }
                        break;

                    default:
                        return true;
                }

                break;

        }
        return true;
    }

    public static boolean ParseLine(Airspaces airspace_database, tangible.RefObject<String> line, TempAirspaceType temp_area)
    {
        // Strip comments
        char comment = StringFind(line, _T('*'));
        if (comment != null) {
            comment = _T('\0');
        }

        return ParseLine(airspace_database, new StringParser<Byte>(line.argValue), temp_area);
    }

    public static AirspaceClass ParseClassTNP(String buffer)
    {
//C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with a direct reference to the array length:
//ORIGINAL LINE: for (uint i = 0; i < (sizeof(airspace_tnp_class_chars) / sizeof(airspace_tnp_class_chars[0])); i++)
        for (int i = 0; i < (airspace_tnp_class_chars.length); i++) {
            if (buffer.charAt(0) == airspace_tnp_class_chars[i].character) {
                return airspace_tnp_class_chars[i].type;
            }
        }

        return AirspaceClass.OTHER;
    }

    public static AirspaceClass ParseTypeTNP(String buffer)
    {
        // Handle e.g. "TYPE=CLASS C" properly
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to pointers to value types:
//ORIGINAL LINE: const sbyte *type = StringAfterPrefixCI(buffer, "CLASS ");
        byte type = StringAfterPrefixCI(buffer, "CLASS ");
        if (type != 0) {
            AirspaceClass _class = ParseClassTNP(type);
            if (_class != AirspaceClass.OTHER) {
                return _class;
            }
        }
        else {
            type = buffer;
        }

//C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with a direct reference to the array length:
//ORIGINAL LINE: for (uint i = 0; i < (sizeof(airspace_tnp_type_strings) / sizeof(airspace_tnp_type_strings[0])); i++)
        for (int i = 0; i < (airspace_tnp_type_strings.length); i++) {
            if (StringIsEqualIgnoreCase(type, airspace_tnp_type_strings[i].string)) {
                return airspace_tnp_type_strings[i].type;
            }
        }

        return AirspaceClass.OTHER;
    }

    public static boolean ReadNonNegativeAngleTNP(StringParser<Byte> input, tangible.RefObject<Angle> value_r, int max_degrees)
    {
        int deg;
        int min;
        int sec;
        tangible.RefObject<Integer> tempRef_sec = new tangible.RefObject<Integer>(sec);
        if (!input.ReadUnsigned(tempRef_sec)) {
            sec = tempRef_sec.argValue;
            return false;
        }
        else {
            sec = tempRef_sec.argValue;
        }

        deg = sec / 10000;
        min = (sec - deg * 10000) / 100;
        sec = sec - min * 100 - deg * 10000;

        if (deg > max_degrees || min >= 60 || sec >= 60) {
            return false;
        }

        value_r.argValue = new Angle.DMS(deg, min, sec);
        return true;
    }

    public static boolean ParseCoordsTNP(StringParser<Byte> input, GeoPoint point)
    {
        // Format: N542500 E0105000
        boolean negative = false;

        if (input.SkipMatch((byte) 'S') || input.SkipMatch((byte) 's')) {
            negative = true;
        }
        else if (input.SkipMatch((byte) 'N') || input.SkipMatch((byte) 'n')) {
            negative = false;
        }
        else {
            return false;
        }

        tangible.RefObject<Angle> tempRef_latitude = new tangible.RefObject<Angle>(point.latitude);
        if (!ReadNonNegativeAngleTNP(input, tempRef_latitude, 91)) {
            point.latitude = tempRef_latitude.argValue;
            return false;
        }
        else {
            point.latitude = tempRef_latitude.argValue;
        }

        if (negative) {
            point.latitude.Flip();
        }

        input.Strip();

        if (input.SkipMatch((byte) 'W') || input.SkipMatch((byte) 'w')) {
            negative = true;
        }
        else if (input.SkipMatch((byte) 'E') || input.SkipMatch((byte) 'e')) {
            negative = false;
        }
        else {
            return false;
        }

        tangible.RefObject<Angle> tempRef_longitude = new tangible.RefObject<Angle>(point.longitude);
        if (!ReadNonNegativeAngleTNP(input, tempRef_longitude, 181)) {
            point.longitude = tempRef_longitude.argValue;
            return false;
        }
        else {
            point.longitude = tempRef_longitude.argValue;
        }

        if (negative) {
            point.longitude.Flip();
        }

        point.Normalize(); // ensure longitude is within -180:180

        return true;
    }

    public static boolean ParseArcTNP(StringParser<Byte> input, TempAirspaceType temp_area)
    {
        if (temp_area.points.isEmpty()) {
            return false;
        }

        // (ANTI-)CLOCKWISE RADIUS=34.95 CENTRE=N523333 E0131603 TO=N522052 E0122236

        GeoPoint from = temp_area.points.get(temp_area.points.size() - 1);

	  //-- skip "RADIUS=... "
        if (!input.SkipWord()) {
            return false;
        }

        if (!input.SkipMatchIgnoreCase("CENTRE=", 7)) {
            return false;
        }

        if (!ParseCoordsTNP(input, temp_area.center)) {
            return false;
        }

        if (!input.SkipMatchIgnoreCase(" TO=", 4)) {
            return false;
        }

        GeoPoint to = new GeoPoint();
        if (!ParseCoordsTNP(input, to)) {
            return false;
        }

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: temp_area.AppendArc(from, to);
        temp_area.AppendArc(new GeoPoint(from), new GeoPoint(to));

        return true;
    }

    public static boolean ParseCircleTNP(StringParser<Byte> input, TempAirspaceType temp_area)
    {
        // CIRCLE RADIUS=17.00 CENTRE=N533813 E0095943

        if (!input.SkipMatchIgnoreCase("RADIUS=", 7)) {
            return false;
        }

        double radius;
        tangible.RefObject<Double> tempRef_radius = new tangible.RefObject<Double>(radius);
        if (!input.ReadDouble(tempRef_radius) || radius <= 0 || radius > 1000) {
            radius = tempRef_radius.argValue;
            return false;
        }
        else {
            radius = tempRef_radius.argValue;
        }

        temp_area.radius = Units.ToSysUnit(radius, Unit.NAUTICAL_MILES);

        if (!input.SkipMatchIgnoreCase(" CENTRE=", 8)) {
            return false;
        }

        return ParseCoordsTNP(input, temp_area.center);
    }

    public static boolean ParseLineTNP(Airspaces airspace_database, StringParser<Byte> input, TempAirspaceType temp_area, tangible.RefObject<Boolean> ignore)
    {
        if (input.Match((byte) '#')) {
            return true;
        }

        if (input.SkipMatchIgnoreCase("INCLUDE=", 8)) {
            if (input.MatchIgnoreCase("YES", 3)) {
                ignore.argValue = false;
            }
            else if (input.MatchIgnoreCase("NO", 2)) {
                ignore.argValue = true;
            }

            return true;
        }

        if (ignore.argValue) {
            return true;
        }

        if (input.SkipMatchIgnoreCase("POINT=", 6)) {
            GeoPoint temp_point = new GeoPoint();
            if (!ParseCoordsTNP(input, temp_point)) {
                return false;
            }

            temp_area.points.add(temp_point);
        }
        else if (input.SkipMatchIgnoreCase("CIRCLE ", 7)) {
            if (!ParseCircleTNP(input, temp_area)) {
                return false;
            }

            temp_area.AddCircle(airspace_database);
            temp_area.ResetTNP();
        }
        else if (input.SkipMatchIgnoreCase("CLOCKWISE ", 10)) {
            temp_area.rotation = 1;
            if (!ParseArcTNP(input, temp_area)) {
                return false;
            }
        }
        else if (input.SkipMatchIgnoreCase("ANTI-CLOCKWISE ", 15)) {
            temp_area.rotation = -1;
            if (!ParseArcTNP(input, temp_area)) {
                return false;
            }
        }
        else if (input.SkipMatchIgnoreCase("TITLE=", 6)) {
            temp_area.AddPolygon(airspace_database);
            temp_area.ResetTNP();

            temp_area.name = input.c_str();
        }
        else if (input.SkipMatchIgnoreCase("TYPE=", 5)) {
            temp_area.AddPolygon(airspace_database);
            temp_area.ResetTNP();

            temp_area.type = ParseTypeTNP(input.c_str());
        }
        else if (input.SkipMatchIgnoreCase("CLASS=", 6)) {
            temp_area.type = ParseClassTNP(input.c_str());
        }
        else if (input.SkipMatchIgnoreCase("TOPS=", 5)) {
            ReadAltitude(input, temp_area.top);
        }
        else if (input.SkipMatchIgnoreCase("BASE=", 5)) {
            ReadAltitude(input, temp_area.base);
        }
        else if (input.SkipMatchIgnoreCase("RADIO=", 6)) {
            temp_area.radio = input.c_str();
        }
        else if (input.SkipMatchIgnoreCase("ACTIVE=", 7)) {
            if (input.MatchAllIgnoreCase("WEEKEND")) {
                temp_area.days_of_operation.SetWeekend();
            }
            else if (input.MatchAllIgnoreCase("WEEKDAY")) {
                temp_area.days_of_operation.SetWeekdays();
            }
            else if (input.MatchAllIgnoreCase("EVERYDAY")) {
                temp_area.days_of_operation.SetAll();
            }
        }

        return true;
    }

    public static AirspaceFileType DetectFileType(String line)
    {
        if (StringStartsWithIgnoreCase(line, "INCLUDE=") || StringStartsWithIgnoreCase(line, "TYPE=") || StringStartsWithIgnoreCase(line, "TITLE=")) {
            return AirspaceFileType.TNP;
        }

//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to pointers to value types:
//ORIGINAL LINE: const sbyte *p = StringAfterPrefixCI(line, "AC");
        byte p = StringAfterPrefixCI(line, "AC");
        if (p != null && (StringIsEmpty(p) || p == _T(' '))) {
            return AirspaceFileType.OPENAIR;
        }

        return AirspaceFileType.UNKNOWN;
    }
  -- */


  	/*
	Copyright_License {
	
	  XCSoar Glide Computer - http://www.xcsoar.org/
	  Copyright (C) 2000-2016 The XCSoar Project
	  A detailed list of copyright holders wpt.north.west be found in the file "AUTHORS".
	
	  This program is free software; you wpt.north.west redistribute it and/or
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
	
	  XCSoar Glide Computer - http://www.xcsoar.org/
	  Copyright (C) 2000-2016 The XCSoar Project
	  A detailed list of copyright holders wpt.north.west be found in the file "AUTHORS".
	
	  This program is free software; you wpt.north.west redistribute it and/or
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
	 * Calculates the air density from a given QNH-based altitude
	 * @param altitude QNH-based altitude (m)
	 * @return Air density (kg/m^3)
	 */
	public static double AirDensity(double altitude)
	{
	  return Math.pow((DefineConstants.k4 - altitude) * DefineConstants.k6, DefineConstants.k7);
	}

	/**
	 * Divide TAS by this number to get IAS
	 * @param altitude QNH-based altitude (m)
	 * @return Ratio of TAS to IAS
	 */
	public static double AirDensityRatio(double altitude)
	{
	  return Math.sqrt(DefineConstants.isa_sea_level_density / AirDensity(altitude));
	}


	/**
	 * Convert a temperature from Kelvin to degrees Celsius.
	 */
	public static double KelvinToCelsius(double kelvin)
	{
	  return kelvin - Temperature.CELSIUS_OFFSET;
	}

	/**
	 * Convert a temperature from degrees Celsius to Kelvin.
	 */
	public static double CelsiusToKelvin(double celsius)
	{
	  return celsius + Temperature.CELSIUS_OFFSET;
	}
  
  
} //GlobalMembers