import com.sun.org.apache.xpath.internal.operations.Mod;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.UserParam;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.PrecursorMz;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 * for more information, please contact me!
 * michael.stadlmeier@cup.uni-muenchen.de
 */
public class Main {

    //in this class, the analysis of MS-data can be done
    //in general, a compiler for JAVA 1.8 is required
    //follow the instructions in the sections
    //if you are done with a section, please comment the respective section. To do so, put /* at the beginning and */ at the end of a section
    //if a section you want to use is in a comment block, remove the comment block by removing /* at the beginning and */ at the end
    //please be advised that the jmzreader and the mzxml-parser class have to be imported for this application to function
    //you can find the source code of these classes here: https://github.com/PRIDE-Utilities/jmzReader



    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, FileNotFoundException {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        //The provided amino acids list is read.
        //TODO: Please change your file path accordingly so that it points to the provided amino acids list
        //This has to stay for the complete analysis
        String filePathAcids = "C:\\Programmierordner\\Aminoacids_list.csv";
        File aminoAcids = new File(filePathAcids);
        ArrayList<AminoAcid> aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        //Here, the spectrum to be analyzed has to be specified
        //It has to be a .mzXML-File which was centroided on MS1 and MS2-levels (see supporting information)
        //TODO: Please change your file path accordingly.
        String filePathSpectrum =  "C:\\Programmierordner\\SOT_HEK_EColi\\20171003_SOT_HEKBkgrnd_EColiSpikeIn_1to1.mzXML";
        //File completemzXMLSource = new File(filePathSpectrum);
        //generating the MzXMLFile object might take a few minutes and will display some warnings.
        //MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);


        //In this section, you have to supply the evidence.txt file from your MaxQuant analysis.
        //At the moment, the software assumes static carbamidomethylation on cysteine residues and variable methionine-oxidation
        //please filter out other modifications
        //TODO: Please change your file path accordingly.
        String evidenceLocation = "C:\\Programmierordner\\SOT_HEK_EColi\\SOT_EC_HEK_EColiSpikeIn_1to1.txt";
        //File evidence = new File(evidenceLocation);
        //TODO: Please provide a directory were the output Files will be saved
        String csvOutPath = "C:\\Programmierordner\\SOT_HEK_EColi\\Analysis\\";


        //Section 1
        //This section handles the first data analyis
        //it creates multiple .csv-Files (one for 500 analyzed spectra each) in the specified directory, containing all matched label-containing fragment ions
        //TODO: change the max. allowed mass deviation in ppm. Currently: 5 ppm; 4th entry
        //TODO: change the used label: use "EC" for the SOT-duplex or "TMT" for the TMT-duplex
        //CSVReader.wholeRunCICChecker(completemzXML, evidence, aminoAcidsList, 5, 250, csvOutPath, "EC");
        //TODO: after compilation, the files should be created! Put section 1 in a comment block!



        //Section 2

        //This line combines the created .csv Files to generate 1 complete file
        //TODO: Remove the comments from this section and put them in front of Section 1
        //CSVCreator.csvFileCombiner(csvOutPath);




        //Section 3
        //in this section, the peptide specific analysis of the results from section 2 is carried out
        //TODO: Remove the comments from this section and be sure that there are comments before and after sections 1 and 2
        //TODO: Change the file path to your file to analyze; in this case, to complete analysis


        String toAnalyze = "C:\\Programmierordner\\SOT_HEK_EColi\\Analysis\\SOT_EC_HEK_EColiSpikeIn_1to1.csv";
        //CSVAnalyzer.cicStatistics(toAnalyze);


        //Section 4
        //in this section, you can analyse the reporter ion intensities of the files
        //TODO:Remove the comments from this section and be sure that there are comments before and after sections 1, 2 and 3
        //TODO: You can specify the allowed reporter ion mass deviation [ppm]. Standard parameter is 5 ppm; 3rd entry
        String statisticsFilePath = "C:\\Programmierordner\\SOT_HEK_EColi\\Analysis\\SOT_EC_HEK_EColiSpikeIn_1to1_statistics.csv";
        //File statisticsFile = new File(statisticsFilePath);
        //CSVReader.wholeRunRepFinder(completemzXML, statisticsFile ,5);

        //Section 5
        //Complementary Ion Cluster analysis
        String fragmentIonFilePath = "C:\\Programmierordner\\SOT_HEK_EColi\\Analysis\\SOT_EC_HEK_EColiSpikeIn_1to1.csv";
       // CSVAnalyzer.cicRatioCalculator(fragmentIonFilePath);

        String testFilePath = "C:\\Programmierordner\\BSA_MS1Diff_Analysis\\TMT\\20171009_stamch_NEB_BSA_TMT_1to1_2uL.mzXML";
        //File testFileSource = new File(testFilePath);
        //MzXMLFile testmzXML = new MzXMLFile(testFileSource);

        /*ArrayList<String> peptidesToCheck = new ArrayList<>();
        peptidesToCheck.add("HLVDEPQNLIK");
        peptidesToCheck.add("HPEYAVSLLR");
        peptidesToCheck.add("VPQVSTPTLVEVSR");
        peptidesToCheck.add("QTALVELLK");
        peptidesToCheck.add("LVTDLTK");
        peptidesToCheck.add("TVMENFVAFVDK");
        peptidesToCheck.add("LVTDLTK");
        peptidesToCheck.add("NECFLSHK");
        peptidesToCheck.add("YCDNQDTISSK");
        peptidesToCheck.add("AEFVEVTK");
        peptidesToCheck.add("YLYEIAR");*/



        /*ArrayList<Double> massDiffList = new ArrayList<>();
        massDiffList = PrecursorMassDiffBinner.precursorMassDiffBinner(testmzXML, peptidesToCheck, aminoAcidsList);
        System.out.println("List size: "+massDiffList.size());
        String csvMassDiffOut = "C:\\Programmierordner\\BSA_MS1Diff_Analysis\\TMT\\";
        CSVCreator.createMassDiffCSV(massDiffList, csvMassDiffOut);
        System.out.println("MS2-scans: "+testmzXML.getMS2ScanCount());*/

        String ir7String = "IEAKGER";
        Peptide ir7 = new Peptide(ir7String, aminoAcidsList);
        ArrayList<Modification> modList = new ArrayList<>();
        modList.add(Modification.acetylation(1));
        modList.add(Modification.uncleavedECDuplex(4));
        Peptide modIR7intact = ir7.peptideModifier(modList);
        modIR7intact.peptidePrinter();
        modList.clear();
        modList.add(Modification.acetylation(1));
        modList.add(Modification.cleavedEC179(4));


        String spectrumFilePath = "C:\\Programmierordner\\IR7_SOT179_MSMS.csv";
        File ir7File = new File(spectrumFilePath);
        MySpectrum ir7Spectrum = CSVReader.spectrumParse(ir7File);
        PeakCompare.peakCompare(ir7Spectrum, modIR7intact, 5);
        System.out.println();

        ir7 = new Peptide(ir7String, aminoAcidsList);
        Peptide modIR7Cleaved = ir7.peptideModifier(modList);
        modIR7Cleaved.peptidePrinter();
        PeakCompare.peakCompare(ir7Spectrum, modIR7Cleaved, 5);


































    }
}
