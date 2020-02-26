import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Michael Stadlmeier on 6/20/2017.
 */
// uses an ArrayList of Peaks to create a spectrum
public class MySpectrum {
    private ArrayList<Peak> peakList;
    private int numberOfPeaks;
    private int scanNumber;
    private String scanHeader;
    //various Decimal Formats for Printer
    private DecimalFormat fourDec = new DecimalFormat("0.0000");
    private DecimalFormat twoDec = new DecimalFormat("0.00");
    private DecimalFormat scientific = new DecimalFormat("0.00E0");

    public MySpectrum(ArrayList<Peak> peaksIn, int scanNumberIn, String scanHeaderIn) {
        this.scanNumber = scanNumberIn;
        this.scanHeader = scanHeaderIn;
        //make sure that peakList is ordered
        //implemented QuickSort Algorithm
        //TODO: I assume that peaksIn is always already ordered, leading to a case where quicksort is not efficient?
        peaksIn = QuickSort.peakListQuickSort(peaksIn);
        this.peakList = peakPacker(peaksIn);
        this.numberOfPeaks = this.peakList.size();
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

    public void chargeStateAssigner() {
        ArrayList<Peak> peaksIn = new ArrayList<>();
        //all peaks from MySpectrum are already ordered. Using Quicksort again would be the worst possible case
        peaksIn = this.getPeakList();
        int numberOfPeaks = peaksIn.size();
        //this variable sets the ppm tolerance and can be tweaked!
        double daTol = 0.03;
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

            //TODO try to search for the charge state explaining most of neighbouring peaks
            //check the whole list of mass differences for multiples of isotope mass
            //also, multiples of delta mass has to be accounted for --> leading to whole isotope pattern


            int chargeState = 0;
            ChargeStateOccurence[] possibleChargeStates = new ChargeStateOccurence[9];
            for (int z = 1; z < 9; z++) {
                possibleChargeStates[z] = new ChargeStateOccurence(z);
                //include another for loop to check different multiples of the isotope envelope
                for (int n = 1; n < 5; n++) {
                    //supposed Diff is the expected absolute mass difference
                    double supposedDiff = n * AtomicMasses.getNEUTRON() / z;
                    for (Neighbour neighbour : neighbours) {
                        if (DeviationCalc.isotopeMatch(supposedDiff, neighbour.getMassDiff(), daTol)) {
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
            //TODO: analyze the charge state occurences, weigh according to highest max int. and occurence
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
    }


    //getter
    public ArrayList<Peak> getPeakList() {
        return this.peakList;
    }

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

    public void smallIntensitiesRemover() {
        ArrayList<Peak> convolutedList = this.peakList;
        ArrayList<Peak> deconvolutedList = new ArrayList<>();
        int removedPeaks = 0;
        for (Peak p : convolutedList) {
            if (p.getIntensity() != 0) {
                deconvolutedList.add(p);
                removedPeaks++;
            }
        }
        System.out.println("Number of removed Peaks with low intensity: " + removedPeaks);

        this.peakList = peakPacker(deconvolutedList);
        this.numberOfPeaks = this.peakList.size();
    }

}
