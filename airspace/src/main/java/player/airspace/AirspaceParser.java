package player.airspace;

import java.util.*;


public class AirspaceParser
{
    /*
    private Airspaces airspaces;

    public AirspaceParser(Airspaces _airspaces)
    {
        this.airspaces = new Airspaces(_airspaces);
    }

    public final boolean Parse(TLineReader reader, OperationEnvironment operation)
    {
        boolean ignore = false;

        // Create and init ProgressDialog
        operation.SetProgressRange(1024);

        final int file_size = reader.GetSize();

        TempAirspaceType temp_area = new TempAirspaceType();
        AirspaceFileType filetype = AirspaceFileType.UNKNOWN;

        String line;

        // Iterate through the lines
        for (int line_num = 1; (line = reader.ReadLine()) != null; line_num++) {
            StripRight(line);

            // Skip empty line
            if (GlobalMembers.StringIsEmpty(line)) {
                continue;
            }

            if (filetype == AirspaceFileType.UNKNOWN) {
                filetype = GlobalMembers.DetectFileType(line);
                if (filetype == AirspaceFileType.UNKNOWN) {
                    continue;
                }
            }

            // Parse the line
            if (filetype == AirspaceFileType.OPENAIR) {
                tangible.RefObject<String> tempRef_line = new tangible.RefObject<String>(line);
                if (!GlobalMembers.ParseLine(airspaces, tempRef_line, temp_area) && !GlobalMembers.ShowParseWarning(line_num, line, operation)) {
                    line = tempRef_line.argValue;
                    return false;
                }
                else {
                    line = tempRef_line.argValue;
                }
            }

            if (filetype == AirspaceFileType.TNP) {
                StringParser<Byte> input = new StringParser<Byte>(line);
                tangible.RefObject<Boolean> tempRef_ignore = new tangible.RefObject<Boolean>(ignore);
                if (!GlobalMembers.ParseLineTNP(airspaces, input, temp_area, tempRef_ignore) && !GlobalMembers.ShowParseWarning(line_num, line, operation)) {
                    ignore = tempRef_ignore.argValue;
                    return false;
                }
                else {
                    ignore = tempRef_ignore.argValue;
                }
            }

            // Update the ProgressDialog
            if ((line_num & 0xff) == 0) {
                operation.SetProgressPosition(reader.Tell() * 1024 / file_size);
            }
        }

        if (filetype == AirspaceFileType.UNKNOWN) {
            operation.SetErrorMessage(_("Unknown airspace filetype"));
            return false;
        }

        // Process final area (if any)
        temp_area.AddPolygon(airspaces);

        return true;
    }
    // */
}