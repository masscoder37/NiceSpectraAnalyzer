/**
 * Created by Michael Stadlmeier on 10/4/2018.
 */
public class ChargeStateOccurence {
    private int chargeState;
    private int occurence;
    private double summedIntensity;
    private boolean representativeNeighbour;

    public ChargeStateOccurence(int chargeState) {
        this.chargeState = chargeState;
        this.occurence = 0;
        this.summedIntensity = 0;
        this.representativeNeighbour = false;
    }

    public int getChargeState() {
        return chargeState;
    }

    public int getOccurence() {
        return occurence;
    }

    public double getSummedIntensity() {
        return summedIntensity;
    }

    public boolean getIfRepresentativeNeighbour() { return representativeNeighbour; }

    public void increaseOccurence() {
        this.occurence = this.occurence+1;
    }

    public void addIntensity(double intensity){
        this.summedIntensity = this.summedIntensity + intensity;
    }

    public void nullifyIntensity() {this.summedIntensity = 0;}

    public void setRepresentativeNeighbour() {this.representativeNeighbour = true;}

}
