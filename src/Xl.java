import java.util.ArrayList;

public class Xl {


    private Peptide peptideAlpha;   // alpha is the longer peptide
    private Peptide peptideBeta;    //beta is the smaller peptide
    private int xlPosAlpha;
    private int xlPosBeta;
    private ArrayList<XlFragmentIon> alphaXLFragments;
    private ArrayList<XlFragmentIon> betaXLFragments;
    private String xlUsed;
    private SumFormula xlSumFormula;
    private Ion theoreticalXLIon;
    private double isolatedMassToCharge;
    private int scanNumber;
    private String fragmentationMethod;
    private boolean monoisotopicSelected;
    private int monoisotopicPeakOffset;
    private double retentionTime; //the retention time of the XL in seconds
    private boolean alphaEqualsBeta; //for unwanted skewing of the data
    //doing so will associate all the matches automatically with the respective XL
    private ArrayList<SpecificXLIonMatch> xlIonMatches;

    public Xl(String peptide1In, String peptide2In, String xlIn, int chargeStateIn,
              int scanNumberIn, String fragmentationMethodIn, double isolatedMassToChargeIn,
              int xlPos1In, int xlPos2In, ArrayList<AminoAcid> aaIn, double retentionTimeIn){

        //only cliXlink is supported atm
        if (!xlIn.equals("cliXlink"))
            throw new IllegalArgumentException("Unknown crosslinker: "+ xlIn);
        //easy to set variables
        this.xlUsed = xlIn;
        this.scanNumber = scanNumberIn;
        this.fragmentationMethod = fragmentationMethodIn;
        this.isolatedMassToCharge = isolatedMassToChargeIn;

        //utilize the xlPeptideModification function to prepare the peptides with their modifications from merox
        if (peptide1In.length() > peptide2In.length()){
            this.peptideAlpha = xlPeptideModification(peptide1In,aaIn);
            this.xlPosAlpha = xlPos1In;
            this.peptideBeta = xlPeptideModification(peptide2In, aaIn);
            this.xlPosBeta = xlPos2In;
        }
        else{
            this.peptideAlpha = xlPeptideModification(peptide2In,aaIn);
            this.xlPosAlpha = xlPos2In;
            this.peptideBeta = xlPeptideModification(peptide1In, aaIn);
            this.xlPosBeta = xlPos1In;

        }
        if (peptide1In.equals(peptide2In))
            this.alphaEqualsBeta = true;
        else
            this.alphaEqualsBeta = false;

        //create the combined theoretical ion
        //combine sum formulas from the peptides and the sum formula of the crosslinker
        this.xlSumFormula = SumFormula.sumFormulaJoiner(peptideAlpha.getSumFormula(),
                SumFormula.sumFormulaJoiner(peptideBeta.getSumFormula(), SumFormula.getCliXlinkFormula()));
        //add protons according to charge state? necessary?
        //TODO: check if protons have to be added for all the classes to work
        for (int i = 1; i < chargeStateIn+1; i++)
        {
            this.xlSumFormula = SumFormula.sumFormulaJoiner(this.xlSumFormula, SumFormula.getProtonFormula());
        }

        this.theoreticalXLIon = new Ion(this.xlSumFormula, chargeStateIn);

        //check if experimental isolated m/z and theoretical mass match or a higher isotope peak was isolated
        double theoMassToCharge = theoreticalXLIon.getMToZ();
        //set ppm Tolerance to something
        double ppmDev = 10;
        //set the default value to something unobtainable high
        this.monoisotopicPeakOffset = 50;
        if (DeviationCalc.ppmMatch(theoMassToCharge, isolatedMassToChargeIn, ppmDev)){
            this.monoisotopicSelected = true;
            this.monoisotopicPeakOffset = 0;
        }
        else{
            this.monoisotopicSelected = false;
            for (int n = 1; n <10; n++){
                double shiftedMass = theoMassToCharge + (AtomicMasses.getNEUTRON() * n / chargeStateIn);
                if (DeviationCalc.ppmMatch(shiftedMass, isolatedMassToChargeIn, ppmDev)) {
                    this.monoisotopicPeakOffset = n;
                    break;
                }
            }

        }

        this.retentionTime = retentionTimeIn;
        this.alphaXLFragments = new ArrayList<XlFragmentIon>();
        this.betaXLFragments = new ArrayList<XlFragmentIon>();
        fragIonCreator();

    }

    //this functions deals with adding the static modifications and the optional Methionine-Oxidation
    //oxidation is specified by small letter 'm'
    private static Peptide xlPeptideModification(String sequenceIn, ArrayList<AminoAcid> aaListIn){
        Peptide out;
        //this list will handle all the modifications
        ArrayList<Modification> modList = new ArrayList<>();

        //check if cysteines are present - they are output by MeroX as B because of the carbamidomethylation
        //also check if oxidized methionines exist - labeled by merox with small letter m
        if (sequenceIn.contains("B") || sequenceIn.contains("m")){
            //handle static carbamidomethylation
            if (sequenceIn.contains("B"))
                modList.add(Modification.carbamidomethylation());
            //to change sequence string, copy string to string builder and do the required changes
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sequenceIn.length()-1; i++){
                char c = sequenceIn.charAt(i);
                switch (c){
                    case 'B':
                        sb.append("C");
                        //the carbamidomethylation modification should only be added to the list once, so that will be handeled in the beginning
                        break;
                    case 'm':
                        sb.append("M"); //doesn't really matter, since Peptide converts to Upper Case anyway
                        modList.add(Modification.oxidation(i));
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            //stringbuilder contains modified sequence now
            String modifiedSequence = sb.toString();
            out = new Peptide(modifiedSequence, aaListIn);
            out = out.peptideModifier(modList);
            return out;
        }
        //otherwise, the peptide can be parsed as-is
        else
            out = new Peptide(sequenceIn, aaListIn);

        return out;
    }
    //in this class, the specific fragment ions of the XL will be set
    //the generation of a XLFragmentIon needs:
    //modified peptide, original peptide sequence peptideType(alpha or beta), clixlinkside (long or short)
    //charge state, sum formula (with correct number of protons), clixlinkpos (where was the modification happening)
    private void fragIonCreator (){
        //first populate alpha peptide list
        //create all possible fragments:long, short, and all charge states up to XL-charge state -1
        //for each charge state, 6 possible fragments: alkene, SO and thial, and long and short
        ArrayList<Modification> shortModListAlpha = Modification.cliXlinkShortModList(this.xlPosAlpha);
        ArrayList<Modification> longModListAlpha = Modification.cliXlinkLongModList(this.xlPosAlpha);
        ArrayList<Modification> shortModListBeta = Modification.cliXlinkShortModList(this.xlPosBeta);
        ArrayList<Modification> longModListBeta = Modification.cliXlinkLongModList(this.xlPosBeta);
        //loop through all the different short modifications
        for (Modification mod:shortModListAlpha){
            ArrayList<Modification> currentModList = new ArrayList<>();
            currentModList.add(mod);
            //first, adjustPeptide and sumFormula of peptide
            Peptide modPeptide = this.peptideAlpha;
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i > z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.alphaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideAlpha.getSequence(),
                        "alpha", "short", z, modPeptideFormula, this.xlPosAlpha));
            }
            currentModList.clear();
        }
        for (Modification mod:longModListAlpha){
            ArrayList<Modification> currentModList = new ArrayList<>();
            currentModList.add(mod);
            //first, adjustPeptide and sumFormula of peptide
            Peptide modPeptide = this.peptideAlpha;
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i > z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.alphaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideAlpha.getSequence(),
                        "alpha", "long", z, modPeptideFormula, this.xlPosAlpha));
            }
            currentModList.clear();
        }
        for (Modification mod:shortModListBeta){
            ArrayList<Modification> currentModList = new ArrayList<>();
            currentModList.add(mod);
            //first, adjustPeptide and sumFormula of peptide
            Peptide modPeptide = this.peptideBeta;
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i > z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.betaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideBeta.getSequence(),
                        "beta", "short", z, modPeptideFormula, this.xlPosBeta));
            }
            currentModList.clear();
        }
        for (Modification mod:longModListBeta){
            ArrayList<Modification> currentModList = new ArrayList<>();
            currentModList.add(mod);
            //first, adjustPeptide and sumFormula of peptide
            Peptide modPeptide = this.peptideBeta;
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i > z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.betaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideBeta.getSequence(),
                        "beta", "long", z, modPeptideFormula, this.xlPosBeta));
            }
            currentModList.clear();
        }
    }

    //this function matches the theoretical fragments to the spectrum
    //specificXLIonMatch needs: matched peak, matched fragment Ion and ppm Dev
    public void xlIonMatcher (MySpectrum spectrumIn, double ppmDevAllowed){
        ArrayList<SpecificXLIonMatch> matchList = new ArrayList<>();
        //MySpectrum to check was already created by CSVReader class
        //loop through the lists of specific fragment ions and check if they match the spectrum
        //also keep in mind the charge state of the fragment ion and the peak
        //for now, also allow undetermined charge states (=0)
        for(XlFragmentIon alphaFragment : this.alphaXLFragments){
            boolean matchFound = false;
            while (!matchFound){
                for(Peak toCheck : spectrumIn.getPeakList()){
                    if(DeviationCalc.massAndChargeMatch(alphaFragment.getMToZ(), alphaFragment.getCharge(), toCheck, ppmDevAllowed)){
                        matchList.add(new SpecificXLIonMatch(toCheck, alphaFragment));
                        matchFound = true;
                        break;
                    }
                }
            }
        }
        for(XlFragmentIon betaFragment : this.betaXLFragments){
            boolean matchFound = false;
            while (!matchFound){
                for(Peak toCheck : spectrumIn.getPeakList()){
                    if(DeviationCalc.massAndChargeMatch(betaFragment.getMToZ(), betaFragment.getCharge(), toCheck, ppmDevAllowed)){
                        matchList.add(new SpecificXLIonMatch(toCheck, betaFragment));
                        matchFound = true;
                        break;
                    }
                }
            }
        }
        this.xlIonMatches = matchList;
    }


    public int getXlPosAlpha() {
        return xlPosAlpha;
    }

    public int getXlPosBeta() {
        return xlPosBeta;
    }

    public ArrayList<XlFragmentIon> getAlphaXLFragments() {
        return alphaXLFragments;
    }

    public ArrayList<XlFragmentIon> getBetaXLFragments() {
        return betaXLFragments;
    }

    public Peptide getPeptideAlpha() {
        return peptideAlpha;
    }

    public Peptide getPeptideBeta() {
        return peptideBeta;
    }

    public String getXlUsed() {
        return xlUsed;
    }

    public SumFormula getXlSumFormula() {
        return xlSumFormula;
    }

    public Ion getTheoreticalXLIon() {
        return theoreticalXLIon;
    }

    public double getIsolatedMassToCharge() {
        return isolatedMassToCharge;
    }

    public int getScanNumber() {
        return scanNumber;
    }

    public String getFragmentationMethod() {
        return fragmentationMethod;
    }

    public boolean isMonoisotopicSelected() {
        return monoisotopicSelected;
    }

    public int getMonoisotopicPeakOffset() {
        return monoisotopicPeakOffset;
    }

    public double getRetentionTime() {
        return retentionTime;
    }

    public ArrayList<SpecificXLIonMatch> getXlIonMatches() {
        return xlIonMatches;
    }

    public boolean getAlphaEqualsBeta() {
        return alphaEqualsBeta;
    }
}
