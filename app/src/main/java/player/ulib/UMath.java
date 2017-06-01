package player.ulib;

public class UMath {

    //-------------------------------------------------------------------------
    // determine whether two numbers are "approximately equal" by seeing if they
    // are within a certain "tolerance percentage," with `tolerancePercentage` given
    // as a percentage (such as 10.0 meaning "10%").
    //
    // @param tolerancePercentage 1 = 1%, 2.5 = 2.5%, etc.
    //
    public static boolean approximatelyEqual(float desiredValue, float actualValue, float tolerancePercentage) 
    {
        float diff = Math.abs(desiredValue - actualValue);         //  1000 - 950  = 50
        float tolerance = tolerancePercentage/100 * desiredValue;  //  20/100*1000 = 200
        return diff < tolerance;                                   //  50<200      = true
    }

    

}