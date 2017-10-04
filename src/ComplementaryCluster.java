import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 10/2/2017.
 */

//this class combines Complementary Ions into complementary clusters
    //in this class, also the calculations regarding the complementary ion signal intensities are carried out
    //in total, the following information has to be made accessible to proceed with the .csv creation process
//[0] Modified Peptide - provided in CSVAnalyzer
//[1] Precursor Mass - provided in CSVAnalyzer
//[2] Precursor Charge State - provided in CSVAnalyzer
//[3] Scan Number
//[4] Leading Proteins - provided in CSVAnalyzer
//[5] Complementary Ion Cluster Pair
//[6] Fragment Ion Amino Acid Sequence
//[7] Unadjusted Intensity SOT180c
//[8] Unadjusted Intensity SOT179c
//[9] Intensity SOT180c
//[10] Intensity SOT179c
//[11] ratio SOT179c/SOT180c

    //this class should also be able to generate a complete String to feed to the Stringbuilder
public class ComplementaryCluster {

    private ComplementaryIon lightCompIon;
    private ComplementaryIon heavyCompIon;
    private int scanNumber;
    private double adjustedLightIntensity;
    private double adjustedHeavyIntensity;
    private double ratio179c180c;

    public ComplementaryCluster (ComplementaryIon lightCompIonIn, ComplementaryIon heavyCompIonIn, String scanNumberIn){
        //set complementary Ions and scan number
        this.lightCompIon = lightCompIonIn;
        this.heavyCompIon = heavyCompIonIn;
        this.scanNumber = Integer.parseInt(scanNumberIn);

        //get adjusted ratios from intensityAdjuster function
        double[] adjustedRatios = intensityAdjuster(this.lightCompIon, this.heavyCompIon);
        this.adjustedLightIntensity = adjustedRatios[0];
        this.adjustedHeavyIntensity = adjustedRatios[1];

        this.ratio179c180c = this.adjustedHeavyIntensity/this.adjustedLightIntensity;
    }

    private static double[] intensityAdjuster (ComplementaryIon lightCompIonIn, ComplementaryIon heavyCompIonIn){
        //adjustedRatios[0]: intensity light comp ion, SOT180c
        //adjustedRatios[1]: intensity heavy comp ion, SOT179c
        double[] adjustedRatios = new double[2];

        //empirical factor setting the isotopical impurity of the SOT179-reagent, stemming from non-complete 13C-labelling
        double isotopicImpurityFactor = 0.0042;

        //set intensities for later calculations
        double unadjustedLightIntensity = lightCompIonIn.getPeakAbsInt();
        double unadjustedHeavyIntensity = heavyCompIonIn.getPeakAbsInt();

        SumFormula sumFormulaSOT180c = new SumFormula(lightCompIonIn.getFragIonSumFormula());
        double isotopePatternFactor = IsotopicDistributer.abundanceAddNeutron(sumFormulaSOT180c);

        double denominator = (1-isotopicImpurityFactor*isotopePatternFactor);

        adjustedRatios[0] = (unadjustedLightIntensity-isotopicImpurityFactor*unadjustedHeavyIntensity)/denominator;
        adjustedRatios[1] = (unadjustedHeavyIntensity-isotopePatternFactor*unadjustedLightIntensity)/denominator;

        return adjustedRatios;
    }

    public static ArrayList<ComplementaryCluster> compClusterMatcher(ArrayList<ComplementaryIon> compIonsIn){
        //generate list of complementary clusters, each containing 2 complementary ions
        ArrayList<ComplementaryCluster> matchedCompClusters = new ArrayList<>();

        //first, divide the compIonsIn-list into 2 lists: one for SOT180c and one for SOT179c
        ArrayList<ComplementaryIon> compIonsSOT179c = new ArrayList<>();
        ArrayList<ComplementaryIon> compIonsSOT180c = new ArrayList<>();

        for (ComplementaryIon compIon : compIonsIn){
            if (compIon.getLabelName().contains("179")&&!compIon.getLabelName().contains("180"))
                compIonsSOT179c.add(compIon);
            if (compIon.getLabelName().contains("180")&&!compIon.getLabelName().contains("179"))
                compIonsSOT180c.add(compIon);
        }

        //now, loop through one of the lists. if the fragment ion and the fragment ion charge is the same in the other list, and the scan number is the same, make a complementary cluster
        String currentFragmentIon;
        int currentFragmentIonCharge;
        int currentScanNumber;

        for(ComplementaryIon sot180c : compIonsSOT180c){
            currentFragmentIon = sot180c.getFragmentIon();
            //sort out all the y1 ions, because there could be quite some interference
            if (currentFragmentIon.equals("y1"))
                continue;
            currentFragmentIonCharge = sot180c.getFragmentIonCharge();
            currentScanNumber = sot180c.getScanNumber();

            //now, loop through all the ions of the other list
            for(ComplementaryIon sot179c : compIonsSOT179c){
                //only if fragment ion, fragment ion charge and scan number are the same, make complementary cluster
                if(currentFragmentIon.equals(sot179c.getFragmentIon())&&currentFragmentIonCharge==sot179c.getFragmentIonCharge()&&currentScanNumber==sot179c.getScanNumber())
                    matchedCompClusters.add(new ComplementaryCluster(sot180c, sot179c, Integer.toString(currentScanNumber)));
            }
        }
        return matchedCompClusters;
    }


    public static String compClusterCSVStringProducer(ArrayList<ComplementaryCluster> compClustersIn, String modPeptideIn, String precursorMassIn, String precursorChargeStateIn,
                                                        String leadingProteinsIn) {
        //The following variables must be produced for the String Builder
        //[0] Modified Peptide
        //[1] Precursor Mass
        //[2] Precursor Charge State
        //[3] Scan Number
        //[4] Leading Proteins
        //[5] Complementary Ion Cluster Pair
        //[6] Fragment Ion Amino Acid Sequence
        //[7] Unadjusted Intensity SOT180c
        //[8] Unadjusted Intensity SOT179c
        //[9] Intensity SOT180c
        //[10] Intensity SOT179c
        //[11] ratio SOT179c/SOT180c
        //[12] Isotope Pattern Factor

        StringBuilder combinedValues = new StringBuilder();

        for (ComplementaryCluster compCluster : compClustersIn){
            SumFormula lightCompIonFormula = new SumFormula(compCluster.getLightCompIon().getFragIonSumFormula());
            double isotopePatternFactor = IsotopicDistributer.abundanceAddNeutron(lightCompIonFormula);
            combinedValues.append(modPeptideIn + ",");
            combinedValues.append(precursorMassIn + ",");
            combinedValues.append(precursorChargeStateIn + ",");
            combinedValues.append(Integer.toString(compCluster.getScanNumber()) + ",");
            combinedValues.append(leadingProteinsIn + ",");
            combinedValues.append(compCluster.getLightCompIon().getFragmentIon() +" " + Integer.toString(compCluster.getLightCompIon().getFragmentIonCharge())+"+,");
            combinedValues.append(compCluster.getLightCompIon().getFragIonSequence() + ",");
            combinedValues.append(Double.toString(compCluster.getLightCompIon().getPeakAbsInt()) + ",");
            combinedValues.append(Double.toString(compCluster.getHeavyCompIon().getPeakAbsInt()) + ",");
            combinedValues.append(Double.toString(compCluster.getAdjustedLightIntensity()) + ",");
            combinedValues.append(Double.toString(compCluster.getAdjustedHeavyIntensity()) + ",");
            combinedValues.append(Double.toString(compCluster.getRatio179c180c()) + ",");
            combinedValues.append(Double.toString(isotopePatternFactor));
            combinedValues.append("\n");
        }
        return combinedValues.toString();
    }

    public ComplementaryIon getLightCompIon() {
        return this.lightCompIon;
    }

    public ComplementaryIon getHeavyCompIon() {
        return this.heavyCompIon;
    }

    public int getScanNumber() {
        return this.scanNumber;
    }

    public double getAdjustedLightIntensity() {
        return this.adjustedLightIntensity;
    }

    public double getAdjustedHeavyIntensity() {
        return this.adjustedHeavyIntensity;
    }

    public double getRatio179c180c() {
        return this.ratio179c180c;
    }
}
