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
    private boolean isLabel = false;
    private boolean isCleaved = false;

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
//getter
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

    public boolean getLabelStatus(){return this.isLabel;}
    public boolean getCleavedStatus(){return this.isCleaved;}


    //predefined EC-modifications
    public static Modification uncleavedECDuplex(int pos){
        Modification intactEC =  new Modification("ECDuplex_intact", "C14CxH28N4O4S",pos);
        intactEC.isLabel = true;
        return intactEC;
    }

    public static Modification cleavedEC180(int pos){
        Modification cleaved180 = new Modification("EC180_cleaved", "C9H14N2O2",pos);
        cleaved180.isLabel = true;
        cleaved180.isCleaved = true;
        return cleaved180;
    }
    public static Modification cleavedEC179(int pos) {
        Modification cleaved179 = new Modification("EC179_cleaved", "C8CxH14N2O2",pos);
        cleaved179.isLabel = true;
        cleaved179.isCleaved = true;
        return cleaved179;
    }
    //predefined TMT-modifications
    public static Modification uncleavedTMTDuplex(int pos){
        Modification intactTMT =  new Modification("TMTDuplex_intact", "C11CxH20N2O2",pos);
        intactTMT.isLabel = true;
        return intactTMT;
    }

    public static Modification cleavedTMT127(int pos) {
        Modification cleaved127 = new Modification("EC179_cleaved", "",pos);
        cleaved127.isLabel = true;
        cleaved127.isCleaved = true;
        return cleaved127;
    }
    public static Modification cleavedTMT126(int pos){
        Modification cleaved126 = new Modification("EC180_cleaved", "",pos);
        cleaved126.isLabel = true;
        cleaved126.isCleaved = true;
        return cleaved126;
    }












    public static Modification carbamidomethylation(){
        Modification carbamidomethyl = new Modification("Carbamidomethyl", "C2H3NO", 'C');
        return carbamidomethyl;
    }

    public static Modification oxidation(int pos) {
        Modification oxidation = new Modification("Ox.", "O", pos);
        return oxidation;
    }

}
