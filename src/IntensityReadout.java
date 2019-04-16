import com.sun.org.apache.xpath.internal.SourceTree;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 * Created by Michael Stadlmeier on 3/1/2019.
 * This class will be used to output the Intensity of a given ion for many full ms spectra
 */
public class IntensityReadout {
    public static void intensityReadout(MzXMLFile mzXMLFileIn, double massToAnalyze, String FilePathOut) throws FileNotFoundException, MzXMLParsingException, JMzReaderException {
        //readout file information
        String fileName = mzXMLFileIn.getParentFile().get(0).getFileName();
        String massToCheck = Double.toString(massToAnalyze);

        //set up .csv file to save analysis results
        String outputFilePath = FilePathOut + "intensityReadout_"+Double.toString(massToAnalyze)+".csv";
        File csvOut = new File(outputFilePath);

        //set up Printwriter to write to csv file and write header
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(csvOut);
        } catch (FileNotFoundException e) {
            System.out.println("Filepath not valid! Path: " + outputFilePath);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(fileName + ',');
        sb.append(massToCheck + '\n');
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);
        //write header
        //Spectrum   Intensity
        sb.append("Spectrum Number,");
        sb.append("Intensity,");
        sb.append('\n');
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //start analysis of spectra
        List<Long> spectrumList = mzXMLFileIn.getScanNumbers();
        int analyzedSpectra = 0;

        for (Long scanNumber : spectrumList) {
            MySpectrum currentSpectrum = MzXMLReadIn.mzXMLToMySpectrum(mzXMLFileIn, scanNumber.toString());
            //extract Peaklist from spektrum
            ArrayList<Peak> peakList = currentSpectrum.getPeakList();
            boolean peakFound = false;
            double intensity = 0;
            for (Peak peak : peakList) {
                if (DeviationCalc.ppmMatch(massToAnalyze, peak.getMass(), 5)) {
                    intensity = peak.getIntensity();
                    peakFound = true;
                }
                //stop for loop if peak is found
                if (peakFound)
                    break;
            }

            //prepare String
            sb.append(scanNumber.toString() + ',');
            sb.append(Double.toString(intensity) + ',');
            sb.append('\n');

            //write to .csv
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            analyzedSpectra++;
            System.out.println("Spectra analyzed: " + analyzedSpectra);
        }

        pw.close();


    }


}

