import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.IndexElement;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 * for more information, please contact me!
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, IOException, InterruptedException {
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat twoDec = new DecimalFormat("0.00");

        //Visualization.spectrumPlotter(mzXMLRun,10);
        String fastaFile = "C:\\Programmingfolder\\FASTA_lists\\HomoSapiens_Uniprot_16102020.fasta";
        String queryFile = "C:\\Programmingfolder\\FASTA_lists\\MBP.fasta";

        //FastaTools.peptideUniquenessChecker(queryFile, fastaFile, 0,2, "Trypsin");


        Peptide peptide = new Peptide("AK", AminoAcid.getAminoAcidList());
        ArrayList<Modification> modList = new ArrayList<>();
        modList.add(Modification.tmtPro0Comp());
        peptide = peptide.peptideModifier(modList);
        System.out.println("lalala");










































    }





    }

