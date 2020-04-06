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

public class ExtractMassIntensities {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");
    //masses to check include the label and mass
    private static class MassToCheck {
        private String label;
        private double mass;

        public MassToCheck(String labelIn, double massIn){
            this.label = labelIn;
            this.mass = massIn;
        }


        public String getLabel(){return this.label;}
        public double getMass(){return this.mass;}
    }

public static void massExctrator(String mzXMLPath, String inputFilePath) throws MzXMLParsingException, JMzReaderException {
    //set up input file
    File inputFile = new File(inputFilePath);
    Scanner scanner = null;
    try {
        scanner = new Scanner(inputFile);
    }
    catch (FileNotFoundException e){
        System.out.println("File not found! File Location: "+inputFilePath);
    }
    //read in of the precursor masses, the masses to check, fragmentation type and if feature intensities should be used
    //precursor masses
    String[] precursorsLine = scanner.nextLine().split(",");
    ArrayList<Double> precursorList = new ArrayList<>();
    for(int i = 1; i < precursorsLine.length; i++){
        //skip first element, thats the header
        try {
            precursorList.add(Double.parseDouble(precursorsLine[i]));
        }
        catch (NumberFormatException e){
            continue;
        }
    }

    //masses to check with the respective label
    String[] massLine = scanner.nextLine().split(",");
    ArrayList<MassToCheck> massList = new ArrayList<>();
    for (int i = 1; i < massLine.length; i++){
        //entries consist of label:mass
        String[] entries = massLine[i].split(":");
        massList.add(new MassToCheck(entries[0],Double.parseDouble(entries[1])));
    }

    //fragmentation method
    String[] fragMethodLine = scanner.nextLine().split(",");
    String fragMethod = fragMethodLine[1];

    //should features be used?
    boolean featureIntUsed;
    String[] featureLine = scanner.nextLine().split(",");
    switch(featureLine[1]){
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
        System.out.println("Something went wrong with the output file creation! Output File Path: "+outputFileLocation);
    }

    //always have 5 columns: scan#, fragmentation energy, precursor mass, precursor z, precursor int
    //in addition, all the queried intensities
    int numberOfHeaders = 5 + massList.size();
    String[] header = new String[numberOfHeaders];
    header[0] = "Scan-#,";
    header[1] = fragMethod + " energy,";
    header[2] = "Precursor m/z,";
    header[3] = "Precursor z,";
    header[4] = "Precursor Int. [a.u.],";

    int currentHeaderPos = 5;

    for(MassToCheck mass : massList){
        header[currentHeaderPos] = mass.getLabel() + " (" + fourDec.format(mass.getMass()) + "),";
        currentHeaderPos++;
    }
    StringBuilder sb = new StringBuilder();

    for(String text : header){
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
    int multipleMatches = 0;

    //go through all spectra and analyze them if the fragmentation method is correct and the precursor mass is in the list

    for (int i = 1; i<numberOfScans;i++){
        System.out.println("working on scan: "+i);
        Scan currentScan = null;
        try {
             currentScan = msRun.getScanByNum((long) i);
        }
        catch (MzXMLParsingException e){
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
        for (double precursor : precursorList){
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
        MySpectrum currentMySpectrum = MzXMLReadIn.mzXMLToMySpectrum(msRun, ""+i);
        currentMySpectrum.assignZAndFeatures(10);
        ArrayList<Peak> peakList = currentMySpectrum.getPeakList();

        //put the measured intensities in this array
        //check the precursor int in the spectrum
        double precursorInt = 0;

        for(Peak peak : peakList){
           if(peak.getMass() < currentPrecursor - 0.5)
               continue;
           if(DeviationCalc.ppmMatch(currentPrecursor, peak.getMass(), 10)){
               if(featureIntUsed){
                   precursorInt = peak.getFeatureIntensity();
               }
               else{
                   precursorInt = peak.getIntensity();
               }
               break;
            }
        }

        //use array to store intensities of the masses to check
        double[] intensities = new double[massList.size()];
        int currentPosInt = 0;

        //loop through all the masses to check
        for(MassToCheck toCheck : massList){
            double ppmDev = 11;
            //loop through all peaks
            for (Peak peak : peakList){
                //this skipping forward should be more efficient than checking for every peak in detail
                if (peak.getMass() < toCheck.getMass() - 0.5)
                    continue;
                //second check to quit loop prematurely
                if (peak.getMass() > toCheck.getMass() + 0.5)
                    break;

                //now only masses are checked in detail which are in the range
                if(DeviationCalc.ppmMatch(toCheck.getMass(), peak.getMass(), 10)){
                    double currentPPMDev = DeviationCalc.ppmDeviationCalc(toCheck.getMass(), peak.getMass());
                    //check if better match than the one before
                    if (Math.abs(currentPPMDev)< Math.abs(ppmDev)){
                        ppmDev = currentPPMDev;
                        multipleMatches++;
                        if(featureIntUsed){
                            intensities[currentPosInt] = peak.getFeatureIntensity();
                        }
                        else {
                            intensities[currentPosInt] = peak.getIntensity();
                        }
                    }
                }
            }
            currentPosInt++;
        }
        //intensities are read out, put tem into .csv
        //scan number
        sb.append(i).append(",");
        //fragmentation energy
        sb.append(Math.round(currentScan.getCollisionEnergy())).append(",");
        //picked precursor M/Z
        sb.append(twoDec.format(currentPrecursor)).append(",");
        //precursor z
        sb.append(precursorCharge).append(",");
        //precursor Intensity
        sb.append(scientific.format(precursorInt)).append(",");
        //now all the found intensities
        for (double intensity : intensities){
            sb.append(scientific.format(intensity)).append(",");
        }
        sb.append("\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);
        //System.out.println("Analyzed Scan-# "+i);
    }
    pw.close();
    System.out.println("Analysis complete! Analyzed "+numberOfScansUsed+" spectra out of " + numberOfScans + " in file!");
    System.out.println("Multiple matches: "+multipleMatches);
}
}
