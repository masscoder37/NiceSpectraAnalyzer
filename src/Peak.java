import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 6/14/2017.
 */

public class Peak {
    private double mass;
    private int charge;
    private double intensity;
    private double relInt;
    private boolean isBasePeak;
    private int scanNumberAffil;
    private boolean chargeStateKnown;
    DecimalFormat twoDec = new DecimalFormat("0.00");


    //initial constructor for peaks
    //can just create peak with basic information, doesn't know of the other peaks from the spectrum
    public Peak(double massIn, double intensityIn, int chargeIn, int scanNumberIn){
        this.mass = massIn;
        this.charge = chargeIn;
        if (this.charge != 0)
            this.chargeStateKnown = true;
        else{
            this.chargeStateKnown = false;
        }
        this.intensity = intensityIn;
        this.scanNumberAffil = scanNumberIn;
        this.relInt = 0;
        this.isBasePeak = false;
    }

    //second constructor if charge is unknown, has to be set later
    public Peak(double massIn, double intensityIn, int scanNumberIn){
        this.mass = massIn;
        this.intensity = intensityIn;
        this.scanNumberAffil = scanNumberIn;
        this.charge = 0;
        this.relInt = 0;
        this.chargeStateKnown= false;
        this.isBasePeak = false;

    }


    //setter for peak properties derived from ArrayList
    public void setRelInt(double relIntIn){
        this.relInt = relIntIn;
    }

    public void setBasePeak(){
        this.isBasePeak = true;
    }

    public void setCharge(int chargeIn){
        this.charge = chargeIn;
        if (this.charge != 0){
            this.chargeStateKnown = true;
        }
        else {
            this.chargeStateKnown = false;
        }

    }




    //getter
    public double getMass() {
        return this.mass;
    }
    public int getCharge(){
        return this.charge;
    }
    public double getIntensity(){
        return this.intensity;
    }
    public double getRelIntensity(){
        return this.relInt;
    }
    public boolean getBasePeak(){
        return this.isBasePeak;
    }
    public int getScanNumber(){
        return this.scanNumberAffil;
    }
    public boolean isChargeStateKnown(){return this.chargeStateKnown;}



    public void peakPrinter (){
        System.out.println("Mass: "+this.mass);
        System.out.println("Charge: "+this.charge);
        System.out.println("Intensity: "+this.intensity);
        System.out.println("rel. intensity: "+twoDec.format(this.relInt));
        System.out.println("Base Peak: "+this.isBasePeak);
        System.out.println("Scan number: "+this.scanNumberAffil);
        System.out.println("");

    }

}
