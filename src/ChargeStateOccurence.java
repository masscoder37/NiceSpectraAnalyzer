/**
 * Created by Michael Stadlmeier on 10/4/2018.
 */
public class ChargeStateOccurence {
    private int chargeState;
    private int occurence;
    private double summedIntensity;

    public ChargeStateOccurence(int chargeState) {
        this.chargeState = chargeState;
        this.occurence = 0;
        this.summedIntensity = 0;
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

    public void increaseOccurence() {
        this.occurence = this.occurence++;
    }

    public void addIntensity(double intensity){
        this.summedIntensity = this.summedIntensity + intensity;
    }
}
