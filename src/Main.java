import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.IndexElement;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
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

        File runFile = new File("D:\\Programmingfolder\\TGR_08914.mzXML");
        MzXMLFile runMZXML = new MzXMLFile(runFile);

        System.out.println("Number of Scans: "+runMZXML.getMS1ScanCount());

        //Visualization.spectrumPlotter(runMZXML, 10);

        Spectrum currentSpectrum = runMZXML.getSpectrumById("272");
        System.out.println("bla");






































































    }





    }

