import com.sun.org.apache.xpath.internal.operations.Mod;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.UserParam;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, FileNotFoundException {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        //testing: read in amino acids
        String filePathAcids = "C:\\Programmierordner\\Aminoacids_list.csv";
        File aminoAcids = new File(filePathAcids);
        ArrayList<AminoAcid> aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        //testing: read in spectrum
        String filePathSpectrum =  "C:\\Programmierordner\\20170529_stamch_EColi_1to1_BSA_14k_15k.mzXML";
        File completemzXMLSource = new File(filePathSpectrum);
        MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);
        //testing: creating peptides
        //Peptide pepA = new Peptide("LLADDVPSK", aminoAcidsList);
        ArrayList<Modification> modList = new ArrayList<>();
        //Modification oxidationM = new Modification("Oxidation", "O", 'M' );
        //modList.add(Modification.uncleavedECDuplex(1));

        ArrayList<CompClusterIonMatch> relevantMatches = new ArrayList<>();
        relevantMatches = ComplementaryClusterChecker.compClusterCheckerEC(aminoAcidsList, "FAENAYFIK", modList, "14223", completemzXML, 5 );
        String csvOutPath = "C:\\Programmierordner\\14223_withoutduplicates.csv";
        CSVCreator.compClusterMatchCSVPrinter(relevantMatches, csvOutPath);











    }
}
