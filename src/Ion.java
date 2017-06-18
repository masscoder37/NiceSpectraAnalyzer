/**
 * Created by micha on 6/13/2017.
 */

//superclass Ion
    //other Ions (fragmentIon) is part of this class
    //normal ion parameters: SumFormula, mass, charge, mass to charge ratio
public class Ion {
    private double exactMass;
    private int charge;
    private boolean chargeStateKnown;
    private double massToCharge;//needs to be implemented later...maybe by going by the protons in the sum formula?
    private SumFormula formula;

    //default Constructor
    public Ion(){
        this.exactMass = 0;
        this.charge = 0;
        this.chargeStateKnown = false;
        this.formula = null;
        this.massToCharge = 0;
    }

    //Constructor: different ones, if Formula is known or unknown, charge is important, etc.
    //Most simple constructor: just mass known
    public Ion(double exactMassIn){
        this.exactMass = exactMassIn;
        this.charge = 0;
        this.chargeStateKnown = false;

    }
//mass and charge known
    public Ion(double exactMassIn, int chargeIn){
        this.exactMass = exactMassIn;
        this.charge = chargeIn;
        if (chargeIn == 0) {
            this.chargeStateKnown = false;
        }
        else {
            this.chargeStateKnown = true;
        }
    }
    //sumformula known
    public Ion(double exactMassIn, String sumFormulaIn) {
        this.formula = new SumFormula(sumFormulaIn);
        this.exactMass = this.formula.getExactMass();
        this.chargeStateKnown = false;
    }
    //sumFormula and charge known
    public Ion(SumFormula sumFormulaIn, int chargeIn) {
        this.formula = sumFormulaIn;
        this.exactMass = this.formula.getExactMass();
        this.charge = chargeIn;
        if (chargeIn == 0)
            throw new IllegalArgumentException("Charge state of Ions can't be 0!");
        this.chargeStateKnown = true;
        this.massToCharge = this.exactMass/this.charge;
    }





    public double getExactMass(){return this.exactMass;}
    public int getCharge(){return this.charge;}
    public boolean getChargeStatus(){return this.chargeStateKnown;}
    public double getMToZ(){return this.massToCharge;}
    public SumFormula getFormula(){ return this.formula;}



}
