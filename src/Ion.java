/**
 * Created by micha on 6/13/2017.
 */

//superclass Ion
    //every other Ion (fragmentIon, matchedIon,...) is part of this class
    //normal ion parameters: SumFormula, mass, charge, mass to charge ratio
public class Ion {
    private double exactMass;
    private double exactNeutalMass;
    private int charge;
    private boolean cahrgeStateKnown;
    private SumFormula Formula;
    private double massToCharge;


    //Constructor: different ones, if Formula is known or unknown, charge is important, etc.
    //Most simple constructor: just mass and charge known
    public Ion(double exactMassIn, int chargeIn){
        this.exactMass = exactMassIn;
        this.charge = chargeIn;

    }
}
