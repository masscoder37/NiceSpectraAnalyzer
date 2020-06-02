/**
 * Created by Michael Stadlmeier on 6/20/2017.
 */
public class DeviationCalc {
    //gives ppm deviation of inserted calculated mass as a range mass_lower and mass_upper in form of an array of doubles

    public static double[] ppmRangeCalc(double ppmDev, double massIn){
        double[] massRange = new double[2];
        massRange[0] = massIn - (ppmDev*massIn)/(Math.pow(10,6));
        massRange[1] = massIn + (ppmDev*massIn)/((Math.pow(10,6)));
        return massRange;
    }


    public static double ppmDeviationCalc (double massInReal, double massInMeasured){
        return Math.abs(massInMeasured-massInReal)/massInReal*(Math.pow(10,6));
    }

    //compares to masses and returns if they are within a specified ppm difference
    public static  boolean ppmMatch (double massInReal, double massInMeasured, double ppmDevAllowed){
        double ppmCalculated = ppmDeviationCalc(massInReal, massInMeasured);
        return (ppmCalculated <= ppmDevAllowed);
    }

    public static  boolean isotopeMatch (double massInReal, double massInMeasured, double daAllowed){
        double massDiff = Math.abs(massInReal - massInMeasured);
        return (massDiff<=daAllowed);
    }

    public static boolean isotopeMatchPPM (double massInReal, double massInMeasured, double maxPPMDev){
        //first, calculate Da difference from max PPMDev
        double allowedDa = massInReal / (Math.pow(10,6)) * maxPPMDev;
        double massDiff = Math.abs(massInReal-massInMeasured);
        return (massDiff <= allowedDa);
    }

    //make a new function which takes into account the charge states of the peak0
    //allows unassigned charge state
    public static boolean massAndChargeMatch (double massInTheoretical, int chargeStateTheoretical, Peak peakIn, double ppmDevAllowed){
        boolean isMatch = false;
        double ppmDev = ppmDeviationCalc(massInTheoretical, peakIn.getMass());
        if (ppmDev <= ppmDevAllowed){
            if (chargeStateTheoretical == peakIn.getCharge() || peakIn.getCharge() == 0)
                isMatch = true;
        }
        return isMatch;
    }


    //TODO: redo this method to take the MySpectrum and look at the real feature instead of checking M0 through M5?
    public static boolean isPartofIsotopeEnvelope(double m0MassIn, double massToCheckIn, int chargeStateIn, double ppmDevAllowed){
        boolean isMatch = false;
        for(int i = 0; i < 5; i++){
            double shiftedMass = m0MassIn + (i*AtomicMasses.getNEUTRON()/chargeStateIn);
            if(ppmMatch(shiftedMass, massToCheckIn, ppmDevAllowed)){
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }







}