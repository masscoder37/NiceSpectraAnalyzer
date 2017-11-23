import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by micha on 7/7/2017.
 */
public class CSVAnalyzer {

    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    //analyzes created comp-Cluster Matches list
    public static void cicStatistics(String filePath) throws FileNotFoundException {
        File cicAnalysis = new File(filePath);
        //scanner reads through results
        Scanner scanner = null;
        try {
            scanner = new Scanner(cicAnalysis);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read file: " + filePath);
        }

        //check if file name contains EC or SOT
        boolean ec = false;
        if (filePath.contains("EC")||filePath.contains("SOT"))
            ec = true;

        //scanner set up
        //set up Stringbuilder and PrinterWriter
        String newFilePath = filePath.replace(".csv", "_");
        newFilePath = newFilePath + "statistics.csv";


        File outputFile = new File(newFilePath);
        StringBuilder sb = new StringBuilder();
        PrintWriter csvWriter = new PrintWriter(outputFile);
        //write header
        //0 Modified Peptide
        //1 Precursor Charge
        //2 Scan Number
        //3 Leading Proteins
        //4 Number uncleaved fragments
        //5 Mean intensity uncleaved fragments
        //6 Number cleaved fragments
        //7 Mean intensity cleaved fragments
        //only in case of EC, also add other information to analyze duplex
        //8 Number EC 179 fragments
        //9 Mean intensity EC179
        //10 Number EC 180 fragments
        //11 Mean intensity EC180
        String header = "Modified Peptide";
        sb.append(header);
        sb.append(',');
        header = "Precursor Charge";
        sb.append(header);
        sb.append(',');
        header = "Scan Number";
        sb.append(header);
        sb.append(',');
        header = "Leading Proteins";
        sb.append(header);
        sb.append(',');
        header = "Number uncleaved fragments";
        sb.append(header);
        sb.append(',');
        header = "Median intensity uncleaved fragments [%]";
        sb.append(header);
        sb.append(',');
        header = "Number cleaved fragments";
        sb.append(header);
        sb.append(',');
        header = "Median intensity cleaved fragments [%]";
        sb.append(header);

        //only write this part of the header if csv contains EC
        if (ec){
            sb.append(',');
            header = "Number SOT179 fragments";
            sb.append(header);
            sb.append(',');
            header = "Median intensity SOT179 fragments [%]";
            sb.append(header);
            sb.append(',');
            header = "Number SOT180 fragments";
            sb.append(header);
            sb.append(',');
            header = "Median intensity SOT180 fragments [%]";
            sb.append(header);
        }
        sb.append('\n');
        csvWriter.write(sb.toString());
        csvWriter.flush();
        //empty stringbuilder
        sb.setLength(0);

        //active and current Peptide, charge state and scan number
        String activePeptide = "";
        String activeChargeStateString = "";
        int activeChargeState = 0;
        String currentPeptide = "";
        String currentChargeStateString;
        int currentChargeState = 0;
        int activeScanNumber;
        int currentScanNumber;
        String activeLeadingProteins;

        //variables that have to be read out or calculated
        int uncleavedFragments = 0;
        int cleavedFragments = 0;
        int cleavedEC179 = 0;
        int cleavedEC180 = 0;

        //lists for Median calculation
        ArrayList<Double> uncleavedIntList = new ArrayList<>();
        ArrayList<Double> cleavedIntList = new ArrayList<>();
        ArrayList<Double> cleavedEC179IntList = new ArrayList<>();
        ArrayList<Double> cleavedEC180IntList = new ArrayList<>();

        //set length of output string[]
        int outputLength;
        if (ec)
            outputLength = 12;
        else
            outputLength = 8;

        int handledPeptides = 1;

        //skip first line for header
        scanner.nextLine();

        //read in first line and set first peptide
        String firstLine = scanner.nextLine();
        String[] firstLineValues = firstLine.split(",");
        activePeptide = firstLineValues[0];
        activeChargeStateString = firstLineValues[2].replace("+","");
        activeChargeState = Integer.parseInt(activeChargeStateString);
        activeScanNumber = Integer.parseInt(firstLineValues[15]);
        activeLeadingProteins = firstLineValues[18];



        //do first readout of values

        //cleaved label
        if (firstLineValues[5].equals("true")){
            cleavedFragments++;
            cleavedIntList.add(Double.parseDouble(firstLineValues[13]));
        }
        //uncleaved label
        if (firstLineValues[5].equals("false")){
            uncleavedFragments++;
            uncleavedIntList.add(Double.parseDouble(firstLineValues[13]));
        }
        //only in case of EC
        if (ec){
            //only if label is cleaved
            if (firstLineValues[5].equals("true")){
                if (firstLineValues[4].contains("179")){
                    cleavedEC179++;
                    cleavedEC179IntList.add(Double.parseDouble(firstLineValues[13]));
                }
                if (firstLineValues[4].contains("180")){
                    cleavedEC180++;
                    cleavedEC180IntList.add(Double.parseDouble(firstLineValues[13]));
                }
            }
        }
        //all the values are read in now
        //active peptide is set
        //continue trough whole list
        while (scanner.hasNextLine()){
            String current = scanner.nextLine();
            String[] values = current.split(",");
            //0 Modified Peptide
            //1 Precursor Mass [m/z]
            //2 Precursor Charge
            //3 Label Count
            //4 Label Name
            //5 Cleaved Label
            //6 Mixed Label
            //7 Fragment Ion
            //8 Fragment Ion Charge
            //9 Fragment Ion Mass [m/z]
            //10 Peak Charge
            //11 Peak Mass [m/z]
            //12 Mass Deviation [ppm]
            //13 Peak rel. Intensity [%]
            //14 Peak abs. Intensity [au]
            //15 Scan Number
            //16 Fragment Ion Amino Acid Sequence
            //17 Fragment Ion Sum Formula
            //18 Leading Proteins

            //set current Peptide with charge state and ScanNumber
            currentPeptide = values[0];
            currentChargeStateString = values[2].replace("+","");
            currentChargeState = Integer.parseInt(currentChargeStateString);
            currentScanNumber = Integer.parseInt(values[15]);



            //check if current Peptide is same as active Peptide
            //if not, start Printout Sequence
            //if (!currentPeptide.equals(activePeptide) || (currentPeptide.equals(activePeptide) && currentChargeState != activeChargeState ))
            if (currentScanNumber != activeScanNumber)
            {
                String[] outputString = new String[outputLength];
                //0 Modified Peptide
                //1 Precursor Charge
                //2 Scan Number
                //3 Leading Proteins
                //4 Number uncleaved fragments
                //5 Mean intensity uncleaved fragments
                //6 Number cleaved fragments
                //7 Mean intensity cleaved fragments
                //only in case of EC, also add other information to analyze duplex
                //8 Number EC 179 fragments
                //9 Mean intensity EC179
                //10 Number EC 180 fragments
                //11 Mean intensity EC180
                outputString[0] = activePeptide;
                outputString[1] = Integer.toString(activeChargeState) + "+";
                outputString[2] = Integer.toString(activeScanNumber);
                outputString[3] = activeLeadingProteins;
                outputString[4] = Integer.toString(uncleavedFragments);
                outputString[5] = twoDec.format(medianCalc(uncleavedIntList));
                outputString[6] = Integer.toString(cleavedFragments);
                outputString[7] = twoDec.format(medianCalc(cleavedIntList));

                if (ec) {
                    outputString[8] = Integer.toString(cleavedEC179);
                    outputString[9] = twoDec.format(medianCalc(cleavedEC179IntList));
                    outputString[10] = Integer.toString(cleavedEC180);
                    outputString[11] = twoDec.format(medianCalc(cleavedEC180IntList));
                }

                //all values are set now
                //start Stringbuilder
                String sep = "";
                for (String string : outputString){
                    sb.append(sep);
                    sb.append(string);
                    sep = ",";
                }
                sb.append("\n");
                csvWriter.write(sb.toString());
                csvWriter.flush();
                System.out.println("Analyzed Peptide: "+handledPeptides);

                //clear stringbuilder
                sb.setLength(0);
                //set all relevant variables to 0
                uncleavedFragments = 0;
                cleavedFragments = 0;
                cleavedEC179 = 0;
                cleavedEC180 = 0;

                uncleavedIntList.clear();
                cleavedIntList.clear();
                cleavedEC179IntList.clear();
                cleavedEC180IntList.clear();


                //set the current peptide and charge state as the active ones
                activePeptide = currentPeptide;
                activeChargeState = currentChargeState;
                activeScanNumber = Integer.parseInt(values[15]);
                activeLeadingProteins = values[18];

                handledPeptides++;
            }
            //now process current values

            //cleaved label
            if (values[5].equals("true")){
                cleavedFragments++;
                cleavedIntList.add(Double.parseDouble(values[13]));
            }
            //uncleaved label
            if (values[5].equals("false")){
                uncleavedFragments++;
                uncleavedIntList.add(Double.parseDouble(values[13]));
            }
            //only in case of EC
            if (ec){
                //only if label is cleaved
                if (values[5].equals("true")){
                    if (values[4].contains("179")){
                        cleavedEC179++;
                        cleavedEC179IntList.add(Double.parseDouble(values[13]));
                    }
                    if (values[4].contains("180")){
                        cleavedEC180++;
                        cleavedEC180IntList.add(Double.parseDouble(values[13]));
                    }
                }
            }
        }
        //handle last peptide
        String[] outputString = new String[outputLength];
        outputString[0] = activePeptide;
        outputString[1] = Integer.toString(activeChargeState) + "+";
        outputString[2] = Integer.toString(activeScanNumber);
        outputString[3] = activeLeadingProteins;
        outputString[4] = Integer.toString(uncleavedFragments);
        outputString[5] = twoDec.format(medianCalc(uncleavedIntList));
        outputString[6] = Integer.toString(cleavedFragments);
        outputString[7] = twoDec.format(medianCalc(cleavedIntList));

        if (ec) {
            outputString[8] = Integer.toString(cleavedEC179);
            outputString[9] = twoDec.format(medianCalc(cleavedEC179IntList));
            outputString[10] = Integer.toString(cleavedEC180);
            outputString[11] = twoDec.format(medianCalc(cleavedEC180IntList));
        }
        //all values are set now
        //start Stringbuilder
        String sep = "";
        for (String string : outputString){
            sb.append(sep);
            sb.append(string);
            sep = ",";
        }
        sb.append("\n");
        csvWriter.write(sb.toString());
        csvWriter.flush();

        handledPeptides++;

        //close everything
        scanner.close();
        csvWriter.close();

        System.out.println("Analysis complete! Peptides handled in total: "+ (handledPeptides-1));
    }


    public static void cicRatioCalculator (String filePath) throws FileNotFoundException {
        File fragmentFile = new File(filePath);

        Scanner scanner = null;
        try {
            scanner = new Scanner(fragmentFile);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read file: " + filePath);
        }
        int scansAnalyzed = 0;
        //open SB and write new header
        //[0] Modified Peptide
        //[1] Precursor Mass
        //[2] Precursor Charge State
        //[3] Scan Number
        //[4] Leading Proteins
        //[5] Complementary Ion Cluster Pair
        //[6] Fragment Ion Amino Acid Sequence
        //[7] Unadjusted Intensity SOT180c
        //[8] Unadjusted Intensity SOT179c
        //[9] Intensity SOT180c
        //[10] Intensity SOT179c
        //[11] ratio SOT179c/SOT180c
        //[12] Isotope Pattern Factor

        //set up Stringbuilder and PrinterWriter
        String newFilePath = filePath.replace(".csv", "_");
        newFilePath = newFilePath + "complementaryClusters_nocutoff.csv";

        File outputFile = new File(newFilePath);
        StringBuilder sb = new StringBuilder();
        PrintWriter csvWriter = new PrintWriter(outputFile);

        String header = "Modified Peptide";
        sb.append(header);
        sb.append(',');
        header = "Precursor Mass [m/z]";
        sb.append(header);
        sb.append(',');
        header = "Precursor Charge State";
        sb.append(header);
        sb.append(',');
        header = "Scan Number";
        sb.append(header);
        sb.append(',');
        header = "Leading Proteins";
        sb.append(header);
        sb.append(',');
        header = "Complementary Ion Cluster Pair";
        sb.append(header);
        sb.append(',');
        header = "Fragment Ion Amino Acid Sequence";
        sb.append(header);
        sb.append(',');
        header = "Unadjusted Intensity SOT180c";
        sb.append(header);
        sb.append(',');
        header = "Unadjusted Intensity SOT179c";
        sb.append(header);
        sb.append(',');
        header = "Intensity SOT180c";
        sb.append(header);
        sb.append(',');
        header = "Intensity SOT179c";
        sb.append(header);
        sb.append(',');
        header = "ratio SOT179c/SOT180c";
        sb.append(header);
        sb.append(',');
        header = "Isotope Pattern Factor";
        sb.append(header);
        sb.append('\n');
        csvWriter.write(sb.toString());
        csvWriter.flush();
        //empty stringbuilder
        sb.setLength(0);


        //determine header positions important in the old fragment Ion list file

        //[0] Modified Peptide
        //[1] Precursor Mass [m/z]
        //[2] Precursor Charge
        //[3] Label Name
        //[4] Cleaved Labels
        //[5] Fragment Ion
        //[6] Fragment Ion Charge
        //[7] Fragment Ion Mass [m/z]
        //[8] Peak Mass [m/z]
        //[9] Mass Deviation [ppm]
        //[10] Peak rel. Intensity [%]
        //[11] Peak abs. Intensity [au]
        //[12] Scan Number
        //[13] Fragment Ion Amino Acid Sequence
        //[14] Fragment Ion Sum Formula
        //[15] Leading Proteins

        String oldHeader = scanner.nextLine();
        String headerCaptions[] = oldHeader.split(",");
        HashMap<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String captions : headerCaptions){
            switch (captions){
                case "Modified Peptide":
                    captionPositions.put("Modified Peptide", index);
                    break;
                case "Precursor Mass [m/z]":
                    captionPositions.put("Precursor Mass [m/z]", index);
                    break;
                case "Precursor Charge":
                    captionPositions.put("Precursor Charge", index);
                    break;
                case "Label Name":
                    captionPositions.put("Label Name", index);
                    break;
                case "Cleaved Labels":
                    captionPositions.put("Cleaved Labels", index);
                    break;
                case "Fragment Ion":
                    captionPositions.put("Fragment Ion", index);
                    break;
                case "Fragment Ion Charge":
                    captionPositions.put("Fragment Ion Charge", index);
                    break;
                case "Fragment Ion Mass [m/z]":
                    captionPositions.put("Fragment Ion Mass [m/z]", index);
                    break;
                case "Peak Mass [m/z]":
                    captionPositions.put("Peak Mass [m/z]", index);
                    break;
                case "Mass Deviation [ppm]":
                    captionPositions.put("Mass Deviation [ppm]", index);
                    break;
                case "Peak rel. Intensity [%]":
                    captionPositions.put("Peak rel. Intensity [%]", index);
                    break;
                case "Peak abs. Intensity [au]":
                    captionPositions.put("Peak abs. Intensity [au]", index);
                    break;
                case "Scan Number":
                    captionPositions.put("Scan Number", index);
                    break;
                case "Fragment Ion Amino Acid Sequence":
                    captionPositions.put("Fragment Ion Amino Acid Sequence", index);
                    break;
                case "Fragment Ion Sum Formula":
                    captionPositions.put("Fragment Ion Sum Formula", index);
                    break;
                case "Leading Proteins":
                    captionPositions.put("Leading Proteins", index);
                    break;
            }
            index++;
        }
        //check if HashMap has all necessary entries
        if(captionPositions.size() != 16)
            throw new IllegalArgumentException("Not all headers could be read in! Number of read in headers, out of 16 necessary: "+captionPositions.size());


        //caption positions read in, start to copy all the information of one scan together
        //the complementary ion information will be stored in an object of the type ComplementaryIon
        ArrayList<ComplementaryIon> compIonList = new ArrayList<>();
        ArrayList<ComplementaryCluster> compClusterList = new ArrayList<>();



        //start with first peptide, all the other direct variables and most importantly the scan number
        String currentLine = scanner.nextLine();
        String[] currentValues = currentLine.split(",");
        String activePeptide = currentValues[captionPositions.get("Modified Peptide")];
        String activePrecMass = currentValues[captionPositions.get("Precursor Mass [m/z]")];
        String activePrecChargeString = currentValues[captionPositions.get("Precursor Charge")].replace("+","");
        int activePrecCharge = Integer.parseInt(activePrecChargeString);
        String activeLeadingProtein = currentValues[captionPositions.get("Leading Proteins")];
        int activeScanNumber = Integer.parseInt(currentValues[captionPositions.get("Scan Number")]);
        double activeRelInt;
        //read the first set of fragment ion specific parameters to create a new complementary ion object and add it to the list
        //do this only if a cleaved Ion is present
        if (currentValues[captionPositions.get("Cleaved Labels")].equals("true")) {
            activeRelInt = Double.parseDouble(currentValues[captionPositions.get("Peak rel. Intensity [%]")]);
            if(activeRelInt >0) {
                compIonList.add(new ComplementaryIon(currentValues[captionPositions.get("Modified Peptide")],
                        currentValues[captionPositions.get("Label Name")], currentValues[captionPositions.get("Fragment Ion")],
                        currentValues[captionPositions.get("Fragment Ion Charge")], currentValues[captionPositions.get("Fragment Ion Mass [m/z]")],
                        currentValues[captionPositions.get("Peak Mass [m/z]")], currentValues[captionPositions.get("Mass Deviation [ppm]")],
                        currentValues[captionPositions.get("Peak rel. Intensity [%]")], currentValues[captionPositions.get("Peak abs. Intensity [au]")],
                        currentValues[captionPositions.get("Scan Number")], currentValues[captionPositions.get("Fragment Ion Amino Acid Sequence")],
                        currentValues[captionPositions.get("Fragment Ion Sum Formula")]));
            }
        }

        //now, start scanner loop and continue with the fragmentIonReading
        while (scanner.hasNextLine()){
            currentLine = scanner.nextLine();
            currentValues = currentLine.split(",");
            int currentScanNumber = Integer.parseInt(currentValues[captionPositions.get("Scan Number")]);
            //check if current Scan Number is still the active scan Number. If not, then lists must be handled
            if (currentScanNumber!=activeScanNumber){

                //with all the complementary ions in the list, now create complementary ion clusters by matching of ions
                compClusterList.addAll(ComplementaryCluster.compClusterMatcher(compIonList));

                //the ComplementaryCluster class will provide a string for the SB
                //write the lines to the .csv File
                sb.append(ComplementaryCluster.compClusterCSVStringProducer(compClusterList, activePeptide, activePrecMass, Integer.toString(activePrecCharge), activeLeadingProtein));
                csvWriter.write(sb.toString());
                csvWriter.flush();
                System.out.println("Analyzed scan: #" + activeScanNumber);

                //after old data is handled, reset everything and start readout of new data
                compIonList.clear();
                compClusterList.clear();
                sb.setLength(0);
                scansAnalyzed++;

                //set new active values
                activePeptide = currentValues[captionPositions.get("Modified Peptide")];
                activePrecMass = currentValues[captionPositions.get("Precursor Mass [m/z]")];
                activePrecChargeString = currentValues[captionPositions.get("Precursor Charge")].replace("+","");
                activePrecCharge = Integer.parseInt(activePrecChargeString);
                activeLeadingProtein = currentValues[captionPositions.get("Leading Proteins")];
                activeScanNumber = currentScanNumber;
            }
            //normal readout of all the values
            if (currentValues[captionPositions.get("Cleaved Labels")].equals("true")) {
                activeRelInt = Double.parseDouble(currentValues[captionPositions.get("Peak rel. Intensity [%]")]);
                if(activeRelInt >0) {
                    compIonList.add(new ComplementaryIon(currentValues[captionPositions.get("Modified Peptide")],
                            currentValues[captionPositions.get("Label Name")], currentValues[captionPositions.get("Fragment Ion")],
                            currentValues[captionPositions.get("Fragment Ion Charge")], currentValues[captionPositions.get("Fragment Ion Mass [m/z]")],
                            currentValues[captionPositions.get("Peak Mass [m/z]")], currentValues[captionPositions.get("Mass Deviation [ppm]")],
                            currentValues[captionPositions.get("Peak rel. Intensity [%]")], currentValues[captionPositions.get("Peak abs. Intensity [au]")],
                            currentValues[captionPositions.get("Scan Number")], currentValues[captionPositions.get("Fragment Ion Amino Acid Sequence")],
                            currentValues[captionPositions.get("Fragment Ion Sum Formula")]));
                }
            }
        }


        //last line read
        //all scans are completed, now create complementary ion clusters by matching of ions
        compClusterList.addAll(ComplementaryCluster.compClusterMatcher(compIonList));

        //the ComplementaryCluster class will provide a string for the SB
        //write the lines to the .csv File
        sb.append(ComplementaryCluster.compClusterCSVStringProducer(compClusterList, activePeptide, activePrecMass, Integer.toString(activePrecCharge), activeLeadingProtein));
        csvWriter.write(sb.toString());
        csvWriter.flush();
        scansAnalyzed++;



        //close everything
        compIonList.clear();
        compClusterList.clear();
        scanner.close();
        csvWriter.close();
        System.out.println("Analyzed scan: #" + activeScanNumber);
        System.out.println("Analysis complete! .csv-File created! Scans analyzed: "+ (scansAnalyzed));

    }


    //this function combines all the complementary ion cluster ratio information for a peptide species
    public static void clusterRatioPerPeptide(String filePathIn) throws  FileNotFoundException{
        //first, prepare new .csv File and write header
        String newFilePath = filePathIn.replace(".csv", "_");
        newFilePath = newFilePath + "ratioPerPeptide.csv";

        File outputFile = new File(newFilePath);
        StringBuilder sb = new StringBuilder();
        PrintWriter csvWriter = new PrintWriter(outputFile);

        //header structure
        //[0] Modified Peptide
        //[1] Number of Scans
        //[2] Scan Numbers
        //[3] Leading Proteins
        //[4] Utilized Ion Clusters
        //[5] Number of Utilized Ion Clusters
        //[6] Median Ratio 179c/180c
        //[7] Mean Ratio 179c/180c

        String[] header = new String[8];
        header[0] = "Modified Peptide";
        header[1] = "Number of Scans";
        header[2] = "Scan Numbers";
        header[3] = "Leading Proteins";
        header[4] = "Utilized Ion Clusters";
        header[5] = "Number of Utilized Ion Clusters";
        header[6] = "Median Ratio 179c/180c";
        header[7] = "Mean Ratio 179c/180c";

        String sep = "";
        for (String headerCaptions : header){
            sb.append(headerCaptions);
            sep = ",";
            sb.append(sep);
        }
        sb.append("\n");
        csvWriter.write(sb.toString());
        csvWriter.flush();
        sb.setLength(0);
        //header written, sb cleared

        //now, set important columns of old file
        File cicFile = new File(filePathIn);

        Scanner scanner = null;
        try {
            scanner = new Scanner(cicFile);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read file: " + filePathIn);
        }

        String oldHeader = scanner.nextLine();
        String oldHeaderCaptions[] = oldHeader.split(",");
        HashMap<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String captions : oldHeaderCaptions) {
        switch (captions){
            case "Modified Peptide":
                captionPositions.put("Modified Peptide", index);
                break;
            case "Scan Number":
                captionPositions.put("Scan Number", index);
                break;
            case "Leading Proteins":
                captionPositions.put("Leading Proteins", index);
                break;
            case "Complementary Ion Cluster Pair":
                captionPositions.put("Complementary Ion Cluster Pair", index);
                break;
            case "ratio SOT179c/SOT180c":
                captionPositions.put("ratio SOT179c/SOT180c", index);
                break;
        }
        index++;
        }

        //check if all important values were set
        if(captionPositions.size() != 5)
            throw new IllegalArgumentException("Not all headers could be read in! Number of read in headers, out of 5 necessary: "+captionPositions.size());

        //header of new file is ready, column positions of old file are read
        //start analysis of data

        //set all necessary variables;
        int handeledClusters = 0;
        String activePeptide, currentPeptide;
        String scanNumber;
        int currentScanNumber = 0;
        ArrayList<Integer> scanNumberList = new ArrayList<>();
        String activeLeadingProteins, currentLeadingProteins;
        String compIonClusterPair;
        int currentCompIonClusterPairNumber = 0;
        ArrayList<Double> currentRatioList = new ArrayList<>();
        double medianRatio, meanRatio, sumRatiosForMean = 0;

        //read first line to set active peptide and values:
        String currentLine = scanner.nextLine();

        String[] currentValues = currentLine.split(",");

        activePeptide = currentValues[captionPositions.get("Modified Peptide")];
        currentPeptide = activePeptide;
        scanNumber = currentValues[captionPositions.get("Scan Number")] + "; ";
        currentScanNumber = Integer.parseInt(currentValues[captionPositions.get("Scan Number")]);
        scanNumberList.add(currentScanNumber);
        activeLeadingProteins = currentValues[captionPositions.get("Leading Proteins")];
        currentLeadingProteins = activeLeadingProteins;
        compIonClusterPair = currentValues[captionPositions.get("Complementary Ion Cluster Pair")] + "; ";
        currentCompIonClusterPairNumber++;
        currentRatioList.add(Double.parseDouble(currentValues[captionPositions.get("ratio SOT179c/SOT180c")]));

        //start scanning through the table

        while (scanner.hasNextLine()){
            currentLine = scanner.nextLine();
            currentValues = currentLine.split(",");

            currentPeptide = currentValues[captionPositions.get("Modified Peptide")];
            currentLeadingProteins = currentValues[captionPositions.get("Leading Proteins")];

            //check if current Peptide & Leading Proteins is the same or different
            if (!currentPeptide.equals(activePeptide) && !currentLeadingProteins.equals(activeLeadingProteins)){
                //if they are different, start writing of the active Peptide
                //sb is empty at this point
                //header structure
                //[0] Modified Peptide
                //[1] Number of Scans
                //[2] Scan Numbers
                //[3] Leading Proteins
                //[4] Utilized Ion Clusters
                //[5] Number of Utilized Ion Clusters
                //[6] Median Ratio 179c/180c
                //[7] Mean Ratio 179c/180c

                sb.append(activePeptide+",");
                sb.append(Integer.toString(scanNumberList.size())+",");
                sb.append(scanNumber+",");
                sb.append(activeLeadingProteins+",");
                sb.append(compIonClusterPair+",");
                sb.append(Integer.toString(currentCompIonClusterPairNumber)+",");
                medianRatio = medianCalc(currentRatioList);
                sb.append(Double.toString(medianRatio)+",");
                for (double value : currentRatioList){
                    sumRatiosForMean =+ value;
                }
                if (currentRatioList.size() !=0)
                meanRatio = sumRatiosForMean / currentRatioList.size();
                else
                    meanRatio = 0;
                sb.append(Double.toString(meanRatio) + "\n");

                csvWriter.write(sb.toString());
                csvWriter.flush();
                handeledClusters++;
                System.out.println("Peptide added: "+activePeptide+" Total Peptides handeled: "+handeledClusters);


                //reset all the values
                sb.setLength(0);
                scanNumberList.clear();
                scanNumber="";
                currentRatioList.clear();
                sumRatiosForMean = 0;
                medianRatio = 0;
                meanRatio = 0;
                compIonClusterPair = "";
                currentCompIonClusterPairNumber = 0;




                //set new active peptide
                activePeptide = currentPeptide;
                activeLeadingProteins = currentLeadingProteins;
            }

            //now handle the other stuff

            //only add scan Number to list if not already in there
            currentScanNumber = Integer.parseInt(currentValues[captionPositions.get("Scan Number")]);
            if (!scanNumberList.contains(currentScanNumber)){
                scanNumberList.add(currentScanNumber);
                scanNumber += Integer.toString(currentScanNumber) + "; ";
            }

            compIonClusterPair += currentValues[captionPositions.get("Complementary Ion Cluster Pair")] + "; ";
            currentCompIonClusterPairNumber++;

            currentRatioList.add(Double.parseDouble(currentValues[captionPositions.get("ratio SOT179c/SOT180c")]));
        }
        //handle last peptide

        sb.append(activePeptide+",");
        sb.append(Integer.toString(scanNumberList.size())+",");
        sb.append(scanNumber+",");
        sb.append(activeLeadingProteins+",");
        sb.append(compIonClusterPair+",");
        sb.append(Integer.toString(currentCompIonClusterPairNumber)+",");
        medianRatio = medianCalc(currentRatioList);
        sb.append(Double.toString(medianRatio)+",");
        for (double value : currentRatioList){
            sumRatiosForMean =+ value;
        }
        if (currentRatioList.size() !=0)
            meanRatio = sumRatiosForMean / currentRatioList.size();
        else
            meanRatio = 0;
        sb.append(Double.toString(meanRatio) + "\n");
        handeledClusters++;
        System.out.println("Peptide added: "+activePeptide+" Total Peptides handeled: "+handeledClusters);

        csvWriter.write(sb.toString());
        csvWriter.flush();
        csvWriter.close();
        scanner.close();
    }




    private static double medianCalc(ArrayList<Double> valuesIn){
        double median;
        if (valuesIn.size() == 0)
        median = 0;
        else {
            //sort list
            Collections.sort(valuesIn);
            int middle = valuesIn.size()/2;
            //check if list length is even or odd
            //even
            if (valuesIn.size() % 2 == 0) {
                median = (valuesIn.get(middle)+valuesIn.get(middle-1))/2.0;
            }
            //odd
            else{
                median = valuesIn.get(middle);
            }
        }

        return median;
    }














}
