import java.text.DecimalFormat;
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
    //unfortunately, isolated mass to charge is shifted to the assumed monoisotopic peak by the MS
    //note: this might differ with the triceratops reported mass, might be the actually isolated one
    //note: in addition, with the triceratops version, the filter line is available to give the isolated mass
    //note: testing showed that triceratops actually gives the isolated mass, while MSConvert gives the monoisotopic one
    //note: because triceratops is working and has more information, use that
    //note: monoisotopic peak of XL can be assigned from feature detection in MS1 and from theoretical ion
    private double isolatedMassToCharge;
    private int hcdScanNumber;
    private int cidScanNumber;
    private String fragmentationMethod;
    //note:this is calculated from the MeroX mass of the XL
    private boolean monoisotopicSelected;
    private int monoisotopicPeakOffset;
    private double retentionTime; //the retention time of the XL in seconds
    private boolean alphaEqualsBeta; //for unwanted skewing of the data
    //doing so will associate all the matches automatically with the respective XL
    private ArrayList<SpecificXLIonMatch> xlIonMatches;

    //TODO: start to make everything ready for using DSSO as well
    public Xl(String peptide1In, String peptide2In, String xlIn, int chargeStateIn,
              int hcdScanNumberIn,int cidScanNumberIn, String fragmentationMethodIn, double isolatedMassToChargeIn,
              int xlPos1In, int xlPos2In, ArrayList<AminoAcid> aaIn, double retentionTimeIn){

        //only cliXlink is supported atm
        if (!xlIn.equals("cliXlink") && !xlIn.equals("DSSO"))
            throw new IllegalArgumentException("Unknown crosslinker: "+ xlIn);
        //easy to set variables
        this.xlUsed = xlIn;
        this.hcdScanNumber = hcdScanNumberIn;
        this.cidScanNumber = cidScanNumberIn;
        this.fragmentationMethod = fragmentationMethodIn;
        this.isolatedMassToCharge = isolatedMassToChargeIn;

        //utilize the xlPeptideModification function to prepare the peptides with their modifications from merox
        if (peptide1In.length() > peptide2In.length()){
            //xlPeptideModification is same for cliXlink and DSSO
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
        //note: why is this here?
        //note: this is useless :D
        //this.xlSumFormula = SumFormula.sumFormulaJoiner(peptideAlpha.getSumFormula(),
        //        SumFormula.sumFormulaJoiner(peptideBeta.getSumFormula(), SumFormula.getCliXlinkFormula()));

        SumFormula joinedPeptidesFormula = SumFormula.sumFormulaJoiner(peptideAlpha.getSumFormula(), peptideBeta.getSumFormula());
        if(xlIn.equals("cliXlink")) {
            this.xlSumFormula = SumFormula.sumFormulaJoiner(joinedPeptidesFormula, SumFormula.getCliXlinkFormula());
        }
        //handles DSSO
        else {
            this.xlSumFormula = SumFormula.sumFormulaJoiner(joinedPeptidesFormula, SumFormula.getDSSOFormula());
        }
        //add protons according to charge state
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
        this.alphaXLFragments = new ArrayList<>();
        this.betaXLFragments = new ArrayList<>();
        if(xlIn.equals("cliXlink"))
        cliXLinkFragIonCreator();
        else
            dssoFragIonCreator();

    }

    //this functions deals with adding the static modifications and the optional Methionine-Oxidation
    //oxidation is specified by small letter 'm'
    //note: works for cliXlink and DSSO, handles other mods
    //note: only includes MeroX modifications
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
            for (int i = 0; i < sequenceIn.length(); i++){
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
    private void cliXLinkFragIonCreator(){
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
            //for peptideModifier to work, peptide can not already be modified!!!
            //fixed in the peptideModifier function, peptide can now be already modified
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            //TODO: Protons are not added to the sum formula
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i < z+1; i++){
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
                for (int i = 1; i < z+1; i++){
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
                for (int i = 1; i < z+1; i++){
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
                for (int i = 1; i < z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.betaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideBeta.getSequence(),
                        "beta", "long", z, modPeptideFormula, this.xlPosBeta));
            }
            currentModList.clear();
        }
    }
    //same class for DSSO
    private void dssoFragIonCreator(){
        //create all possible fragments: all charge states up to XL-charge state -1
        //for each charge state, 3 possible fragments: alkene, SO and thial
        ArrayList<Modification> modListAlpha = Modification.dssoModList(this.xlPosAlpha);
        ArrayList<Modification> modListBeta = Modification.dssoModList(this.xlPosBeta);
        //loop through all the modifications for alpha
        for (Modification mod:modListAlpha){
            ArrayList<Modification> currentModList = new ArrayList<>();
            currentModList.add(mod);
            //first, adjustPeptide and sumFormula of peptide
            Peptide modPeptide = this.peptideAlpha;
            //for peptideModifier to work, peptide can not already be modified!!!
            //fixed in the peptideModifier function, peptide can now be already modified
            modPeptide= modPeptide.peptideModifier(currentModList);
            //loop through all the different charge states
            //Protons are not added to the sum formula, have to take care of that
            for (int z = 1; z < this.theoreticalXLIon.getCharge(); z++){
                SumFormula modPeptideFormula = modPeptide.getSumFormula();
                //to this formula, the protons have to be added
                //add the proton formula as often as it is charged
                for (int i = 1; i < z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.alphaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideAlpha.getSequence(),
                        "alpha", "dsso", z, modPeptideFormula, this.xlPosAlpha));
            }
            currentModList.clear();
        }

        for (Modification mod:modListBeta){
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
                for (int i = 1; i < z+1; i++){
                    modPeptideFormula = SumFormula.sumFormulaJoiner(modPeptideFormula, SumFormula.getProtonFormula());
                }
                //now, create a xlfragmention and add it to the list of alpha-fragment ions
                this.betaXLFragments.add(new XlFragmentIon(modPeptide, this.peptideBeta.getSequence(),
                        "beta", "dsso", z, modPeptideFormula, this.xlPosBeta));
            }
            currentModList.clear();
        }
    }

    //this function matches the theoretical fragments to the spectrum
    //specificXLIonMatch needs: matched peak, matched fragment Ion and ppm Dev
    //note: works for both cliXlink and DSSO
    public void xlIonMatcher (MySpectrum spectrumIn, double ppmDevAllowed){
        //make sure that charge states and features were assigned previously
        if (!spectrumIn.areZAndFeaturesAssigned())
            spectrumIn.assignZAndFeatures(ppmDevAllowed);

        ArrayList<SpecificXLIonMatch> matchList = new ArrayList<>();
        //MySpectrum to check was already created by CSVReader class
        //loop through the lists of specific fragment ions and check if they match the spectrum
        //also keep in mind the charge state of the fragment ion and the peak
        //for now, also allow undetermined charge states (=0)
        //could now also be done by MySpectrum.getMatchingPeak, but this handling is also fine
        for(XlFragmentIon alphaFragment : this.alphaXLFragments){
                for(Peak toCheck : spectrumIn.getPeakList()){
                    if(DeviationCalc.massAndChargeMatch(alphaFragment.getMToZ(), alphaFragment.getCharge(), toCheck, ppmDevAllowed)){
                        matchList.add(new SpecificXLIonMatch(toCheck, alphaFragment));
                        break;
                }
            }
        }
        for(XlFragmentIon betaFragment : this.betaXLFragments){
                for(Peak toCheck : spectrumIn.getPeakList()){
                    if(DeviationCalc.massAndChargeMatch(betaFragment.getMToZ(), betaFragment.getCharge(), toCheck, ppmDevAllowed)){
                        matchList.add(new SpecificXLIonMatch(toCheck, betaFragment));
                        break;
                }
            }
        }
        this.xlIonMatches = matchList;
        //note: changed this to sort after Feature intensity instead of peak intensity
        QuickSort.xlIonMatchesQuickSort(this.xlIonMatches);
    }
    //comma is the used separator for the .csv file
    //note: this is the cliXlinkProducer
    public String cliXlinkMatchesStringProducer(MySpectrum triggeringFullScanIn, MySpectrum closestFullScanIn, MySpectrum hcdScanIn, MySpectrum cidScanIn, double ppmDevIn){
        DecimalFormat twoDec = new DecimalFormat("0.00");
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat scientific = new DecimalFormat("0.00E0");
        String output = "";
        StringBuilder sb = new StringBuilder();

        //pep alpha, pep beta, alpha amino acid, beta amino acid, alpha pos, beta pos
        sb.append(this.getPeptideAlpha().getSequence()).append(",");
        sb.append((this.getPeptideBeta().getSequence())).append(",");

        //to figure out mod. amino acid, go to position in sequence.
        String alphaAA = Character.toString(this.getPeptideAlpha().getUnmodifiedSequence().charAt(this.xlPosAlpha-1));
        sb.append(alphaAA).append(",");
        String betaAA = Character.toString(this.getPeptideBeta().getUnmodifiedSequence().charAt(this.xlPosBeta-1));
        sb.append(betaAA).append(",");
        sb.append(this.xlPosAlpha).append(",");
        sb.append(this.xlPosBeta).append(",");

        //set HCD and CID scan Numbers
        sb.append(""+this.hcdScanNumber).append(",");
        sb.append(""+this.cidScanNumber).append(",");

        //information about the theoretical xl precursor
        sb.append(fourDec.format(this.theoreticalXLIon.getMToZ())).append(",");;
        sb.append(""+this.theoreticalXLIon.getCharge()).append(",");
        //note: this is the real isolated mass to charge now with the triceratops mzxml
        sb.append(fourDec.format(this.isolatedMassToCharge)).append(",");
        sb.append(triggeringFullScanIn.getScanNumber()).append(",");
        //find precursor peak from triggering spectrum
        //note: for this, the real peak intensity matters...this is what is triggered upon after all
        //the mass queried is the isolated mass to charge ratio
        Peak triggeredPrecPeak = triggeringFullScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);

        //what happens if triggering peak isn't detected...shouldn't happen, but error handling
        if (triggeredPrecPeak == null){
            triggeredPrecPeak = new Peak(this.theoreticalXLIon.getExactMass(), 0, 1, 1);
        }

        //for the ppmDev, compare isolated m/z with the calculated m/z having the correct isotopic offset!
        double shiftedCalculatedMass = (this.theoreticalXLIon.getExactMass() + AtomicMasses.getNEUTRON() * this.monoisotopicPeakOffset) / this.theoreticalXLIon.getCharge();
        double detectedPPMDev = DeviationCalc.ppmDeviationCalc(shiftedCalculatedMass, triggeredPrecPeak.getMass());
        sb.append(twoDec.format(detectedPPMDev)).append(",");
        sb.append(""+this.monoisotopicPeakOffset).append(",");
        //check full scan spectrum for the precursor information

        sb.append(twoDec.format(triggeredPrecPeak.getRelIntensity())).append(",");
        sb.append(scientific.format(triggeredPrecPeak.getIntensity())).append(",");
        //information about the precursor in the MS1 directly previous to triggering
        //again, just the peak information is needed, not the feature intensity...
        //TODO: take into account the isolation window width?
        sb.append(closestFullScanIn.getScanNumber()).append(",");
        Peak closestPrecPeak = closestFullScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);
        //same error handling as with triggered precursor peak
        if(closestPrecPeak == null){
            closestPrecPeak = new Peak(this.theoreticalXLIon.getExactMass(), 0, 1, 1);
        }

        sb.append(twoDec.format(closestPrecPeak.getRelIntensity())).append(",");
        sb.append(scientific.format(closestPrecPeak.getIntensity())).append(",");

        //get information about the surviving precursor in the MS2 CID spectrum
        Peak survivingPrecursor = cidScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);
        if(survivingPrecursor == null){
            survivingPrecursor = new Peak(this.isolatedMassToCharge, 0, 1, 1);
        }
        sb.append(twoDec.format(survivingPrecursor.getFeatureRelIntPerTIC())).append(",");
        sb.append(scientific.format(survivingPrecursor.getFeatureIntensity())).append(",");

        //information about the specific signature peaks
        //all the matches are contained in the xlIonMatches list
        //split them up into individual lists

        ArrayList<SpecificXLIonMatch> alphaAlkShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaSOShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaThialShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaAlkLongMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaSOLongMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaThialLongMatches = new ArrayList<>();

        ArrayList<SpecificXLIonMatch> betaAlkShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaSOShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaThialShortMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaAlkLongMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaSOLongMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaThialLongMatches = new ArrayList<>();

        for (SpecificXLIonMatch match : this.xlIonMatches){
            if (match.getMatchedFragIon().getPepType().equals("alpha")){
                if (match.getModSize().equals("short")) {
                    if (match.getModType().equals("alk")) {
                        alphaAlkShortMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        alphaThialShortMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        alphaSOShortMatches.add(match);
                        continue;
                    }
                }
                if (match.getModSize().equals("long")) {
                    if (match.getModType().equals("alk")) {
                        alphaAlkLongMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        alphaThialLongMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        alphaSOLongMatches.add(match);
                        continue;
                    }
                }
            }
            if (match.getMatchedFragIon().getPepType().equals("beta")){
                if (match.getModSize().equals("short")) {
                    if (match.getModType().equals("alk")) {
                        betaAlkShortMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        betaThialShortMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        betaSOShortMatches.add(match);
                        continue;
                    }
                }
                if (match.getModSize().equals("long")) {
                    if (match.getModType().equals("alk")) {
                        betaAlkLongMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        betaThialLongMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        betaSOLongMatches.add(match);
                    }
                }
                }

        }
        //ordered lists are now populated

        //determine number of signature peaks detected; 0-6 for alpha, beta and combined
        //signature peaks summed abs intensity and in relation to MS2 TIC
        int numberOfSignaturePeaksAlpha = 0;
        double absIntSignaturePeaksAlpha = 0;
        int numberOfSignaturePeaksBeta = 0;
        double absIntSignaturePeaksBeta = 0;
        int numberOfSignaturePeaksTotal = 0;
        double absIntSignaturePeaksTotal = 0;
        ArrayList<ArrayList<SpecificXLIonMatch>> matchedListsAlpha = new ArrayList<>();
        ArrayList<ArrayList<SpecificXLIonMatch>> matchedListsBeta = new ArrayList<>();

        matchedListsAlpha.add(alphaAlkShortMatches);
        matchedListsAlpha.add(alphaThialShortMatches);
        matchedListsAlpha.add(alphaSOShortMatches);
        matchedListsAlpha.add(alphaAlkLongMatches);
        matchedListsAlpha.add(alphaThialLongMatches);
        matchedListsAlpha.add(alphaSOLongMatches);

        matchedListsBeta.add(betaAlkShortMatches);
        matchedListsBeta.add(betaThialShortMatches);
        matchedListsBeta.add(betaSOShortMatches);
        matchedListsBeta.add(betaAlkLongMatches);
        matchedListsBeta.add(betaThialLongMatches);
        matchedListsBeta.add(betaSOLongMatches);


        //note: feature intensities

        for (ArrayList<SpecificXLIonMatch> individualMatchList : matchedListsAlpha){
            if (individualMatchList.size() != 0) {
                numberOfSignaturePeaksAlpha++;
                for (SpecificXLIonMatch matchedIon : individualMatchList){
                    absIntSignaturePeaksAlpha += matchedIon.getMatchedPeak().getFeatureIntensity();
                }
            }
        }
        for (ArrayList<SpecificXLIonMatch> individualMatchList : matchedListsBeta){
            if (individualMatchList.size() != 0) {
                numberOfSignaturePeaksBeta++;
                for (SpecificXLIonMatch matchedIon : individualMatchList){
                    absIntSignaturePeaksBeta += matchedIon.getMatchedPeak().getFeatureIntensity();
                }
            }
        }
        numberOfSignaturePeaksTotal = numberOfSignaturePeaksAlpha + numberOfSignaturePeaksBeta;
        absIntSignaturePeaksTotal = absIntSignaturePeaksAlpha + absIntSignaturePeaksBeta;

        //figure out total TIC intensity in respective MS2 CID scan
        double cidTIC = cidScanIn.getSpectrumTIC();
        double ratioAlpha = absIntSignaturePeaksAlpha / cidTIC * 100;
        double ratioBeta =  absIntSignaturePeaksBeta / cidTIC * 100;
        double ratioTotal = absIntSignaturePeaksTotal / cidTIC * 100;

        sb.append(numberOfSignaturePeaksAlpha).append(",");
        sb.append(scientific.format(absIntSignaturePeaksAlpha)).append(",");
        sb.append(twoDec.format(ratioAlpha)).append(",");

        sb.append(numberOfSignaturePeaksBeta).append(",");
        sb.append(scientific.format(absIntSignaturePeaksBeta)).append(",");
        sb.append(twoDec.format(ratioBeta)).append(",");

        sb.append(numberOfSignaturePeaksTotal).append(",");
        sb.append(scientific.format(absIntSignaturePeaksTotal)).append(",");
        sb.append(twoDec.format(ratioTotal)).append(",");


        //figure out charge states detected for alpha and beta and dominant charge states
        //charge state information is stored in XLFragmentIon
        //to determine which charge state is the dominant one, use the one with the highest abs. intensity
        //possible: 1 through charge state of xl
        int maximumChargeState = this.theoreticalXLIon.getCharge();
        double [] alphaChargeStateIntensities = new double[maximumChargeState];
        double [] betaChargeStateIntensities = new double[maximumChargeState];

        ArrayList<SpecificXLIonMatch> alphaMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaMatches = new ArrayList<>();

        alphaMatches.addAll(alphaAlkShortMatches);
        alphaMatches.addAll(alphaThialShortMatches);
        alphaMatches.addAll(alphaSOShortMatches);
        alphaMatches.addAll(alphaAlkLongMatches);
        alphaMatches.addAll(alphaThialLongMatches);
        alphaMatches.addAll(alphaSOLongMatches);

        betaMatches.addAll(betaAlkShortMatches);
        betaMatches.addAll(betaThialShortMatches);
        betaMatches.addAll(betaSOShortMatches);
        betaMatches.addAll(betaAlkLongMatches);
        betaMatches.addAll(betaThialLongMatches);
        betaMatches.addAll(betaSOLongMatches);

        //note feature intensities
        for (SpecificXLIonMatch alphaMatch : alphaMatches){
            int currentCharge = alphaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = alphaMatch.getMatchedPeak().getFeatureIntensity();
            //should work since array is initialized to 0.0
            alphaChargeStateIntensities[currentCharge-1] += currentAbsIntensity;
        }
        for (SpecificXLIonMatch betaMatch : betaMatches){
            int currentCharge = betaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = betaMatch.getMatchedPeak().getFeatureIntensity();
            //should work since array is initialized to 0.0
            betaChargeStateIntensities[currentCharge-1] += currentAbsIntensity;
        }
        //alpha and beta charge states are now set as arrays with intensities; if intensity is 0, charge state was not detected
        String alphaZdetected = "";
        String betaZdetected = "";
        int alphaDominantZ = 0;
        int betaDominantZ = 0;
        double alphaMaxIntensity = 0;
        double betaMaxIntensity = 0;
        //this loop can handle both arrays
        //zSep is used to add a semicolon after an entry
        String zSepAlpha = "";
        String zSepBeta = "";
        for (int i = 0; i < maximumChargeState; i++){
            double alphaCurrentIntensity = alphaChargeStateIntensities[i];
            double betaCurrentIntensity = betaChargeStateIntensities[i];
            if (alphaCurrentIntensity != 0){
                alphaZdetected = alphaZdetected + zSepAlpha + (i+1);
                zSepAlpha = ";";
                if (alphaCurrentIntensity>alphaMaxIntensity){
                    alphaMaxIntensity = alphaCurrentIntensity;
                    alphaDominantZ = (i+1);
                }
            }
            if (betaCurrentIntensity != 0){
                betaZdetected = betaZdetected + zSepBeta + (i+1);
                zSepBeta = ";";
                if (betaCurrentIntensity>betaMaxIntensity){
                    betaMaxIntensity = betaCurrentIntensity;
                    betaDominantZ = (i+1);
                }
            }
        }
        //handle if nothing is detected
        if (alphaZdetected.equals(""))
            alphaZdetected = "0";
        if (betaZdetected.equals(""))
            betaZdetected = "0";


        sb.append(alphaZdetected).append(",");
        sb.append(betaZdetected).append(",");
        sb.append(alphaDominantZ).append(",");
        sb.append(betaDominantZ).append(",");

        //sides detected: short, long or both and dominant side
        //dominant side is chosen with abs Int again
        //note: feature intensity
        double alphaShortAbsInt = 0;
        double alphaLongAbsInt = 0;
        double betaShortAbsInt = 0;
        double betaLongAbsInt = 0;
        for (SpecificXLIonMatch alphaMatch : alphaMatches){
            String currentAlpha = alphaMatch.getMatchedFragIon().getSize();
            if (currentAlpha.equals("short"))
                alphaShortAbsInt += alphaMatch.getMatchedPeak().getFeatureIntensity();
            if (currentAlpha.equals("long"))
                alphaLongAbsInt += alphaMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaMatch : betaMatches){
            String currentBeta = betaMatch.getMatchedFragIon().getSize();
            if (currentBeta.equals("short"))
                betaShortAbsInt += betaMatch.getMatchedPeak().getFeatureIntensity();
            if (currentBeta.equals("long"))
                betaLongAbsInt += betaMatch.getMatchedPeak().getFeatureIntensity();
        }

        String alphaSideDetected = "false";
        if (alphaShortAbsInt != 0)
            alphaSideDetected = "short";
        if (alphaLongAbsInt != 0)
            alphaSideDetected = "long";
        if (alphaShortAbsInt != 0 && alphaLongAbsInt !=0)
            alphaSideDetected = "both";

        String betaSideDetected = "false";
        if (betaShortAbsInt != 0)
            betaSideDetected = "short";
        if (betaLongAbsInt != 0)
            betaSideDetected = "long";
        if (betaShortAbsInt != 0 && betaLongAbsInt !=0)
            betaSideDetected = "both";


        String alphaDominantSide = "false";
        String betaDominantSide = "false";
        if (alphaShortAbsInt > alphaLongAbsInt)
            alphaDominantSide = "short";
        else if (alphaShortAbsInt < alphaLongAbsInt)
            alphaDominantSide = "long";

        if (betaShortAbsInt > betaLongAbsInt)
            betaDominantSide = "short";
        else if (betaShortAbsInt < betaLongAbsInt)
            betaDominantSide = "long";

        sb.append(alphaSideDetected).append(",");
        sb.append(betaSideDetected).append(",");
        sb.append(alphaDominantSide).append(",");
        sb.append(betaDominantSide).append(",");

        //handle in the same way as the sides
        String alphaModsDetected = "";
        String alphaDominantMod = "";
        String betaModsDetected = "";
        String betaDominantMod = "";
        double alphaAlkAbsIntensities = 0;
        double alphaThialAbsIntensities = 0;
        double alphaSOAbsIntensities = 0;
        double betaAlkAbsIntensities = 0;
        double betaThialAbsIntensities = 0;
        double betaSOAbsIntensities = 0;

        //all fragments in this list are already sorted, so just sum intensities

        //note: feature intensity
        for (SpecificXLIonMatch alphaAlkMatch : alphaAlkShortMatches){
            alphaAlkAbsIntensities += alphaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaThialMatch : alphaThialShortMatches){
            alphaThialAbsIntensities += alphaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaSOMatch : alphaSOShortMatches){
            alphaSOAbsIntensities += alphaSOMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaAlkMatch : alphaAlkLongMatches){
            alphaAlkAbsIntensities += alphaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaThialMatch : alphaThialLongMatches){
            alphaThialAbsIntensities += alphaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaSOMatch : alphaSOLongMatches){
            alphaSOAbsIntensities += alphaSOMatch.getMatchedPeak().getFeatureIntensity();
        }

        for (SpecificXLIonMatch betaAlkMatch : betaAlkShortMatches){
            betaAlkAbsIntensities += betaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaThialMatch : betaThialShortMatches){
            betaThialAbsIntensities += betaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaSOMatch : betaSOShortMatches){
            betaSOAbsIntensities += betaSOMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaAlkMatch : betaAlkLongMatches){
            betaAlkAbsIntensities += betaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaThialMatch : betaThialLongMatches){
            betaThialAbsIntensities += betaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaSOMatch : betaSOLongMatches){
            betaSOAbsIntensities += betaSOMatch.getMatchedPeak().getFeatureIntensity();
        }

        String alphaModSep = "";
        if (alphaAlkAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + "alk";
            alphaModSep = ";";
        }
        if (alphaThialAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + alphaModSep + "thial";
            alphaModSep = ";";
        }
        if (alphaSOAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + alphaModSep + "SO";
        }
        if (alphaAlkAbsIntensities != 0 && alphaThialAbsIntensities != 0 && alphaSOAbsIntensities != 0)
            alphaModsDetected = "all";

        String betaModSep = "";
        if (betaAlkAbsIntensities != 0){
            betaModsDetected = betaModsDetected + "alk";
            betaModSep = ";";
        }
        if (betaThialAbsIntensities != 0){
            betaModsDetected = betaModsDetected + betaModSep + "thial";
            betaModSep = ";";
        }
        if (betaSOAbsIntensities != 0){
            betaModsDetected = betaModsDetected + betaModSep + "SO";
        }
        if (betaAlkAbsIntensities != 0 && betaThialAbsIntensities != 0 && betaSOAbsIntensities != 0)
            betaModsDetected = "all";

        //determine dominant peak
        double currentAlphaModHighestInt = 0;
        double currentBetaModHighestInt = 0;


        if (alphaAlkAbsIntensities > currentAlphaModHighestInt){
            currentAlphaModHighestInt = alphaAlkAbsIntensities;
            alphaDominantMod = "alk";
        }

        if (alphaThialAbsIntensities > currentAlphaModHighestInt){
            currentAlphaModHighestInt = alphaThialAbsIntensities;
            alphaDominantMod = "thial";
        }

        if (alphaSOAbsIntensities > currentAlphaModHighestInt){
            alphaDominantMod = "SO";
        }

        if (betaAlkAbsIntensities > currentBetaModHighestInt){
            currentBetaModHighestInt = betaAlkAbsIntensities;
            betaDominantMod = "alk";
        }

        if (betaThialAbsIntensities > currentBetaModHighestInt){
            currentBetaModHighestInt = betaThialAbsIntensities;
            betaDominantMod = "thial";
        }

        if (betaSOAbsIntensities > currentBetaModHighestInt){
            betaDominantMod = "SO";
        }

        //handle if all intensities of fragments are 0
        if (alphaAlkAbsIntensities == 0 && alphaThialAbsIntensities == 0 && alphaSOAbsIntensities == 0){
            alphaModsDetected = "false";
            alphaDominantMod = "false";
        }
        if (betaAlkAbsIntensities == 0 && betaThialAbsIntensities == 0 && betaSOAbsIntensities == 0){
            betaModsDetected = "false";
            betaDominantMod = "false";
        }
        sb.append(alphaModsDetected).append(",");
        sb.append(betaModsDetected).append(",");
        sb.append(alphaDominantMod).append(",");
        sb.append(betaDominantMod).append(",");

        //now, information about the detected fragments individually
        boolean aAlkShort = false;
        boolean aThialShort = false;
        boolean aSOShort = false;
        boolean aAlkLong = false;
        boolean aThialLong = false;
        boolean aSOLong = false;

        boolean bAlkShort = false;
        boolean bThialShort = false;
        boolean bSOShort = false;
        boolean bAlkLong = false;
        boolean bThialLong = false;
        boolean bSOLong = false;

        double aAlkShortRelInt = 0;
        double aThialShortRelInt = 0;
        double aSOShortRelInt = 0;
        double aAlkLongRelInt = 0;
        double aThialLongRelInt = 0;
        double aSOLongRelInt = 0;

        double bAlkShortRelInt = 0;
        double bThialShortRelInt = 0;
        double bSOShortRelInt = 0;
        double bAlkLongRelInt = 0;
        double bThialLongRelInt = 0;
        double bSOLongRelInt = 0;

        double aAlkShortAbsInt = 0;
        double aThialShortAbsInt = 0;
        double aSOShortAbsInt = 0;
        double aAlkLongAbsInt = 0;
        double aThialLongAbsInt = 0;
        double aSOLongAbsInt = 0;

        double bAlkShortAbsInt = 0;
        double bThialShortAbsInt = 0;
        double bSOShortAbsInt = 0;
        double bAlkLongAbsInt = 0;
        double bThialLongAbsInt = 0;
        double bSOLongAbsInt = 0;

        //note: use feature intensities for that

        for (SpecificXLIonMatch match : alphaAlkShortMatches){
            aAlkShort = true;
            aAlkShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aAlkShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaThialShortMatches){
            aThialShort = true;
            aThialShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aThialShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaSOShortMatches){
            aSOShort = true;
            aSOShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aSOShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaAlkLongMatches){
            aAlkLong = true;
            aAlkLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aAlkLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaThialLongMatches){
            aThialLong = true;
            aThialLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aThialLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaSOLongMatches){
            aSOLong = true;
            aSOLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aSOLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }


        for (SpecificXLIonMatch match : betaAlkShortMatches){
            bAlkShort = true;
            bAlkShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bAlkShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaThialShortMatches){
            bThialShort = true;
            bThialShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bThialShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaSOShortMatches){
            bSOShort = true;
            bSOShortAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bSOShortRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaAlkLongMatches){
            bAlkLong = true;
            bAlkLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bAlkLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaThialLongMatches){
            bThialLong = true;
            bThialLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bThialLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaSOLongMatches){
            bSOLong = true;
            bSOLongAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bSOLongRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }


        sb.append(aAlkShort).append(",");
        sb.append(aThialShort).append(",");
        sb.append(aSOShort).append(",");
        sb.append(aAlkLong).append(",");
        sb.append(aThialLong).append(",");
        sb.append(aSOLong).append(",");

        sb.append(bAlkShort).append(",");
        sb.append(bThialShort).append(",");
        sb.append(bSOShort).append(",");
        sb.append(bAlkLong).append(",");
        sb.append(bThialLong).append(",");
        sb.append(bSOLong).append(",");

        sb.append(twoDec.format(aAlkShortRelInt)).append(",");
        sb.append(twoDec.format(aThialShortRelInt)).append(",");
        sb.append(twoDec.format(aSOShortRelInt)).append(",");
        sb.append(twoDec.format(aAlkLongRelInt)).append(",");
        sb.append(twoDec.format(aThialLongRelInt)).append(",");
        sb.append(twoDec.format(aSOLongRelInt)).append(",");

        sb.append(twoDec.format(bAlkShortRelInt)).append(",");
        sb.append(twoDec.format(bThialShortRelInt)).append(",");
        sb.append(twoDec.format(bSOShortRelInt)).append(",");
        sb.append(twoDec.format(bAlkLongRelInt)).append(",");
        sb.append(twoDec.format(bThialLongRelInt)).append(",");
        sb.append(twoDec.format(bSOLongRelInt)).append(",");

        sb.append(scientific.format(aAlkShortAbsInt)).append(",");
        sb.append(scientific.format(aThialShortAbsInt)).append(",");
        sb.append(scientific.format(aSOShortAbsInt)).append(",");
        sb.append(scientific.format(aAlkLongAbsInt)).append(",");
        sb.append(scientific.format(aThialLongAbsInt)).append(",");
        sb.append(scientific.format(aSOLongAbsInt)).append(",");

        sb.append(scientific.format(bAlkShortAbsInt)).append(",");
        sb.append(scientific.format(bThialShortAbsInt)).append(",");
        sb.append(scientific.format(bSOShortAbsInt)).append(",");
        sb.append(scientific.format(bAlkLongAbsInt)).append(",");
        sb.append(scientific.format(bThialLongAbsInt)).append(",");
        sb.append(scientific.format(bSOLongAbsInt)).append(",");

        //information about 4 most intense signature peaks
        //list of specific XL ion matches is already ordered, so take first 4 entries
        int currentMaxPeak = 0;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getSize()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getSize()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getSize()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getSize()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        //number of peaks with 31.9715 Da mass difference
        int numberOfTriggerDistances = cidScanIn.getNumberofPeaksWithSpecificMassDifference(31.9715, ppmDevIn).size();
        sb.append(numberOfTriggerDistances).append(",");
        //include next line command
        sb.append("\n");
        //sb should be completely ready now
        output = sb.toString();
        return output;
    }

    //TODO: adjust for DSSO
    public String dssoMatchesStringProducer(MySpectrum triggeringFullScanIn, MySpectrum closestFullScanIn, MySpectrum hcdScanIn, MySpectrum cidScanIn, double ppmDevIn){
        DecimalFormat twoDec = new DecimalFormat("0.00");
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat scientific = new DecimalFormat("0.00E0");
        String output = "";
        StringBuilder sb = new StringBuilder();

        //pep alpha, pep beta, alpha amino acid, beta amino acid, alpha pos, beta pos
        sb.append(this.getPeptideAlpha().getSequence()).append(",");
        sb.append((this.getPeptideBeta().getSequence())).append(",");

        //to figure out mod. amino acid, go to position in sequence.
        String alphaAA = Character.toString(this.getPeptideAlpha().getUnmodifiedSequence().charAt(this.xlPosAlpha-1));
        sb.append(alphaAA).append(",");
        String betaAA = Character.toString(this.getPeptideBeta().getUnmodifiedSequence().charAt(this.xlPosBeta-1));
        sb.append(betaAA).append(",");
        sb.append(this.xlPosAlpha).append(",");
        sb.append(this.xlPosBeta).append(",");

        //set HCD and CID scan Numbers
        sb.append(""+this.hcdScanNumber).append(",");
        sb.append(""+this.cidScanNumber).append(",");

        //information about the theoretical xl precursor
        sb.append(fourDec.format(this.theoreticalXLIon.getMToZ())).append(",");;
        sb.append(""+this.theoreticalXLIon.getCharge()).append(",");
        //note: this is the real isolated mass to charge now with the triceratops mzxml
        sb.append(fourDec.format(this.isolatedMassToCharge)).append(",");
        sb.append(triggeringFullScanIn.getScanNumber()).append(",");
        //find precursor peak from triggering spectrum
        //note: for this, the real peak intensity matters...this is what is triggered upon after all
        //the mass queried is the isolated mass to charge ratio
        Peak triggeredPrecPeak = triggeringFullScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);

        //what happens if triggering peak isn't detected...shouldn't happen, but error handling
        if (triggeredPrecPeak == null){
            triggeredPrecPeak = new Peak(this.theoreticalXLIon.getExactMass(), 0, 1, 1);
        }

        //for the ppmDev, compare isolated m/z with the calculated m/z having the correct isotopic offset!
        double shiftedCalculatedMass = (this.theoreticalXLIon.getExactMass() + AtomicMasses.getNEUTRON() * this.monoisotopicPeakOffset) / this.theoreticalXLIon.getCharge();
        double detectedPPMDev = DeviationCalc.ppmDeviationCalc(shiftedCalculatedMass, triggeredPrecPeak.getMass());
        sb.append(twoDec.format(detectedPPMDev)).append(",");
        sb.append(""+this.monoisotopicPeakOffset).append(",");
        //check full scan spectrum for the precursor information

        sb.append(twoDec.format(triggeredPrecPeak.getRelIntensity())).append(",");
        sb.append(scientific.format(triggeredPrecPeak.getIntensity())).append(",");
        //information about the precursor in the MS1 directly previous to triggering
        //again, just the peak information is needed, not the feature intensity...
        //TODO: take into account the isolation window width?
        sb.append(closestFullScanIn.getScanNumber()).append(",");
        Peak closestPrecPeak = closestFullScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);
        //same error handling as with triggered precursor peak
        if(closestPrecPeak == null){
            closestPrecPeak = new Peak(this.theoreticalXLIon.getExactMass(), 0, 1, 1);
        }

        sb.append(twoDec.format(closestPrecPeak.getFeatureRelIntPerTIC())).append(",");
        sb.append(scientific.format(closestPrecPeak.getFeatureIntensity())).append(",");

        //get information about the surviving precursor in the MS2 CID spectrum
        Peak survivingPrecursor = cidScanIn.getMatchingPeak(this.isolatedMassToCharge, ppmDevIn);
        if(survivingPrecursor == null){
            survivingPrecursor = new Peak(this.isolatedMassToCharge, 0, 1, 1);
        }
        sb.append(twoDec.format(survivingPrecursor.getRelIntensity())).append(",");
        sb.append(scientific.format(survivingPrecursor.getIntensity())).append(",");


        //information about the specific signature peaks
        //all the matches are contained in the xlIonMatches list
        //split them up into individual lists

        ArrayList<SpecificXLIonMatch> alphaAlkMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaSOMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaThialMatches = new ArrayList<>();


        ArrayList<SpecificXLIonMatch> betaAlkMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaSOMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaThialMatches = new ArrayList<>();


        for (SpecificXLIonMatch match : this.xlIonMatches){
            if (match.getMatchedFragIon().getPepType().equals("alpha")){
                    if (match.getModType().equals("alk")) {
                        alphaAlkMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        alphaThialMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        alphaSOMatches.add(match);
                        continue;
                    }
            }
            if (match.getMatchedFragIon().getPepType().equals("beta")){
                    if (match.getModType().equals("alk")) {
                        betaAlkMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("thial")) {
                        betaThialMatches.add(match);
                        continue;
                    }
                    if (match.getModType().equals("SO")) {
                        betaSOMatches.add(match);
                    }
            }

        }
        //ordered lists are now populated

        //determine number of signature peaks detected; 0-6 for alpha, beta and combined
        //signature peaks summed abs intensity and in relation to MS2 TIC
        int numberOfSignaturePeaksAlpha = 0;
        double absIntSignaturePeaksAlpha = 0;
        int numberOfSignaturePeaksBeta = 0;
        double absIntSignaturePeaksBeta = 0;
        int numberOfSignaturePeaksTotal = 0;
        double absIntSignaturePeaksTotal = 0;
        ArrayList<ArrayList<SpecificXLIonMatch>> matchedListsAlpha = new ArrayList<>();
        ArrayList<ArrayList<SpecificXLIonMatch>> matchedListsBeta = new ArrayList<>();

        matchedListsAlpha.add(alphaAlkMatches);
        matchedListsAlpha.add(alphaThialMatches);
        matchedListsAlpha.add(alphaSOMatches);


        matchedListsBeta.add(betaAlkMatches);
        matchedListsBeta.add(betaThialMatches);
        matchedListsBeta.add(betaSOMatches);



        //note: feature intensities

        for (ArrayList<SpecificXLIonMatch> individualMatchList : matchedListsAlpha){
            if (individualMatchList.size() != 0) {
                numberOfSignaturePeaksAlpha++;
                for (SpecificXLIonMatch matchedIon : individualMatchList){
                    absIntSignaturePeaksAlpha += matchedIon.getMatchedPeak().getFeatureIntensity();
                }
            }
        }
        for (ArrayList<SpecificXLIonMatch> individualMatchList : matchedListsBeta){
            if (individualMatchList.size() != 0) {
                numberOfSignaturePeaksBeta++;
                for (SpecificXLIonMatch matchedIon : individualMatchList){
                    absIntSignaturePeaksBeta += matchedIon.getMatchedPeak().getFeatureIntensity();
                }
            }
        }
        numberOfSignaturePeaksTotal = numberOfSignaturePeaksAlpha + numberOfSignaturePeaksBeta;
        absIntSignaturePeaksTotal = absIntSignaturePeaksAlpha + absIntSignaturePeaksBeta;

        //figure out total TIC intensity in respective MS2 CID scan
        double cidTIC = cidScanIn.getSpectrumTIC();
        double ratioAlpha = absIntSignaturePeaksAlpha / cidTIC * 100;
        double ratioBeta =  absIntSignaturePeaksBeta / cidTIC * 100;
        double ratioTotal = absIntSignaturePeaksTotal / cidTIC * 100;

        sb.append(numberOfSignaturePeaksAlpha).append(",");
        sb.append(scientific.format(absIntSignaturePeaksAlpha)).append(",");
        sb.append(twoDec.format(ratioAlpha)).append(",");

        sb.append(numberOfSignaturePeaksBeta).append(",");
        sb.append(scientific.format(absIntSignaturePeaksBeta)).append(",");
        sb.append(twoDec.format(ratioBeta)).append(",");

        sb.append(numberOfSignaturePeaksTotal).append(",");
        sb.append(scientific.format(absIntSignaturePeaksTotal)).append(",");
        sb.append(twoDec.format(ratioTotal)).append(",");


        //figure out charge states detected for alpha and beta and dominant charge states
        //charge state information is stored in XLFragmentIon
        //to determine which charge state is the dominant one, use the one with the highest abs. intensity
        //possible: 1 through charge state of xl
        int maximumChargeState = this.theoreticalXLIon.getCharge();
        double [] alphaChargeStateIntensities = new double[maximumChargeState];
        double [] betaChargeStateIntensities = new double[maximumChargeState];
        //these lists hold the individual entries, and not the lists themselves
        ArrayList<SpecificXLIonMatch> alphaMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaMatches = new ArrayList<>();

        alphaMatches.addAll(alphaAlkMatches);
        alphaMatches.addAll(alphaThialMatches);
        alphaMatches.addAll(alphaSOMatches);

        betaMatches.addAll(betaAlkMatches);
        betaMatches.addAll(betaThialMatches);
        betaMatches.addAll(betaSOMatches);


        //note feature intensities
        for (SpecificXLIonMatch alphaMatch : alphaMatches){
            int currentCharge = alphaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = alphaMatch.getMatchedPeak().getFeatureIntensity();
            //should work since array is initialized to 0.0
            alphaChargeStateIntensities[currentCharge-1] += currentAbsIntensity;
        }
        for (SpecificXLIonMatch betaMatch : betaMatches){
            int currentCharge = betaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = betaMatch.getMatchedPeak().getFeatureIntensity();
            //should work since array is initialized to 0.0
            betaChargeStateIntensities[currentCharge-1] += currentAbsIntensity;
        }
        //alpha and beta charge states are now set as arrays with intensities; if intensity is 0, charge state was not detected
        String alphaZdetected = "";
        String betaZdetected = "";
        int alphaDominantZ = 0;
        int betaDominantZ = 0;
        double alphaMaxIntensity = 0;
        double betaMaxIntensity = 0;
        //this loop can handle both arrays
        //zSep is used to add a semicolon after an entry
        String zSepAlpha = "";
        String zSepBeta = "";
        for (int i = 0; i < maximumChargeState; i++){
            double alphaCurrentIntensity = alphaChargeStateIntensities[i];
            double betaCurrentIntensity = betaChargeStateIntensities[i];
            if (alphaCurrentIntensity != 0){
                alphaZdetected = alphaZdetected + zSepAlpha + (i+1);
                zSepAlpha = ";";
                if (alphaCurrentIntensity>alphaMaxIntensity){
                    alphaMaxIntensity = alphaCurrentIntensity;
                    alphaDominantZ = (i+1);
                }
            }
            if (betaCurrentIntensity != 0){
                betaZdetected = betaZdetected + zSepBeta + (i+1);
                zSepBeta = ";";
                if (betaCurrentIntensity>betaMaxIntensity){
                    betaMaxIntensity = betaCurrentIntensity;
                    betaDominantZ = (i+1);
                }
            }
        }
        //handle if nothing is detected
        if (alphaZdetected.equals(""))
            alphaZdetected = "0";
        if (betaZdetected.equals(""))
            betaZdetected = "0";


        sb.append(alphaZdetected).append(",");
        sb.append(betaZdetected).append(",");
        sb.append(alphaDominantZ).append(",");
        sb.append(betaDominantZ).append(",");


        String alphaModsDetected = "";
        String alphaDominantMod = "";
        String betaModsDetected = "";
        String betaDominantMod = "";
        double alphaAlkAbsIntensities = 0;
        double alphaThialAbsIntensities = 0;
        double alphaSOAbsIntensities = 0;
        double betaAlkAbsIntensities = 0;
        double betaThialAbsIntensities = 0;
        double betaSOAbsIntensities = 0;

        //all fragments in this list are already sorted, so just sum intensities

        //note: feature intensity
        for (SpecificXLIonMatch alphaAlkMatch : alphaAlkMatches){
            alphaAlkAbsIntensities += alphaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaThialMatch : alphaThialMatches){
            alphaThialAbsIntensities += alphaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch alphaSOMatch : alphaSOMatches){
            alphaSOAbsIntensities += alphaSOMatch.getMatchedPeak().getFeatureIntensity();
        }


        for (SpecificXLIonMatch betaAlkMatch : betaAlkMatches){
            betaAlkAbsIntensities += betaAlkMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaThialMatch : betaThialMatches){
            betaThialAbsIntensities += betaThialMatch.getMatchedPeak().getFeatureIntensity();
        }
        for (SpecificXLIonMatch betaSOMatch : betaSOMatches){
            betaSOAbsIntensities += betaSOMatch.getMatchedPeak().getFeatureIntensity();
        }


        String alphaModSep = "";
        if (alphaAlkAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + "alk";
            alphaModSep = ";";
        }
        if (alphaThialAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + alphaModSep + "thial";
            alphaModSep = ";";
        }
        if (alphaSOAbsIntensities != 0){
            alphaModsDetected = alphaModsDetected + alphaModSep + "SO";
        }
        if (alphaAlkAbsIntensities != 0 && alphaThialAbsIntensities != 0 && alphaSOAbsIntensities != 0)
            alphaModsDetected = "all";

        String betaModSep = "";
        if (betaAlkAbsIntensities != 0){
            betaModsDetected = betaModsDetected + "alk";
            betaModSep = ";";
        }
        if (betaThialAbsIntensities != 0){
            betaModsDetected = betaModsDetected + betaModSep + "thial";
            betaModSep = ";";
        }
        if (betaSOAbsIntensities != 0){
            betaModsDetected = betaModsDetected + betaModSep + "SO";
        }
        if (betaAlkAbsIntensities != 0 && betaThialAbsIntensities != 0 && betaSOAbsIntensities != 0)
            betaModsDetected = "all";

        //determine dominant peak
        double currentAlphaModHighestInt = 0;
        double currentBetaModHighestInt = 0;


        if (alphaAlkAbsIntensities > currentAlphaModHighestInt){
            currentAlphaModHighestInt = alphaAlkAbsIntensities;
            alphaDominantMod = "alk";
        }

        if (alphaThialAbsIntensities > currentAlphaModHighestInt){
            currentAlphaModHighestInt = alphaThialAbsIntensities;
            alphaDominantMod = "thial";
        }

        if (alphaSOAbsIntensities > currentAlphaModHighestInt){
            alphaDominantMod = "SO";
        }

        if (betaAlkAbsIntensities > currentBetaModHighestInt){
            currentBetaModHighestInt = betaAlkAbsIntensities;
            betaDominantMod = "alk";
        }

        if (betaThialAbsIntensities > currentBetaModHighestInt){
            currentBetaModHighestInt = betaThialAbsIntensities;
            betaDominantMod = "thial";
        }

        if (betaSOAbsIntensities > currentBetaModHighestInt){
            betaDominantMod = "SO";
        }

        //handle if all intensities of fragments are 0
        if (alphaAlkAbsIntensities == 0 && alphaThialAbsIntensities == 0 && alphaSOAbsIntensities == 0){
            alphaModsDetected = "false";
            alphaDominantMod = "false";
        }
        if (betaAlkAbsIntensities == 0 && betaThialAbsIntensities == 0 && betaSOAbsIntensities == 0){
            betaModsDetected = "false";
            betaDominantMod = "false";
        }
        sb.append(alphaModsDetected).append(",");
        sb.append(betaModsDetected).append(",");
        sb.append(alphaDominantMod).append(",");
        sb.append(betaDominantMod).append(",");

        //now, information about the detected fragments individually
        boolean aAlk = false;
        boolean aThial = false;
        boolean aSO = false;


        boolean bAlk = false;
        boolean bThial = false;
        boolean bSO = false;


        double aAlkRelInt = 0;
        double aThialRelInt = 0;
        double aSORelInt = 0;


        double bAlkRelInt = 0;
        double bThialRelInt = 0;
        double bSORelInt = 0;


        double aAlkAbsInt = 0;
        double aThialAbsInt = 0;
        double aSOAbsInt = 0;

        double bAlkAbsInt = 0;
        double bThialAbsInt = 0;
        double bSOAbsInt = 0;

        //note: use feature intensities for that

        for (SpecificXLIonMatch match : alphaAlkMatches){
            aAlk = true;
            aAlkAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aAlkRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaThialMatches){
            aThial = true;
            aThialAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aThialRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : alphaSOMatches){
            aSO = true;
            aSOAbsInt += match.getMatchedPeak().getFeatureIntensity();
            aSORelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }



        for (SpecificXLIonMatch match : betaAlkMatches){
            bAlk = true;
            bAlkAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bAlkRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaThialMatches){
            bThial = true;
            bThialAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bThialRelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }
        for (SpecificXLIonMatch match : betaSOMatches){
            bSO = true;
            bSOAbsInt += match.getMatchedPeak().getFeatureIntensity();
            bSORelInt += match.getMatchedPeak().getFeatureRelIntPerTIC();
        }



        sb.append(aAlk).append(",");
        sb.append(aThial).append(",");
        sb.append(aSO).append(",");


        sb.append(bAlk).append(",");
        sb.append(bThial).append(",");
        sb.append(bSO).append(",");


        sb.append(twoDec.format(aAlkRelInt)).append(",");
        sb.append(twoDec.format(aThialRelInt)).append(",");
        sb.append(twoDec.format(aSORelInt)).append(",");


        sb.append(twoDec.format(bAlkRelInt)).append(",");
        sb.append(twoDec.format(bThialRelInt)).append(",");
        sb.append(twoDec.format(bSORelInt)).append(",");


        sb.append(scientific.format(aAlkAbsInt)).append(",");
        sb.append(scientific.format(aThialAbsInt)).append(",");
        sb.append(scientific.format(aSOAbsInt)).append(",");


        sb.append(scientific.format(bAlkAbsInt)).append(",");
        sb.append(scientific.format(bThialAbsInt)).append(",");
        sb.append(scientific.format(bSOAbsInt)).append(",");


        //information about 4 most intense signature peaks
        //list of specific XL ion matches is already ordered, so take first 4 entries
        int currentMaxPeak = 0;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        currentMaxPeak++;
        if(currentMaxPeak < this.xlIonMatches.size()){
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getPepType()).append(" ").append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getCharge()).append("+").append(",");
            sb.append(this.xlIonMatches.get(currentMaxPeak).getMatchedFragIon().getModType()).append(",");
            sb.append(twoDec.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureRelIntPerTIC())).append(",");
            sb.append(scientific.format(this.xlIonMatches.get(currentMaxPeak).getMatchedPeak().getFeatureIntensity())).append(",");
        }
        else{
            sb.append("NA").append(",");
            sb.append("NA").append(",");
            sb.append(0).append(",");
            sb.append(0).append(",");
        }
        //number of peaks with 31.9715 Da mass difference
        int numberOfTriggerDistances = cidScanIn.getNumberofPeaksWithSpecificMassDifference(31.9715, ppmDevIn).size();
        sb.append(numberOfTriggerDistances).append(",");
        //include next line command
        sb.append("\n");
        //sb should be completely ready now
        output = sb.toString();
        return output;
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

    public int getHCDScanNumber() {return hcdScanNumber;}
    public int getCidScanNumber() {return cidScanNumber;}

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
