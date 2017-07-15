import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by micha on 6/14/2017.
 */
public class CSVReader {
    public static ArrayList<AminoAcid> aminoAcidParse(File file) {

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read given file - " + file.getAbsolutePath());
            return null;
        }
        ArrayList<AminoAcid> acids = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] fields = line.split(",");
            if (fields.length < 4)
                continue;

            try {
                acids.add(new AminoAcid(fields[0], fields[1], fields[2], fields[3]));
            } catch (NumberFormatException e) {
                System.out.println("Invalid format : " + line);
                continue;
            }
        }
        scanner.close();
        return acids;
    }

    public static MySpectrum spectrumParse(File file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read given file - " + file.getAbsolutePath());
            return null;
        }
        //advance first line to read out scanNumber and scanHeader
        //scan Header format: first Scan Header, then scan number
        String header = scanner.nextLine();
        String[] headerInput = header.split(",");
        String scanHeader = headerInput[0];
        int spectrumScanNumber = Integer.parseInt(headerInput[1]);

        ArrayList<Peak> peaks = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] fields = line.split(",");
            if (fields.length < 4)
                continue;

            try {
                //format of fields: [0] exact mass, [1] intensity, [3] charge, [4] scanNumber of Peak
                double massIn = Double.parseDouble(fields[0]);
                double intIn = Double.parseDouble(fields[1]);
                int chargeIn = Integer.parseInt(fields[2]);
                int scanNumberIn = Integer.parseInt(fields[3]);
                peaks.add(new Peak(massIn, intIn, chargeIn, scanNumberIn));
            } catch (NumberFormatException e) {
                System.out.println("Invalid format : " + line);
                continue;
            }
        }
        scanner.close();
        MySpectrum spectrumOut = new MySpectrum(peaks, spectrumScanNumber, scanHeader);
        return spectrumOut;
    }


    public static void wholeRunCICChecker(MzXMLFile runIn, File evidence, ArrayList<AminoAcid> aminoAcids, double accuracy, int spectraAtOnce, String filePath, String labelIn) throws JMzReaderException, FileNotFoundException {
        if (!labelIn.equals("TMT") && !labelIn.equals("EC"))
            throw new IllegalArgumentException("Unknown label: "+labelIn+"! Please use TMT or EC");


        ArrayList<CompClusterIonMatch> allResults = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(evidence);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read given file - " + evidence.getAbsolutePath());
        }
        //read out the captions
        String captions = scanner.nextLine();
        String[] splitCaptions = captions.split("\t");
        //determine the important columns: "Sequence", "Modifications", "Modified sequence", "MS/MS Scan Number" and "Reporter intensity count 0"
        //create new map with those keywords and the column indices (starting from 0!) as values
        Map<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String caption : splitCaptions) {
            switch (caption) {
                case "Sequence":
                    captionPositions.put("Sequence", index);
                    break;
                case "Modifications":
                    captionPositions.put("Modifications", index);
                    break;
                case "Modified sequence":
                    captionPositions.put("Modified sequence", index);
                    break;
                case "MS/MS Scan Number":
                    captionPositions.put("MS/MS Scan Number", index);
                case "Reporter intensity count 0":
                    captionPositions.put("Reporter intensity count 0", index);
                    break;
            }
            index++;
        }

        if (captionPositions.size() != 5)
            throw new IllegalArgumentException("Not all required captions could be read!");
        int sortedOut = 0;
        int processedSpectra = 0;
        int addedSpectra = 0;

        //TODO : Writer open, write header

        //start reading in of values
        while (scanner.hasNextLine()) {
            String currentRow = scanner.nextLine();
            String[] values = currentRow.split("\t");
            //is there a reporter present?
            //if not, go to next line
            if (Integer.parseInt(values[captionPositions.get("Reporter intensity count 0")]) == 0) {
                sortedOut++;
                continue;
            }


            //read out the direct informations
            String sequence = values[captionPositions.get("Sequence")];
            String modSequence = values[captionPositions.get("Modified sequence")];
            String scanNumber = values[captionPositions.get("MS/MS Scan Number")];
            String modStatus = values[captionPositions.get("Modifications")];


            //use this information to create the ArrayList<Modification> necessary for the CompClusterChecker
            ArrayList<Modification> mods = new ArrayList<>();
            if (modStatus.equals("Unmodified")) {
                if (sequence.contains("C")) {
                    mods.add(Modification.carbamidomethylation());
                }
                ArrayList<CompClusterIonMatch> currentSpectrumMatches = new ArrayList<>();
                if (labelIn.equals("EC"))
                    currentSpectrumMatches = ComplementaryClusterChecker.compClusterCheckerEC(aminoAcids, sequence, mods, scanNumber, runIn, accuracy);
                if (labelIn.equals("TMT"))
                    currentSpectrumMatches = ComplementaryClusterChecker.compClusterCheckerTMT(aminoAcids, sequence, mods, scanNumber, runIn, accuracy);
                allResults.addAll(currentSpectrumMatches);
                processedSpectra++;
                System.out.println("Processed spectrum number: " + scanNumber);
                System.out.println("Processed spectra: " + processedSpectra);
                addedSpectra++;
                if (addedSpectra == spectraAtOnce) {
                    int indices = processedSpectra - addedSpectra +1;
                    String path = filePath + "_" + indices + "_" + processedSpectra + ".csv";
                    CSVCreator.compClusterMatchCSVPrinter(allResults, path);
                    allResults = new ArrayList<>();
                    addedSpectra = 0;
                }
                continue;
            }

            modSequence = modSequence.replace("_", "");
            while (modSequence.contains("(")) {
                int methPointer = 0;
                for (int a = 0; a < modSequence.length(); a++) {
                    char current = modSequence.charAt(a);
                    if (current == '(') {
                        //Methionine is at position a, because modifications start counting at 1
                        methPointer = a;
                        break;
                    }
                }
                if (methPointer != 0)
                    mods.add(Modification.oxidation(methPointer));

                //remove occurence of brackets handled
                if (methPointer + 5 >= modSequence.length())
                    break;
                String firstPart = modSequence.substring(0, methPointer - 1);
                String secondPart = modSequence.substring(methPointer + 5, modSequence.length());
                modSequence = firstPart + secondPart;
            }

            if (sequence.contains("C")) {
                mods.add(Modification.carbamidomethylation());
            }
            ArrayList<CompClusterIonMatch> currentSpectrumMatches = new ArrayList<>();
            if (labelIn.equals("EC"))
                currentSpectrumMatches = ComplementaryClusterChecker.compClusterCheckerEC(aminoAcids, sequence, mods, scanNumber, runIn, accuracy);
            if (labelIn.equals("TMT"))
                currentSpectrumMatches = ComplementaryClusterChecker.compClusterCheckerTMT(aminoAcids, sequence, mods, scanNumber, runIn, accuracy);
            allResults.addAll(currentSpectrumMatches);
            processedSpectra++;
            addedSpectra++;
            System.out.println("Processed spectrum number: " + scanNumber);
            System.out.println("Processed spectra: " + processedSpectra);
            if (addedSpectra == spectraAtOnce) {
                int indices = processedSpectra - addedSpectra + 1 ;
                String path = filePath + "_" + indices + "_" + processedSpectra + ".csv";
                CSVCreator.compClusterMatchCSVPrinter(allResults, path);
                allResults = new ArrayList<>();
                addedSpectra = 0;
            }
        }

        scanner.close();
        int indices = processedSpectra - addedSpectra +1;
        String path = filePath + "_" + indices + "_" + processedSpectra + ".csv";
        CSVCreator.compClusterMatchCSVPrinter(allResults, path);
        //TODO : writer.close

        System.out.println("Sorted out spectra (no reporter ion intensities): " + sortedOut);
        System.out.println("Processed spectra: " + processedSpectra);
    }

public static void wholeRunRepFinder(MzXMLFile runIn, File statisticsAnalysis, double ppmDev) throws FileNotFoundException, JMzReaderException {
    DecimalFormat twoDec = new DecimalFormat("0.00");
    DecimalFormat scientific = new DecimalFormat("0.00E0");
        //first, set label name
    String labelName = "";
    String fileName = statisticsAnalysis.getName();
    if (!fileName.contains("TMT")&&!fileName.contains("EC"))
        throw new IllegalArgumentException("Unknown label! Use Filename with EC or TMT. Filename: "+fileName);
    if (fileName.contains("TMT"))
        labelName = "TMT";
    if (fileName.contains("EC"))
        labelName = "EC";

    //initialize scanner
    Scanner scanner = null;
    try {
         scanner = new Scanner(statisticsAnalysis);
    } catch (FileNotFoundException e) {
        System.out.println("File not found! Location: " +statisticsAnalysis.getAbsolutePath());
    }

    //get header positions
    String header = scanner.nextLine();
    String headerCaptions[] = header.split(",");
    HashMap<String, Integer> captionPositions = new HashMap<>();
    int index = 0;
    for (String captions : headerCaptions){
        switch (captions){
            case "Modified Peptide":
                captionPositions.put("Modified Peptide", index);
                break;
            case "Precursor Charge":
                captionPositions.put("Precursor Charge", index);
                break;
            case "Scan Number":
                captionPositions.put("Scan Number", index);
                break;
        }
        index++;
    }

    //prepare new File and write header
    String newFilePath = statisticsAnalysis.getAbsolutePath().replace("_statistics.csv", "")+"_reporterIons.csv";
    File outputCSV = new File(newFilePath);
    PrintWriter csvWriter = new PrintWriter(outputCSV);
    StringBuilder sb = new StringBuilder();
    String[] newHeader = new String[9];
    newHeader[0] = "Modified Peptide";
    newHeader[1] = "Precursor Charge";
    newHeader[2] = "Scan Number";
    newHeader[3] = "Rep0 Relative Intensity [%]";
    newHeader[4] = "Rep0 Absolut Intensity [au]";
    newHeader[5] = "Rep0 Mass Deviation [ppm]";
    newHeader[6] = "Rep1 Relative Intensity [%]";
    newHeader[7] = "Rep1 Absolut Intensity [au]";
    newHeader[8] = "Rep1 Mass Deviation [ppm]";
    String sep = "";
    for (String s : newHeader){
        sb.append(sep);
        sb.append(s);
        sep = ",";
    }
    sb.append("\n");
    csvWriter.write(sb.toString());
    sb.setLength(0);

    //continue with scanning of the csv and readout of the values
    int handledSpectra = 1;
    while (scanner.hasNextLine()){
        String currentLine = scanner.nextLine();
        String[] values = currentLine.split(",");
        String[] newValues = new String[9];
        Arrays.fill(newValues, "");
        newValues[0] = values[captionPositions.get("Modified Peptide")];
        newValues[1] = values[captionPositions.get("Precursor Charge")];
        newValues[2] = values[captionPositions.get("Scan Number")];
        //generate MySpectrum and start search for Reporter Ions
        MySpectrum currentSpectrum = MzXMLReadIn.mzXMLToMySpectrum(runIn, newValues[2]);
        ArrayList<ReporterMatch> repMatches = new ArrayList<>();
        repMatches = PeakCompare.reporterFinder(currentSpectrum, labelName, ppmDev);
        //if no reporter ions are found, set values to 0
        if (repMatches.isEmpty()){
            for (int i = 3; i<newHeader.length; i++){
                newHeader[i] = "0";
            }
        }
        //loop through the matches
        if (repMatches.size() > 2)
            throw new IllegalArgumentException("More than 2 matched reporters! Size: "+repMatches.size());
        for (ReporterMatch rep : repMatches){
            if (rep.getRepName().equals("Rep0")){
                newValues[3] = twoDec.format(rep.getPeak().getRelIntensity());
                newValues[4] = scientific.format(rep.getPeak().getIntensity());
                newValues[5] = twoDec.format(rep.getPPMDev());
            }
            if (rep.getRepName().equals("Rep1")){
                newValues[6] = twoDec.format(rep.getPeak().getRelIntensity());
                newValues[7] = scientific.format(rep.getPeak().getIntensity());
                newValues[8] = twoDec.format(rep.getPPMDev());
            }
        }
        //set values to 0 if no rep was found
        for (int a = 0; a < newValues.length;a++){
            if (newValues[a].length()==0)
                newValues[a] += "0";
        }
        //use sb and write to file
        sep = "";
        for (String s : newValues){
            sb.append(sep);
            sb.append(s);
            sep = ",";
        }
        sb.append("\n");
        csvWriter.write(sb.toString());
        csvWriter.flush();
        System.out.println("Analyzed peptide: "+handledSpectra);
        handledSpectra++;

        //set values to 0 again
        sb.setLength(0);
        repMatches.clear();
    }
    scanner.close();
    csvWriter.close();
    System.out.println("Analysis complete! .csv File with "+ (handledSpectra-1)+" peptides created!");

}
}
