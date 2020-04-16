//this class creates objects for the specific crosslink fragment ions
//every instance of the class is an individual crosslink fragment ion of a single xl peptide

public class XlFragmentIon extends Ion {

    private Peptide modPeptide;
    private String originPeptideSequence;
    private String pepType; //alpha or beta peptide
    private String size; //long or short side of clixlink
    private String modType; //alk, thial, or SO
    private int modPos; //where is the peptide modified

    public XlFragmentIon(Peptide modPeptideIn, String originPeptideSequenceIn, String pepTypeIn, String sideIn,
                         int chargeStateIn, SumFormula sumFormulaIn, int posIn){
        super (sumFormulaIn, chargeStateIn);
        this.modPeptide = modPeptideIn;
        if (this.modPeptide.getSequence().contains("alk"))
            this.modType = "alk";
        else if (this.modPeptide.getSequence().contains("thial"))
            this.modType = "thial";
        else if (this.modPeptide.getSequence().contains("SO"))
            this.modType = "SO";
        else
            throw new IllegalArgumentException("Modification of XlFragmentIon not recognized! Please use alk, thial or SO. Mod. sequence: "+this.modPeptide.getSequence());
        this.originPeptideSequence = originPeptideSequenceIn;
        this.pepType = pepTypeIn;
        this.size = sideIn; //states "dsso" for DSSO
        this.modPos = posIn;
    }

    public Peptide getModPeptide() {
        return modPeptide;
    }

    public String getOriginPeptideSequence() {
        return originPeptideSequence;
    }

    public String getPepType() {
        return pepType;
    }

    public String getSize() {
        return size;
    }

    public int getModPos() {
        return modPos;
    }

    public String getModType() {return modType; };

}
