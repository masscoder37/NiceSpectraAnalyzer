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
    private double isolatedMassToCharge;
    private int hcdScanNumber;
    private int cidScanNumber;
    private String fragmentationMethod;
    private boolean monoisotopicSelected;
    private int monoisotopicPeakOffset;
    private double retentionTime; //the retention time of the XL in seconds
    private boolean alphaEqualsBeta; //for unwanted skewing of the data
    //doing so will associate all the matches automatically with the respective XL
    private ArrayList<SpecificXLIonMatch> xlIonMatches;

    public Xl(String peptide1In, String peptide2In, String xlIn, int chargeStateIn,
              int hcdScanNumberIn,int cidScanNumberIn, String fragmentationMethodIn, double isolatedMassToChargeIn,
              int xlPos1In, int xlPos2In, ArrayList<AminoAcid> aaIn, double retentionTimeIn){

        //only cliXlink is supported atm
        if (!xlIn.equals("cliXlink"))
            throw new IllegalArgumentException("Unknown crosslinker: "+ xlIn);
        //easy to set variables
        this.xlUsed = xlIn;
        this.hcdScanNumber = hcdScanNumberIn;
        this.cidScanNumber = cidScanNumberIn;
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
    //TODO: generate String output for stringbuilder
    //comma is the used separator for the .csv file
    public String xlMatchesStringProducer(MySpectrum fullScanIn, MySpectrum hcdScanIn, MySpectrum cidScanIn){
        DecimalFormat twoDec = new DecimalFormat("0.00");
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat scientific = new DecimalFormat("0.00E0");
        String output = "";
        StringBuilder sb = new StringBuilder();

        //pep alpha, pep beta, alpha amino acid, beta amino acid, alpha pos, beta pos
        sb.append(this.getPeptideAlpha().getSequence()).append(",");
        sb.append((this.getPeptideBeta().getSequence())).append(",");

        //to figure out mod. amino acid, go to position in sequence.
        String alphaAA = Character.toString(this.getPeptideAlpha().getSequence().charAt(this.getXlPosAlpha()-1));
        sb.append(alphaAA).append(",");
        String betaAA = Character.toString(this.getPeptideBeta().getSequence().charAt(this.getXlPosBeta()-1));
        sb.append(alphaAA).append(",");
        sb.append(""+this.xlPosAlpha).append(",");
        sb.append(""+this.xlPosBeta).append(",");

        //set HCD and CID scan Numbers
        sb.append(""+this.hcdScanNumber).append(",");
        sb.append(""+this.cidScanNumber).append(",");

        //information about the theoretical xl precursor
        sb.append(fourDec.format(this.theoreticalXLIon.getMToZ())).append(",");;
        sb.append(""+this.theoreticalXLIon.getCharge()).append(",");
        sb.append(fourDec.format(this.isolatedMassToCharge)).append(",");
        double detectedPPMDev = DeviationCalc.ppmDeviationCalc(this.theoreticalXLIon.getMToZ(), this.isolatedMassToCharge);
        sb.append(twoDec.format(detectedPPMDev)).append(",");
        sb.append(""+this.monoisotopicPeakOffset).append(",");
        //check full scan spectrum for the precursor information
        ArrayList<Peak> fullScanPeakList = fullScanIn.getPeakList();
        double peakRelInt = 0;
        double peakAbsInt = 0;

        for (Peak peak : fullScanPeakList){
                if (DeviationCalc.ppmMatch(this.isolatedMassToCharge, peak.getMass(), 6)){
                    peakRelInt = peak.getRelIntensity();
                    peakAbsInt = peak.getIntensity();
                    break;
                }
            }
        sb.append(twoDec.format(peakRelInt)).append(",");
        sb.append(scientific.format(peakAbsInt)).append(",");
        //information about the specific signature peaks
        //all the matches are contained in the xlIonMatches list
        //split them up into individual lists

        // is there anywhere the information what is SO, thial, alk???
        //TODO: added the information to the specificXLionMatches
        ArrayList<SpecificXLIonMatch> alphaAlkMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaSOMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> alphaThialMatches = new ArrayList<>();

        ArrayList<SpecificXLIonMatch> betaAlkMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaSOMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaThialMatches = new ArrayList<>();
        for (SpecificXLIonMatch match : this.xlIonMatches){
            if (match.getMatchedFragIon().getPepType().equals("alpha")){
                if (match.getModType().equals("alk")){
                    alphaAlkMatches.add(match);
                    continue;
                }
                if (match.getModType().equals("thial")){
                    alphaThialMatches.add(match);
                    continue;
                }
                if (match.getModType().equals("SO")){
                    alphaSOMatches.add(match);
                    continue;
                }
            }
            if (match.getMatchedFragIon().getPepType().equals("beta")){
                if (match.getModType().equals("alk")){
                    betaAlkMatches.add(match);
                    continue;
                }
                if (match.getModType().equals("thial")){
                    betaThialMatches.add(match);
                    continue;
                }
                if (match.getModType().equals("SO")){
                    betaSOMatches.add(match);
                }
            }
        }
        //ordered lists are now populated
        //TODO: get the information from the ordered lists

        //determine number of signature peaks detected; 0-6
        //signature peaks summed abs intensity and in relation to MS2 TIC
        int numberOfSignaturePeaks = 0;
        double absIntSignaturePeaks = 0;
        ArrayList<ArrayList<SpecificXLIonMatch>> matchedLists = new ArrayList<>();
        matchedLists.add(alphaAlkMatches);
        matchedLists.add(alphaThialMatches);
        matchedLists.add(alphaSOMatches);
        matchedLists.add(betaAlkMatches);
        matchedLists.add(betaThialMatches);
        matchedLists.add(betaSOMatches);

        for (ArrayList<SpecificXLIonMatch> individualMatchList : matchedLists){
            if (individualMatchList.size() != 0) {
                numberOfSignaturePeaks++;
                for (SpecificXLIonMatch matchedIon : individualMatchList){
                    absIntSignaturePeaks += matchedIon.getMatchedPeak().getIntensity();
                }
            }
        }
        sb.append(""+numberOfSignaturePeaks).append(",");
        //figure out total TIC intensity in respective MS2 CID scan
        ArrayList<Peak> cidPeakList = new ArrayList<>();
        cidPeakList = cidScanIn.getPeakList();
        double cidTIC = 0;
        for (Peak cidPeak : cidPeakList){
            cidTIC += cidPeak.getIntensity();
        }
        double ratio = absIntSignaturePeaks / cidTIC *100;
        sb.append(twoDec.format(ratio)).append(",");

        //figure out charge states detected for alpha and beta and dominant charge states
        //charge state information is stored in XLFragmentIon
        //to determine which charge state is the dominant one, use the one with the highest abs. intensity
        //possible: 1 through charge state of xl
        int maximumChargeState = this.theoreticalXLIon.getCharge();
        double [] alphaChargeStateIntensities = new double[maximumChargeState];
        double [] betaChargeStateIntensities = new double[maximumChargeState];

        ArrayList<SpecificXLIonMatch> alphaMatches = new ArrayList<>();
        ArrayList<SpecificXLIonMatch> betaMatches = new ArrayList<>();

        alphaMatches.addAll(alphaAlkMatches);
        alphaMatches.addAll(alphaThialMatches);
        alphaMatches.addAll(alphaSOMatches);
        betaMatches.addAll(betaAlkMatches);
        betaMatches.addAll(betaThialMatches);
        betaMatches.addAll(betaSOMatches);
        for (SpecificXLIonMatch alphaMatch : alphaMatches){
            int currentCharge = alphaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = alphaMatch.getMatchedPeak().getIntensity();
            //should work since array is initialized to 0.0
            alphaChargeStateIntensities[currentCharge-1] += currentAbsIntensity;
        }
        for (SpecificXLIonMatch betaMatch : betaMatches){
            int currentCharge = betaMatch.getMatchedFragIon().getCharge();
            double currentAbsIntensity = betaMatch.getMatchedPeak().getIntensity();
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
        double alphaShortAbsInt = 0;
        double alphaLongAbsInt = 0;
        double betaShortAbsInt = 0;
        double betaLongAbsInt = 0;
        for (SpecificXLIonMatch alphaMatch : alphaMatches){
            String currentAlpha = alphaMatch.getMatchedFragIon().getCliXlinkSize();
            if (currentAlpha.equals("short"))
                alphaShortAbsInt += alphaMatch.getMatchedPeak().getIntensity();
            if (currentAlpha.equals("long"))
                alphaLongAbsInt += alphaMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch betaMatch : betaMatches){
            String currentBeta = betaMatch.getMatchedFragIon().getCliXlinkSize();
            if (currentBeta.equals("short"))
                betaShortAbsInt += betaMatch.getMatchedPeak().getIntensity();
            if (currentBeta.equals("long"))
                betaLongAbsInt += betaMatch.getMatchedPeak().getIntensity();
        }

        String alphaSideDetected = "NA";
        if (alphaShortAbsInt != 0)
            alphaSideDetected = "short";
        if (alphaLongAbsInt != 0)
            alphaSideDetected = "long";
        if (alphaShortAbsInt != 0 && alphaLongAbsInt !=0)
            alphaSideDetected = "both";

        String betaSideDetected = "NA";
        if (betaShortAbsInt != 0)
            betaSideDetected = "short";
        if (betaLongAbsInt != 0)
            betaSideDetected = "long";
        if (betaShortAbsInt != 0 && betaLongAbsInt !=0)
            betaSideDetected = "both";


        String alphaDominantSide = "NA";
        String betaDominantSide = "NA";
        if (alphaShortAbsInt > alphaLongAbsInt)
            alphaDominantSide = "short";
        else
            alphaDominantSide = "long";
        if (betaShortAbsInt > betaLongAbsInt)
            betaDominantSide = "short";
        else
            betaDominantSide = "long";
        sb.append(alphaSideDetected).append(",");
        sb.append(betaSideDetected).append(",");
        sb.append(alphaDominantSide).append(",");
        sb.append(betaDominantSide).append(",");

        //TODO: alpha and beta modifications (SO, alk, thial, all)
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

        for (SpecificXLIonMatch alphaAlkMatch : alphaAlkMatches){
            alphaAlkAbsIntensities += alphaAlkMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch alphaThialMatch : alphaThialMatches){
            alphaThialAbsIntensities += alphaThialMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch alphaSOMatch : alphaSOMatches){
            alphaSOAbsIntensities += alphaSOMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch betaAlkMatch : betaAlkMatches){
            betaAlkAbsIntensities += betaAlkMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch betaThialMatch : betaThialMatches){
            betaThialAbsIntensities += betaThialMatch.getMatchedPeak().getIntensity();
        }
        for (SpecificXLIonMatch betaSOMatch : betaSOMatches){
            betaSOAbsIntensities += betaSOMatch.getMatchedPeak().getIntensity();
        }

        String modSep = "";

        if (alphaAlkAbsIntensities != 0){
            
        }
        















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
