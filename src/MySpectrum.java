import java.text.DecimalFormat;
import java.util.ArrayList;

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

    public MySpectrum(ArrayList<Peak> peaksIn, int scanNumberIn, String scanHeaderIn){
        this.scanNumber = scanNumberIn;
        this.scanHeader = scanHeaderIn;
        //make sure that peakList is ordered
        //implemented QuickSort Algorithm
        peaksIn = MassQuickSort.peakListQuickSort(peaksIn);
        //remove peaks with intensity of 0
        this.peakList = peakPacker(peaksIn);
        this.numberOfPeaks = this.peakList.size();

    }


    //use this function to remove peaks not wanted in spectrum and to assign rel Intensities and base peak
    private ArrayList<Peak> peakPacker (ArrayList<Peak> peaksToPack){
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
        System.out.println("");

        System.out.println(""+wrongScanNumber+" peak(s) were removed(different scan number).");
        System.out.println("");

        return peaksToPack;
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

    public int[] getChargeStateDistributionNumber(){
        int chargeUnknown=0;
        int charge1=0;
        int charge2=0;
        int charge3=0;
        int charge4=0;
        int chargeHigher=0;
        for (Peak peak : this.peakList){
            int current = peak.getCharge();
            switch(current){
                case 0: chargeUnknown++;
                break;
                case 1: charge1++;
                break;
                case 2: charge2++;
                break;
                case 3: charge3++;
                break;
                case 4: charge4++;
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

    public void spectrumPrinter(){
        System.out.println("");
        System.out.println("Generating Spectrum information...");
        System.out.println("Scan Header: "+this.scanHeader);
        System.out.println("Scan Number: "+this.scanNumber);
        double summedIntensity = 0;
        for (Peak peak : this.peakList){
            System.out.println("Peak mass: "+fourDec.format(peak.getMass())
                    +"   Charge: "+peak.getCharge()
                    +"   Rel. Int.: "+twoDec.format(peak.getRelIntensity())
                    +"   Base Peak: "+peak.getBasePeak());
            summedIntensity += peak.getRelIntensity();
        }
        System.out.println("");
        System.out.println("General spectra properties:");
        System.out.println("");
        System.out.println("Number of Peaks: "+this.peakList.size());
        System.out.println("Mean rel. Intensity: "+twoDec.format((summedIntensity / this.peakList.size()))+"%");
        int basePeakIndex = this.getBasePeakIndex();
        double maxInt = this.peakList.get(basePeakIndex).getIntensity();
        System.out.println("Base peak Intensity: "+scientific.format(maxInt));
        System.out.println("End of spectrum analysis!");
        System.out.println("");
    }

    public void smallIntensitiesRemover (){
        ArrayList<Peak> convolutedList = this.peakList;
        ArrayList<Peak> deconvolutedList = new ArrayList<>();
        int removedPeaks = 0;
        for (Peak p : convolutedList){
            if (p.getIntensity() != 0){
                deconvolutedList.add(p);
                removedPeaks++;
            }
        }
        System.out.println("Number of removed Peaks with low intensity: "+removedPeaks);

        this.peakList = peakPacker(deconvolutedList);
        this.numberOfPeaks = this.peakList.size();
    }

}
