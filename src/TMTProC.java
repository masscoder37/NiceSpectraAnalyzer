//This class handles TMTProC quantification
//in Principle, the old SOT-stuff could be adjusted, but feels very clunky and awkward to use, so create new one instead
//handle TMTProC cluster detection & quantification

import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TMTProC {

    //this class identifies TMTProC complementary clusters
    //requires: Allosaurus mzXML run File, Peptide list with scan number
    public static void tmtproCCLusterID (String runFileLocIn, String idListIn, int allowedPPMDeviance, boolean TMTPro0, String alkylationIn) throws MzXMLParsingException, FileNotFoundException {
        ArrayList<String> allowedCysProt = new ArrayList<>();
        allowedCysProt.add("NEM");
        allowedCysProt.add("IAA");
        if(!allowedCysProt.contains(alkylationIn))
            throw new IllegalArgumentException("Alkylation method not recognized. Please use NEM or IAA. Input: "+alkylationIn);
        //prepare access to .mzxml file
        File runFile = new File(runFileLocIn);
        MzXMLFile run = new MzXMLFile(runFile);

        //read ID list
        File idFile = new File(idListIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(idFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! Location: " +idFile.getAbsolutePath());
        }
        //variable to see how many PSMs are in a file
        int numberOfPSMs = 0;
        assert scanner != null;
        while (scanner.hasNext()){
            scanner.next();
            numberOfPSMs++;
        }
        //reset scanner by reconstruction
        scanner = new Scanner(idFile);
        //skip first line, where the header is
        //relevant positions: [0] is ScanF, [1] is precursor z, [4] is peptide, [3] is reference, but detect dynamically
        String header = scanner.nextLine();
        String[] headerCaptions = header.split(",");
        HashMap<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String captions : headerCaptions){
            switch (captions){
                case "ScanF":
                    captionPositions.put("ScanF", index);
                    break;
                case "z":
                    captionPositions.put("z", index);
                    break;
                case "Peptide":
                    captionPositions.put("Peptide", index);
                    break;
                case "Reference":
                    captionPositions.put("Reference", index);
                    break;
            }
            index++;
        }
        if (captionPositions.size() != 4)
            throw new IllegalArgumentException("Something went wrong with reading in the caption positions of the allosaurus results file!");


        //prepare output file
        //output file should act as container: store all complementary ions in there
        //******************************************************//
        //*********************format of file*******************//
        //regex to split individual entries: §
        //regex to split fields: <........>
        //regex to split individual properties: #
        //regex to split ion properties: ~
        //§<PSMINFO >[-10]ScanF[0]#PrecursorMass[1]#PrecursorZ[2]#PrecursorIsoOffset[3]#Peptide[4]#Reference[5]#AllowedDeviation[6]#TMTPro0(true/false; if false, 8 channels TMTPro)
        //<COMPIONS>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~tmtAdded#next ion
        //<COMPFRAG>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~fragion(b4, y3,...)[4]~tmtAdded[5]~mixed(cleavage of multiple TMTs, false or true)#next ion
        //******************************************************//
        String newFilePath = idFile.getAbsolutePath().replace(".csv", "")+"_CompIons.txt";
        File outputCSV = new File(newFilePath);
        PrintWriter resultWriter = new PrintWriter(outputCSV);

        //initialize progress bar
        ProgressBar.progressBar("Finding complementary (fragment) ions.", numberOfPSMs);

        //loop through Scanner
        while(scanner.hasNext()){
            String[] currentValues = scanner.next().split(",");
            //generate Peptide with all modifications
            //this sequence has . in between, remove them
            //remove trailing dots by using method in RandomTools
            String peptideSequence = RandomTools.sequenceOnly(currentValues[captionPositions.get("Peptide")]);
            //prepare modification of precursor peptide
            ArrayList<Modification> modList = new ArrayList<>();
            //check if sequence has Cysteines which need to be protected
            if(peptideSequence.contains("C")){
                if(alkylationIn.equals("IAA"))
                    modList.add(Modification.carbamidomethylation());
                else
                    modList.add(Modification.nemModification());
            }
            //check for presence of variable ox.
            if(peptideSequence.contains("*")){
                modList.addAll(RandomTools.oxidizedMethionineModification(peptideSequence));
                peptideSequence = RandomTools.removeOxidationSigns(peptideSequence);
            }
            //modlist now contains alkylation and oxidation, last thing to add is TMTPro-mod




            //at the end, advance progress bar
            ProgressBar.progress();
        }






    }

}
