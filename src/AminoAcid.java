/**
 * Created by micha on 6/14/2017.
 */
public class AminoAcid {
    private String name;
    private String threeLetter;
    private String oneLetter;
    private Double exactMass;
    private SumFormula sumFormula;
    private Double waterLossMass;

    public AminoAcid(String nameIn, String threeLetIn, String oneLetIn, String sumFormulaIn){
        this.name = nameIn;
        this.threeLetter = threeLetIn;
        this.oneLetter = oneLetIn.toUpperCase();
        this.sumFormula = new SumFormula(sumFormulaIn);
        this.exactMass = this.sumFormula.getExactMass();
        this.waterLossMass = this.exactMass- ( 2 * AtomicMasses.getHMASS() + AtomicMasses.getOMASS());
    }

    public String getName() {
        return this.name;
    }

    public String get3Let() {
        return this.threeLetter;
    }

    public String get1Let() {
        return this.oneLetter;
    }

    public double getMass() {
        return this.exactMass;
    }

    public double getwaterLossMass() {
        return this.waterLossMass;
    }
    public SumFormula getSumFormula(){ return this.sumFormula;}

}
