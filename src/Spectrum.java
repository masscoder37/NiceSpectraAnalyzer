import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/20/2017.
 */
// uses an ArrayList of Peaks to create a spectrum
public class Spectrum {
    private ArrayList<Peak> peakList;
    private int numberOfPeaks;
    private int scanNumber;
    private String scanHeader;

    public Spectrum(ArrayList<Peak> peaksIn, int scanNumberIn, String scanHeaderIn){
        this.scanNumber = scanNumberIn;
        this.scanHeader = scanHeaderIn;
        this.peakList = peakPacker(peaksIn);
        this.numberOfPeaks = this.peakList.size();

    }


    //use this function to remove peaks not wanted in spectrum and to asign rel Intensities and base peak
    private ArrayList<Peak> peakPacker (ArrayList<Peak> peaksToPack){
        ArrayList<Peak> packedPeaks = new ArrayList<>();
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
                peakNumber = peakNumber -1;
                wrongScanNumber++;
            }
        }
        //find highest intensity
        for (Peak peak : peaksToPack){
            currentInt = peak.getIntensity();
            if (currentInt > highestInt){
                highestInt = currentInt;
            }
        }
        //calculate rel Intensities
        for (Peak peak : peaksToPack){
            relIntCalc = (peak.getIntensity()/highestInt)*100;
            peak.setRelInt(relIntCalc);
            if (relIntCalc == 100){
                peak.setBasePeak();
            }
        }

        System.out.println("");
        System.out.println("Generating Peak list:");
        System.out.println(""+wrongScanNumber+" peak(s) were removed(different scan number).");
        System.out.println("");

        return packedPeaks;
    }

    public ArrayList<Peak> getPeakList(){ return this.peakList;}
    public int getScanNumber(){return   this.scanNumber;}
    public int getNumberOfPeaks(){return this.numberOfPeaks;}
    public String getScanHeader(){return this.scanHeader;}
    public int getBasePeakIndex(){
        int index = 0;
        for (int i = 0; i<this.numberOfPeaks;i++){
            if (this.peakList.get(i).getBasePeak())
                index = i;
        }
        return index;
    }
}
