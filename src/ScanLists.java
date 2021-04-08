//create scanLists from a .mzXML file and prove functions to iterate through them

import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.util.ArrayList;
import java.util.List;

public class ScanLists {
    private int maxMSLevel;
    private int numberOfSpectra;
    private int numberMS1 = 0;
    private int numberMS2 = 0;
    private int numberMS3 = 0;
    private ArrayList<Integer> ms1ScanNumbers = null;
    private ArrayList<Integer> ms2ScanNumbers = null;
    private ArrayList<Integer> ms3ScanNumbers = null;

    public ScanLists(MzXMLFile runIn) throws MzXMLParsingException {
        List<Long> allScanNumbers = runIn.getScanNumbers();
        ArrayList<Integer> ms1ScanNumbersList = new ArrayList<>();
        ArrayList<Integer> ms2ScanNumbersList = new ArrayList<>();
        ArrayList<Integer> ms3ScanNumbersList = new ArrayList<>();

        //loop through all spectra
        for (long scanNumber : allScanNumbers) {
            Scan currentScan = runIn.getScanByNum(scanNumber);
            int currentMSLevel = Math.toIntExact(currentScan.getMsLevel());
            if (currentMSLevel == 1) {
                ms1ScanNumbersList.add(Math.toIntExact(scanNumber));
                continue;
            }
            if (currentMSLevel == 2) {
                ms2ScanNumbersList.add(Math.toIntExact(scanNumber));
            }
            if (currentMSLevel == 3) {
                ms3ScanNumbersList.add(Math.toIntExact(scanNumber));
            }
        }
        //set variables
        if (ms2ScanNumbersList.size() == 0 && ms3ScanNumbersList.size() == 0)
            this.maxMSLevel = 1;
        else if (ms2ScanNumbersList.size() != 0 && ms3ScanNumbersList.size() == 0)
            this.maxMSLevel = 2;
        else
            this.maxMSLevel = 3;
        this.numberOfSpectra = allScanNumbers.size();
        this.numberMS1 = ms1ScanNumbersList.size();
        this.numberMS2 = ms2ScanNumbersList.size();
        this.numberMS3 = ms3ScanNumbersList.size();
        this.ms1ScanNumbers = ms1ScanNumbersList;
        this.ms2ScanNumbers = ms2ScanNumbersList;
        this.ms3ScanNumbers = ms3ScanNumbersList;
    }

    public int getMaxMSLevel() {
        return maxMSLevel;
    }

    public int getNumberOfSpectra() {
        return numberOfSpectra;
    }

    public int getNumberMS1() {
        return numberMS1;
    }

    public int getNumberMS2() {
        return numberMS2;
    }

    public int getNumberMS3() {
        return numberMS3;
    }

    public ArrayList<Integer> getMs1ScanNumbers() {
        return ms1ScanNumbers;
    }

    public ArrayList<Integer> getMs2ScanNumbers() {
        return ms2ScanNumbers;
    }

    public ArrayList<Integer> getMs3ScanNumbers() {
        return ms3ScanNumbers;
    }

    public static int getPreviousMS1ScanNumber(ScanLists scanListIn, int msNScanNumber) {
        int out = 1;
        if (msNScanNumber == 1)
            return out;
        for (int ms1ScanNumber : scanListIn.getMs1ScanNumbers()) {
            if (ms1ScanNumber > msNScanNumber)
                break;
            out = ms1ScanNumber;
        }
        return out;
    }

    //return List with next n MS1 scans
    //doesn't matter if the scan you give is MS1 or MSn...just return next full scans
    public ArrayList<Integer> getNextNMS1Scans(int startingScan, int n) {
        ArrayList<Integer> out = new ArrayList<>();
        //contains is O(n) and doesn't scale well, but whatever for the numbers involved here
        int usedStartingScan = 0;
        if (this.ms1ScanNumbers.contains(startingScan))
            usedStartingScan = startingScan;
        else
            //this sets the previous MS1 scan as the starting scan. if ONLY the NEXT MS1 scans are considered in output, still the only SUBSEQUENT scans will be considered
            usedStartingScan = getPreviousMS1ScanNumber(this, startingScan);

        //determine startingScanIndex
        int startingScanIndex = 0;
        for (int i = 0; i < this.ms1ScanNumbers.size(); i++) {
            int current = this.ms1ScanNumbers.get(i);
            if (current == usedStartingScan) {
                startingScanIndex = i;
                break;
            }
        }
        //check if there are that many scans still present; if not, do maximum number of scans possible
        if (this.ms1ScanNumbers.size()< startingScanIndex + n){
            n = this.ms1ScanNumbers.size()-startingScanIndex-1;
        }
        //copy all ints from starting scan index +1 to +n
        for(int i = 0; i < n; i++){
           out.add(this.ms1ScanNumbers.get(startingScanIndex+1+i));
        }
        return out;
    }
}
