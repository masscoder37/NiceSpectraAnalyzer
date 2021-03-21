import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//tools for targeted proteomics experiments
public class TargetedTools {
    //create .csv file with m/z, z and the relative mass offset for targeted TMTpro0 / TMTpro experiments
    public static void inclusionListCreator (String filePathIn){
        //create input file
        File outputFile1 = new File(filePathIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(outputFile1);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + filePathIn);
        }
        //prepare output .csv File
        String outputFilePath = filePathIn.replace(".csv", "_preparedList.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }
        StringBuilder sb = new StringBuilder();
        //prepare header
        sb.append("Peptide,m/z trigger,z,m/z target,massOffset").append("\n");
        pw.write(sb.toString());
        sb.setLength(0);
        //where are the required fields located
        String captions = scanner.nextLine();
        String[] splitCaptions = captions.split(",");
        Map<String, Integer> captionPositions = new HashMap<>();
        for (int i = 0; i < splitCaptions.length; i++){
            String caption = splitCaptions[i].toLowerCase();
            switch(caption){
                case("obs m/z"):
                    captionPositions.put("m/z", i);
                    break;
                case("z"):
                    captionPositions.put("z", i);
                    break;
                case("peptide"):
                    captionPositions.put("Peptide",i);
                    break;
            }
        }
        if (captionPositions.size() != 3)
            throw new IllegalArgumentException("Expected headers in input file missing! Please check input file!");
        //with the required fields known, loop through rest of list
        while(scanner.hasNext()){
            String valuesLine = scanner.nextLine();
            String[] values = valuesLine.split(",");
            //read in the required fields
            String sequence = values[captionPositions.get("Peptide")];
            double triggerMZ = Double.parseDouble(values[captionPositions.get("m/z")]);
            int z = Integer.parseInt(values[captionPositions.get("z")]);

            //with required things read in, calculate new mass and the offset
            //remove leading and trailing AAs, but leave M oxidation in
            sequence = RandomTools.sequenceOnly(sequence);
            int numberOfTags = RandomTools.getNumberOfTMTTags(sequence);
            double targetMZ = 0;
            double mzOffset = 0;
            //calculate new target mass and offset
            if(z == 2){
                targetMZ = triggerMZ + 4.509 * numberOfTags;
                mzOffset = targetMZ - triggerMZ;
            }
            if(z == 3){
                targetMZ = triggerMZ + 3.0058 * numberOfTags;
                mzOffset = targetMZ - triggerMZ;
            }
            if(z == 4){
                targetMZ = triggerMZ + 2.25 * numberOfTags;
                mzOffset = targetMZ - triggerMZ;
            }
            //header: "Peptide,m/z trigger,z,m/z target,massOffset"
            sb.append(sequence).append(",");
            sb.append(triggerMZ).append(",");
            sb.append(z).append(",");
            sb.append(targetMZ).append(",");
            sb.append(mzOffset).append("\n");
            pw.write(sb.toString());
            sb.setLength(0);
        }
        pw.flush();
        pw.close();
    }





}
