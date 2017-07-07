import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by micha on 7/7/2017.
 */
public class CSVAnalyzer {
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

        //scanner set up
        //set up Stringbuilder and PrinterWriter
        String newFilePath = filePath.replace(".csv", "\\");
        newFilePath = newFilePath + "statistics.csv";
        newFilePath.replace("\\", "\\\\");

        File outputFile = new File(newFilePath);
        StringBuilder sb = new StringBuilder();
        PrintWriter csvWriter = new PrintWriter(outputFile);
        //write header
        //0Modified Peptide
        //1 Precursor Charge
        //2Scan Number



        while (scanner.hasNextLine()){
            String current = scanner.nextLine();
            String[] values = current.split(",");
            //0Modified Peptide
            //1Precursor Mass [m/z]
            //2Precursor Charge
            //3Label Count
            //4Label Name
            //5Cleaved Label
            //6Mixed Label
            //7Fragment Ion
            //8Fragment Ion Charge
            //9Fragment Ion Mass [m/z]
            //10Peak Charge
            //11Peak Mass [m/z]
            //12Mass Deviation [ppm]
            //13Peak rel. Intensity [%]
            //14Peak abs. Intensity [au]
            //15Scan Number
            //16Fragment Ion Amino Acid Sequence
            //17Fragment Ion Sum Formula









        }













    }
}
