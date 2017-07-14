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
        String filePathSpectrum =  "C:\\Programmierordner\\20170529_stamch_EColi_1to1_BSA_1pmol_1ug.mzXML";
        //File completemzXMLSource = new File(filePathSpectrum);
        //MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);

        //testing: creating peptides
        //Peptide pepA = new Peptide("LLADDVPSK", aminoAcidsList);
        //ArrayList<Modification> modList = new ArrayList<>();
        //Modification oxidationM = new Modification("Oxidation", "O", 'M' );
        //modList.add(Modification.uncleavedECDuplex(1));

        String evidenceLocation = "C:\\Programmierordner\\24052017_EC_BSA_SpikeIn_evidence_1to1.txt";
        //File evidence = new File(evidenceLocation);
        //ArrayList<CompClusterIonMatch> relevantMatches = new ArrayList<>();
        //relevantMatches = CSVReader.wholeRunCICChecker(completemzXML, evidence, aminoAcidsList, 5);
        String csvOutPath = "C:\\Programmierordner\\completeAnalysisTMT";
        //CSVCreator.compClusterMatchCSVPrinter(relevantMatches, csvOutPath);
        //CSVReader.wholeRunCICChecker(completemzXML, evidence, aminoAcidsList, 5, 500, csvOutPath, "EC");

        //CSVCreator.csvFileCombiner("C:\\Programmierordner\\completeAnalysis_withz2_EC\\");

        File csvFileLeander = new File ("C:\\Programmierordner\\ILNAHMDSLQWVDQSSALLQR_MSMS.csv");
        MySpectrum csvSpectrumLeander = CSVReader.spectrumParse(csvFileLeander);
        Peptide ilnahmdslqwvdqssallqr = new Peptide("ILNAHMDSLQWVDQSSALLQR", aminoAcidsList);
        ilnahmdslqwvdqssallqr.peptidePrinter();
        Modification hvXL = new Modification("photoXL", "C21H23N3O4S", 'M');
        ArrayList<Modification> modList = new ArrayList<>();
        modList.add(hvXL);
        Peptide modPep = ilnahmdslqwvdqssallqr.peptideModifier(modList);
        modPep.peptidePrinter();

        PeakCompare.peakCompare(csvSpectrumLeander, modPep, 10);
















    }
}
