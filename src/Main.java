import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

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
    //please be advised that the jmzreader and the mzxml-parser modules have to be imported for this application to work
    //you can find the source code of these classes here: https://github.com/PRIDE-Utilities/jmzReader



    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, FileNotFoundException {
        DecimalFormat fourDec = new DecimalFormat("0.0000");

        //The provided amino acids list is read.
        //TODO: Please change your file path accordingly so that it points to the provided amino acids list
        //This has to stay for the complete analysis
        String filePathAcids = "C:\\Programmingfolder\\AAList\\Aminoacids_list.csv";
        File aminoAcids = new File(filePathAcids);
        ArrayList<AminoAcid> aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        //Here, the spectrum to be analyzed has to be specified
        //It has to be a .mzXML-File which was centroided on MS1 and MS2-levels (see supporting information)
        //TODO: Please change your file path accordingly.
        //String filePathSpectrum =  "C:\\MeroX Massedaten\\CMT labeling\\lsSOT\\20190315_LabelTest_EColi_lsSOT_1x_1p5uL.mzXML";
        //File completemzXMLSource = new File(filePathSpectrum);
        //generating the MzXMLFile object might take a few minutes and will display some warnings.
        //MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);


        //In this section, you have to supply the evidence.txt file from your MaxQuant analysis.
        //At the moment, the software assumes static carbamidomethylation on cysteine residues and variable methionine-oxidation
        //please filter out other modifications
        //TODO: Please change your file path accordingly.
        //String evidenceLocation = "C:\\MeroX Massedaten\\CMT labeling\\lsSOT\\txt\\evidence.txt";
        //File evidence = new File(evidenceLocation);
        //TODO: Please provide a directory were the output Files will be saved
        //String csvOutPath = "C:\\MeroX Massedaten\\CMT labeling\\lsSOT\\Analyse\\";




        //Section 1
        //This section handles the first data analyis
        //it creates multiple .csv-Files (one for 500 analyzed spectra each) in the specified directory, containing all matched label-containing fragment ions
        //TODO: change the max. allowed mass deviation in ppm. Currently: 5 ppm; 4th entry
        //TODO: change the used label: use "EC" for the SOT-duplex or "TMT" for the TMT-duplex
        //CSVReader.wholeRunCICChecker(completemzXML, evidence, aminoAcidsList, 5, csvOutPath, "lsST");
        //TODO: after compilation, the files should be created! Put section 1 in a comment block!



        //Section 2

        //This line combines the created .csv Files to generate 1 complete file
        //TODO: Remove the comments from this section and put them in front of Section 1
        //CSVCreator.csvFileCombiner(csvOutPath);




        //Section 3
        //in this section, the peptide specific analysis of the results from section 2 is carried out
        //TODO: Remove the comments from this section and be sure that there are comments before and after sections 1 and 2
        //TODO: Change the file path to your file to analyze; in this case, to complete analysis

        //String toAnalyze = "C:\\Programmierordner\\Analyse_Wuehr\\My Analysis\\labelFragmentIons.csv";
        //CSVAnalyzer.cicStatistics(toAnalyze, "EC");


        //Section 4
        //in this section, you can analyse the reporter ion intensities of the files
        //TODO:Remove the comments from this section and be sure that there are comments before and after sections 1, 2 and 3
        //TODO: You can specify the allowed reporter ion mass deviation [ppm]. Standard parameter is 5 ppm; 3rd entry
        //String statisticsFilePath = "C:\\Programmierordner\\Analyse_Wuehr\\My Analysis\\labelFragmentIons_statistics.csv";
        //File statisticsFile = new File(statisticsFilePath);
        //CSVReader.wholeRunRepFinder(completemzXML, statisticsFile ,5, "EC");

        //Section 5
        //Complementary Ion Cluster analysis
        //String fragmentIonFilePath = "C:\\Programmierordner\\Analyse_Wuehr\\My Analysis\\labelFragmentIons.csv";
        //CSVAnalyzer.cicRatioCalculator(fragmentIonFilePath);

        //Section 6
        //Complementary Ion Cluster analysis per Peptide
        //String cicFilePath = "C:\\Programmierordner\\Analyse_Wuehr\\My Analysis\\labelFragmentIons_complementaryClusters_nocutoff.csv";
        //CSVAnalyzer.clusterRatioPerPeptide(cicFilePath);

        /*String testFasta = "DTHKSEIAHRFKDLGEEHFKGLVLIAFSQYLQQCPFDEHVKLVNELTEFAKTCVADESHAGCEKSLHTLFGDELCKVASLRETYGDMADCCEKQEPERNECFLSHKDDSPDLPKLKPDPNTLCDEFKADEKKFWGKYLYEIARRHPYFYAPELLYYANKYNGVFQECCQAEDKGACLLPKIETMREKVLASSARQRLRCASIQKFGERALKAWSVARLSQKFPKAEFVEVTKLVTDLTKVHKECCHGDLLECADDRADLAKYICDNQDTISSKLKECCDKPLLEKSHCIAEVEKDAIPENLPPLTADFAEDKDVCKNYQEAKDAFLGSFLYEYSRRHPEYAVSVLLRLAKEYEATLEECCAKDDPHACYSTVFDKLKHLVDEPQNLIKQNCDQFEKLGEYGFQNALIVRYTRKVPQVSTPTLVEVSRSLGKVGTRCCTKPESERMPCTEDYLSLILNRLCVLHEKTPVSEKVTKCCTESLVNRRPCFSALTPDETYVPKAFDEKLFTFHADICTLPDTEKQIKKQTALVELLKHKPKATEEQLKTVMENFVAFVDKCCAADDKEACFAVEGPKLVVSTQTALA";
        ArrayList<String> testDigest = new ArrayList<>();
        testDigest = FastaDigester.digestFasta(testFasta, "Trypsin", 1);
        for (String pep : testDigest){
            System.out.println(pep);
        }*/

       /*String path = "C:\\Programmingfolder\\Distances_BSA\\";
       String sequence = "DTHKSEIAHRFKDLGEEHFKGLVLIAFSQYLQQCPFDEHVKLVNELTEFAKTCVADESHAGCEKSLHTLFGDELCKVASLRETYGDMADCCEKQEPERNECFLSHKDDSPDLPKLKPDPNTLCDEFKADEKKFWGKYLYEIARRHPYFYAPELLYYANKYNGVFQECCQAEDKGACLLPKIETMREKVLASSARQRLRCASIQKFGERALKAWSVARLSQKFPKAEFVEVTKLVTDLTKVHKECCHGDLLECADDRADLAKYICDNQDTISSKLKECCDKPLLEKSHCIAEVEKDAIPENLPPLTADFAEDKDVCKNYQEAKDAFLGSFLYEYSRRHPEYAVSVLLRLAKEYEATLEECCAKDDPHACYSTVFDKLKHLVDEPQNLIKQNCDQFEKLGEYGFQNALIVRYTRKVPQVSTPTLVEVSRSLGKVGTRCCTKPESERMPCTEDYLSLILNRLCVLHEKTPVSEKVTKCCTESLVNRRPCFSALTPDETYVPKAFDEKLFTFHADICTLPDTEKQIKKQTALVELLKHKPKATEEQLKTVMENFVAFVDKCCAADDKEACFAVEGPKLVVSTQTALA";

       ArrayList<AminoAcid> aaToCheck = new ArrayList<>();
       aaToCheck.add(AminoAcid.createSpecificAA("k"));
        aaToCheck.add(AminoAcid.createSpecificAA("Ser"));
        aaToCheck.add(AminoAcid.createSpecificAA("tyr"));
        aaToCheck.add(AminoAcid.createSpecificAA("tHR"));

       CSVCreator.xWalkInputFileGenerator(sequence,aaToCheck, path, AminoAcid.createSpecificAA("K"));*/


        


        String runFilePath = "C:\\Programmingfolder\\Thao_Hai\\TGR_08785.mzXML";
       File runFile = new File(runFilePath);
        //MzXMLFile mzXMLRun = new MzXMLFile(runFile);
        //System.out.println("spectrum number: "+mzXMLRun.getSpectraCount());
        //spectrum 16184
       //Scan testScan = mzXMLRun.getScanByNum(12423L);
      // List<Peaks> scanPeakList =   testScan.getPeaks();
     //  Spectrum testSpectrum = mzXMLRun.getSpectrumById("12394");




        String meroxFilePath = "C:\\Programmingfolder\\CID_HCD_Comparison\\newAnalysis\\TGR_08646_CID20_10ms_forAnalysis_CSV.csv";
        //File meroxFile = new File(meroxFilePath);

        //CSVReader.xlSpectraChecker(meroxFile, mzXMLRun, "CID", "cliXlink", aminoAcidsList, 10);


        /*Peptide unmod = new Peptide("PCMC", aminoAcidsList);
        ArrayList<Modification> modList1 = new ArrayList<>();
        modList1.add(Modification.carbamidomethylation());
        Peptide mod1 = unmod.peptideModifier(modList1);
        ArrayList<Modification> modList2 = new ArrayList<>();
        modList2.add(Modification.oxidation(3));
        Peptide mod2 = mod1.peptideModifier(modList2);*/

       //Visualization.spectrumPlotter(mzXMLRun,10);
        String toExtractFilePath = "C:\\Programmingfolder\\Thao_Hai\\defaultInput.csv";
        String scanDescriptionFilePath = "C:\\Programmingfolder\\Thao_Hai\\scanNumbersUsed.csv";


        //ExtractSpectrumData.getInjectionTimes(runFilePath);
        ExtractSpectrumData.massExctrator(runFilePath, toExtractFilePath);
        //System.out.println("Number of spectra: " +ExtractSpectrumData.getMsNScanCount(runFilePath, 686.9669, 2, "CID"));



        }





    }

