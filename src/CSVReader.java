import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static Spectrum spectrumParse(File file) {
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
        Spectrum spectrumOut = new Spectrum(peaks, spectrumScanNumber, scanHeader);
        return spectrumOut;
    }
}
