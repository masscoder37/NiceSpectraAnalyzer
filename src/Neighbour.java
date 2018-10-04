/**
 * Created by Michael Stadlmeier on 10/4/2018.
 */
public class Neighbour{
private double massDiff;
private Peak peak;

public Neighbour(Peak peakIn, double mainPeakMass){
    this.peak = peakIn;
    this.massDiff = Math.abs(peakIn.getMass()-mainPeakMass);

}

    public double getMassDiff() {
        return massDiff;
    }

    public Peak getNeighbourPeak() {
        return peak;
    }
}
