import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 6/21/2017.
 */

//uses a spectrum and a (modified) Peptide to generate a list of matched ions
    //utilizes Deviation Calc to generate the appropriate ppm windows
public class PeakCompare {
    private DecimalFormat twoDec = new DecimalFormat("0.00");

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
                if (i>1 && i<5)
                peptideIn.createAddFragmentIonChargestate(i);
            }
        }
        ArrayList<FragmentIon> fragmentIonsList = peptideIn.getbIons();
        fragmentIonsList.addAll(peptideIn.getyIons());
        for (FragmentIon fragment : fragmentIonsList){
            double[] massRange = DeviationCalc.ppmRangeCalc(ppmDev, fragment.getMToZ());
            for (Peak peak : peakList){
                if (peak.getMass() >= massRange[0]&& peak.getMass()<= massRange[1]){
                    double deviation = DeviationCalc.ppmDeviationCalc(fragment.getMToZ(), peak.getMass());
                    matches.add(new IonMatch(fragment, peak, deviation));
                    System.out.println("Match! Ion: "+fragment.getCompleteIon()
                            +" "+fragment.getCharge()+"+"
                            +"  Mass found: "+peak.getMass()
                            +"   Deviation: " +twoDec.format(deviation)+" ppm"
                    +" is modfied: "+fragment.getModificationStatus());
                }
            }

        }

        return matches;
    }
}
