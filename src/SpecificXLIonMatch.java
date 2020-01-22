//objects of this class store the information about an successful match between a spectrum signal and
//the specific XL fragment ions


public class SpecificXLIonMatch {

    private Peak matchedPeak;
    private XlFragmentIon matchedFragIon;
    private double massDeviation;


    public SpecificXLIonMatch (Peak peakIn,XlFragmentIon fragIonIn){
        this.matchedPeak = peakIn;
        this.matchedFragIon = fragIonIn;
        this.massDeviation = DeviationCalc.ppmDeviationCalc(this.matchedFragIon.getMToZ(), this.matchedPeak.getMass());
    }

    //TODO: this method should provide the String for the output file
    public static String matchStringProducer (){
        String out = "";
        return out;
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
