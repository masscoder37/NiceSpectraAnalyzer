/**
 * Created by Michael Stadlmeier on 6/17/2017.
 */
//Modification consists off different Peptide/Amino Acid modifications which can change the sum formula and mass of certain positions in a peptide or change all occurences of the amino acid in the peptide
public class Modification {
    private String modificationName;
    private SumFormula modificationFormula;
    private double modificationMass;
    //check if position or certain AA is modified: true means that a certain position is modified, false means that a certain AA is modified
    private boolean certainPosition;
    private int positionNumber;
    private char aminoAcidName;

    //make 2 constructors: one for position and one for AA
    public Modification(String modNameIn, String sumFormulaIn, char aminoAcidModIn){
        this.modificationName = modNameIn;
        this.modificationFormula = new SumFormula(sumFormulaIn);
        this.modificationMass = this.modificationFormula.getExactMass();
        this.modificationMass = this.modificationFormula.getExactMass();
        this.certainPosition = false;
        this.aminoAcidName = aminoAcidModIn;
    }

    public Modification(String modNameIn, String sumFormulaIn, int positionModIn){
        this.modificationName = modNameIn;
        this.modificationFormula = new SumFormula(sumFormulaIn);
        this.modificationMass = this.modificationFormula.getExactMass();
        this.modificationMass = this.modificationFormula.getExactMass();
        this.certainPosition = true;
        //calculate -1 to directly get index of lists
        this.positionNumber = positionModIn-1;
    }

    public boolean getPositionType(){return this.certainPosition;}
    public String getModificationName(){return this.modificationName;}
    public SumFormula getModificationFormula(){return this.modificationFormula;}
    public double getModificationMass(){return this.modificationMass;}
    public int getPositionNumber(){
        if (!certainPosition)
    throw new IllegalArgumentException("AminoAcid was modified, not certain position!");
        return this.positionNumber;
    }
    public char getAminoAcidName(){
        if (certainPosition == true)
            throw new IllegalArgumentException("Certain position was modified, not specific amino acid!");
        return this.aminoAcidName;
    }
    public static Modification uncleavedECDuplexNTerm(){
        Modification mod = new Modification("ECDuplex_intact_NTerm", "C14CxH28N4O4S", 1);
        return mod;
    }
    public static Modification uncleavedECDuplexLys(int pos){
        return new Modification("ECDuplex_intact_Lys", "C14CxH28N4O4S",pos);
    }
    public static Modification cleavedEC180NTerm(){
        return new Modification("EC180_cleaved_NTerm", "C9H14N2O2", 1);
    }
    public static Modification cleavedEC180Lys(int pos){
        return new Modification("EC180_cleaved_Lys", "C9H14N2O2",pos);
    }
    public static Modification cleavedEC179NTerm(){
        return new Modification("EC179_cleaved_NTerm", "C8CxH14N2O2", 1);
    }
    public static Modification cleavedEC179Lys(int pos){
        return new Modification("EC179_cleaved_Lys", "C8CxH14N2O2",pos);
    }

}
