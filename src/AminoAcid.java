

/**
 * Created by micha on 6/14/2017.
 */
public class AminoAcid {
    private String name;
    private String threeLetter;
    private char oneLetter;
    private Double exactMass;
    private SumFormula SumFormula;
    private Double waterLossMass;
    private SumFormula waterLossFormula;
    private boolean hasModification;


    public AminoAcid(String nameIn, String threeLetIn, String oneLetIn, String SumFormIn){
        this.name = nameIn;
        this.threeLetter = threeLetIn;
        //Be sure that one letter code is always written in upper case
        String oneLetterUpperCase = oneLetIn.toUpperCase();
        if (oneLetterUpperCase.length() == 1) {
            this.oneLetter = oneLetterUpperCase.charAt(0);
        }
        else throw new IllegalArgumentException("Invalid One-letter Code formatting!");

        this.SumFormula = new SumFormula(SumFormIn);
        this.exactMass = this.SumFormula.getExactMass();
        this.waterLossFormula = SumFormula.sumFormulaSubstractor(this.SumFormula, SumFormula.getWaterFormula());
        this.waterLossMass = this.waterLossFormula.getExactMass();
        this.hasModification = false;
    }

    public String getName() {
        return this.name;
    }

    public String get3Let() {
        return this.threeLetter;
    }

    public char get1Let() {
        return this.oneLetter;
    }

    public double getMass() {
        return this.exactMass;
    }

    public double getwaterLossMass() {
        return this.waterLossMass;
    }
    public SumFormula getSumFormula(){ return this.SumFormula;}
    public SumFormula getWaterLossFormula(){return this.waterLossFormula;}
    public boolean getModificationStatus(){ return  this.hasModification;}

}
