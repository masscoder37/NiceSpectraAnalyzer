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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        //testing: read in amino acids
        String filePathAcids = "C:\\Programmierordner\\Aminoacids_list.csv";
        File aminoAcids = new File(filePathAcids);
        ArrayList<AminoAcid> aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        //testing: read in spectrum
        String filePathSpectrum =  "C:\\Programmierordner\\20170529_stamch_EColi_1to1_BSA_1pmol_1ug.mzXML";
        File completemzXMLSource = new File(filePathSpectrum);
        MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);
        //testing: creating peptides
        Peptide pepA = new Peptide("LLADDIVPSR", aminoAcidsList);
        ArrayList<Modification> modList = new ArrayList<>();
        Modification testMod = new Modification("Oxidation", "O", 'M' );
        //modList.add(testMod);

        /*ArrayList<ArrayList<Modification>> testList = ComplementaryClusterChecker.modCreator(pepA, 3, modList);
        System.out.println("Size: "+testList.size());
        int i = 1;
        for (ArrayList<Modification> modLists : testList){
            System.out.println("List"+i+":");
            for (Modification mod : modLists){
                System.out.println("Modification: "+mod.getModificationName());
            }
            System.out.println("");
            i++;
        }*/

        ComplementaryClusterChecker.compClusterCheckerEC(aminoAcidsList, "NIYDYYK", modList, "8794", completemzXML, 5 );













    }
}
