import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 10/8/2017.
 */

//class to analyze the mass differences between unmodified Peptides (or Peptides with known modifications) and the picked precursor masses
public class PrecursorMassDiffBinner {
    static DecimalFormat fourDec = new DecimalFormat("0.0000");

    public static ArrayList<Double> precursorMassDiffBinner(MzXMLFile mzXMLFileIn, ArrayList<String> peptidesToCheck, ArrayList<AminoAcid> aminoAcidsListIn) throws MzXMLParsingException {
        //only positive mass differences are stored, because modifications are concerned
        ArrayList<Double> massDiffList = new ArrayList<>();
        //first, make the Strings into peptides to get the peptide masses
        //if necessary, modify the peptides
        ArrayList<Peptide> peptidesList = new ArrayList<>();
        for (String peptideSequence : peptidesToCheck){
            try {
                Peptide newPeptide = new Peptide(peptideSequence, aminoAcidsListIn);
                ArrayList<Modification> mods = new ArrayList<>();
                if (peptideSequence.contains("C")){
                    mods.add(Modification.carbamidomethylation());
                }
                if (peptideSequence.contains("M")){
                    mods.add(new Modification("Oxidation", "O", 'M'));
                }
                newPeptide = newPeptide.peptideModifier(mods);
                peptidesList.add(newPeptide);
            }
            catch (IllegalArgumentException e){
                System.out.println("Queried Peptide sequence '" + peptideSequence + "' can't be translated into a valid peptide!");
            }
        }
        //now, loop through the MS2-scans, determine the unprotonated mass and check the mass difference of all the peptides
        MzXMLFile.MzXMLScanIterator ms2Iterator =  mzXMLFileIn.getMS2ScanIterator();
        while (ms2Iterator.hasNext()){
            MzXMLSpectrum currentSpectrum = new MzXMLSpectrum(ms2Iterator.next());
            Double precursorMZ = currentSpectrum.getPrecursorMZ();
            int precursorCharge = currentSpectrum.getPrecursorCharge();
            //uncharged Mass [M] of the Precursor
            double unchargedMass = precursorMZ * precursorCharge - precursorCharge *AtomicMasses.getPROTON();
            //loop through all the peptides
            for (Peptide peptide : peptidesList){
                Double difference = unchargedMass - peptide.getExactMass();
                //only add difference to the list if difference is positive
                if (difference > 0){
                    massDiffList.add(difference);
                }
            }
            System.out.println("Precursor Mass analyzed! Spectrum Number: "+currentSpectrum.getId());
            currentSpectrum = null;
        }
        return massDiffList;
    }
}
