import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.xml.datatype.Duration;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class RandomTools {
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");


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

    private static class PeptideMatch {
        private PeptideID pepID1;
        private double pepID1MS2TIC;
        private PeptideID pepID2;
        private double pepID2MS2TIC;

        public PeptideMatch(PeptideID pepID1In, double pepID1MS2TICIn, PeptideID pepID2In, double pepID2MS2TICIn) {
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

        System.out.println("--- .mzXML files read in. " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds passed ---");

        //prepare output file
        String outputFilePath = output1In.replace(".csv", "_matchingPeptides.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
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
        HashMap<String, PeptideID> sequences1 = new HashMap<>();
        //advance over header
        scanner1.next();
        while (scanner1.hasNext()) {
            String[] values = scanner1.next().split(",");
            //[0]: Peptide; [1]Scan#; [2]z; [3];XCorr
            String sequence = sequenceOnly(values[0]);
            //only consider if peptide length > 8 && < 16
            if (sequence.length() < 8 || sequence.length() > 16)
                continue;
            //otherwise, create Peptide ID object
            sequences1.put(sequence, new PeptideID(sequence, Integer.parseInt(values[2]), Integer.parseInt(values[1]), Double.parseDouble(values[3])));
        }
        scanner1.close();
        System.out.println("--- Input 1 read in: " + ((System.currentTimeMillis() - startTime)) + " milliseconds passed ---");
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
        while (scanner2.hasNext()) {
            String[] values = scanner2.next().split(",");
            //[0]: Peptide; [1]Scan#; [2]z; [3];XCorr
            //skip if z=3
            int charge = Integer.parseInt(values[2]);
            if (charge != 2)
                continue;
            String sequence = sequenceOnly(values[0]);
            //if sequence is present in sequences1, then create new Peptide Match object
            if (sequences1.containsKey(sequence)) {
                //sequence is present, so check if XCorrs are similar: +- 0.3
                PeptideID pepID1 = sequences1.get(sequence);
                double xcorr2 = Double.parseDouble(values[3]);
                if (xcorr2 < pepID1.getXcorr() - 0.3 || xcorr2 > pepID1.getXcorr() + 0.3)
                    continue;
                //if sequence are the same && xcorr is similar, extract MS2TIC
                PeptideID pepID2 = new PeptideID(sequence, charge, Integer.parseInt(values[1]), xcorr2);
                int scanNumber1 = pepID1.getScanNumber();
                int scanNumber2 = pepID2.getScanNumber();

                MySpectrum spectrum1 = MzXMLReadIn.mzXMLToMySpectrum(msRun1, "" + scanNumber1);
                double spectrum1TIC = spectrum1.getSpectrumTIC();
                MySpectrum spectrum2 = MzXMLReadIn.mzXMLToMySpectrum(msRun2, "" + scanNumber2);
                double spectrum2TIC = spectrum2.getSpectrumTIC();
                //check if spectrum TICs are within 10% of each other
                if (spectrum1TIC < spectrum2TIC / 1.1 || spectrum1TIC > spectrum2TIC * 1.1)
                    continue;
                //all filters passed, generate PeptideMatch
                PeptideMatch match = new PeptideMatch(pepID1, spectrum1TIC, pepID2, spectrum2TIC);
                matchList.add(match);
                numberOfMatches++;
            }
        }
        scanner2.close();
        System.out.println("--- Overlapping peptides found: " + numberOfMatches + " matches. " + ((System.currentTimeMillis() - startTime)) + " milliseconds passed ---");
        //fill output file
        startTime = System.currentTimeMillis();
        for (PeptideMatch match : matchList) {
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
        System.out.println("--- Output file created: " + ((System.currentTimeMillis() - startTime)) + " milliseconds passed ---");
    }


    //this function is handed 2 sequences: query and target. It generates tryptic peptides for both
    //TODO: build this
    public static void sequenceUniquenessCalculator() {

    }

    //removes the starting and leading characters from allosaurus sequences
    //e.g.: K.PEPTIDE.R --> PEPTIDE
    //TODO: error handling? detection of points?
    public static String sequenceOnly(String extendedSequenceIn) {
        //just in case something is odd with the dots, use split method
        return extendedSequenceIn.split("[.]")[1];
    }

    //adds Methionine-oxidations to the positions indicated by a * from allosaurus output
    public static ArrayList<Modification> oxidizedMethionineModification(String sequenceIn) {
        int oxCounter = 0;
        ArrayList<Integer> posList = new ArrayList<>();
        for (int i = 0; i < sequenceIn.length(); i++) {
            if (sequenceIn.charAt(i) == '*') {
                //posList adjusts for reporting the correct location of the M. E.g in ABCMDEF, M would be 4 for modlist because of human counting
                posList.add(i - oxCounter);
                oxCounter++;
            }
        }
        ArrayList<Modification> out = new ArrayList<>();
        for (int pos : posList) {
            out.add(Modification.oxidation(pos));
        }
        return out;
    }

    //removes * from oxidized methionine
    //e.g. PEPTM*IDE becomes PEPTMIDE
    public static String removeOxidationSigns(String sequenceIn){
        return sequenceIn.replace("*","");
    }

    public static int determineIsolationOffset(double monoisotopicMZ, double isolatedMZ, double allowedDeviation, int precursorZ){
        //check if experimental isolated m/z and theoretical mass match or a higher isotope peak was isolated
        //set the default value to something unobtainable high
        int precursorIsoOffset = 50;
        if (DeviationCalc.ppmMatch(monoisotopicMZ, isolatedMZ, allowedDeviation)){
            precursorIsoOffset = 0;
        }
        else{
            for (int n = 1; n <10; n++){
                double shiftedMass = monoisotopicMZ + (AtomicMasses.getNEUTRON() * n / precursorZ);
                if (DeviationCalc.ppmMatch(shiftedMass, isolatedMZ, allowedDeviation)) {
                    precursorIsoOffset = n;
                    break;
                }
            }
        }
        return precursorIsoOffset;
    }


    public static double calculateMedian(ArrayList<Double> valueListIn){
        if (valueListIn.size() == 0)
            return 0;

        Collections.sort(valueListIn);
        if (valueListIn.size() % 2 == 0)
            return  (valueListIn.get(valueListIn.size()/2) + valueListIn.get((valueListIn.size()-1)/2))/2;
        else
            return valueListIn.get(valueListIn.size()/2);
    }

    public static int getNumberOfTMTTags(String sequenceIn){
        //always 1 for Nterm
        int out = 1;
        for (int i = 0; i < sequenceIn.length(); i++){
            char query = sequenceIn.charAt(i);
            if(query == 'K')
                out++;
        }
        return out;
    }

    //removes the annoying additional error messages from TMTproC output
    //lines to filter: "<br />"
    public static void fixTMTproCOutput(String filePathIn){
        //create input file
        File inputFile = new File(filePathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + filePathIn);
        }
        //prepare output .csv File
        String outputFilePath = filePathIn.replace(".csv", "_fixed.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw !=null;
        assert scanner != null;
        while (scanner.hasNext()){
            String line = scanner.nextLine();
            //filters out lines with that string
            if(line.contains("<br />"))
                continue;
            //if not, then write line to file
            //append line break
            pw.write(line + "\n");
        }
        pw.flush();
        pw.close();
    }

    //method to extract precursor ion intensities for TMTpro tagged BSA peptides (targeted-related)
    //output: peptide ID, peptide (allosaurus format), z, scan#, RT, intensity (if !=0), SN, ion injection time
    public static void ms1PrecursorInfo(String peptideListIn, String runFilePathIn) throws MzXMLParsingException, JMzReaderException {
        //create input peptide file
        File peptideInputFile = new File(peptideListIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(peptideInputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + peptideListIn);
        }
        assert scanner != null;
        //prepare output .csv File
        String outputFilePath = peptideListIn.replace(".csv", "_extractedMS1Int.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw !=null;
        //write header
        StringBuilder sb = new StringBuilder();
        sb.append("Peptide ID,Peptide,z,scan#,RT[s],MS1 intensity[a.u.],signal/noise,MS1 ion injection time[ms],\n");
        pw.write(sb.toString());
        sb.setLength(0);

        //get columns of "Peptide" ,"ScanF" and "Theo m/z", "z"
        //where are the required fields located
        String headers = scanner.nextLine();
        String[] splitHeaders = headers.split(",");
        Map<String, Integer> headerPositions = new HashMap<>();
        for (int i = 0; i < splitHeaders.length; i++){
            String caption = splitHeaders[i];
            switch(caption){
                case("Peptide"):
                    headerPositions.put("peptide", i);
                    break;
                case("z"):
                    headerPositions.put("z", i);
                    break;
                case("ScanF"):
                    headerPositions.put("scan#",i);
                    break;
                case("Theo m/z"):
                    headerPositions.put("m/z",i);
                    break;
            }
        }
        if(headerPositions.size() != 4)
            throw new IllegalArgumentException("Missing input columns, please check input! Of 4 headers detected: "+headerPositions.size());

        //prepare allosaurus mzXML file
        File runFile = new File(runFilePathIn);
        MzXMLFile run = new MzXMLFile(runFile);
        //create ScanList
        ScanLists runScanLists = new ScanLists(run);
        //Peptide ID variable which is individual for each
        int pepID = 1;
        //loop through different peptides
        while(scanner.hasNext()){
            //get line
            String values = scanner.nextLine();
            //split
            String[] splitValues = values.split(",");
            //get values according to the headers
            String peptide = splitValues[headerPositions.get("peptide")];
            String z = splitValues[headerPositions.get("z")];
            int startingScanNumber = Integer.parseInt(splitValues[headerPositions.get("scan#")]);
            double massToCharge = Double.parseDouble(splitValues[headerPositions.get("m/z")]);
            //get scans to look at
            //start from scan - 200 and go to 300 scans; so -200 scans and +200 scans
            ArrayList<Integer> relevantScanNumbers = runScanLists.getNextNMS1Scans(startingScanNumber-200,400);
            //inner loop: go through all the MS1 spectra
            for(Integer scan:relevantScanNumbers){
                //create MySpectrum object
                MySpectrum spectrum = MzXMLReadIn.mzXMLToMySpectrum(run, scan);
                //check if peak is present
                double intensity = 0;
                Peak matchingPeak = spectrum.getMatchingPeak(massToCharge, 15);
                if(matchingPeak!= null){
                    intensity = matchingPeak.getIntensity();
                }
                //this skips peaks with int = 0;
                else
                    continue;
                //get Scan for injection time
                Scan mzXMLScan = run.getScanByNum((long) scan);
                //TODO: injection time not currently supported. Will be fixed later
                //double injectionTime = mzXMLScan.getIonInjectionTime();
                //TODO: this is WRONG, but trying to fix one thing at a time
                double injectionTime = mzXMLScan.getCollisionEnergy();
                Duration d = mzXMLScan.getRetentionTime();
                String rt = d.toString();
                rt = rt.replace("PT", "");
                rt = rt.replace("S","");
                //write output
                //sb.append("Peptide ID,Peptide,z,scan#,MS1 intensity[a.u.],signal/noise,MS1 ion injection time[ms],\n");
                sb.append(pepID).append(",");
                sb.append(peptide).append(",");
                sb.append(z).append(",");
                sb.append(scan).append(",");
                sb.append(rt).append(",");
                sb.append(scientific.format(intensity)).append(",");
                sb.append(fourDec.format(matchingPeak.getSignalToNoise())).append(",");
                sb.append(fourDec.format(injectionTime)).append(",");
                sb.append("\n");
                pw.write(sb.toString());
                sb.setLength(0);
            }
            //increment pepID prior to next peptide
            pepID++;
        }
        pw.flush();
        pw.close();
        scanner.close();
    }

    //this method takes the TMTpro0 tagged peptides, determines at which scan they occur, gets max peak int, and then corresponding max peak in for targeted version
    public static void ms1PrecursorInfoAdvanced(String triggerListIn, String runFilePathIn) throws MzXMLParsingException, JMzReaderException {
        //create input peptide file
        File peptideInputFile = new File(triggerListIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(peptideInputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + triggerListIn);
        }
        assert scanner != null;
        //prepare output .csv File
        String outputFilePath = triggerListIn.replace(".csv", "_extractedMS1PeakMaxInt.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw !=null;
        //write header
        StringBuilder sb = new StringBuilder();
        sb.append("Peptide,z,scan trigger,RT [s],scan target,m/z pro0 trigger,m/z pro target,max. int. pro0 trigger [a.u.],max. int. pro target [a.u.],\n");
        pw.write(sb.toString());
        sb.setLength(0);

        //get columns of "Peptide" ,"ScanF" and "Theo m/z", "z"
        //where are the required fields located
        String headers = scanner.nextLine();
        String[] splitHeaders = headers.split(",");
        Map<String, Integer> headerPositions = new HashMap<>();
        for (int i = 0; i < splitHeaders.length; i++){
            String caption = splitHeaders[i];
            switch(caption){
                case("Peptide"):
                    headerPositions.put("peptide", i);
                    break;
                case("z"):
                    headerPositions.put("z", i);
                    break;
                case("ScanF"):
                    headerPositions.put("scan#",i);
                    break;
                case("Theo m/z"):
                    headerPositions.put("m/z",i);
                    break;
            }
        }
        if(headerPositions.size() != 4)
            throw new IllegalArgumentException("Missing input columns, please check input! Of 4 headers detected: "+headerPositions.size());

        //prepare allosaurus mzXML file
        File runFile = new File(runFilePathIn);
        MzXMLFile run = new MzXMLFile(runFile);
        //create ScanList
        ScanLists runScanLists = new ScanLists(run);
        //loop through different peptides
        while(scanner.hasNext()){
            //get line
            String values = scanner.nextLine();
            //split
            String[] splitValues = values.split(",");
            //get values according to the headers
            String peptide = splitValues[headerPositions.get("peptide")];
            int z = Integer.parseInt(splitValues[headerPositions.get("z")]);
            int startingScanNumber = Integer.parseInt(splitValues[headerPositions.get("scan#")]);
            double massToChargeTrigger = Double.parseDouble(splitValues[headerPositions.get("m/z")]);
            double maxIntensityTrigger = 0;
            int maxIntensityTriggerScan = 0;
            double maxIntensityTarget = 0;
            int maxIntensityTargetScan=0;
            //set masses for the pro peptides
            //with required things read in, calculate new mass and the offset
            //remove leading and trailing AAs, but leave M oxidation in
            String sequence = RandomTools.sequenceOnly(peptide);
            int numberOfTags = RandomTools.getNumberOfTMTTags(sequence);
            double massToChargeTarget = 0;
            //calculate new target mass and offset
            if(z == 2){
                massToChargeTarget = massToChargeTrigger + 4.509 * numberOfTags;
            }
            if(z == 3){
                massToChargeTarget = massToChargeTrigger + 3.0058 * numberOfTags;
            }
            if(z == 4){
                massToChargeTarget = massToChargeTrigger + 2.25 * numberOfTags;
            }
            //get scans to look at
            //start from scan - 5 and go to 15 scans;
            ArrayList<Integer> relevantScanNumbers = runScanLists.getNextNMS1Scans(startingScanNumber,75);
            //go through all the relevant MS1 spectra
            for(Integer scan:relevantScanNumbers){
                //create MySpectrum object
                MySpectrum spectrum = MzXMLReadIn.mzXMLToMySpectrum(run, scan);
                //check if peak is present
                double intensityTrigger = 0;
                double intensityTarget = 0;
                Peak matchingPeakTrigger = spectrum.getMatchingPeak(massToChargeTrigger, 15);
                if(matchingPeakTrigger!= null){
                    intensityTrigger = matchingPeakTrigger.getIntensity();
                }
                //check if this is the new max
                if(intensityTrigger > maxIntensityTrigger) {
                    maxIntensityTrigger = intensityTrigger;
                    maxIntensityTriggerScan = scan;
                }
            }
            //max precursor intensity is set, check +-3 scans for the target
            ArrayList<Integer> relevantScanNumbersTarget = runScanLists.getNextNMS1Scans(maxIntensityTriggerScan -3,6);


            //go through all the relevant MS1 spectra
            for(Integer scan:relevantScanNumbersTarget){
                //create MySpectrum object
                MySpectrum spectrum = MzXMLReadIn.mzXMLToMySpectrum(run, scan);
                //check if peak is present
                double intensityTarget = 0;
                Peak matchingPeakTarget = spectrum.getMatchingPeak(massToChargeTarget, 15);
                if(matchingPeakTarget!= null){
                    intensityTarget = matchingPeakTarget.getIntensity();
                }
                //check if this is the new max
                if(intensityTarget > maxIntensityTarget) {
                    maxIntensityTarget = intensityTarget;
                    maxIntensityTargetScan = scan;
                }
            }

            //get retention time
            Scan bestScan  = null;
            String rt = "";
            try {
                bestScan = run.getScanByNum((long) maxIntensityTriggerScan);
                rt = RandomTools.formatRetentionTime(bestScan.getRetentionTime());
            }
            catch (MzXMLParsingException e){
                rt = "";
            }

            //write output
            //sb.append("Peptide,z,scan trigger,RT [s],m/z pro0 trigger,m/z pro target,max. int. pro0 trigger [a.u.],max. int. pro target [a.u.],\n");
            sb.append(peptide).append(",");
            sb.append(z).append(",");
            sb.append(maxIntensityTriggerScan).append(",");
            sb.append(rt).append(",");
            sb.append(maxIntensityTargetScan).append(",");
            sb.append(fourDec.format(massToChargeTrigger)).append(",");
            sb.append(fourDec.format(massToChargeTarget)).append(",");
            sb.append(scientific.format(maxIntensityTrigger)).append(",");
            sb.append(scientific.format(maxIntensityTarget)).append(",");
            sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
        scanner.close();
    }

    //reformats retention times from the format PTxxxx.xxS to xxxx.xx
    public static String formatRetentionTime(Duration dIn){
        String rt = dIn.toString();
        rt = rt.replace("PT", "");
        rt = rt.replace("S","");
        return rt;
    }

    //random method to prepare file for peptide number rank ordering of proteins
    public static void rankOrdering(String filePathIn){
        File geneSymbolInputFile = new File(filePathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(geneSymbolInputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + filePathIn);
        }
        assert scanner != null;
        //prepare output .csv File
        String outputFilePath = filePathIn.replace(".csv", "_prepared.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw !=null;
        //write header
        StringBuilder sb = new StringBuilder();
        sb.append("Gene Symbol, # total peptides detected,\n");
        pw.write(sb.toString());
        sb.setLength(0);
        //advance over header
        scanner.next();

        //create dictionary and populate
        Map<String,Integer> geneNames = new HashMap<>();
        while(scanner.hasNext()){
            //get line
            String geneName = scanner.nextLine();
            if(!geneNames.containsKey(geneName)){
                geneNames.put(geneName, 1);
            }
            if(geneNames.containsKey(geneName))
                geneNames.put(geneName, geneNames.get(geneName) +1);
        }
        //iterate over map and write values
        for(Map.Entry<String, Integer> entry:geneNames.entrySet()){
            sb.append(entry.getKey()).append(",").append(entry.getValue()).append(",\n");
            pw.write(sb.toString());
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
    }



}
