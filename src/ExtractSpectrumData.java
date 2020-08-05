//class to handle the extraction of ion intensities in certain scans

import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ExtractSpectrumData {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");

    //masses to check include the label and mass
    private static class MassToCheck {
        private String label;
        private double mass;

        public MassToCheck(String labelIn, double massIn) {
            this.label = labelIn;
            this.mass = massIn;
        }


        public String getLabel() {
            return this.label;
        }

        public double getMass() {
            return this.mass;
        }
    }

    public static void massExtractor(String mzXMLPath, String inputFilePath) throws MzXMLParsingException, JMzReaderException {
        //set up input file
        File inputFile = new File(inputFilePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + inputFilePath);
        }
        //read in of the precursor masses, the masses to check, fragmentation type and if feature intensities should be used
        //precursor masses
        String[] precursorsLine = scanner.nextLine().split(",");
        ArrayList<Double> precursorList = new ArrayList<>();
        for (int i = 1; i < precursorsLine.length; i++) {
            //skip first element, thats the header
            try {
                precursorList.add(Double.parseDouble(precursorsLine[i]));
            } catch (NumberFormatException e) {
                continue;
            }
        }

        //masses to check with the respective label
        String[] massLine = scanner.nextLine().split(",");
        ArrayList<MassToCheck> massList = new ArrayList<>();
        for (int i = 1; i < massLine.length; i++) {
            //entries consist of label:mass
            String[] entries = massLine[i].split(":");
            massList.add(new MassToCheck(entries[0], Double.parseDouble(entries[1])));
        }

        //fragmentation method
        String[] fragMethodLine = scanner.nextLine().split(",");
        String fragMethod = fragMethodLine[1];

        //should features be used?
        boolean featureIntUsed;
        String[] featureLine = scanner.nextLine().split(",");
        switch (featureLine[1]) {
            case "Y":
                featureIntUsed = true;
                break;
            case "+":
                featureIntUsed = true;
                break;
            default:
                featureIntUsed = false;
        }
//read-in of data complete, setup output file

        String outputFileLocation = inputFilePath.replace(".csv", "_ExtractedIntensities.csv");
        File output = new File(outputFileLocation);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(output);
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong with the output file creation! Output File Path: " + outputFileLocation);
        }

        //always have 5 columns: scan#, fragmentation energy, precursor mass, precursor z, precursor int
        //in addition, all the queried intensities
        int numberOfHeaders = 6 + massList.size();
        String[] header = new String[numberOfHeaders];
        header[0] = "Scan-#,";
        header[1] = fragMethod + " energy,";
        header[2] = "Scan Description,";
        header[3] = "Precursor m/z,";
        header[4] = "Precursor z,";
        header[5] = "Rel. Precursor Int. [%],";

        int currentHeaderPos = 6;

        for (MassToCheck mass : massList) {
            header[currentHeaderPos] = mass.getLabel() + " (" + fourDec.format(mass.getMass()) + ")[%],";
            currentHeaderPos++;
        }
        StringBuilder sb = new StringBuilder();

        for (String text : header) {
            sb.append(text);
        }
        sb.append("\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //start working with MzXML file
        File mzXMLFile = new File(mzXMLPath);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);
        int numberOfScans = msRun.getSpectraCount();
        int numberOfScansUsed = 0;

        //go through all spectra and analyze them if the fragmentation method is correct and the precursor mass is in the list

        for (int i = 1; i < numberOfScans; i++) {
            System.out.println("working on scan: " + i);
            Scan currentScan = null;
            try {
                currentScan = msRun.getScanByNum((long) i);
            } catch (MzXMLParsingException e) {
                continue;
            }
            //only analyze MS2
            if (currentScan.getMsLevel() != 2L)
                continue;
            //only if frag method is correct
            String currentFragMethod = currentScan.getPrecursorMz().get(0).getActivationMethod();
            if (!currentFragMethod.equals(fragMethod))
                continue;
            //check all the precursors and see if the mass is present
            boolean precursorPresent = false;
            double currentPrecursor = currentScan.getPrecursorMz().get(0).getValue();
            int precursorCharge = Math.toIntExact(currentScan.getPrecursorMz().get(0).getPrecursorCharge());
            //loop through all the precursors and set variable if one is present
            for (double precursor : precursorList) {
                //note: also need to check the shift to the M1 peak
                if (DeviationCalc.isPartofIsotopeEnvelope(precursor, currentPrecursor, precursorCharge, 10)) {
                    precursorPresent = true;
                    break;
                }

            }
            //skip if still false
            if (!precursorPresent)
                continue;

            //now, this spectrum is analyzed
            numberOfScansUsed++;

            //get precursor charge state


            //now, only if spectrum fulfills requirements, it'll be analyzed
            //convert to MySpectrum and run Feature Assignment
            MySpectrum currentMySpectrum = MzXMLReadIn.mzXMLToMySpectrum(msRun, "" + i);
            currentMySpectrum.assignZAndFeatures(10);
            ArrayList<Peak> peakList = currentMySpectrum.getPeakList();

            //put the measured intensities in this array
            //check the precursor int in the spectrum
            double precursorInt;


        /*for(Peak peak : peakList){
           if(peak.getMass() < currentPrecursor - 0.5)
               continue;
           if(DeviationCalc.ppmMatch(currentPrecursor, peak.getMass(), 10)){
               if(featureIntUsed){
                   precursorInt = peak.getFeatureRelIntPerTIC();
               }
               else{
                   precursorInt = peak.getRelIntensity();
               }
               break;
            }
        }*/

            //use returnMatchingPeak function from MySpectrum
            Peak precursorPeak = currentMySpectrum.getMatchingPeak(currentPrecursor, 10);
            if (precursorPeak == null) {
                precursorInt = 0;
            } else {
                if (featureIntUsed) {
                    precursorInt = precursorPeak.getFeatureRelIntPerTIC();
                    if(!precursorPeak.isPartOfFeature())
                        precursorInt = precursorPeak.getRelIntensity()/currentMySpectrum.getSpectrumTIC()*100;
                } else {
                    precursorInt = precursorPeak.getRelIntensity();
                }
            }


            //use array to store intensities of the masses to check
            double[] intensities = new double[massList.size()];
            int currentPosInt = 0;

            //loop through all the masses to check
            for (MassToCheck toCheck : massList) {

                //use returnMatchingPeak function from MySpectrum
                Peak matchingPeak = currentMySpectrum.getMatchingPeak(toCheck.getMass(), 10);
                if (matchingPeak == null) {
                    intensities[currentPosInt] = 0;
                } else {
                    if (featureIntUsed) {
                        intensities[currentPosInt] = matchingPeak.getFeatureRelIntPerTIC();
                        if(!matchingPeak.isPartOfFeature())
                            intensities[currentPosInt] = matchingPeak.getRelIntensity()/currentMySpectrum.getSpectrumTIC()*100;

                    } else {
                        intensities[currentPosInt] = matchingPeak.getRelIntensity();
                    }
                }
                currentPosInt++;
            }
            //intensities are read out, put them into .csv
            //scan number
            sb.append(i).append(",");
            //fragmentation energy
            sb.append(Math.round(currentScan.getCollisionEnergy())).append(",");
            //Scan description
            sb.append(currentScan.getDescription()).append(",");
            //picked precursor M/Z
            sb.append(twoDec.format(currentPrecursor)).append(",");
            //precursor z
            sb.append(precursorCharge).append(",");
            //precursor Intensity
            sb.append(twoDec.format(precursorInt)).append(",");
            //now all the found intensities
            for (double intensity : intensities) {
                sb.append(twoDec.format(intensity)).append(",");
            }
            sb.append("\n");
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            //System.out.println("Analyzed Scan-# "+i);
        }
        pw.close();
        System.out.println("Analysis complete! Analyzed " + numberOfScansUsed + " spectra out of " + numberOfScans + " in file!");
    }
//method to extract the scan descriptions of given scans from weird triceratops MzXML files

    public static void getScanDescriptions(String filePathIn, String triceratopsRunIn) throws MzXMLParsingException {
        //set up input file
        File inputFile = new File(filePathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + filePathIn);
        }

        //start working with MzXML file
        File mzXMLFile = new File(triceratopsRunIn);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);


        ArrayList<Integer> scanNumbers = new ArrayList<>();

        while (scanner.hasNext()) {
            String currentLine = scanner.nextLine();
            String currentValue[] = currentLine.split(",");
            try {
                scanNumbers.add(Integer.parseInt(currentValue[0]));
            } catch (NumberFormatException e) {
                continue;
            }
        }

        String outputFileLocation = filePathIn.replace(".csv", "_ScanDescriptions.csv");
        File output = new File(outputFileLocation);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(output);
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong with the output file creation! Output File Path: " + outputFileLocation);
        }

        StringBuilder sb = new StringBuilder();

        for (Integer scan : scanNumbers) {
            Scan currentScan = null;
            try {
                currentScan = msRun.getScanByNum((long) scan);
            } catch (MzXMLParsingException e) {
                continue;
            }
            sb.append(currentScan.getDescription()).append(",\n");
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
    }


    public static void getInjectionTimes(String runFilePathIn) throws MzXMLParsingException {
        //prepare creation of output file
        String outputFileLocation = runFilePathIn.replace(".mzXML", "_IonInjectionTimes.csv");
        File output = new File(outputFileLocation);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(output);
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong with the output file creation! Output File Path: " + outputFileLocation);
        }
        StringBuilder sb = new StringBuilder();
        //write header
        sb.append("Scan Number,").append("Ion Injection Time [ms],\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //create MzXML file and loop through
        //start working with MzXML file
        File mzXMLFile = new File(runFilePathIn);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);
        int numberOfScans = msRun.getSpectraCount();
        System.out.println("Number of spectra to Analyze: " + numberOfScans);

        //loop through all scans
        for (int i = 0; i < numberOfScans; i++) {
            System.out.println("working on scan: " +( i + 1));
            Scan currentScan = null;
            try {
                currentScan = msRun.getScanByNum((long) i + 1);
            } catch (MzXMLParsingException e) {
                continue;
            }
            //read out the ITT
            double ionInjectionTime = currentScan.getIonInjectionTime();
            sb.append(i + 1).append(",").append(fourDec.format(ionInjectionTime)).append(",\n");
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
    }

    //get the number of triggered Spectra on a specific precursor
    public static int getMsNScanCount(String spectrumPathIn, double precursorIn, int scanLevelIn, String fragMethodIn) throws MzXMLParsingException {
        //only supports MS levels 2 and 3
        if (!(scanLevelIn == 2 || scanLevelIn == 3))
            throw new IllegalArgumentException("Only scan levels 2 and 3 are supported! queried scan level: "+scanLevelIn);

        int numberOfSpectra = 0;

        //start working with MzXML file
        File mzXMLFile = new File(spectrumPathIn);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);
        int numberMS1 = 0;
        int numberMS2 = 0;
        int numberMS2HCD=0;
        int numberMS2CID=0;
        int numberMS3=0;


        //loop through all the spectra
        for (int i = 1; i < msRun.getSpectraCount(); i++) {
        Scan currentScan = null;
        //see if scan is present
        try {
            currentScan = msRun.getScanByNum((long) i);
        }
        catch (MzXMLParsingException e){
            continue;
        }
        if(currentScan.getMsLevel() == 1)
            numberMS1++;
        if(currentScan.getMsLevel()==2){
            numberMS2++;
            if(currentScan.getPrecursorMz().get(0).getActivationMethod().equals("HCD"))
                numberMS2HCD++;
            if(currentScan.getPrecursorMz().get(0).getActivationMethod().equals("CID"))
                numberMS2CID++;
        }
        if(currentScan.getMsLevel() == 3){
            numberMS3++;
        }

        //only look at current MS level, skip otherwise
        if(currentScan.getMsLevel() != (long) scanLevelIn)
            continue;
        //only look at specified fragmentation method, skip otherwise
       if(!currentScan.getPrecursorMz().get(0).getActivationMethod().equals(fragMethodIn))
           continue;

        //check if precursor is within limits
        double currentPrecursor = currentScan.getPrecursorMz().get(0).getValue();
        int precCharge = Math.toIntExact(currentScan.getPrecursorMz().get(0).getPrecursorCharge());
        if(DeviationCalc.isPartofIsotopeEnvelope(precursorIn, currentPrecursor, precCharge, 20))
            numberOfSpectra++;
        }

        System.out.println("MS1-Scans: "+numberMS1);
        System.out.println("MS2-Scans: "+numberMS2);
        System.out.println("MS2-HCD: "+numberMS2HCD);
        System.out.println("MS2-CID: "+numberMS2CID);
        System.out.println("MS3-Scans: "+numberMS3);

        return numberOfSpectra;
    }

    public static void extractStoNValues(String runPathIn, String inputFilePath) throws MzXMLParsingException, JMzReaderException {
        //set up input file
        File inputFile = new File(inputFilePath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + inputFilePath);
        }

        //set up MzXML File
        File mzXMLFile = new File(runPathIn);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);

        //set up output file
        String outputFilePath = inputFilePath.replace(".csv","_extractedStoNValues.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        }
        catch (FileNotFoundException e){
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        StringBuilder sb = new StringBuilder();
        //Header: Scan-#, Peak Mass, Signal to Noise
        sb.append("Scan-#").append(",");
        sb.append("Peak Mass [m/z]").append(",");
        sb.append("S/N [a.u.]").append(",\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);
        //written, start main loop
        //skip first line, that's the header
        scanner.next();
        int analyzedSpectra = 0;

        while (scanner.hasNext()){
            double signalToNoise;

            //get values
            String[] values = scanner.next().split(",");
            //error handling if value is no integer
            try {
                int scanNumber = Integer.parseInt(values[0]);
            }
            catch (NumberFormatException e){
                continue;
            }
            double peakMass = Double.parseDouble(values[1]);

            //open corresponding spectrum
            MySpectrum currentSpectrum = MzXMLReadIn.mzXMLToMySpectrum(msRun, values[0]);
            //check if spectrum has noise band
            if(!currentSpectrum.isNoisePresent()) {
                throw new IllegalArgumentException("No noise band present in spectrum!: " + values[0]);
            }

            //get matching peak
            Peak peak = currentSpectrum.getMatchingPeak(peakMass, 8);
            if (peak == null)
                signalToNoise = 0;
            else {
                signalToNoise = peak.getSignalToNoise();
            }

            //write info to file
            sb.append(values[0]).append(",");
            sb.append(values[1]).append(",");
            sb.append(signalToNoise).append(",\n");

            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            System.out.println("Analyzed spectra: "+values[0]);
            analyzedSpectra++;
        }

        pw.flush();
        pw.close();
        System.out.println("Analysis complete! Analyzed "+analyzedSpectra+" spectra.");
    }


    public static void extractStoNAllMS2Prec(String runPathIn) throws MzXMLParsingException, JMzReaderException {

        //set up MzXML File
        File mzXMLFile = new File(runPathIn);
        MzXMLFile msRun = new MzXMLFile(mzXMLFile);

        //set up output file
        String outputFilePath = runPathIn.replace(".mzXML","_extractedMS2PrecursorSN.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        }
        catch (FileNotFoundException e){
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        //Header
        StringBuilder sb = new StringBuilder();
        sb.append("MS2 Scan-#").append(",");
        sb.append("Precursor Mass isolated [m/z]").append(",");
        sb.append("S/N isolated Precursor").append(",\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        long startTime = System.currentTimeMillis();
        //get the scan numbers to look at
        List<Long> allScanNumbers = msRun.getScanNumbers();
        ArrayList<Integer> ms1ScanNumbers = new ArrayList<>();
        ArrayList<Integer> ms2ScanNumbers = new ArrayList<>();

        for(long scanNumber : allScanNumbers){
            Scan currentScan = msRun.getScanByNum(scanNumber);
            int currentMSLevel = Math.toIntExact(currentScan.getMsLevel());
            if (currentMSLevel == 1){
                ms1ScanNumbers.add(Math.toIntExact(scanNumber));
                continue;
            }
            if(currentMSLevel == 2){
                ms2ScanNumbers.add(Math.toIntExact(scanNumber));
            }
        }
        System.out.println("Scan Lists created: "+((System.currentTimeMillis()-startTime)/1000) + " seconds passed");
        System.out.println("MS1-Scans: "+ms1ScanNumbers.size());
        System.out.println("MS2-Scans: "+ms2ScanNumbers.size());


        //preparation done, start main loop
        int analyzedSpectra = 0;

        startTime = System.currentTimeMillis();
        for(int currentMS2ScanNumber : ms2ScanNumbers){
            //get Precursor information
            Scan currentMS2Scan = msRun.getScanByNum((long) currentMS2ScanNumber);
            //to actually extract the isolated peak, parse filter line
            String filterLine = currentMS2Scan.getFilterLine();
            //filter line format: "FTMS + c NSI d Full ms2 1831.9665@cid30.00 [225.0000-1674.0000]"
            //isolated precursor info is in front of @
            //indexOf returns -1 if char is not present
            //with with 4 decimals, 8 or 9 chars are present, with 1xxx. or xxx., but space doesn't matter

            double isolatedPrecursorMass = MySpectrum.extractPrecursorFromFilterLine(filterLine);

            //isolatedPrecursorMass is the real peak which was isolated
            //find previous MS1 spectrum
            int closestMS1Scan = 1;
            for (int ms1ScanNumber : ms1ScanNumbers){
                if(ms1ScanNumber > currentMS2ScanNumber)
                    break;
                closestMS1Scan = ms1ScanNumber;
            }
            //load this scan
            MySpectrum previousMS1Spectrum = MzXMLReadIn.mzXMLToMySpectrum(msRun, ""+closestMS1Scan);
            Peak matchedIsoPrecursor = previousMS1Spectrum.getMatchingPeak(isolatedPrecursorMass,8);

            double snIsolatedPeak;

            if(matchedIsoPrecursor == null){
                snIsolatedPeak = 0;
            }
            else {
                snIsolatedPeak = matchedIsoPrecursor.getSignalToNoise();
            }


            //write info
            sb.append(currentMS2ScanNumber).append(",");
            sb.append(isolatedPrecursorMass).append(",");
            sb.append(snIsolatedPeak).append(",\n");

            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            analyzedSpectra++;
        }

        pw.flush();
        pw.close();
        System.out.println("Loop complete: "+((System.currentTimeMillis()-startTime)/1000) +" seconds passed.");
        System.out.println("Analysis complete! Analyzed "+analyzedSpectra+" spectra.");
    }


    public static void whichMassIsReported(String triceratopsRunIn, String msConvertRunIn) throws MzXMLParsingException, JMzReaderException {

        //set up MzXML Files
        File mzXMLFile = new File(triceratopsRunIn);
        MzXMLFile triceratops = new MzXMLFile(mzXMLFile);

        File mzXMLFile2 = new File(msConvertRunIn);
        MzXMLFile msConvert = new MzXMLFile(mzXMLFile2);

        //set up output file
        String outputFilePath = triceratopsRunIn.replace(".mzXML","_reportedPrecursor.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        }
        catch (FileNotFoundException e){
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        //Header
        StringBuilder sb = new StringBuilder();
        sb.append("MS2 Scan-#").append(",");
        sb.append("Precursor Mass isolated [m/z]").append(",");
        sb.append("Precursor Mass reported triceratops [m/z]").append(",");
        sb.append("Precursor Mass reported MSConvert [m/z]").append(",\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);



        //preparation done, start main loop
        int analyzedSpectra = 0;

        for(long scanNumber : triceratops.getScanNumbers()){
            //get Precursor information
            Scan currentMSScan = triceratops.getScanByNum(scanNumber);
            if (currentMSScan.getMsLevel() != 2L)
                continue;
            //to actually extract the isolated peak, parse filter line
            String filterLine = currentMSScan.getFilterLine();
            double isolatedPrecursorMass = MySpectrum.extractPrecursorFromFilterLine(filterLine);

            double triceratopsMass = currentMSScan.getPrecursorMz().get(0).getValue();

            Scan msConvertScan = msConvert.getScanByNum(scanNumber);

            double msConvertMass = msConvertScan.getPrecursorMz().get(0).getValue();


            //write info
            sb.append(scanNumber).append(",");
            sb.append(fourDec.format(isolatedPrecursorMass)).append(",");
            sb.append(fourDec.format(triceratopsMass)).append(",");
            sb.append(fourDec.format(msConvertMass)).append(",\n");

            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            analyzedSpectra++;
        }

        pw.flush();
        pw.close();
        System.out.println("Analysis complete! Analyzed "+analyzedSpectra+" spectra.");
    }

}
