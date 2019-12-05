import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 11/3/2017.
 */

//this class checks a complete MS-run for the occurrence of the reporter ions
    //loop through all the MS2-spectra and see if the mass is present
    //at the same time, to gain more information, also report the intensities of the reporter ions, if present


    //this class checks all the MS2-spectra
    //create output file with the respective label intensities and a report at the end how many % of all MS2-spectra contain the signals
public class LabelEfficiencyEvaluator {

    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");
    public static void labelEvaluator(MzXMLFile mzXMLFileIn, String reagentIn, double ppmDevIn, String filePathToStore) throws FileNotFoundException, MzXMLParsingException {
        //check if label is known
        //currently accepted labels:
        //SOT/EC - sulfoxide tag
        //TMT - tandem mass tag
        if (!reagentIn.equals("TMT")&&!reagentIn.equals("EC")&&!reagentIn.equals("SOT"))
            throw new IllegalArgumentException("Label not know: "+reagentIn+"! Please use the labels TMT or SOT/EC!");

        double lightTagMass = 0;
        double heavyTagMass = 0;

        if (reagentIn.equals("TMT")){
            lightTagMass = 126.12773;
            heavyTagMass = 127.13108;
        }
        if (reagentIn.equals("SOT")||reagentIn.equals("EC")){
            lightTagMass = 179.08487;
            heavyTagMass = 180.08823;
        }







        //first, prepare .csv-File Output
        filePathToStore = filePathToStore + "labelAnalysis.csv";
        File csvFile = new File(filePathToStore);
        PrintWriter csvPrinter = new PrintWriter(csvFile);

        //prepare header
        //[0] Scan Number
        //[1] Mass Dev Light Tag
        //[2] Intensity Light Tag
        //[3] rel. intensity Light Tag
        //[4] Mass Dev heavy Tag
        //[5] Intensity heavy Tag
        //[6] rel. intensity heavy Tag
        //[7] ratio light/heavy
        //these lines need to be added after the analysis
        //[8] number analyzed MS2-spectra
        //[9] number light tag found
        //[10] number heavy tag found
        //[11] % of spectra labelled light
        //[12] % of spectra labelled heavy
        //[13] median rel. Intensity light
        //[14] meadian rel. intensity heavy

        String[] header = new String[9];
        header[0] = "scan number";
        header[1] = "peaks found";
        header[2] = "mass deviation light label [ppm]";
        header[3] = "abs. intensity light label [au]";
        header[4] = "rel. intensity light label [%]";
        header[5] = "mass deviation heavy label [ppm]";
        header[6] = "abs. intensity heavy label [au]";
        header[7] = "rel. intensity heavy label [%]";
        header[8] = "ratio light label/heavy label";

        String[] postHeader = new String[7];
        postHeader[0] = "analyzed MS²-spectra";
        postHeader[1] = "count light label found";
        postHeader[2] = "count heavy label found";
        postHeader[3] = "spectra labeled light [%]";
        postHeader[4] = "spectra labeled heavy [%]";
        postHeader[5] = "median rel. intensity light label [%]";
        postHeader[6] = "median rel. intensity heavy label [%]";

        //now, write header
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String headerCaption : header){
            sb.append(sep);
            sb.append(headerCaption);
            sep = ";";
        }
        csvPrinter.write(sb.toString());
        csvPrinter.flush();
        //empty sb
        sb.setLength(0);

        //now, loop through all the MS2-scans
        MzXMLFile.MzXMLScanIterator ms2Scans =  mzXMLFileIn.getMS2ScanIterator();


        //prepare some variables for the loop
        //no more peaks have to be compared if mass is higher than that
        double upperMassLimit = heavyTagMass +1;

        while (ms2Scans.hasNext()){

            //spectrum is converted to my file format for easier handling
            MzXMLSpectrum currentMzXMLSpectrum = new MzXMLSpectrum(ms2Scans.next());
            MySpectrum currentMySpectrum = MzXMLReadIn.spectrumConvert(currentMzXMLSpectrum);

            //go through the peaks up until upperMassLimit
            ArrayList<Peak> spectraPeaks = new ArrayList<>();
            spectraPeaks = currentMySpectrum.getPeakList();

            //it is possible that there are more peaks that would match the masses, hence pick the one with the lowest mass deviation
            ArrayList<Peak> lightTagPeaks = new ArrayList<>();
            ArrayList<Peak> heavyTagPeaks = new ArrayList<>();

            for (Peak peak : spectraPeaks){
                double peakMass = peak.getMass();
                if (PeakCompare.isMatch(peakMass, lightTagMass, ppmDevIn))
                    lightTagPeaks.add(peak);
                if (PeakCompare.isMatch(peakMass, heavyTagMass, ppmDevIn))
                    heavyTagPeaks.add(peak);
                //break the loop if upper mass limit is reached
                if (peakMass > upperMassLimit)
                    break;
            }
            //now, the peakLists are filled and the best match has to be found

            Peak bestLightMatch;
            Peak bestHeavyMatch;
            double minppmDevLight = ppmDevIn;
            double minppmDevHeavy = ppmDevIn;

            //find the best light tag match
            for (Peak peak : lightTagPeaks){
                double ppmDev = DeviationCalc.ppmDeviationCalc(lightTagMass, peak.getMass());
                if (ppmDev<=minppmDevLight) {
                    minppmDevLight = ppmDev;
                    bestLightMatch = peak;
                }
            }
            //find the best heavy tag match
            for (Peak peak : heavyTagPeaks){
                double ppmDev = DeviationCalc.ppmDeviationCalc(heavyTagMass, peak.getMass());
                if (ppmDev<=minppmDevHeavy) {
                    minppmDevHeavy = ppmDev;
                    bestHeavyMatch = peak;
                }
            }

            //now, prepare the StringBuilder

            //TODO: handle what to do if peaks are not found

            String[] valuesForSB = new String[8];
            //[0] Scan Number
            //[1] Peaks found
            //[2] Mass Dev Light Tag
            //[3] Intensity Light Tag
            //[4] rel. intensity Light Tag
            //[5] Mass Dev heavy Tag
            //[6] Intensity heavy Tag
            //[7] rel. intensity heavy Tag
            //[8] ratio light/heavy

            valuesForSB[0] = currentMzXMLSpectrum.getId();










        }














    }
}
