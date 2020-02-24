import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by micha on 6/30/2017.
 */
public class CSVCreator {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");

    public static void compClusterMatchCSVPrinter(ArrayList<CompClusterIonMatch> matchesIn, String filePathIn) throws FileNotFoundException{
        File csvOut = new File(filePathIn);
        PrintWriter csvWriter =  new PrintWriter(csvOut);
        StringBuilder sb = new StringBuilder();

        //define captions
        String[] header = new String[19];
        header[0] = "Modified Peptide";
        header[1] = "Precursor Mass [m/z]";
        header[2] = "Precursor Charge";
        header[3] = "Label Count";
        header[4] = "Label Name";
        header[5] = "Cleaved Labels";
        header[6] = "Mixed Labels";
        header[7] = "Fragment Ion";
        header[8] = "Fragment Ion Charge";
        header[9] = "Fragment Ion Mass [m/z]";
        header[10] = "Peak Charge";
        header[11] = "Peak Mass [m/z]";
        header[12] = "Mass Deviation [ppm]";
        header[13] = "Peak rel. Intensity [%]";
        header[14] = "Peak abs. Intensity [au]";
        header[15] = "Scan Number";
        header[16] = "Fragment Ion Amino Acid Sequence";
        header[17] = "Fragment Ion Sum Formula";
        header[18] = "Leading Proteins";


        //Write captions in file
        //first seperator is empty
        String sep = "";
        for (String caption : header){
            sb.append(sep);
            sb.append(caption);
            sep = ",";
        }
        //new row
        sb.append('\n');

        //handle the CompClusterMatches now
        for (CompClusterIonMatch match : matchesIn){
            //get the values
            String[] values = new String[19];
            values[0] = match.getFragmentIon().getPrecursorSequence(); //"Modified Peptide";


            String[] splitScanHeader = match.getScanHeader().split(";");

            values[1] = splitScanHeader[0]; //"Precursor Mass [m/z]";
            values[2] = splitScanHeader[1]; //"Precursor Charge";

            values[3] = Integer.toString(match.getFragmentIon().getLabelQuantity()); //"Label Count";
            values[4] = match.getLabelName(); //"Label Name";
            values[5] = ""+match.getIsCleaved();//"Cleaved Labels";
            values[6] = ""+match.getMixedLabels(); //"Mixed Labels";
            values[7] = match.getFragmentIon().getCompleteIon(); //"Fragment Ion";
            values[8] = Integer.toString(match.getFragmentIon().getCharge()); //"Fragment Ion Charge";
            values[9] = fourDec.format(match.getFragmentIon().getMToZ()); //"Fragment Ion Mass [m/z]";
            values[10] = Integer.toString(match.getPeak().getCharge()); //"Peak Charge";
            values[11] = fourDec.format(match.getPeak().getMass()); //"Peak Mass [m/z]";
            values[12] = twoDec.format(match.getPpmDeviation());//"Mass Deviation [ppm]";
            values[13] = twoDec.format(match.getPeak().getRelIntensity()); //"Peak rel. Intensity [%]";
            values[14] = scientific.format(match.getPeak().getIntensity()); //"Peak abs. Intensity [au]";
            values[15] = Integer.toString(match.getPeak().getScanNumber()); //"Scan Number";
            values[16] = match.getFragmentIon().getAASequence();//"Fragment Ion Sequence";
            values[17] = match.getFragmentIon().getFormula().getSumFormula();//"Fragment Ion Sum Formula";
            values[18] = match.getLeadingProteins();

            //start writing
            sep = "";
            for (String value : values){
                sb.append(sep);
                sb.append(value);
                sep = ",";
            }
            sb.append('\n');
        }
        //all the matches are added to StringBuilder
        //write to .csv File
        csvWriter.write(sb.toString());
        csvWriter.close();
        System.out.println(".csv-file created!");
    }
    public static StringBuilder compClusterSBCreator(ArrayList<CompClusterIonMatch> matchesIn){
        StringBuilder matchesSB = new StringBuilder();



        //handle the CompClusterMatches now
        for (CompClusterIonMatch match : matchesIn){
            //get the values
            String[] values = new String[19];
            values[0] = match.getFragmentIon().getPrecursorSequence(); //"Modified Peptide";


            String[] splitScanHeader = match.getScanHeader().split(";");

            values[1] = splitScanHeader[0]; //"Precursor Mass [m/z]";
            values[2] = splitScanHeader[1]; //"Precursor Charge";

            values[3] = Integer.toString(match.getFragmentIon().getLabelQuantity()); //"Label Count";
            values[4] = match.getLabelName(); //"Label Name";
            values[5] = ""+match.getIsCleaved();//"Cleaved Labels";
            values[6] = ""+match.getMixedLabels(); //"Mixed Labels";
            values[7] = match.getFragmentIon().getCompleteIon(); //"Fragment Ion";
            values[8] = Integer.toString(match.getFragmentIon().getCharge()); //"Fragment Ion Charge";
            values[9] = fourDec.format(match.getFragmentIon().getMToZ()); //"Fragment Ion Mass [m/z]";
            values[10] = Integer.toString(match.getPeak().getCharge()); //"Peak Charge";
            values[11] = fourDec.format(match.getPeak().getMass()); //"Peak Mass [m/z]";
            values[12] = twoDec.format(match.getPpmDeviation());//"Mass Deviation [ppm]";
            values[13] = twoDec.format(match.getPeak().getRelIntensity()); //"Peak rel. Intensity [%]";
            values[14] = scientific.format(match.getPeak().getIntensity()); //"Peak abs. Intensity [au]";
            values[15] = Integer.toString(match.getPeak().getScanNumber()); //"Scan Number";
            values[16] = match.getFragmentIon().getAASequence();//"Fragment Ion Sequence";
            values[17] = match.getFragmentIon().getFormula().getSumFormula();//"Fragment Ion Sum Formula";
            values[18] = match.getLeadingProteins();

            //start writing
            String sep = "";
            for (String value : values){
                matchesSB.append(sep);
                matchesSB.append(value);
                sep = ",";
            }
            matchesSB.append('\n');
        }
        //all the matches are added to StringBuilder
        return matchesSB;

    }

    //combine different .csv Files into one big .csv file
    public static void csvFileCombiner(String folderPath) throws FileNotFoundException {
        ArrayList<String> fileNames = new ArrayList<>();
        File folder = new File(folderPath);
        //get ArrayList<String> of all the .csv files in this folder
        fileNames = getFolderCSVFileNames(folder);
        ArrayList<File> csvFiles = new ArrayList<>();
        //create all the .csv File objects
        folderPath = folderPath.replace("\\", "\\\\");
        for (String fileName : fileNames){
            String pathName = folderPath+fileName;
            csvFiles.add(new File(pathName));
        }
        //prepare writing of new .csv
        String folderName = folder.getName();
        String completeCSVName = folderPath + "\\"+folderName+"_complete.csv";
        File completeCSV = new File(completeCSVName);
        PrintWriter csvWriter = new PrintWriter(completeCSV);

        //set boolean for headers
        boolean headerWritten = false;
        //loop through all the Files
        for (File currentCSV : csvFiles){
            //starting scanner and StringBuilder
            Scanner scanner = new Scanner(currentCSV);
            StringBuilder sb = new StringBuilder();
            //handle writing of headers
            if (!headerWritten){
                String header = scanner.nextLine();
                sb.append(header);
                sb.append('\n');
                headerWritten = true;
            }
            //if not first File, advance scanner one line
            else {
                scanner.nextLine();
            }
            //go through whole file
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                sb.append(line);
                sb.append('\n');
            }
            //write to complete .csv and close this scanner
            scanner.close();
            csvWriter.write(sb.toString());
            csvWriter.flush();
            //leave csvWriter open for next go
            System.out.println(".csv File added to complete File: "+currentCSV.getName());
            //delete current StringBuilder

        }
        csvWriter.close();
        System.out.println(".csv File created from "+csvFiles.size()+" individual files.");

    }

    private static ArrayList<String> getFolderCSVFileNames(final File folder){
        ArrayList<String> fileNames = new ArrayList<>();
        for (final File individualFiles : folder.listFiles()){
            if (individualFiles.getName().contains(".csv"))
                fileNames.add(individualFiles.getName());
        }
        return fileNames;
    }

    public static void createMassDiffCSV(ArrayList<Double> diffsIn, String filePathIn) throws FileNotFoundException {
        filePathIn = filePathIn + "massDiffAnalysis.csv";
        File csvOut = new File(filePathIn);
        PrintWriter csvWriter =  new PrintWriter(csvOut);
        StringBuilder sb = new StringBuilder();
        for (Double diff : diffsIn){
            sb.append(fourDec.format(diff));
            sb.append("\n");
            csvWriter.write(sb.toString());
            csvWriter.flush();
            sb.setLength(0);
        }
        csvWriter.close();
        System.out.println(".csv-File created!");
    }
    public static ArrayList<String> xWalkAminoAcidCounter(String sequence, AminoAcid aaToCheck) {
        ArrayList<String> output = new ArrayList<>();
        int occurences = 0;
        char charToCheck = aaToCheck.get1Let();
        String threeLetter = aaToCheck.get3Let().toUpperCase();
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == charToCheck) {
                output.add(threeLetter + "-" + (i + 1)+"-A-CA");
                occurences++;
            }
        }
        System.out.println("Counting complete! " +aaToCheck.getName()+ " occurences: " + occurences);
        return output;
    }

    public static void xWalkInputFileGenerator(String sequence, ArrayList<AminoAcid> aaToCheck, String folderPath, AminoAcid necessaryAA) throws FileNotFoundException {
        ArrayList<String> combinedList = new ArrayList<>();

        for (AminoAcid currentAA : aaToCheck){
            combinedList.addAll(xWalkAminoAcidCounter(sequence, currentAA));
        }
        //combined List contains all the occurrences of the AAs
        ArrayList<String> allCombinationsList = new ArrayList<>();
        for (int i = 0; i < combinedList.size(); i++){
            String current = combinedList.get(i);
            for (int j = i+1; j< combinedList.size(); j++){
                allCombinationsList.add(current + "\t" + combinedList.get(j));
            }
        }
        String threeLetterNecessary = necessaryAA.get3Let().toUpperCase();
        int iterator = 1;

        String filePath = folderPath + "forXWalkAnalysis.txt";
        File txtFile = new File(filePath);
        PrintWriter tsvWriter = new PrintWriter(txtFile);

        for (String current : allCombinationsList){
            if (current.contains(threeLetterNecessary)){
                String currentLine = iterator + "\t\t" + current + "\n";
                tsvWriter.write(currentLine);
                tsvWriter.flush();
                iterator++;
            }
        }
        System.out.println("List generation complete with "+iterator+" lines.");
        tsvWriter.close();
    }

}
