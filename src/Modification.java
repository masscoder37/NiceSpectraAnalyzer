

import java.util.ArrayList;

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
        this.certainPosition = false;
        this.aminoAcidName = aminoAcidModIn;
    }

    public Modification(String modNameIn, String sumFormulaIn, int positionModIn){
        this.modificationName = modNameIn;
        this.modificationFormula = new SumFormula(sumFormulaIn);
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


    //cleaved TMT looses isotope label by CO-loss
    //can't distinguish cleaved TMT126 and TMT127
    //Modification of +1 fragment ion only, not of peptide! because of negative charge
    public static Modification cleavedTMTduplex(int pos) {
        Modification cleaved127 = new Modification("TMTduplex_cleaved", "C3H5NO",pos);
        cleaved127.isLabel = true;
        cleaved127.isCleaved = true;
        return cleaved127;
    }



    public static Modification carbamidomethylation(){
        return new Modification("Cam", "C2H3NO", 'C');
    }
    public static Modification nemModification(){
        return new Modification("NEM", "C6H7NO2", 'C');
    }


    public static Modification oxidation(int pos) {
        return new Modification("Ox.", "O", pos);
    }

    public static Modification acetylation(int pos){
        return new Modification("Ac", "H2C2O",pos);
    }


    public static  Modification phosphorylation (int pos) {
        return new Modification("phos.", "HO3P", pos);
    }

    public static Modification cliXlinkShortAlk (int pos) {
        return new Modification("alk_s", "C3H2O", pos);
    }
    public static Modification cliXlinkShortSO(int pos) {
        return new Modification("SO_s", "C3H4O2S", pos);
    }
    public static Modification cliXlinkShortThial (int pos) {
        return new Modification("thial_s", "C3H2OS", pos);
    }
    public static ArrayList<Modification> cliXlinkShortModList (int pos){
        ArrayList<Modification> modListOut = new ArrayList<>();
        modListOut.add(cliXlinkShortAlk(pos));
        modListOut.add(cliXlinkShortSO(pos));
        modListOut.add(cliXlinkShortThial(pos));
        return modListOut;
    }
    public static ArrayList<Modification> cliXlinkLongModList (int pos){
        ArrayList<Modification> modListOut = new ArrayList<>();
        modListOut.add(cliXlinkLongAlk(pos));
        modListOut.add(cliXlinkLongSO(pos));
        modListOut.add(cliXlinkLongThial(pos));
        return modListOut;
    }

    public static ArrayList<Modification> dssoModList (int pos){
        ArrayList<Modification> modListOut = new ArrayList<>();
        //DSSO mods are equal to the cliXlink short mods
        modListOut.add(cliXlinkShortAlk(pos));
        modListOut.add(cliXlinkShortSO(pos));
        modListOut.add(cliXlinkShortThial(pos));
        return modListOut;
    }

    public static Modification cliXlinkLongAlk (int pos) {
        return new Modification("alk_l", "C9H9NO2", pos);
    }
    public static Modification cliXlinkLongSO(int pos) {
        return new Modification("SO_l", "C9H11NO3S", pos);
    }
    public static Modification cliXlinkLongThial (int pos) {
        return new Modification("thial_l", "C9H9NO2S", pos);
    }

    public static Modification ncHSEC_A(int pos){
        return new Modification("ncHSEC_A", "C12H13NO4", pos);
    }

    public static Modification cHSEC_A(int pos){
        return new Modification("cHSEK_A", "C7H9NO3", pos);
    }

    public static Modification ncHSEC_SH(int pos){
        return new Modification("ncHSEC_SH", "C12H13NO4S", pos);

    }

    //predefined lsST-modification
    public static Modification uncleavedlsST(int pos){
        Modification intactlsST =  new Modification("lsST_intact", "C9H16N2O2S",pos);
        intactlsST.isLabel = true;
        return intactlsST;
    }


    //cleaved lsST, reporter known but structure of complementary ion not clear
    //assume alkene residue?
    public static Modification cleavedlsST(int pos) {
        Modification cleavedlsST = new Modification("lsST_cleaved", "C3H2O",pos);
        cleavedlsST.isLabel = true;
        cleavedlsST.isCleaved = true;
        return cleavedlsST;
    }

    //TMTPro Modifications
    //lys
    public static Modification tmtPro0(){
        Modification out = new Modification("TMTPro0","C15H25N3O3",'K');
        out.isLabel = true;
        out.isCleaved = false;
        return out;
    }
    public static Modification tmtPro0(int pos){
        Modification out = new Modification("TMTPro0","C15H25N3O3",pos);
        out.isLabel = true;
        out.isCleaved = false;
        return out;
    }
    //lys
    public static Modification tmtPro(){
        Modification out = new Modification("TMTPro","C8Cx7H25NNx2O3",'K');
        out.isLabel = true;
        out.isCleaved = false;
        return out;
    }
    public static Modification tmtPro(int pos){
        Modification out = new Modification("TMTPro","C8Cx7H25NNx2O3",pos);
        out.isLabel = true;
        out.isCleaved = false;
        return out;
    }

    //complementary ions TMTPro0
    //lys
    public static Modification tmtPro0Comp(){
        Modification out = new Modification("TMTPro0Comp","C6H9N2O2-",'K');
        out.isLabel = true;
        out.isCleaved = true;
        return out;
    }

    public static Modification tmtPro0Comp(int pos){
        Modification out = new Modification("TMTPro0Comp","C6H9N2O2-",pos);
        out.isLabel = true;
        out.isCleaved = true;
        return out;
    }



    //if this function gets a modified peptide, then return the list of modifications present on the peptide
    public static ArrayList<Modification> modifiedPeptideModListCreator (Peptide modPeptideIn){
        ArrayList<Modification> modList = new ArrayList<>();
        //unmodified peptide can return empty list
        //modifying a peptide with an empty list is no problem and doesn't change modification status
        if (!modPeptideIn.getModificationStatus())
            return modList;
        ArrayList<AminoAcid> peptideAAList = modPeptideIn.getAminoAcidsList();
        //the aminoAcids list contains the modification status and the modification
        for (int i = 0; i < peptideAAList.size(); i++){
            AminoAcid current = peptideAAList.get(i);
            if (current.getModificationStatus()){
                //i+1 because AA counting starts at 1, peptide Modifier method deals with the rest
                modList.add(new Modification(current.getModification().getModificationName(), current.getModification().getModificationFormula().getSumFormula(), (i+1)));
            }
        }
        return modList;
    }

}
