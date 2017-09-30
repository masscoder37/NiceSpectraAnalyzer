import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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

        //check if file name contains EC
        boolean ec = false;
        if (filePath.contains("EC"))
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
            header = "Number EC179 fragments";
            sb.append(header);
            sb.append(',');
            header = "Median intensity EC179 fragments [%]";
            sb.append(header);
            sb.append(',');
            header = "Number EC180 fragments";
            sb.append(header);
            sb.append(',');
            header = "Median intensity EC180 fragments [%]";
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
        //TODO:determine header positions

        //TODO:open SB and write new header
        //[0] Modified Peptide
        //[1] Precursor Mass
        //[2] Precursor Charge State
        //[3] Scan Number
        //[4] Leading Proteins
        //[5] Complementary Ion Cluster Pair
        //[6] Unadjusted Intensity SOT180c
        //[7] Unadjusted Intensity SOT179c
        //[8] Intensity SOT180c
        //[9] Intensity SOT179c
        //[10] ratio SOT179c/SOT180c




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
