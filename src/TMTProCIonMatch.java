//objects of this class combine the theoretical Ion with the experimentally determined Peak
//object is only created if there is a real match for referencing everything at once

public class TMTProCIonMatch {
    private TMTproCCompIon compIon;
    private Peak matchedPeak;
    private double ppmDev;
    private String peptideSequence;
    private int scanNumber;

    public TMTProCIonMatch(TMTproCCompIon compIonIn, Peak matchedPeakIn, String peptideSequenceIn, int scanNumberIn){
        this.compIon = compIonIn;
        this.matchedPeak = matchedPeakIn;
        this.ppmDev = DeviationCalc.ppmDeviationCalc(compIonIn.getIon().getMToZ(), matchedPeakIn.getMass());
        this.peptideSequence = peptideSequenceIn;
        this.scanNumber = scanNumberIn;
    }

    public TMTproCCompIon getCompIon() {
        return compIon;
    }

    public Peak getMatchedPeak() {
        return matchedPeak;
    }

    public double getPpmDev() {
        return ppmDev;
    }

    public String getPeptideSequence() {
        return peptideSequence;
    }

    public int getScanNumber() {
        return scanNumber;
    }
}
