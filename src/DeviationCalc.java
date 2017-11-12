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







}