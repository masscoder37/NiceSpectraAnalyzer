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

    public static void massExctrator(String mzXMLPath, String inputFilePath) throws MzXMLParsingException, JMzReaderException {
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
            //loop through all the precursors and set variable if one is present
            for (double precursor : precursorList) {
                if (DeviationCalc.ppmMatch(precursor, currentPrecursor, 10))
                    precursorPresent = true;
            }
            //skip if still false
            if (!precursorPresent)
                continue;

            //now, this spectrum is analyzed
            numberOfScansUsed++;

            //get precursor charge state
            int precursorCharge = Math.toIntExact(currentScan.getPrecursorMz().get(0).getPrecursorCharge());

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
            System.out.println("working on scan: " + i + 1);
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
            System.out.println("spectrum: "+i);
        //only look at current MS level, skip otherwise
        if(currentScan.getMsLevel() != (long) scanLevelIn)
            continue;
        //only look at specified fragmentation method, skip otherwise
       if(!currentScan.getPrecursorMz().get(0).getActivationMethod().equals(fragMethodIn))
           continue;

        //check if precursor is within limits
        double currentPrecursor = currentScan.getPrecursorMz().get(0).getValue();
        if(precursorIn - 0.005 < currentPrecursor && precursorIn + 0.005 > currentPrecursor)
            numberOfSpectra++;
        }

        return numberOfSpectra;
    }

}
