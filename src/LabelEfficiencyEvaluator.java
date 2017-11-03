import com.sun.javaws.exceptions.InvalidArgumentException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by micha on 11/3/2017.
 */

//this class checks a complete MS-run for the occurence of the reporter ions
    //loop through all the MS2-spectra and see if the mass is present
    //at the same time, to gain more information, also report the intensities of the reporter ions, if present


    //this class checks all the MS2-spectra
    //create output file with the respective label intensities and a report at the end how many % of all MS2-spectra contain the signals
public class LabelEfficiencyEvaluator {
    public static void labelEvaluator(MzXMLFile mzXMLFileIn, String reagentIn, double ppmDevIn, String filePathtoStore){
        //check if label is known
        //currently accepted labels:
        //SOT - sulfoxide tag
        //TMT - tandem mass tag
        if (!reagentIn.equals("TMT")&&!reagentIn.equals("EC")&&!reagentIn.equals("SOT"))
            throw new IllegalArgumentException("Label not know: "+reagentIn"! Please use the labels TMT or SOT/EC!");

        //first, prepare .csv-File Output
        filePathtoStore = filePathtoStore + "labelAnalysis.csv";
        File csvFile = new File(filePathtoStore);
        try{
            PrintWriter csvPrinter = new PrintWriter(csvFile);
        }
        catch (FileNotFoundException e){
            System.out.println("File Location was not found! "+filePathtoStore);
        }
        //prepare header
        //[0] Scan Number
        //[1] Mass Dev Light Tag
        //[2] Intensity Light Tag
        //[3] rel. intensity Light Tag
        //[4] Mass Dev heavy Tag
        //[5] Intensity heavy Tag
        //[6] rel. intensity heavy Tag
        //[7] ratio light/heavy
        //these lines need to be added
        //[8] number analyzed MS2-spectra
        //[9] number light tag found
        //[10] number heavy tag found
        //[11] % of spectra labelled light
        //[12] % of spectra labelled heavy
        //[13] median rel. Intensity light
        //[14] meadian rel. intensity heavy

        String[] header = new String[15];
        header[0] = "scan number";
        header[1] = "mass deviation light label [ppm]";
        header[2] = "abs. intensity light label [au]";
        header[3] = "rel. intensity light label [%]";
        header[4] = "mass deviation heavy label [ppm]";
        header[5] = "abs. intensity heavy label [au]";
        header[6] = "rel. intensity heavy label [%]";
        header[7] = "ratio light label/heavy label";

        header[8] = "analyzed MS²-spectra";
        header[9] = "count light label found";
        header[10] = "count heavy label found";
        header[11] = "spectra labeled light [%]";
        header[12] = "spectra labeled heavy [%]";
        header[13] = "median rel. intensity light label [%]";
        header[14] = "median rel. intensity heavy label [%]";












    }
}
