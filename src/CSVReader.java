import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

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


    public static void wholeRunCICChecker(MzXMLFile runIn, File evidence,
                                          ArrayList<AminoAcid> aminoAcids, double accuracy,
                                          String filePath, String labelIn) throws JMzReaderException, FileNotFoundException, MzXMLParsingException {
       /* if (!labelIn.equals("TMT") && !labelIn.equals("EC"))
            throw new IllegalArgumentException("Unknown label: "+labelIn+"! Please use TMT or EC");*/

        Scanner scanner = null;
        try {
            scanner = new Scanner(evidence);
        } catch (FileNotFoundException e) {
            System.out.println("Could not read given file - " + evidence.getAbsolutePath());
        }
        //read out the captions
        String captions = scanner.nextLine();
        String[] splitCaptions = captions.split("\t");
        //determine the important columns: "Sequence", "Modifications", "Modified sequence", "MS/MS Scan Number", "Reporter intensity count 0" and "Leading Proteins"
        //create new map with those keywords and the column indices (starting from 0!) as values
        Map<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String caption : splitCaptions) {
            caption = caption.toLowerCase();
            switch (caption) {
                case "sequence":
                    captionPositions.put("Sequence", index);
                    break;
                case "modifications":
                    captionPositions.put("Modifications", index);
                    break;
                case "modified sequence":
                    captionPositions.put("Modified sequence", index);
                    break;
                case "leading proteins":
                    captionPositions.put("Leading proteins", index);
                    break;
                case "ms/ms scan number":
                    captionPositions.put("MS/MS Scan Number", index);
                break;
                case "reporter intensity count 0":
                    captionPositions.put("Reporter intensity count 0", index);
                break;
            }
            index++;
        }

        if (captionPositions.size() != 6)
            throw new IllegalArgumentException("Not all required captions could be read!");
        int sortedOut = 0;
        int processedSpectra = 0;
        int addedSpectra = 0;

        //TODO : Writer open, write header
        File folder = new File(filePath);
        folder.mkdirs();
        String path = filePath + "labelFragmentIons.csv";
        File outputFile = new File(path);
        PrintWriter csvWriter = new PrintWriter(outputFile);
        StringBuilder sb = new StringBuilder();


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

        //write header and empty sb
        csvWriter.write(sb.toString());
        csvWriter.flush();
        sb.setLength(0);
        //TODO: Headers should be written by now

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


            //read out the direct information
            String sequence = values[captionPositions.get("Sequence")];
            String modSequence = values[captionPositions.get("Modified sequence")];
            String scanNumber = values[captionPositions.get("MS/MS Scan Number")];
            String modStatus = values[captionPositions.get("Modifications")];
            String leadingProteins = values[captionPositions.get("Leading proteins")];


            //use this information to create the ArrayList<Modification> necessary for the CompClusterChecker
            ArrayList<Modification> mods = new ArrayList<>();
            if (modStatus.equals("Unmodified")) {
                if (sequence.contains("C")) {
                    mods.add(Modification.carbamidomethylation());
                }
                ArrayList<CompClusterIonMatch> currentSpectrumMatches = new ArrayList<>();
                if (labelIn.equals("EC"))
                    currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerEC(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
                if (labelIn.equals("TMT"))
                    currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerTMT(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
                if (labelIn.equals("lsST"))
                    currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerlsST(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
                //TODO: here, the current spectral matches have to be handled
                //the compClusterCSVPrinter-Function has to provide a String for the String Builder in this case, or even a String Builder
                processedSpectra++;
                System.out.println("Processed spectrum number: " + scanNumber);
                System.out.println("Processed spectra: " + processedSpectra);
                //Printout-Procedure
                sb = CSVCreator.compClusterSBCreator(currentSpectrumMatches);
                csvWriter.write(sb.toString());
                csvWriter.flush();
                //clear variables again
                sb.setLength(0);
                currentSpectrumMatches.clear();
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
                currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerEC(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
            if (labelIn.equals("TMT"))
                currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerTMT(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
            if (labelIn.equals("lsST"))
                currentSpectrumMatches = LabelFragmentIonChecker.compClusterCheckerlsST(aminoAcids, sequence, mods, scanNumber, runIn, accuracy, leadingProteins);
            processedSpectra++;
            System.out.println("Processed spectrum number: " + scanNumber);
            System.out.println("Processed spectra: " + processedSpectra);


            //Printout-Procedure
            sb = CSVCreator.compClusterSBCreator(currentSpectrumMatches);
            csvWriter.write(sb.toString());
            csvWriter.flush();
            //clear variables again
            sb.setLength(0);
            currentSpectrumMatches.clear();
        }

        scanner.close();
        csvWriter.close();

        System.out.println("Sorted out spectra (no reporter ion intensities): " + sortedOut);
        System.out.println("Processed spectra: " + processedSpectra);
    }

public static void wholeRunRepFinder(MzXMLFile runIn, File statisticsAnalysis, double ppmDev, String labelIn) throws FileNotFoundException, JMzReaderException {
    DecimalFormat twoDec = new DecimalFormat("0.00");
    DecimalFormat scientific = new DecimalFormat("0.00E0");
        //first, set label name
    String labelName = "";
    boolean labelKnown = false;
    if (labelIn.contains("TMT")) {
        labelName = "TMT";
        labelKnown = true;
    }

    if (labelIn.contains("EC")||labelIn.contains("SOT")) {
        labelName = "EC";
        labelKnown = true;
    }

    if (!labelKnown)
        throw new   IllegalArgumentException("Label not recognized! Please use TMT or EC/SOT. Unrecognized label: "+labelIn);

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
            case "Leading Proteins":
                captionPositions.put("Leading Proteins", index);
                break;
        }
        index++;
    }

    //prepare new File and write header
    String newFilePath = statisticsAnalysis.getAbsolutePath().replace("_statistics.csv", "")+"_reporterIons.csv";
    File outputCSV = new File(newFilePath);
    PrintWriter csvWriter = new PrintWriter(outputCSV);
    StringBuilder sb = new StringBuilder();
    String[] newHeader = new String[10];
    newHeader[0] = "Modified Peptide";
    newHeader[1] = "Precursor Charge";
    newHeader[2] = "Scan Number";
    newHeader[3] = "Leading Proteins";
    newHeader[4] = "Rep0 Relative Intensity [%]";
    newHeader[5] = "Rep0 Absolute Intensity [au]";
    newHeader[6] = "Rep0 Mass Deviation [ppm]";
    newHeader[7] = "Rep1 Relative Intensity [%]";
    newHeader[8] = "Rep1 Absolute Intensity [au]";
    newHeader[9] = "Rep1 Mass Deviation [ppm]";
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
        String[] newValues = new String[10];
        Arrays.fill(newValues, "");
        newValues[0] = values[captionPositions.get("Modified Peptide")];
        newValues[1] = values[captionPositions.get("Precursor Charge")];
        newValues[2] = values[captionPositions.get("Scan Number")];
        newValues[3] = values[captionPositions.get("Leading Proteins")];
        //generate MySpectrum and start search for Reporter Ions
        MySpectrum currentSpectrum = MzXMLReadIn.mzXMLToMySpectrum(runIn, newValues[2]);
        ArrayList<ReporterMatch> repMatches = new ArrayList<>();
        repMatches = PeakCompare.reporterFinder(currentSpectrum, labelName, ppmDev);
        //if no reporter ions are found, set values to 0
        if (repMatches.isEmpty()){
            for (int i = 4; i<newHeader.length; i++){
                newHeader[i] = "0";
            }
        }
        //loop through the matches
        if (repMatches.size() > 2)
            throw new IllegalArgumentException("More than 2 matched reporters! Size: "+repMatches.size());
        for (ReporterMatch rep : repMatches){
            if (rep.getRepName().equals("Rep0")){
                newValues[4] = twoDec.format(rep.getPeak().getRelIntensity());
                newValues[5] = scientific.format(rep.getPeak().getIntensity());
                newValues[6] = twoDec.format(rep.getPPMDev());
            }
            if (rep.getRepName().equals("Rep1")){
                newValues[7] = twoDec.format(rep.getPeak().getRelIntensity());
                newValues[8] = scientific.format(rep.getPeak().getIntensity());
                newValues[9] = twoDec.format(rep.getPPMDev());
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
//this function checks the mod. resultfile from merox and a provided mzxml file
    //it analyzes the corresponding MS2-spectra (CID or HCD) of an identified crosslink
    //it gives the number of matched peaks, type (long or short), structure (alkene, thial, SO), rel intensities, rel. intensities towards highest signature peak,
    //overall proportion of signature peaks compared to spectrum, misaligned M0 precursor info
public static void xlSpectraChecker(File resultFileIn, MzXMLFile runIn, String fragMethodIn, String xlIn) throws FileNotFoundException {
        //check if correct crosslinker and fragmentation type is used
    if (!xlIn.equals("cliXlink"))
        throw new IllegalArgumentException("Unknown cross-linker! Only 'cliXlink' is supported! Input: "+xlIn);
    if(!fragMethodIn.equals("HCD")) {
        if (!fragMethodIn.equals("CID"))
            throw new IllegalArgumentException("Unknown frag method! Input: " + fragMethodIn);
    }

    //initialize scanner for MeroX data
    Scanner scanner = null;
    try {
        scanner = new Scanner(resultFileIn);
    } catch (FileNotFoundException e) {
        System.out.println("File not found! Location: " +resultFileIn.getAbsolutePath());
    }
    //the following information is necessary:
    //Pep1 String, Pep2 String, xl1Pos, xl2Pos, charge state of XL, scan number, isolated m/z, retention time
    //the MeroX results are structured in this way
    //[0] "Peptide 1" [1] "Peptide 2" [2] "Scan number" [3] "Retention time in sec" [4] "best linkage position peptide 1" [5] "best linkage position peptide 2"

    //get header positions
    String header = scanner.nextLine();
    String headerCaptions[] = header.split(",");
    HashMap<String, Integer> captionPositions = new HashMap<>();
    int index = 0;
    for (String captions : headerCaptions){
        switch (captions){
            case "Peptide 1":
                captionPositions.put("Peptide 1", index);
                break;
            case "Peptide 2":
                captionPositions.put("Peptide 2", index);
                break;
            case "Scan Number":
                captionPositions.put("Scan Number", index);
                break;
            case "Retention time in sec":
                captionPositions.put("Retention time in sec", index);
                break;
            case "best linkage position peptide 1":
                captionPositions.put("best linkage position peptide 1", index);
                break;
            case "best linkage position peptide 2":
                captionPositions.put("best linkage position peptide 2", index);
                break;
        }
        index++;
    }
    //prepare new File and write header
    String newFilePath = resultFileIn.getAbsolutePath().replace(".csv", "")+"_XLIonsAnalysis.csv";
    File outputCSV = new File(newFilePath);
    PrintWriter csvWriter = new PrintWriter(outputCSV);
    StringBuilder sb = new StringBuilder();
    String[] newHeader = new String[50];
    newHeader[0] = "Peptide alpha";
    newHeader[1] = "Peptide beta";
    newHeader[2] = "Alpha amino acid";
    newHeader[3] = "Beta amino acid";
    newHeader[4] = "Alpha position";
    newHeader[5] = "Beta position";
    newHeader[6] = "Scan Number";
    newHeader[7] = "Precursor m/z";
    newHeader[8] = "Precursor Charge";
    newHeader[9] = "Precursor mass dev [ppm]";
    newHeader[10] = "Precursor monoisotopic offset";
    newHeader[11] = "Precursor rel. intensity [%]";
    newHeader[12] = "Precursor abs. intensity [au]";
    newHeader[13] = "Signature peaks detected"; //0-6
    newHeader[14] = "Signature peaks summed rel int. [%]";
    newHeader[15] = "Alpha charge states detected";
    newHeader[16] = "Beta charge states detected";
    newHeader[17] = "Alpha dominant charge state";
    newHeader[18] = "Beta dominant charge state";
    newHeader[19] = "Alpha residue detected"; //short, long, both
    newHeader[20] = "Beta residue detected";
    newHeader[21] = "Alpha dominant residue detected";
    newHeader[22] = "Beta dominant residue detected";
    newHeader[23] = "Alpha alkene short detected"; //true||false
    newHeader[24] = "Alpha SO short detected";
    newHeader[25] = "Alpha thial short detected";
    newHeader[26] = "Alpha alkene long detected";
    newHeader[27] = "Alpha SO long detected";
    newHeader[28] = "Alpha thial long detected";
    newHeader[29] = "Beta alkene short detected"; //true||false
    newHeader[30] = "Beta SO short detected";
    newHeader[31] = "Beta thial short detected";
    newHeader[32] = "Beta alkene long detected";
    newHeader[33] = "Beta SO long detected";
    newHeader[34] = "Beta thial long detected";










    String sep = "";
    for (String s : newHeader){
        sb.append(sep);
        sb.append(s);
        sep = ",";
    }
    sb.append("\n");
    csvWriter.write(sb.toString());
    sb.setLength(0);









}



}
