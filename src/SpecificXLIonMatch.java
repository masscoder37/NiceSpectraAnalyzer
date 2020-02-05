//objects of this class store the information about an successful match between a spectrum signal and
//the specific XL fragment ions


public class SpecificXLIonMatch {

    private Peak matchedPeak;
    private XlFragmentIon matchedFragIon;
    private double massDeviation;
    private String modType; //alk, thial, SO
    private String modSize; //long, short


    public SpecificXLIonMatch(Peak peakIn, XlFragmentIon fragIonIn) {
        this.matchedPeak = peakIn;
        this.matchedFragIon = fragIonIn;
        this.massDeviation = DeviationCalc.ppmDeviationCalc(this.matchedFragIon.getMToZ(), this.matchedPeak.getMass());
        this.modSize = fragIonIn.getCliXlinkSize();
        int position = fragIonIn.getCliXlinkPos();
        String modName = fragIonIn.getModPeptide().getAminoAcidsList().get(position - 1).getModification().getModificationName();
        if (modName.contains("alk"))
            this.modType = "alk";
        else if (modName.contains("thial"))
            this.modType = "thial";
        else if (modName.contains("SO"))
            this.modType = "SO";
        if (this.modType == null)
            throw new IllegalArgumentException("matched Fragment ion does not contain expected modification!");
    }


    public String getModType() {
        return modType;
    }

    public String getModSize() {
        return modSize;
    }

    public Peak getMatchedPeak() {
        return matchedPeak;
    }

    public XlFragmentIon getMatchedFragIon() {
        return matchedFragIon;
    }

    public double getMassDeviation() {
        return massDeviation;
    }

}
