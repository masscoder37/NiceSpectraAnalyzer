import com.sun.javaws.exceptions.InvalidArgumentException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by micha on 11/3/2017.
 */

//this class checks a complete MS-run for the occurrence of the reporter ions
    //loop through all the MS2-spectra and see if the mass is present
    //at the same time, to gain more information, also report the intensities of the reporter ions, if present


    //this class checks all the MS2-spectra
    //create output file with the respective label intensities and a report at the end how many % of all MS2-spectra contain the signals
public class LabelEfficiencyEvaluator {
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

        String[] header = new String[8];
        header[0] = "scan number";
        header[1] = "mass deviation light label [ppm]";
        header[2] = "abs. intensity light label [au]";
        header[3] = "rel. intensity light label [%]";
        header[4] = "mass deviation heavy label [ppm]";
        header[5] = "abs. intensity heavy label [au]";
        header[6] = "rel. intensity heavy label [%]";
        header[7] = "ratio light label/heavy label";

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

        while (ms2Scans.hasNext()){

            //TODO: read from MzXML spectrum or convert to MySpectrum Class
            MzXMLSpectrum currentMzXMLSpectrum = new MzXMLSpectrum(ms2Scans.next());



        }














    }
}
