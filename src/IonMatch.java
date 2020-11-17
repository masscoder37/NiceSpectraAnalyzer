/**
 * Created by micha on 6/14/2017.
 */
public class IonMatch {
    private Ion ion = null;
    private FragmentIon fragIon = null;
    private TMTproCCompIon tmtProCCompIon = null;
    private Peak peak;
    private double ppmDeviation;

    public IonMatch(Ion ionIn, Peak peakIn, double ppmDeviationIn){
        this.ion = ionIn;
        this.peak = peakIn;
        this.ppmDeviation = ppmDeviationIn;
    }

    public IonMatch(TMTproCCompIon ionIn, Peak peakIn){
        this.tmtProCCompIon = ionIn;
        this.peak = peakIn;
        this.ppmDeviation = DeviationCalc.ppmDeviationCalc(ionIn.getIon().getMToZ(), peakIn.getMass());
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
    public TMTproCCompIon getTmtProCCompIon(){return this.tmtProCCompIon;}
    //returns true if the match is a fullLength comp ion, false if it's a comp. fragment ion
    public boolean isFullLengthIon(){
        if(this.getTmtProCCompIon() != null)
            return true;
        return false;
    }


}
