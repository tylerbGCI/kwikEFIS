package player.airspace;

import java.util.*;

/*
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

/*
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


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Airspaces;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class TLineReader;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class OperationEnvironment;

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