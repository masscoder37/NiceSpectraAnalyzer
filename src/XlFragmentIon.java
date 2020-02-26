//this class creates objects for the specific crosslink fragment ions
//every instance of the class is an individual crosslink fragment ion of a single xl peptide

public class XlFragmentIon extends Ion {

    private Peptide modPeptide;
    private String originPeptideSequence;
    private String pepType; //alpha or beta peptide
    private String cliXlinkSize; //long or short side of clixlink
    private String cliXlinkMod; //alk, thial, or SO
    private int cliXlinkPos; //where is the peptide modified

    public XlFragmentIon(Peptide modPeptideIn, String originPeptideSequenceIn, String pepTypeIn, String cliXlinkSideIn,
                         int chargeStateIn, SumFormula sumFormulaIn, int cliXlinkPosIn){
        super (sumFormulaIn, chargeStateIn);
        this.modPeptide = modPeptideIn;
        if (this.modPeptide.getSequence().contains("alk"))
            this.cliXlinkMod = "alk";
        else if (this.modPeptide.getSequence().contains("thial"))
            this.cliXlinkMod = "thial";
        else if (this.modPeptide.getSequence().contains("SO"))
            this.cliXlinkMod = "SO";
        else
            throw new IllegalArgumentException("Modification of XlFragmentIon not recognized! Please use alk, thial or SO. Mod. sequence: "+this.modPeptide.getSequence());
        this.originPeptideSequence = originPeptideSequenceIn;
        this.pepType = pepTypeIn;
        this.cliXlinkSize = cliXlinkSideIn;
        this.cliXlinkPos = cliXlinkPosIn;
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

    public String getCliXlinkSize() {
        return cliXlinkSize;
    }

    public int getCliXlinkPos() {
        return cliXlinkPos;
    }

    public String getCliXlinkMod() {return cliXlinkMod; };

}
