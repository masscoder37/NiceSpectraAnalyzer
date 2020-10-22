/**
 * Created by micha on 6/14/2017.
 */
public class IonMatch {
    private Ion ion;
    private FragmentIon fragIon;
    private Peak peak;
    private double ppmDeviation;

    public IonMatch(Ion ionIn, Peak peakIn, double ppmDeviationIn){
        this.ion = ionIn;
        this.peak = peakIn;
        this.ppmDeviation = ppmDeviationIn;
    }
    public IonMatch(FragmentIon ionIn, Peak peakIn, double ppmDeviationIn){
        this.fragIon = ionIn;
        this.peak = peakIn;
        this.ppmDeviation = ppmDeviationIn;
    }

    public IonMatch(Ion ionIn, Peak peakIn){
        this.ion = ionIn;
        this.peak = peakIn;
        this.ppmDeviation = DeviationCalc.ppmDeviationCalc(this.ion.getMToZ(), this.peak.getMass());
    }



    public Ion getIon() {return this.ion;}
    public FragmentIon getFragmentIon(){return this.fragIon;}
    public Peak getPeak() {return this.peak;}
    public double getPpmDeviation() {return this.ppmDeviation;}


}
