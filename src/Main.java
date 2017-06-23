import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        /*String filePath = "C:\\Anwendungen\\IntelliJProjects\\NiceSpectraAnalyzer\\NiceSpectraAnalyzer\\Aminoacids_list.csv";
        File aminoAcids = new File(filePath);
        ArrayList<AminoAcid> aminoAcidsList = new ArrayList<>();
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        File spectrum = new File("C:\\Users\\micha\\Desktop\\5451.csv");
        MySpectrum testSpectrum = CSVReader.spectrumParse(spectrum);

        Peptide pepA = new Peptide("AAALAAADAR", aminoAcidsList);
        ArrayList<Modification> mods = new ArrayList<>();
        mods.add(Modification.uncleavedECDuplexNTerm());
        Peptide pepAMod = pepA.peptideModifier(mods);
        //pepAMod.createAddFragmentIonChargestate(2);
        System.out.println("");
        System.out.println("");
        ArrayList<FragmentIon> fragments = pepAMod.getbIons();
        fragments.addAll(pepAMod.getyIons());
        for (FragmentIon fragment : fragments){
            //FragmentIon.fragmentIonFormulaPrinter(fragment);
            FragmentIon.fragmentIonPrinter(fragment);
        }
       ArrayList<IonMatch> matchedIons = PeakCompare.peakCompare(testSpectrum, pepAMod, 5);
*/

        MzXMLFile file = new MzXMLFile(new File("C:\\Users\\Michael Stadlmeier\\Desktop\\Programmierzeugs\\20170519_stamch_ECDuplex_NEBBSA_newbatches_1to1_R1.mzxml"));
        Spectrum spectrum = file.getSpectrumById("2050");
        int spectraNumber = file.getSpectraCount();
        System.out.println("spectra count: "+spectraNumber);







    }
}
