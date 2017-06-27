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

        String filePathAcids = "C:\\Users\\Michael Stadlmeier\\Desktop\\Programmierzeugs\\Aminoacids_list.csv";
        File aminoAcids = new File(filePathAcids);
        ArrayList<AminoAcid> aminoAcidsList = new ArrayList<>();
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        //testing: read in spectrum
        String filePathSpectrum =  "C:\\Users\\Michael Stadlmeier\\Desktop\\Programmierzeugs\\20170529_stamch_EColi_1to1_BSA_1pmol_1ug.mzxml";
        File completemzXMLSource = new File(filePathSpectrum);
        MzXMLFile completemzXML = new MzXMLFile(completemzXMLSource);
        MySpectrum spectrumOfInterest = MzXMLReadIn.mzXMLToMySpectrum(completemzXML, "15681");

        //testing: creating peptides
        Peptide pepA = new Peptide("ALMEYDESLR", aminoAcidsList);
        ArrayList<Modification> mods = new ArrayList<>();
        mods.add(Modification.cleavedEC180NTerm());
        Peptide pepAMod = pepA.peptideModifier(mods);
        System.out.println("");
        System.out.println("");
        pepA.peptidePrinter();
        pepAMod.peptidePrinter();

        ArrayList<FragmentIon> fragments = new ArrayList<>();
        fragments.addAll(pepAMod.getbIons());
        fragments.addAll(pepAMod.getyIons());
        for (FragmentIon fragmentIon : fragments) {
            //FragmentIon.fragmentIonFormulaPrinter(fragment);
            FragmentIon.fragmentIonPrinter(fragmentIon);
        }

        //ArrayList<IonMatch> matchedIons = PeakCompare.peakCompare(spectrumOfInterest, pepAMod, 5);


        /*String filePath = "C:\\Universität\\Doktorarbeit\\Aktuelle Massedaten\\Programmiertests\\20170529_stamch_EColi_1to1_BSA_1pmol_1ug.mzxml";
        File spectrumFile = new File(filePath);
        MzXMLFile completemzXML = new MzXMLFile(spectrumFile);
        System.out.println("Number of spectra: "+completemzXML.getSpectraCount());
        for (int i = 17000; i<18001;i++) {
            MySpectrum convertedSpectrum = MzXMLReadIn.mzXMLToMySpectrum(completemzXML, Integer.toString(i));
            System.out.println("MySpectrum created: "+i+"  with "+convertedSpectrum.getNumberOfPeaks()+" peaks!");
        }*/








    }
}
