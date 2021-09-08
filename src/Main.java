import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.IndexElement;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 * for more information, please contact me!
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, IOException, InterruptedException, JAXBException {
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat twoDec = new DecimalFormat("0.00");
        DecimalFormat xDec = new DecimalFormat("0.000000000");


        String runPath = "C:\\Users\\ms93\\Desktop\\spectroswiss_tgr_10805_aft_sn0p6_centroid-mzxml_2021-09-07_2128\\TGR_10805.mzXML";
        String resultFile = "C:\\Users\\ms93\\Google Drive\\PostDoc\\Projects\\SuperResolution\\SR2\\TGR_10805_SR2_Tar3_EColi_TMTproC_60min_1ug_unfiltered.csv";
        //MzXMLFile runMZXML = new MzXMLFile(runFile);
        //Visualization.spectrumPlotter(runMZXML, 10);
        //DnD.willNimaDie(100000000);
        //RandomTools.ms1PrecursorInfoAdvanced(peptidePath, runPath);
        //Benenodin.twoRotaxaneMassCreator(massListPath, false,2);
        //Benenodin.fragmentAnalyzerLumos(runPath, massListPath);
        //MLModifier.mzXMLModifier(runPath);
        RandomTools.fixTMTproCOutput(resultFile);



































































    }





    }

