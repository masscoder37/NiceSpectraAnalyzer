import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Michael Stadlmeier on 6/20/2017.
 */
// uses an ArrayList of Peaks to create a spectrum
public class MySpectrum {
    private ArrayList<Peak> peakList;
    private ArrayList<Feature> featureList;
    private int numberOfPeaks;
    private int scanNumber;
    private String scanHeader;
    private boolean zAndFeaturesAssigned;
    private double spectrumTIC;
    private String fragmentationMethod;
    private boolean noisePresent;
    //various Decimal Formats for Printer
    private DecimalFormat fourDec = new DecimalFormat("0.0000");
    private DecimalFormat twoDec = new DecimalFormat("0.00");
    private DecimalFormat scientific = new DecimalFormat("0.00E0");

    //helper class to deal with the determined charge and associated peaklist
    private static class WinningChargeState{
        private int chargeState;
        private ArrayList<Peak> peakList; //this peaklist also includes the original peak

        private WinningChargeState(int chargeStateIn, ArrayList<Peak> peakListIn){
            this.chargeState = chargeStateIn;
            this.peakList = peakListIn;
        }


        public static WinningChargeState determineChargeState(ArrayList<ArrayList<Peak>> candidatesIn) {
            ArrayList<Peak> winningPeaks = new ArrayList<>();
            int winningZ = 0;
            //loop through all the lists...there will be 8
            if (candidatesIn.size() != 8)
                throw new IllegalArgumentException("Something went wrong with charge state determination!");

            //find the highest list size
            //list size is always at least 1 for the current peak
            //check afterwards if maxSize is still 1
            int maxSize = 1;

            for (ArrayList<Peak> list : candidatesIn) {
                if (list.size() > maxSize)
                    maxSize = list.size();
            }

            //if all charge state lists are 1, there were no features detected
            if (maxSize == 1) {
                //charge state detected is zero, list doesn't matter
                return new WinningChargeState(0, candidatesIn.get(0));
            }
            //determine which charge states have the highest abundance
            ArrayList<ArrayList<Peak>> biggestSizes = new ArrayList<>();
            //add index for keeping track of the charge state, this time with REAL charge states
            ArrayList<Integer> zTracking = new ArrayList<>();
            for (int m = 0; m < candidatesIn.size(); m++) {
                if (candidatesIn.get(m).size() == maxSize) {
                    biggestSizes.add(candidatesIn.get(m));
                    //REAL charge state is added there!
                    zTracking.add(m + 1);
                }
            }
            if (biggestSizes.size() == 1) {
                return new WinningChargeState(zTracking.get(0), biggestSizes.get(0));
            } else {
                //now compare the summed intensities and take the charge state with the highest one
                double maxInt = 0;
                int index = 0;
                //this loop goes through all the lists
                for (int o = 0; o < biggestSizes.size(); o++) {
                    double currentInt = 0;
                    for (Peak peak : biggestSizes.get(o)) {
                        currentInt += peak.getIntensity();
                    }
                    if (currentInt > maxInt) {
                        maxInt = currentInt;
                        index = o;
                    }
                }
                return new WinningChargeState(zTracking.get(index), biggestSizes.get(index));
            }
        }

        public int getChargeState(){return this.chargeState;}
        public ArrayList<Peak> getPeakList(){return this.peakList;}

    }


    public MySpectrum(ArrayList<Peak> peaksIn, int scanNumberIn, String scanHeaderIn, String fragmentationMethodIn) {
        this.scanNumber = scanNumberIn;
        this.scanHeader = scanHeaderIn;
        //make sure that peakList is ordered
        //implemented QuickSort Algorithm
        //TODO: I assume that peaksIn is always already ordered, leading to a case where quicksort is not efficient?
        peaksIn = QuickSort.peakListQuickSort(peaksIn);
        this.peakList = peakPacker(peaksIn);
        this.numberOfPeaks = this.peakList.size();
        this.zAndFeaturesAssigned = false;
        this.spectrumTIC = 0;
        for (Peak peak : this.peakList){
            spectrumTIC += peak.getIntensity();
        }

        if (fragmentationMethodIn.equals("CID")||fragmentationMethodIn.equals("HCD"))
        this.fragmentationMethod = fragmentationMethodIn;
        else
            this.fragmentationMethod = "NA";
        //only if noise isn't present, this method might be used somewhere
        this.noisePresent = false;
    }

    //overloaded constructor for spectra with noise band used by MzXMLReadIn
    public MySpectrum(ArrayList<Peak> peaksIn, boolean noisePresentIn, int scanNumberIn, String scanHeaderIn, String fragmentationMethodIn) {
        this.scanNumber = scanNumberIn;
        this.scanHeader = scanHeaderIn;
        //make sure that peakList is ordered
        //implemented QuickSort Algorithm
        peaksIn = QuickSort.peakListQuickSort(peaksIn);
        this.peakList = peakPacker(peaksIn);
        this.noisePresent = noisePresentIn;
        this.numberOfPeaks = this.peakList.size();
        this.zAndFeaturesAssigned = false;
        this.spectrumTIC = 0;
        for (Peak peak : this.peakList){
            spectrumTIC += peak.getIntensity();
        }
        if (fragmentationMethodIn.equals("CID")||fragmentationMethodIn.equals("HCD"))
            this.fragmentationMethod = fragmentationMethodIn;
        else
            this.fragmentationMethod = "NA";
    }


    //use this function to remove peaks not wanted in spectrum and to assign rel Intensities and base peak
    private ArrayList<Peak> peakPacker(ArrayList<Peak> peaksToPack) {
        ArrayList<Peak> packedPeaks = new ArrayList<>();
        //set scanNumber from respective MySpectrum
        int scanNumber = this.scanNumber;
        double highestInt = 0;
        double currentInt;
        double relIntCalc;
        int wrongScanNumber = 0;
        int peakNumber = peaksToPack.size();
//sort out all the peaks which don't belong to spectrum
        for (int a = 0; a < peakNumber; a++) {
            if (scanNumber != peaksToPack.get(a).getScanNumber()) {
                peaksToPack.remove(a);
                peakNumber = peakNumber - 1;
                wrongScanNumber++;
            }
        }
        //find highest intensity
        for (Peak peak : peaksToPack) {
            currentInt = peak.getIntensity();
            if (currentInt > highestInt) {
                highestInt = currentInt;
            }
        }
        //calculate rel Intensities
        for (Peak peak : peaksToPack) {
            relIntCalc = (peak.getIntensity() / highestInt) * 100;
            peak.setRelInt(relIntCalc);
            if (relIntCalc == 100) {
                peak.setBasePeak();
            }
        }

        /*System.out.println("");
        System.out.println("Generating Peak list:");
        System.out.println("");

        System.out.println(""+wrongScanNumber+" peak(s) were removed(different scan number).");
        System.out.println("");*/

        return peaksToPack;
    }

    //this is an iteration of the assignChargeStates and the Feature.featureAssigner functions
    //since you can't have a feature without a charge state and vice versa, it makes sense to combine them
    //so, concurrently detect charge states and features
    public void assignZAndFeatures(double ppmDevIn){
        this.featureList = new ArrayList<>();
        //double ppmDev to cover the worst case scenario
        double ppmDev = 2*ppmDevIn;

        //set up HashMap to quickly access the peaks to set values without looping through the complete arraylist
        //peak mass is unique and key
        HashMap<Double, Peak> fastAccessMap = new HashMap<>();
        for (Peak peak : this.peakList){
            fastAccessMap.put(peak.getMass(), peak);
        }

        //variable to count number of features
        int numberOfFeatures = 1;


        //set up "exclusion list" which handles peaks to be ignored as soon as they are assigned to be part of a feature
        ArrayList<Double> excludedPeaks = new ArrayList<>();
        //start looping through the peaks
        for (int i = 0; i < this.peakList.size(); i++){
            Peak current = this.peakList.get(i);
            //handle if peak was already assigned
            if (excludedPeaks.contains(current.getMass()))
                continue;
            //follow the same logic as FeatureAssigner: a feature always stars with the first peak (duh), so just set the following 15 peaks as neighbours
            ArrayList<Peak> followingPeaks = new ArrayList<>();
            //here, again a check for peaks which were already assigned is needed


            for (int m = 1; m < 16; m++){
                try {
                    //skip if peak was already assigned to feature
                    if(excludedPeaks.contains(this.peakList.get(m+i).getMass()))
                        continue;

                    followingPeaks.add(this.peakList.get(m+i));
                }
                //skip if not enough neighbours present
                catch (IndexOutOfBoundsException e){
                    continue;
                }
            }
            //followingPeaks contains all the peaks now
            //loop through
            //here, 2 loops are present: outer loop handles different charge states, inner loop the followingPeaks
            //note: have a helper class which takes care of the ArrayLists for each individual charge state

            ArrayList<ArrayList<Peak>> matchesPerZ = new ArrayList<>();

            for (int z = 1; z < 9; z++) {
                //add all matching peaks to the arrow list
                ArrayList<Peak> initialMatches = new ArrayList<>();
                //add current peak also to the initial matches that the feature list which can be passed to Feature is complete
                initialMatches.add(current);

                //for multiples of the difference, include another variable n
                int n = 1;
                double isotopeDifference = AtomicMasses.getNEUTRON() / z * n;
                for (Peak peak : followingPeaks) {
                    double realMass = current.getMass() + isotopeDifference;
                    if (DeviationCalc.isotopeMatchPPM(realMass, peak.getMass(), ppmDev)) {
                        initialMatches.add(peak);
                        //doing this will avoid hitting a peak several times
                        n++;
                        //isotopeDifference changes after match, because n changed
                        isotopeDifference = AtomicMasses.getNEUTRON() / z * n;
                    }
                }
                //for one charge state, all matches are now complete
                matchesPerZ.add(initialMatches);
            }
            //matchesPerZ now has multiple lists of matches, one for each charge state
            //some of the lists might be only size 1
            //have a function check which charge state can explain the highest signal
            WinningChargeState winningZ = WinningChargeState.determineChargeState(matchesPerZ);
            //winningZ should now contains the winning charge state and the corresponding list of peaks
            //if charge state is 0, only the current peak is in the ArrayList
            if (winningZ.getChargeState() == 0){
                //add properties via the fast Access hashmap
                fastAccessMap.get(current.getMass()).setCharge(0);
                //if the current peak is not part of a feature, it can be ignored for the rest of the analysis
                excludedPeaks.add(current.getMass());
            }
            //else, for each peak, charge states have to be assigned and a feature must be created
            else {
                Feature feature = new Feature(winningZ.getPeakList(), winningZ.getChargeState(), this.spectrumTIC, numberOfFeatures);
                numberOfFeatures++;
                this.featureList.add(feature);
                //now, set all the peaks in the arrayList of peak with the fastAccessMap
                //also, add masses to the excluded list
                //current is also part of the peaklist
                for (Peak peak : winningZ.getPeakList()){
                    Peak toSet = fastAccessMap.get(peak.getMass());
                    toSet.setCharge(winningZ.getChargeState());
                    toSet.setFeature(feature);
                    //add all the masses to the exclusion list
                    excludedPeaks.add(peak.getMass());
                }
            }
        }
        //set flags
        this.zAndFeaturesAssigned = true;
    }

    //this class should return the best matching peak (or null) in the spectrum
    public Peak getMatchingPeak(double massIn, double ppmDevIn) {

        Peak out = null;
        //set starting ppmDev, way higher than allowed ppm
        double minPPMDev = 2 * ppmDevIn;


        //loop through all peaks
        for (Peak peak : peakList) {
            //this skipping forward should be more efficient than checking for every peak in detail
            if (peak.getMass() < massIn - 0.8)
                continue;
            //second check to quit loop prematurely
            if (peak.getMass() > massIn + 0.8)
                break;

            //now only masses are checked in detail which are in the range
            if (DeviationCalc.ppmMatch(massIn, peak.getMass(), ppmDevIn)) {
                double currentPPMDev = DeviationCalc.ppmDeviationCalc(massIn, peak.getMass());
                //check if better match than the one before
                if (Math.abs(currentPPMDev) < Math.abs(minPPMDev)) {
                    minPPMDev = currentPPMDev;
                    out = peak;
                }
            }
        }
        return out;
    }












    //getter
    public ArrayList<Peak> getPeakList() {
        return this.peakList;
    }

    public ArrayList<Feature> getFeatureList(){return this.featureList;}

    public int getScanNumber() {
        return this.scanNumber;
    }

    public int getNumberOfPeaks() {
        return this.numberOfPeaks;
    }

    public String getScanHeader() {
        return this.scanHeader;
    }

    public int getBasePeakIndex() {
        int index = 0;
        for (int i = 0; i < this.numberOfPeaks; i++) {
            if (this.peakList.get(i).getBasePeak())
                index = i;
        }
        return index;
    }

    public boolean areZAndFeaturesAssigned(){return this.zAndFeaturesAssigned;}
    public double getSpectrumTIC(){return this.spectrumTIC;}
    public String getFragmentationMethod(){return this.fragmentationMethod;}
    public boolean isNoisePresent(){return this.noisePresent;}

    public void spectrumPrinter() {
        System.out.println("");
        System.out.println("Generating Spectrum information...");
        System.out.println("Scan Header: " + this.scanHeader);
        System.out.println("Scan Number: " + this.scanNumber);
        double summedIntensity = 0;
        int printedPeaks = 0;
        double cutOff = 0;
        for (Peak peak : this.peakList) {
            if (peak.getRelIntensity() > cutOff) {
                System.out.println("Peak mass: " + fourDec.format(peak.getMass())
                        + "   Charge: " + peak.getCharge()
                        + "   Rel. Int.: " + twoDec.format(peak.getRelIntensity())
                        + "   Base Peak: " + peak.getBasePeak());
                printedPeaks++;
            }
            summedIntensity += peak.getRelIntensity();
        }
        System.out.println("");
        System.out.println("General spectra properties:");
        System.out.println("");
        System.out.println("Number of Peaks: " + this.peakList.size());
        System.out.println("Number of printed Peaks (>"+cutOff+"% rel. Int.): " + printedPeaks);
        System.out.println("Mean rel. Intensity: " + twoDec.format((summedIntensity / this.peakList.size())) + "%");
        int basePeakIndex = this.getBasePeakIndex();
        double maxInt = this.peakList.get(basePeakIndex).getIntensity();
        System.out.println("Base peak Intensity: " + scientific.format(maxInt));
        System.out.println("End of spectrum analysis!");
        System.out.println("");
    }


    public static double extractPrecursorFromFilterLine(String filterLine){

        int indexOfAt = filterLine.indexOf('@');
        String subString = filterLine.substring(indexOfAt-9, indexOfAt);
        double isolatedPrecursorMass;
        try {
            isolatedPrecursorMass = Double.parseDouble(subString);
        }
        //if for some reason only 3 decimals are present (shouldn't be the case), number format exception would trigger
        catch (NumberFormatException e){
            String reducedSubstring = subString.substring(1);
            isolatedPrecursorMass = Double.parseDouble(reducedSubstring);
        }
        return isolatedPrecursorMass;
    }

    public ArrayList<Peak[]> getNumberofPeaksWithSpecificMassDifference(double massDiffIn, double ppmDevIn){
        //note: validating
        ArrayList<Peak[]> out = new ArrayList<>();

        //for accounting of 2 peaks, double ppmDev
        ppmDevIn = 2 * ppmDevIn;

        int occurrence = 0;
        //check if charge states and features where assigned in current spectrum
        if(!this.zAndFeaturesAssigned)
            this.assignZAndFeatures(ppmDevIn);

        //Work with exclusion list
        ArrayList<Double> excludedMasses = new ArrayList<>();
        //loop through all the peaks
        for (int i = 0; i < this.peakList.size(); i++) {
            //only want to compare the monoisotopic peaks if part of feature
            //since coming from the left, peak is always the monoisotopic one, just add all the peaks of the feature to exclusion list afterwards
            Peak current = this.peakList.get(i);
            //check if peak is part of excluded masses list
            if (excludedMasses.contains(current.getMass()))
                continue;
            //assemble following peaks
            ArrayList<Peak> followingPeaks = new ArrayList<>();
            for (int m = 1; m < 30; m++) {
                //try deals with not enough neighbours
                try {
                    Peak next = this.peakList.get(m + i);
                    //first check if distance is important. otherwise, break the loop prematurely
                    if (next.getMass() > massDiffIn + 2 + current.getMass())
                        break;
                    //check if part of exclusion list, skip if that's the case
                    if (excludedMasses.contains(next.getMass()))
                        continue;
                    //check if peak is part of any feature
                    if (next.isPartOfFeature()) {
                        //exclude if its part of the same feature as current peak
                        if (current.getFeature() == next.getFeature())
                            continue;
                        //exclude if it's not the monoisotopic peak of the feature
                        if (!next.isMonoisotopicPeak())
                            continue;
                    }
                    //adds if: not out of mass range, not in exclusion list, not part of same feature, is featureless or monoisotopic peak
                    followingPeaks.add(next);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            double massShifted;
            //of charge is known, that takes precedent
            if (current.isChargeStateKnown()) {
                massShifted = current.getMass() + massDiffIn / current.getCharge();
            }
            //if not, assume z=1
            else {
                massShifted = current.getMass() + massDiffIn;
            }

            for (Peak nextPeak : followingPeaks) {
                //first check if charge state of the following peak is known. if it's different, then discard
                if (nextPeak.getCharge() == current.getCharge() || !nextPeak.isChargeStateKnown()) {
                    //if they have the same charge, use the normal shifted mass
                    if (DeviationCalc.ppmMatch(massShifted, nextPeak.getMass(), ppmDevIn)) {
                        occurrence++;
                        Peak[] match = new Peak[2];
                        match[0] = current;
                        match[1] = nextPeak;
                        out.add(match);
                        //add all masses to exclusion list
                        if (nextPeak.isPartOfFeature()) {
                            for (Peak peak : nextPeak.getFeature().getPeakList()) {
                                excludedMasses.add(peak.getMass());
                            }
                        } else {
                            excludedMasses.add(nextPeak.getMass());
                        }
                        break;
                    }
                }
                //handle case if current charge state is unknown, but next peak charge state is known
                if (!current.isChargeStateKnown() && nextPeak.isChargeStateKnown()) {
                    massShifted = current.getMass() + massDiffIn / nextPeak.getCharge();
                    if (DeviationCalc.ppmMatch(massShifted, nextPeak.getMass(), ppmDevIn)) {
                        occurrence++;
                        Peak[] match = new Peak[2];
                        match[0] = current;
                        match[1] = nextPeak;
                        out.add(match);
                        //peak has to be part of feature, otherwise charge state wouldn't be known
                        for (Peak peak : nextPeak.getFeature().getPeakList()) {
                            excludedMasses.add(peak.getMass());
                        }
                        break;
                    }
                }

                if (!current.isChargeStateKnown() && !nextPeak.isChargeStateKnown()) {
                    if (DeviationCalc.ppmMatch(massShifted, nextPeak.getMass(), ppmDevIn)) {
                        occurrence++;
                        Peak[] match = new Peak[2];
                        match[0] = current;
                        match[1] = nextPeak;
                        out.add(match);
                        excludedMasses.add(nextPeak.getMass());
                        break;
                    }
                }
            }
            //add current peak and all feature peaks to exclusion list
            if (current.isPartOfFeature()) {
                for (Peak peak : current.getFeature().getPeakList()) {
                    excludedMasses.add(peak.getMass());
                }
            } else {
                excludedMasses.add(current.getMass());
            }
        }
        return out;
    }




    public int[] getChargeStateDistributionNumber() {
        int chargeUnknown = 0;
        int charge1 = 0;
        int charge2 = 0;
        int charge3 = 0;
        int charge4 = 0;
        int chargeHigher = 0;
        for (Peak peak : this.peakList) {
            int current = peak.getCharge();
            switch (current) {
                case 0:
                    chargeUnknown++;
                    break;
                case 1:
                    charge1++;
                    break;
                case 2:
                    charge2++;
                    break;
                case 3:
                    charge3++;
                    break;
                case 4:
                    charge4++;
                    break;
                default:
                    chargeHigher++;
                    break;
            }
        }
        int[] chargeStateDistri = new int[6];
        chargeStateDistri[0] = chargeUnknown;
        chargeStateDistri[1] = charge1;
        chargeStateDistri[2] = charge2;
        chargeStateDistri[3] = charge3;
        chargeStateDistri[4] = charge4;
        chargeStateDistri[5] = chargeHigher;
        return chargeStateDistri;
    }


}




    //note: old versions of charge state assigner and feature assigner
    /*public void assignChargeStates(double ppmDevIn) {
        ArrayList<Peak> peaksIn = this.getPeakList();
        //all peaks from MySpectrum are already ordered. Using Quicksort again would be the worst possible case
        int numberOfPeaks = peaksIn.size();
        //this variable sets the ppm tolerance and can be tweaked!
        //with this tolerance, a peak with 2000Da mass can be detected with a deviation of about 7 ppm
        //e.g. if M(0) is -7ppm and M(1) is +7ppm (max deviation), resulting mass error is 0.028 Da
        //however, for peak with 500 mass 0.03 is equivalent to 30 ppm --> change to ppm dependence
        //double daTol = 0.03;
        double ppmTol = ppmDevIn *2.1; //this is 2.1 * 10 ppm, which takes into account the worst case scenario, -10 ppm for M(0) and +10 ppm for M(1), and gives some leeway
        //for every peak, look at 10 neighbours
        ArrayList<Neighbour> neighbours = new ArrayList<>();
        //loop through all the peaks in the peaklist
        //starting from a low charge state, assign charge state if correct neighbours are detected
        for (int i = 0; i < numberOfPeaks; i++) {
            Peak current = peaksIn.get(i);
            //assemble neighbours of current
            for (int m = -5; m < 6; m++) {
                //m == 0 would be current peak, skip
                if (m == 0)
                    continue;
                //try...catch deals with not enough neighbours
                try {
                    neighbours.add(new Neighbour(peaksIn.get(i + m), current.getMass()));
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
            }

            //try to search for the charge state explaining most of neighbouring peaks
            //check the whole list of mass differences for multiples of isotope mass
            //also, multiples of delta mass has to be accounted for --> leading to whole isotope pattern


            int chargeState = 0;
            ChargeStateOccurence[] possibleChargeStates = new ChargeStateOccurence[9];
            for (int z = 1; z < 9; z++) {
                possibleChargeStates[z] = new ChargeStateOccurence(z);
                //include another for loop to check different multiples of the isotope envelope
                for (int n = 1; n < 5; n++) {
                    //supposed Diff is the expected absolute mass difference
                    double supposedM1 = current.getMass() + n * AtomicMasses.getNEUTRON() / z;
                    for (Neighbour neighbour : neighbours) {
                        //if (DeviationCalc.isotopeMatch(supposedM1, neighbour.getMassDiff(), DaTol)) ... switched from Da to PPM based cal
                        if (DeviationCalc.isotopeMatchPPM(supposedM1, neighbour.getNeighbourPeak().getMass(), ppmTol)) {
                            possibleChargeStates[z].increaseOccurence();
                            possibleChargeStates[z].addIntensity(neighbour.getNeighbourPeak().getIntensity());
                            //only if n = 1, there is one peak which is a representative of the charge state
                            if (n == 1) {
                                possibleChargeStates[z].setRepresentativeNeighbour();
                            }
                        }
                    }
                }
            }
            //now, all charge state occurences are set and can be analyzed
            //analyze the charge state occurences, weigh according to highest max int. and occurence
            //first, find the charge states which occur the most times
            ArrayList<ChargeStateOccurence> topOccurences = new ArrayList<>();
            int topOccurenceNumber = 0;
            int currentOccurence = 0;
            for (int z = 1; z < 9; z++) {
                try {
                    currentOccurence = possibleChargeStates[z].getOccurence();
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                if (currentOccurence > topOccurenceNumber)
                    topOccurenceNumber = currentOccurence;
                currentOccurence = 0;
            }

            //it is possible that multiple charge states have the same abundance. In this case, put them into a list
            for (int z = 1; z < 9; z++) {
                if (possibleChargeStates[z].getOccurence() == topOccurenceNumber) {
                    topOccurences.add(possibleChargeStates[z]);
                }
            }

            //set variable if representative peak is present
            boolean representativePeakPresent = false;

            //now, check if the list contains more than one element
            if (topOccurences.size() != 1) {
                //if multiple possible zs' have the same occurence, check if one of them has the represent. peak
                for (ChargeStateOccurence currentZ : topOccurences) {
                    //if not, set intensity to 0; the other possible z's with higher intensities will win
                    if (!currentZ.getIfRepresentativeNeighbour()) {
                        currentZ.nullifyIntensity();
                    }
                }
                //after that, compare the summed intensities of neighbouring peaks
                double maxInt = 0;
                for (ChargeStateOccurence currentZ : topOccurences) {
                    if (currentZ.getSummedIntensity() > maxInt) {
                        maxInt = currentZ.getSummedIntensity();
                    }
                }

                for (ChargeStateOccurence currentZ : topOccurences) {
                    if (currentZ.getSummedIntensity() == maxInt) {
                        //check if represent. Peak is present
                        if (currentZ.getIfRepresentativeNeighbour())
                            representativePeakPresent = true;

                        chargeState = currentZ.getChargeState();
                    }
                }
                //exit if max intensity == 0
                if (maxInt == 0) {
                    chargeState = 0;
                    representativePeakPresent = false;
                }
            } else {
                if (topOccurences.get(0).getIfRepresentativeNeighbour()) {
                    representativePeakPresent = true;
                    chargeState = topOccurences.get(0).getChargeState();
                }
            }


            //if all the occurences are 0, set this charge state
            if (topOccurenceNumber == 0 || !representativePeakPresent) {
                chargeState = 0;
            }

            //now set the determined charge state
            current.setCharge(chargeState);
            //empty stuff
            neighbours.clear();
            topOccurences.clear();
            Arrays.fill(possibleChargeStates, null);
        }
        this.chargeStatesAssigned = true;
    }*/





    /*public void assignFeatures(double ppmDev){
        //first, check if charge states were assigned
        if (!this.chargeStatesAssigned)
            this.assignChargeStates(ppmDev);
        //set the feature list
        this.featureList = new ArrayList<>();
        this.featureList.addAll(Feature.featureAssigner(this,ppmDev));
        //modify the peaks to know about their belonging to a feature and point to the feature
        //to avoid having to iterate over the peaklist again and again, convert into LinkedHashMap
        HashMap<Double, Peak> fastAccessMap = new HashMap<>();
        for (Peak peak : this.peakList){
            fastAccessMap.put(peak.getMass(), peak);
        }
        //now loop through the Featurelist and modify the respective peaks
        for (Feature feature : this.featureList){
            ArrayList<Peak> featurePeaks = new ArrayList<>();
            featurePeaks.addAll(feature.getPeakList());
            for(Peak featurePeak : featurePeaks){
                fastAccessMap.get(featurePeak.getMass()).setFeature(feature);
            }
        }
        this.featuresAssigned = true;
    }*/
