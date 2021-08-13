import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.swing.*;
import javax.swing.tree.AbstractLayoutCache;
import javax.xml.datatype.Duration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

public class Benenodin {
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat twoDec = new DecimalFormat("0.00");

    public static void fragmentAnalyzer(String runPathIn, String massListPathIn) throws MzXMLParsingException, JMzReaderException {
        //prepare runFile
        File runFile = new File(runPathIn);
        MzXMLFile run = new MzXMLFile(runFile);
        //agilent mzxml file is stupid
        //scan list is NOT starting from 1 - random numbers
        //fixed scanlists to deal with empty ms1-spectra - they get skipped
        //however, more convenient to just loop through all the spectra
        List<Long> scanList = run.getScanNumbers();

        //read in all the masses from the massList
        //format: "name of ion",massToCheck,\n
        File massListFile = new File(massListPathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(massListFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + massListPathIn);
        }
        assert scanner != null;
        //put masses in Map: names as key, masses as value
        //treemap should be ordered
        LinkedHashMap<String, Double> massesToCheck = new LinkedHashMap<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            massesToCheck.put(values[0], Double.parseDouble(values[1]));
        }

        //prepare output .csv File
        String outputFilePath = runPathIn.replace(".mzXML", "_massAnalysis.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw != null;
        StringBuilder sb = new StringBuilder();
        //first part of header is always the same
        sb.append("MS2 scan#,RT[s],MS2 spectrum TIC [a.u.], Precursor MS1 scan#,Precursor MS1 m/z, Precursor MS1 int. [a.u.],");
        //add names of all the masses of interest in the way: ion peak int [a.u.], ion peak rel. to TIC [%]
        for (String ion : massesToCheck.keySet()) {
            sb.append(ion).append(" peak int. [a.u.],");
            sb.append(ion).append(" peak rel. to TIC [%],");
        }
        //add last entry
        sb.append("\n");
        //write header
        pw.write(sb.toString());
        sb.setLength(0);
        //start ProgressBar
        ProgressBar.progressBar("Analyzing spectra", scanList.size());
        //set Benenodin mass
        double precursorMZ = 1007.01475;
        int analyzedSpectra = 0;
        int skipped =0;
        //loop through all spectra
        for (int i = 0; i < scanList.size(); i++) {
            ProgressBar.progress();
            //try to load spectrum
            //could be empty for agilent data
            Scan currentScan = null;
            try {
                currentScan = run.getScanByNum(scanList.get(i));
            }
            //if spectrum is empty, continue
            catch (MzXMLParsingException e) {
                continue;
            }
            //check if MS1, otherwise continue
            if (currentScan.getMsLevel() != (long) 1) {
                continue;
            }
            //in a MS1 scan, check if precursor is present
            //convert to MySpectrum first
            MySpectrum currentMS1 = MzXMLReadIn.mzXMLToMySpectrum(run, Math.toIntExact(scanList.get(i)));
            //check if precursor is present
            Peak precursorMS1 = currentMS1.getMatchingPeak(precursorMZ, 50);
            //if precursor is not present, continue loop
            if(precursorMS1 == null){
                skipped++;
                continue;
            }
            //otherwise, get the next spectrum, which should be MS2
            //skip if last scan however
            if(!(i+1<scanList.size())){
                continue;
            }
            Scan followingMS2 = null;
            try{
                followingMS2 = run.getScanByNum(scanList.get(i+1));
            }
            //just if something goes wrong with conversion again
            catch (MzXMLParsingException e){
                continue;
            }
            //skip if this spectrum is not MS2
            if(followingMS2.getMsLevel() != (long) 2)
                continue;
            MySpectrum followingMS2MySpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, Math.toIntExact(scanList.get(i+1)));
            //now, MS1 and MS2 should be present
            //get the values for output into the sb
            //"MS2 scan#,RT[s],MS2 spectrum TIC [a.u.], Precursor MS1 scan#,Precursor MS1 m/z, Precursor MS1 int. [a.u.],"
            //ion peak int. [a.u.]
            //ion peak rel. to TIC [%]
            //first add the standard values
            sb.append(scanList.get(i+1)).append(",");
            sb.append(RandomTools.formatRetentionTime(followingMS2.getRetentionTime())).append(",");
            //put MS2 TIC in variable for easier access
            double ms2TIC = followingMS2MySpectrum.getSpectrumTIC();
            sb.append(scientific.format(ms2TIC)).append(",");
            sb.append(scanList.get(i)).append(",");
            sb.append(fourDec.format(precursorMS1.getMass())).append(",");
            sb.append((fourDec.format(precursorMS1.getIntensity()))).append(",");
            //next, handle all the masses in the HashMap
            //access it over key to ensure ordering is correct
            for(String ion : massesToCheck.keySet()){
                double massToCheck = massesToCheck.get(ion);
                //check if there is a matching peak
                Peak ms2Match = followingMS2MySpectrum.getMatchingPeak(massToCheck,50);
                //case if no peak was found
                if(ms2Match == null){
                    //just put 2 zeros
                    sb.append("0,0,");
                }
                else {
                    //peak was found, get the peak data
                    sb.append(scientific.format(ms2Match.getIntensity())).append(",");
                    double intRelToTIC = ms2Match.getIntensity()/ms2TIC*100;
                    sb.append(fourDec.format(intRelToTIC)).append(",");
                }
            }
            //append line break
            sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
            analyzedSpectra++;

        }
        pw.close();
        ProgressBar.close();
        System.out.println("Spectra analysis complete! Analyzed "+analyzedSpectra+" spectra.");
        System.out.println("Skipped: "+skipped);
    }

    //same function, but for Lumos data
    public static void fragmentAnalyzerLumos(String runPathIn, String massListPathIn) throws MzXMLParsingException, JMzReaderException {
        //prepare runFile
        File runFile = new File(runPathIn);
        MzXMLFile run = new MzXMLFile(runFile);
        //get the scanList object
        ScanLists scanList = new ScanLists(run);

        //TODO: automate this?
        //scan ranges for the individual peaks
        int[] peak1Range = new int[]{4833,5145};
        int[] peak2Range = new int[]{6063,6375};
        int[] peak3Range = new int[]{10035,10191};
        int[] peak4Range = new int[]{10839,11037};

        //read in all the masses from the massList
        //format: "name of ion",massToCheck,\n
        File massListFile = new File(massListPathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(massListFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + massListPathIn);
        }
        assert scanner != null;
        //put masses in Map: names as key, masses as value
        //treemap should be ordered
        LinkedHashMap<String, Double> massesToCheck = new LinkedHashMap<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            massesToCheck.put(values[0], Double.parseDouble(values[1]));
        }

        //prepare output .csv File
        String outputFilePath = runPathIn.replace(".mzXML", "_massAnalysis.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw != null;
        StringBuilder sb = new StringBuilder();
        //first part of header is always the same
        sb.append("Peak,MS2 scan#,RT[s],MS2 spectrum TIC [a.u.], Precursor MS1 scan#,Precursor MS1 m/z, Precursor MS1 int. [a.u.], norm. CID [%],");
        //add names of all the masses of interest in the way: ion (mz) peak int [a.u.], ion (mz) peak rel. to TIC [%]
        for (String ion : massesToCheck.keySet()) {
            sb.append(ion).append(" (m/z ").append(fourDec.format(massesToCheck.get(ion))).append(") peak int. [a.u.],");
            sb.append(ion).append(" (m/z ").append(fourDec.format(massesToCheck.get(ion))).append(") peak rel. to TIC [%],");
        }
        //add last entry
        sb.append("\n");
        //write header
        pw.write(sb.toString());
        sb.setLength(0);
        //start ProgressBar
        ProgressBar.progressBar("Analyzing spectra", scanList.getNumberMS2());
        //set Benenodin mass
        double benenodin1MZ = 1007.01475;
        int analyzedSpectra = 0;
        //loop through all  MS2 spectra
        for(int currentMS2Number : scanList.getMs2ScanNumbers()){
            ProgressBar.progress();
            //complicated way if anything goes wrong with the readin
            Scan ms2Scan = null;
            try{
                ms2Scan = run.getScanByNum((long) currentMS2Number);
            }
            catch (MzXMLParsingException e){
                continue;
            }
            MySpectrum ms2Spectrum = MzXMLReadIn.mzXMLToMySpectrum(run, currentMS2Number);
            //get precursor scan
            //this is redundant for many spectra, but easier to do it this way...shouldn't be an issue with the numbers involved
            int precursorMS1Number = scanList.getPreviousMS1ScanNumber(currentMS2Number);
            Scan ms1Scan = null;
            try{
                ms1Scan = run.getScanByNum((long) precursorMS1Number);
            }
            catch (MzXMLParsingException e){
                continue;
            }
            MySpectrum ms1Spectrum = MzXMLReadIn.mzXMLToMySpectrum(run, precursorMS1Number);
            //ms1 and ms2 spectrum are ready
            //determine if precursor was present with minimum intensity (6E6)...if not, skip
            Peak precursorPeak = ms1Spectrum.getMatchingPeak(benenodin1MZ, 10);
            if(precursorPeak == null ||precursorPeak.getIntensity() < 6E6)
                continue;
            //get the information for the general info
            //"Peak#, MS2 scan#,RT[s],MS2 spectrum TIC [a.u.], Precursor MS1 scan#,Precursor MS1 m/z, Precursor MS1 int. [a.u.], norm. HCD [%],"
            //figure out peak it belongs to certain peak
            if(ms2Spectrum.getScanNumber() >= peak1Range[0] && ms2Spectrum.getScanNumber()<= peak1Range[1])
                sb.append(1).append(",");
            else if(ms2Spectrum.getScanNumber() >= peak2Range[0] && ms2Spectrum.getScanNumber()<= peak2Range[1])
                sb.append(2).append(",");
            else if(ms2Spectrum.getScanNumber() >= peak3Range[0] && ms2Spectrum.getScanNumber()<= peak3Range[1])
                sb.append(3).append(",");
            else if(ms2Spectrum.getScanNumber() >= peak4Range[0] && ms2Spectrum.getScanNumber()<= peak4Range[1])
                sb.append(4).append(",");
            else
                sb.append("other").append(",");

            sb.append(ms2Spectrum.getScanNumber()).append(",");
            sb.append(RandomTools.formatRetentionTime(ms2Scan.getRetentionTime())).append(",");
            sb.append(scientific.format(ms2Spectrum.getSpectrumTIC())).append(",");
            sb.append(ms1Spectrum.getScanNumber()).append(",");
            sb.append(fourDec.format(precursorPeak.getMass())).append(",");
            sb.append(scientific.format(precursorPeak.getIntensity())).append(",");
            sb.append(ms2Scan.getCollisionEnergy()).append(",");

            //next, handle all the masses in the HashMap
            //access it over key to ensure ordering is correct
            for(String ion : massesToCheck.keySet()){
                double massToCheck = massesToCheck.get(ion);
                //check if there is a matching peak
                Peak ms2Match = ms2Spectrum.getMatchingPeak(massToCheck,10);
                //case if no peak was found
                if(ms2Match == null){
                    //just put 2 zeros
                    sb.append("0,0,");
                }
                else {
                    //peak was found, get the peak data
                    sb.append(scientific.format(ms2Match.getIntensity())).append(",");
                    double intRelToTIC = ms2Match.getIntensity()/ms2Spectrum.getSpectrumTIC()*100;
                    sb.append(fourDec.format(intRelToTIC)).append(",");
                }
            }
            //append line break
            sb.append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
            analyzedSpectra++;
        }
        pw.close();
        ProgressBar.close();
        System.out.println("Spectra analysis complete! Analyzed "+analyzedSpectra+" spectra.");
    }

    public static void tester(String runPathIn) throws MzXMLParsingException, JMzReaderException {
        //prepare runFile
        File runFile = new File(runPathIn);
        MzXMLFile run = new MzXMLFile(runFile);
        //agilent mzxml file is stupid
        //scan list is NOT starting from 1 - random numbers
        //fixed scanlists to deal with empty ms1-spectra - they get skipped
        //loop through the MS2 scans and check relevant precursor
        ScanLists scanList = new ScanLists(run);
        ArrayList<Integer> ms2ScanList = scanList.getMs2ScanNumbers();

        //prepare output .csv File
        String outputFilePath = runPathIn.replace(".mzXML", "_testing.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw != null;
        StringBuilder sb = new StringBuilder();
        sb.append("MySpectrumTIC,ScanTIC,diff,\n");
        pw.write(sb.toString());
        sb.setLength(0);
        ProgressBar.progressBar("Testing...", ms2ScanList.size());

        for (int scanNumber : ms2ScanList) {
            Scan ms2 = run.getScanByNum((long) scanNumber);
            MySpectrum ms2MySpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, scanNumber);
            double mySpectrumTIC = ms2MySpectrum.getSpectrumTIC();
            double scanTIC = ms2.getTotIonCurrent();
            double diff = Math.abs(mySpectrumTIC - scanTIC);
            sb.append(mySpectrumTIC).append(",");
            sb.append(scanTIC).append(",");
            sb.append(diff).append(",\n");
            pw.write(sb.toString());
            sb.setLength(0);
            ProgressBar.progress();
        }
        pw.close();
        ProgressBar.close();
    }

    public static void pepDigestTester() {
        Peptide pep = new Peptide("GVGFGRPDSILTQEQAKPM", AminoAcid.getAminoAcidListDWater());
        SumFormula pepFormula = pep.getSumFormula();
        pepFormula = SumFormula.sumFormulaJoiner(pepFormula, SumFormula.getProtonFormula());
        pepFormula = SumFormula.sumFormulaJoiner(pepFormula, SumFormula.getProtonFormula());
        Ion ion = new Ion(pepFormula);
        System.out.println(ion.getMToZ());
    }

    public static void twoRotaxaneMassCreator(String filePathIn, boolean lostFragmentMasses, int rotaxaneCharge){
        //error handling
        if(rotaxaneCharge!=1 && rotaxaneCharge!=2)
            throw new IllegalArgumentException("Please specify a [2]rotaxane charge between 1 and 2");
        //scanner for input file with the fragments
        File massListFile = new File(filePathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(massListFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + filePathIn);
        }
        assert scanner != null;

        //prepare output file
        //prepare output .csv File
        String outputFilePath;
        if(lostFragmentMasses)
                        outputFilePath = filePathIn.replace(".csv", "_lostFragments_toCheck.csv");
        else {
            String replacement = "";
            if(rotaxaneCharge == 1)
                replacement = "_[2]rotaxanes_z1_toCheck.csv";
            if(rotaxaneCharge == 2)
                replacement = "_[2]rotaxanes_z2_toCheck.csv";
            outputFilePath = filePathIn.replace(".csv", replacement);
        }

        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        assert pw != null;
        StringBuilder sb = new StringBuilder();

        //this is the neutral mass
        double benenodinMass = 2012.01495;
        //loop through the file and get the fragments to calculate
        while(scanner.hasNext()){
            String values = scanner.nextLine();
            //just the first value is required
            String value = values.split(",")[0];
            //mass for this fragment is: Benenodin - AA(s) - water
            Peptide fragment = new Peptide(value, AminoAcid.getAminoAcidList());
            SumFormula fragmentFormula = fragment.getSumFormula();
            fragmentFormula = SumFormula.sumFormulaSubstractor(fragmentFormula, SumFormula.getWaterFormula());
            double massToCheck;
            //in case of [2]rotaxanes
            if(!lostFragmentMasses) {
                massToCheck = ((benenodinMass - fragmentFormula.getExactMass()) + rotaxaneCharge * AtomicMasses.getPROTON() )/rotaxaneCharge;
                sb.append(value).append(" loss ").append(rotaxaneCharge).append("+,").append(fourDec.format(massToCheck)).append(",\n");
            }
            //for lost fragment masses
            else {
                massToCheck = SumFormula.sumFormulaJoiner(fragmentFormula, SumFormula.getProtonFormula()).getExactMass();
                sb.append(value).append(",").append(fourDec.format(massToCheck)).append(",\n");
            }

            pw.write(sb.toString());
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
    }


}
