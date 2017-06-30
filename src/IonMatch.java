/**
 * Created by micha on 6/14/2017.
 */
//this class
public class IonMatch {
    private FragmentIon fragmentIon;
    private Peak peak;
    private double ppmDeviation;

    public IonMatch(FragmentIon fragmentIonIn, Peak peakIn, double ppmDeviationIn){
        this.fragmentIon = fragmentIonIn;
        this.peak = peakIn;
        this.ppmDeviation = ppmDeviationIn;
    }



    public FragmentIon getFragmentIon() {return this.fragmentIon;}
    public Peak getPeak() {return this.peak;}
    public double getPpmDeviation() {return this.ppmDeviation;}


}
