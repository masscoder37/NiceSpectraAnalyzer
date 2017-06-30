import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 6/30/2017.
 */
public class CSVCreator {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.00E0");

    public static void compClusterMatchCSVPrinter(ArrayList<CompClusterIonMatch> matchesIn, String FilePathIn) throws FileNotFoundException{
        File csvOut = new File(FilePathIn);
        PrintWriter csvWriter =  new PrintWriter(csvOut);
        StringBuilder sb = new StringBuilder();

        //define captions
        String[] header = new String[18];
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
            String[] values = new String[18];
            values[0] = match.getFragmentIon().getPrecursor().getSequence(); //"Modified Peptide";


            String[] splitScanHeader = match.getScanHeader().split(";");

            values[1] = splitScanHeader[0]; //"Precursor Mass [m/z]";
            values[2] = splitScanHeader[1]; //"Precursor Charge";

            values[3] = Integer.toString(match.getFragmentIon().getLabelQuantity()); //"Label Count";
            values[4] = match.getLabelName(); //"Label Name";
            values[5] = ""+match.onlyCleaved();//"Cleaved Labels";
            values[6] = ""+match.mixedLabels(); //"Mixed Labels";
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
}
