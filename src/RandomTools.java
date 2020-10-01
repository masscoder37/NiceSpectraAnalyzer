import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RandomTools {
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");
    private static DecimalFormat twoDec = new DecimalFormat("0.00");

    private static class PeptideID {
        private String pepSequence;
        private int charge;
        private int scanNumber;
        private double xcorr;

        public PeptideID(String pepSequenceIn, int chargeIn, int scanNumberIn, double xcorrIn) {
            this.pepSequence = pepSequenceIn;
            this.charge = chargeIn;
            this.scanNumber = scanNumberIn;
            this.xcorr = xcorrIn;
        }

        public String getPepSequence() {
            return pepSequence;
        }

        public int getCharge() {
            return charge;
        }

        public int getScanNumber() {
            return scanNumber;
        }

        public double getXcorr() {
            return xcorr;
        }
    }
    private static class PeptideMatch{
        private PeptideID pepID1;
        private double pepID1MS2TIC;
        private PeptideID pepID2;
        private double pepID2MS2TIC;

        public PeptideMatch(PeptideID pepID1In, double pepID1MS2TICIn, PeptideID pepID2In, double pepID2MS2TICIn){
            this.pepID1 = pepID1In;
            this.pepID1MS2TIC = pepID1MS2TICIn;
            this.pepID2 = pepID2In;
            this.pepID2MS2TIC = pepID2MS2TICIn;
        }

        public PeptideID getPepID1() {
            return pepID1;
        }

        public double getPepID1MS2TIC() {
            return pepID1MS2TIC;
        }

        public PeptideID getPepID2() {
            return pepID2;
        }

        public double getPepID2MS2TIC() {
            return pepID2MS2TIC;
        }
    }


    //compare TIC of MS2 spectra for specific peptides and create a list with spectra to check
    public static void findSimilarSpectra(String output1In, String runFileLoc1In, String output2In, String runFileLoc2In) throws MzXMLParsingException, JMzReaderException {
        long startTime = System.currentTimeMillis();
        //set up MzXML files
        File mzXMLFile1 = new File(runFileLoc1In);
        MzXMLFile msRun1 = new MzXMLFile(mzXMLFile1);
        File mzXMLFile2 = new File(runFileLoc2In);
        MzXMLFile msRun2 = new MzXMLFile(mzXMLFile2);

        System.out.println("--- .mzXML files read in. " +((System.currentTimeMillis()-startTime)/1000) + " seconds passed ---");

        //prepare output file
        String outputFilePath = output1In.replace(".csv", "_matchingPeptides.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        }
        catch (FileNotFoundException e){
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        //create header of output file
        StringBuilder sb = new StringBuilder();
        sb.append("Sequence").append(",");
        sb.append("Scan-# 1").append(",");
        sb.append("Scan-# 2").append(",");
        sb.append("MS2-TIC 1").append(",");
        sb.append("MS2-TIC 2").append(",");
        sb.append("XCorr 1").append(",");
        sb.append("XCorr 2").append(",").append(",\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //read in file 1 and create HashMap with peptide sequence as key and Peptide ID object as value
        startTime = System.currentTimeMillis();
        File outputFile1 = new File(output1In);
        Scanner scanner1 = null;
        try {
            scanner1 = new Scanner(outputFile1);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + output1In);
        }
        HashMap <String, PeptideID> sequences1= new HashMap<>();
        //advance over header
        scanner1.next();
        while (scanner1.hasNext()){
            String[] values = scanner1.next().split(",");
            //[0]: Peptide; [1]Scan#; [2]z; [3];XCorr
            String sequence = sequenceOnly(values[0]);
            //only consider if peptide length > 8 && < 16
            if(sequence.length() < 8 || sequence.length() > 16)
                continue;
            //otherwise, create Peptide ID object
            sequences1.put(sequence, new PeptideID(sequence, Integer.parseInt(values[2]), Integer.parseInt(values[1]),Double.parseDouble(values[3])));
        }
        scanner1.close();
        System.out.println("--- Input 1 read in: "+((System.currentTimeMillis()-startTime)) + " milliseconds passed ---");
        startTime = System.currentTimeMillis();

        //read 2nd file and compare with first file
        //only keep overlap

        int numberOfMatches = 0;
        ArrayList<PeptideMatch> matchList = new ArrayList<>();


        File outputFile2 = new File(output2In);
        Scanner scanner2 = null;
        try {
            scanner2 = new Scanner(outputFile2);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + output2In);
        }
        //advance over header
        scanner2.next();
        while (scanner2.hasNext()){
            String[] values = scanner2.next().split(",");
            //[0]: Peptide; [1]Scan#; [2]z; [3];XCorr
            //skip if z=3
            int charge = Integer.parseInt(values[2]);
            if (charge != 2)
                continue;
            String sequence = sequenceOnly(values[0]);
            //if sequence is present in sequences1, then create new Peptide Match object
            if (sequences1.containsKey(sequence)){
                //sequence is present, so check if XCorrs are similar: +- 0.3
                PeptideID pepID1 = sequences1.get(sequence);
                double xcorr2 = Double.parseDouble(values[3]);
                if (xcorr2 < pepID1.getXcorr() -0.3 || xcorr2 > pepID1.getXcorr() +0.3 )
                    continue;
                //if sequence are the same && xcorr is similar, extract MS2TIC
                PeptideID pepID2 = new PeptideID(sequence, charge, Integer.parseInt(values[1]), xcorr2);
                int scanNumber1 = pepID1.getScanNumber();
                int scanNumber2 = pepID2.getScanNumber();

                MySpectrum spectrum1 = MzXMLReadIn.mzXMLToMySpectrum(msRun1, ""+scanNumber1);
                double spectrum1TIC = spectrum1.getSpectrumTIC();
                MySpectrum spectrum2 = MzXMLReadIn.mzXMLToMySpectrum(msRun2, ""+scanNumber2);
                double spectrum2TIC = spectrum2.getSpectrumTIC();
                //check if spectrum TICs are within 10% of each other
                if(spectrum1TIC < spectrum2TIC / 1.1 || spectrum1TIC > spectrum2TIC * 1.1)
                    continue;
                //all filters passed, generate PeptideMatch
                PeptideMatch match = new PeptideMatch(pepID1, spectrum1TIC, pepID2, spectrum2TIC);
                matchList.add(match);
                numberOfMatches++;
            }
        }
        scanner2.close();
        System.out.println("--- Overlapping peptides found: "+ numberOfMatches + " matches. " +((System.currentTimeMillis()-startTime)) + " milliseconds passed ---");
        //fill output file
        startTime = System.currentTimeMillis();
        for(PeptideMatch match : matchList){
            sb.append(match.getPepID1().getPepSequence()).append(",");
            sb.append(match.getPepID1().getScanNumber()).append(",");
            sb.append(match.getPepID2().getScanNumber()).append(",");
            sb.append(scientific.format(match.getPepID1MS2TIC())).append(",");
            sb.append(scientific.format(match.getPepID2MS2TIC())).append(",");
            sb.append(twoDec.format(match.getPepID1().getXcorr())).append(",");
            sb.append(twoDec.format(match.getPepID2().getXcorr())).append(",");
            sb.append("\n");
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
        System.out.println("--- Output file created: "+((System.currentTimeMillis()-startTime)) + " milliseconds passed ---");
    }

    //removes the starting and leading characters from allosaurus sequences
    //e.g.: K.PEPTIDE.R --> PEPTIDE
    //TODO: error handling? detection of points?
    public static String sequenceOnly(String extendedSequenceIn){
        String out = extendedSequenceIn.substring(2);
        out = out.substring(0,out.length()-2);
        return out;
    }

}
