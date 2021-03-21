
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 6/21/2017.
 */

//uses a spectrum and a (modified) Peptide to generate a list of matched ions
    //utilizes Deviation Calc to generate the appropriate ppm windows
public class PeakCompare {
    private DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fiveDec = new DecimalFormat("0.00000");

    public static ArrayList<IonMatch> peakCompare(MySpectrum spectrumIn, Peptide peptideIn, double ppmDev) {
        DecimalFormat twoDec = new DecimalFormat("0.00");
        ArrayList<IonMatch> matches = new ArrayList<>();
        ArrayList<Peak> peakList = spectrumIn.getPeakList();
        //check if many multiple charged peaks are contained in the spectrum (>2.5%)
        //if that's the case, invoke addition of fragment ion charge states
        int[] chargeStateDistro = spectrumIn.getChargeStateDistributionNumber();
        for (int i = 0; i<chargeStateDistro.length; i++){
            double percentChargeState = ((double) chargeStateDistro[i])/spectrumIn.getNumberOfPeaks()*100;
            if (percentChargeState > 2.5){
                //charge state 1 is already initialized
                //more than z = +5 is unrealistic
                if (i>1 && i<5)
                peptideIn.createAddFragmentIonChargestate(i);
            }
        }
        ArrayList<FragmentIon> fragmentIonsList = new ArrayList<>();
        fragmentIonsList.addAll(peptideIn.getbIons());
        fragmentIonsList.addAll(peptideIn.getyIons());
        for (FragmentIon fragment : fragmentIonsList){
            double[] massRange = DeviationCalc.ppmRangeCalc(ppmDev, fragment.getMToZ());
            for (Peak peak : peakList){
                if (peak.getMass() >= massRange[0]&& peak.getMass()<= massRange[1]){
                    double deviation = DeviationCalc.ppmDeviationCalc(fragment.getMToZ(), peak.getMass());
                    matches.add(new IonMatch(fragment, peak, deviation));
                    System.out.println("Match! Ion: "+fragment.getCompleteIon()
                            +" "+fragment.getCharge()+"+"
                            +"  Mass found: "+fiveDec.format(peak.getMass()) + " m/z"
                            +"   Deviation: " +twoDec.format(deviation)+" ppm"
                            +"  rel. Int.: "+twoDec.format(peak.getRelIntensity())+"%"+
                    "        is modfied: "+fragment.getModificationStatus());
                }
            }

        }

        return matches;
    }

    public static ArrayList<ReporterMatch> reporterFinder(MySpectrum spectrumIn, String labelName, double ppmDev){
        ArrayList<ReporterMatch> matchedReporters = new ArrayList<>();
        double rep0Mass = 0;
        double rep1Mass = 0;
        if (!labelName.equals("EC")&&!labelName.equals("TMT"))
            throw new IllegalArgumentException("Label unknown! Please use TMT or EC! Unknown Label: "+labelName);

        //set reporter masses
        if (labelName.equals("TMT")){
            rep0Mass = 126.12773;
            rep1Mass = 127.13108;
        }

        if (labelName.equals("EC")){
            rep0Mass = 179.08487;
            rep1Mass = 180.08823;
        }
        //calculate mass Ranges
        double[] massRangeRep0 = DeviationCalc.ppmRangeCalc(ppmDev, rep0Mass);
        double[] massRangeRep1 = DeviationCalc.ppmRangeCalc(ppmDev, rep1Mass);

        //check Spectrum if peaks match
        ArrayList<Peak> peakList = spectrumIn.getPeakList();

        for (Peak peak : peakList){
            if (peak.getMass()>=massRangeRep0[0] && peak.getMass() <= massRangeRep0[1])
                matchedReporters.add(new ReporterMatch(peak, rep0Mass, labelName, "Rep0"));
            if (peak.getMass()>=massRangeRep1[0] && peak.getMass() <= massRangeRep1[1])
                matchedReporters.add(new ReporterMatch(peak, rep1Mass, labelName, "Rep1"));
            if (peak.getMass() > (rep1Mass+20))
                break;
        }

        return matchedReporters;
    }

    public static boolean isMatch(double massToCheck, double massCalculated, double ppmDeviation ){
        boolean match = false;
        double[] massRange = DeviationCalc.ppmRangeCalc(ppmDeviation, massCalculated);
        if (massToCheck >= massRange[0] && massToCheck <= massRange[1])
            match = true;
        return match;
    }
}
