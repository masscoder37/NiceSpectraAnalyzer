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

        String filePath = "C:\\Universität\\Doktorarbeit\\Aktuelle Massedaten\\24052017_EC_BSA_SpikeIn\\with Tag\\3 ratios\\20170529_stamch_EColi_1to1_BSA_1pmol_1ug.mzXML";
        File spectrumFile = new File(filePath);
        MzXMLFile mzXMLFile = new MzXMLFile(spectrumFile);

        MySpectrum convertedSpectrum = MzXMLReadIn.mzXMLToMySpectrum(mzXMLFile, "4031");
        Spectrum uncovertedSpectrum = mzXMLFile.getSpectrumById("4031");
        System.out.println("unconverted Spectrum Size: "+uncovertedSpectrum.getPeakList().size());
        //convertedSpectrum.spectrumPrinter();








    }
}
