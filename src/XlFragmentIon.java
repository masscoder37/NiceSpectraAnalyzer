//this class creates objects for the specific crosslink fragment ions
//every instance of the class is an individual crosslink fragment ion of a single xl peptide

public class XlFragmentIon extends Ion {

    private Peptide modPeptide;
    private String originPeptideSequence;
    private String pepType; //alpha or beta peptide
    private String cliXlinkSize; //long or short side of clixlink
    private int cliXlinkPos; //where is the peptide modified

    public XlFragmentIon(Peptide modPeptideIn, String originPeptideSequenceIn, String pepTypeIn, String cliXlinkSideIn,
                         int chargeStateIn, SumFormula sumFormulaIn, int cliXlinkPosIn){
        super (sumFormulaIn, chargeStateIn);
        this.modPeptide = modPeptideIn;
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
}
