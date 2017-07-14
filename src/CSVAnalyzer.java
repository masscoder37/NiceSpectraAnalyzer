import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
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
        //3 Number uncleaved fragments
        //4 Mean intensity uncleaved fragments
        //5 Number cleaved fragments
        //6 Mean intensity cleaved fragments
        //only in case of EC, also add other information to analyze duplex
        //7 Number EC 179 fragments
        //8 Mean intensity EC179
        //9 Number EC 180 fragments
        //10 Mean intensity EC180
        String header = "Modified Peptide";
        sb.append(header);
        sb.append(',');
        header = "Precursor Charge";
        sb.append(header);
        sb.append(',');
        header = "Scan Number";
        sb.append(header);
        sb.append(',');
        header = "Number uncleaved fragments";
        sb.append(header);
        sb.append(',');
        header = "Mean intensity uncleaved fragments [%]";
        sb.append(header);
        sb.append(',');
        header = "Number cleaved fragments";
        sb.append(header);
        sb.append(',');
        header = "Mean intensity cleaved fragments [%]";
        sb.append(header);

        //only write this part of the header if csv contains EC
        if (ec){
            sb.append(',');
            header = "Number EC179 fragments";
            sb.append(header);
            sb.append(',');
            header = "Mean intensity EC179 fragments [%]";
            sb.append(header);
            sb.append(',');
            header = "Number EC180 fragments";
            sb.append(header);
            sb.append(',');
            header = "Mean intensity EC180 fragments [%]";
            sb.append(header);
        }
        sb.append('\n');
        csvWriter.write(sb.toString());
        csvWriter.flush();
        //empty stringbuilder
        sb.setLength(0);

        //active and current Peptide and charge state
        String activePeptide = "";
        String activeChargeStateString = "";
        int activeChargeState = 0;
        String currentPeptide = "";
        String currentChargeStateString;
        int currentChargeState = 0;
        String activeScanNumber;

        //variables that have to be read out or calculated
        int uncleavedFragments = 0;
        int cleavedFragments = 0;
        double uncleavedRelInt = 0;
        double cleavedRelInt = 0;
        double uncleavedMeanInt = 0;
        double cleavedMeanInt = 0;
            int cleavedEC179 = 0;
            int cleavedEC180 = 0;
            double cleavedEC179RelInt = 0;
            double cleavedEC180RelInt = 0;
            double cleavedEC179MeanInt = 0;
            double cleavedEC180MeanInt = 0;


        //set length of output string[]
        int outputLength;
        if (ec)
            outputLength = 11;
        else
            outputLength = 7;

        int handledPeptides = 1;

        //skip first line for header
        scanner.nextLine();

        //read in first line and set first peptide
        String firstLine = scanner.nextLine();
        String[] firstLineValues = firstLine.split(",");
        activePeptide = firstLineValues[0];
        activeChargeStateString = firstLineValues[2].replace("+","");
        activeChargeState = Integer.parseInt(activeChargeStateString);
        activeScanNumber = firstLineValues[15];

        //do first readout of values

        //cleaved label
        if (firstLineValues[5].equals("true")){
            cleavedFragments++;
            cleavedRelInt += Double.parseDouble(firstLineValues[13]);
        }
        //uncleaved label
        if (firstLineValues[5].equals("false")){
            uncleavedFragments++;
            uncleavedRelInt += Double.parseDouble(firstLineValues[13]);
        }
        //only in case of EC
        if (ec){
            //only if label is cleaved
            if (firstLineValues[5].equals("true")){
                if (firstLineValues[4].contains("179")){
                    cleavedEC179++;
                    cleavedEC179RelInt += Double.parseDouble(firstLineValues[13]);
                }
                if (firstLineValues[4].contains("180")){
                    cleavedEC180++;
                    cleavedEC180RelInt += Double.parseDouble(firstLineValues[13]);
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

            //set current Peptide with charge state
            currentPeptide = values[0];
            currentChargeStateString = values[2].replace("+","");
            currentChargeState = Integer.parseInt(currentChargeStateString);

            //check if current Peptide is same as active Peptide
            //if not, start Printout Sequence
            if (!currentPeptide.equals(activePeptide) || (currentPeptide.equals(activePeptide) && currentChargeState != activeChargeState )){
                String[] outputString = new String[outputLength];
                //0 Modified Peptide
                //1 Precursor Charge
                //2 Scan Number
                //3 Number uncleaved fragments
                //4 Mean intensity uncleaved fragments
                //5 Number cleaved fragments
                //6 Mean intensity cleaved fragments
                //only in case of EC, also add other information to analyze duplex
                //7 Number EC 179 fragments
                //8 Mean intensity EC179
                //9 Number EC 180 fragments
                //10 Mean intensity EC180
                outputString[0] = activePeptide;
                outputString[1] = Integer.toString(activeChargeState) + "+";
                outputString[2] = activeScanNumber;
                outputString[3] = Integer.toString(uncleavedFragments);
                if (uncleavedFragments == 0){
                    uncleavedMeanInt = 0;
                }
                else{
                    uncleavedMeanInt = uncleavedRelInt / (double) uncleavedFragments;
                }
                outputString[4] = twoDec.format(uncleavedMeanInt);
                outputString[5] = Integer.toString(cleavedFragments);
                if (cleavedFragments == 0){
                    cleavedMeanInt = 0;
                }
                else{
                    cleavedMeanInt = cleavedRelInt / (double)cleavedFragments;
                }
                outputString[6] = twoDec.format(cleavedMeanInt);
                if (ec) {
                    outputString[7] = Integer.toString(cleavedEC179);
                    if (cleavedEC179 == 0){
                        cleavedEC179MeanInt = 0;
                    }
                    else{
                        cleavedEC179MeanInt = cleavedEC179RelInt / (double)cleavedEC179;
                    }
                    outputString[8] = twoDec.format(cleavedEC179MeanInt);
                    outputString[9] = Integer.toString(cleavedEC180);
                    if (cleavedEC180 == 0){
                        cleavedEC180MeanInt = 0;
                    }
                    else{
                        cleavedEC180MeanInt = cleavedEC180RelInt / (double)cleavedEC180;
                    }
                    outputString[10] = twoDec.format(cleavedEC180MeanInt);
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
                uncleavedRelInt = 0;
                cleavedRelInt = 0;
                uncleavedMeanInt = 0;
                cleavedMeanInt = 0;
                cleavedEC179 = 0;
                cleavedEC180 = 0;
                cleavedEC179RelInt = 0;
                cleavedEC180RelInt = 0;
                cleavedEC179MeanInt = 0;
                cleavedEC180MeanInt = 0;

                //set the current peptide and charge state as the active ones
                activePeptide = currentPeptide;
                activeChargeState = currentChargeState;
                activeScanNumber = values[15];

                handledPeptides++;
            }
            //now process current values

            //cleaved label
            if (values[5].equals("true")){
                cleavedFragments++;
                cleavedRelInt += Double.parseDouble(values[13]);
            }
            //uncleaved label
            if (values[5].equals("false")){
                uncleavedFragments++;
                uncleavedRelInt += Double.parseDouble(values[13]);
            }
            //only in case of EC
            if (ec){
                //only if label is cleaved
                if (values[5].equals("true")){
                    if (values[4].contains("179")){
                        cleavedEC179++;
                        cleavedEC179RelInt += Double.parseDouble(values[13]);
                    }
                    if (values[4].contains("180")){
                        cleavedEC180++;
                        cleavedEC180RelInt += Double.parseDouble(values[13]);
                    }
                }
            }
        }
        //handle last peptide
        String[] outputString = new String[outputLength];
        outputString[0] = activePeptide;
        outputString[1] = Integer.toString(activeChargeState) + "+";
        outputString[2] = activeScanNumber;
        outputString[3] = Integer.toString(uncleavedFragments);
        if (uncleavedFragments == 0){
            uncleavedMeanInt = 0;
        }
        else{
            uncleavedMeanInt = uncleavedRelInt / (double) uncleavedFragments;
        }
        outputString[4] = twoDec.format(uncleavedMeanInt);
        outputString[5] = Integer.toString(cleavedFragments);
        if (cleavedFragments == 0){
            cleavedMeanInt = 0;
        }
        else{
            cleavedMeanInt = cleavedRelInt / (double)cleavedFragments;
        }
        outputString[6] = twoDec.format(cleavedMeanInt);
        if (ec) {
            outputString[7] = Integer.toString(cleavedEC179);
            if (cleavedEC179 == 0){
                cleavedEC179MeanInt = 0;
            }
            else{
                cleavedEC179MeanInt = cleavedEC179RelInt / (double)cleavedEC179;
            }
            outputString[8] = twoDec.format(cleavedEC179MeanInt);
            outputString[9] = Integer.toString(cleavedEC180);
            if (cleavedEC180 == 0){
                cleavedEC180MeanInt = 0;
            }
            else{
                cleavedEC180MeanInt = cleavedEC180RelInt / (double)cleavedEC180;
            }
            outputString[10] = twoDec.format(cleavedEC180MeanInt);
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
}
