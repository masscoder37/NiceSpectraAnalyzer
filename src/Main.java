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
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        String filePathSpectrum =  "C:\\Programmierordner\\EC_HEK\\20170821_stamch_HEKLysate_EC_1to1_5uL.mzXML";
        File completemzXMLSource = new File(filePathSpectrum);
        //MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);

        //testing: creating peptides
        //Peptide pepA = new Peptide("LLADDVPSK", aminoAcidsList);
        //ArrayList<Modification> modList = new ArrayList<>();
        //Modification oxidationM = new Modification("Oxidation", "O", 'M' );
        //modList.add(Modification.uncleavedECDuplex(1));

        String evidenceLocation = "C:\\Programmierordner\\EC_HEK\\21082017_EC_TMT_HEK_filtered.txt";
        File evidence = new File(evidenceLocation);
        String csvOutPath = "C:\\Programmierordner\\completeAnalysisTMT";
        //CSVCreator.compClusterMatchCSVPrinter(relevantMatches, csvOutPath);
        //CSVReader.wholeRunCICChecker(completemzXML, evidence, aminoAcidsList, 5, 500, csvOutPath, "EC");

        //CSVCreator.csvFileCombiner("C:\\Programmierordner\\EC_HEK\\Complete Analysis\\");

        /*File csvFileLeander = new File ("C:\\Programmierordner\\ILNAHMDSLQWVDQSSALLQR_MSMS.csv");
        MySpectrum csvSpectrumLeander = CSVReader.spectrumParse(csvFileLeander);
        Peptide ilnahmdslqwvdqssallqr = new Peptide("ILNAHMDSLQWVDQSSALLQR", aminoAcidsList);
        ilnahmdslqwvdqssallqr.peptidePrinter();
        Modification hvXL = new Modification("photoXL", "C21H23N3O4S", 'M');
        ArrayList<Modification> modList = new ArrayList<>();
        modList.add(hvXL);
        Peptide modPep = ilnahmdslqwvdqssallqr.peptideModifier(modList);
        modPep.peptidePrinter();

        PeakCompare.peakCompare(csvSpectrumLeander, modPep, 10);*/

        //String toAnalyze = "C:\\Programmierordner\\EC_HEK\\Complete Analysis\\EC_HEK_complete.csv";
        //CSVAnalyzer.cicStatistics(toAnalyze);
        //String statisticsFilePath = "C:\\Programmierordner\\EC_HEK\\Complete Analysis\\EC_HEK_complete_statistics.csv";
        //File statisticsFile = new File(statisticsFilePath);
        //CSVReader.wholeRunRepFinder(completemzXML, statisticsFile ,5);

       Peptide testPeptide = new Peptide("EMLPVLEAVAK", aminoAcidsList);
       ArrayList<Modification> modList = new ArrayList<>();
       modList.add(Modification.uncleavedECDuplex(1));
       modList.add(Modification.uncleavedECDuplex(11));
       Peptide modPeptide = testPeptide.peptideModifier(modList);
       modPeptide.peptidePrinter();
       ArrayList<FragmentIon> fragments = new ArrayList<>();
       fragments.addAll(modPeptide.getbIons());
       fragments.addAll(modPeptide.getyIons());
       for (FragmentIon f : fragments){
           FragmentIon.fragmentIonPrinter(f);
       }
       File spectrumFile = new File("C:\\Users\\Michael Stadlmeier\\Desktop\\SOT-paper\\current version\\Figure 3\\V04\\EMLPVLEAVAK_24249.csv");
        MySpectrum csvPepToCheck = CSVReader.spectrumParse(spectrumFile);
        PeakCompare.peakCompare(csvPepToCheck, modPeptide, 5);



        //SumFormula abundanceTest = new SumFormula("C18H33N5O5H+1");
        //System.out.println("Peptide mass: "+abundanceTest.getExactMass());
        //System.out.println("Abundance of first isotopic peak: "+IsotopicDistributer.abundanceAddNeutron(abundanceTest)*100);


















    }
}
