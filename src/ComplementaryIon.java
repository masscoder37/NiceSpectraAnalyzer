/**
 * Created by Michael Stadlmeier on 10/1/2017.
 */


//this class is used to store all the information of the fragment ion analysis
    //by doing so, individual ComplementaryIons of one Scan event can be sorted and combined into Complementary Ion Clusters
public class ComplementaryIon {

    private String modPeptideOrigin;
    private String labelName;
    private String fragmentIon;
    private int fragmentIonCharge;
    private double fragmentIonMass;
    private double peakMass;
    private double massDeviation;
    private double peakRelInt;
    private double peakAbsInt;
    private int scanNumber;
    private String fragIonSequence;
    private String fragIonSumFormula;

    public ComplementaryIon (String pepOriginIn, String labelNameIn, String fragIonIn, String fragIonChargeIn, String fragIonMassIn, String peakMassIn, String massDevIn,
                             String peakRelIntIn, String peakAbsIntIn, String scanNumberIn, String fragIonSequenceIn, String fragIonSumFormulaIn){
        this.modPeptideOrigin = pepOriginIn;
        this.labelName = labelNameIn;
        this.fragmentIon = fragIonIn;
        this.fragmentIonCharge = Integer.parseInt(fragIonChargeIn);
        this.fragmentIonMass = Double.parseDouble(fragIonMassIn);
        this.peakMass = Double.parseDouble(peakMassIn);
        this.massDeviation = Double.parseDouble(massDevIn);
        this.peakRelInt = Double.parseDouble(peakRelIntIn);
        this.peakAbsInt = Double.parseDouble(peakAbsIntIn);
        this.scanNumber = Integer.parseInt(scanNumberIn);
        this.fragIonSequence = fragIonSequenceIn;
        this.fragIonSumFormula = fragIonSumFormulaIn;
    }


    public String getModPeptideOrigin() {
        return this.modPeptideOrigin;
    }

    public String getLabelName() {
        return this.labelName;
    }

    public String getFragmentIon() {
        return this.fragmentIon;
    }

    public int getFragmentIonCharge() {
        return this.fragmentIonCharge;
    }

    public double getFragmentIonMass() {
        return this.fragmentIonMass;
    }

    public double getPeakMass() {
        return this.peakMass;
    }

    public double getMassDeviation() {
        return this.massDeviation;
    }

    public double getPeakRelInt() {
        return this.peakRelInt;
    }

    public double getPeakAbsInt() {
        return this.peakAbsInt;
    }

    public int getScanNumber() {
        return this.scanNumber;
    }

    public String getFragIonSequence() {
        return this.fragIonSequence;
    }

    public String getFragIonSumFormula() {
        return this.fragIonSumFormula;
    }
}
