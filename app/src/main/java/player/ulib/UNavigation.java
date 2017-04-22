package player.ulib;

public class UNavigation {


    //-------------------------------------------------------------------------
    // Calculate the DME distance in nm
    //
    float calcDme(float lat1, float lon1, float lat2, float lon2)
    {
        float deltaLat = lat2 - lat1;
        float deltaLon = lon2 - lon1;

        //d =  364800 * Math.hypot(deltaLon, deltaLat);  // in ft, 1 deg of lat  6080 * 60 = 364,80 note hypot uses convergenge and is very slow.
        float d = (float) (60 * Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat));  // in nm, 1 deg of lat
        return d;

    }

    //-------------------------------------------------------------------------
    // Calculate the Relative Bearing in degrees
    //
    float calcRelBrg(float lat1, float lon1, float lat2, float lon2, float hdg)
    {
        float deltaLat = lat2 - lat1;
        float deltaLon = lon2 - lon1;

        float relBrg = (float) (Math.toDegrees(Math.atan2(deltaLon, deltaLat)) - hdg) % 360;  // the relative bearing to the apt
        if (relBrg > 180) relBrg = relBrg - 360;
        if (relBrg < -180) relBrg = relBrg + 360;
        return relBrg;
    }


    //-------------------------------------------------------------------------
    // Calculate the Absolute Bearing in degrees
    //
    float calcAbsBrg(float lat1, float lon1, float lat2, float lon2)
    {
        float deltaLat = lat2 - lat1;
        float deltaLon = lon2 - lon1;

        float absBrg = (float) (Math.toDegrees(Math.atan2(deltaLon, deltaLat))) % 360;
        while (absBrg < 0) absBrg += 360;
        return absBrg;
    }

    //-------------------------------------------------------------------------
    // Calculate the Distance to the Horizon in m
    // h in meters
    //
    float calcHorizonMetric(float h)  // in m
    {
        return (float) (3570 * Math.sqrt(h)); // in m
    }


    //-------------------------------------------------------------------------
    // Calculate the Distance to the Horizon in nm
    // h in ft
    //
    float calcHorizonNautical(float h) // in ft
    {
        return (float) Math.sqrt(h); // in nm
    }





}