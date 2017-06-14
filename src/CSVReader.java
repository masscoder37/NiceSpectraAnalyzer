import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by micha on 6/14/2017.
 */
public class CSVReader {
    public static ArrayList<AminoAcid> aminoAcidParse(File file){

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch(FileNotFoundException e) {
            System.out.println("Could not read given file - " + file.getAbsolutePath());
            return null;
        }
        ArrayList<AminoAcid> acids = new ArrayList<>();

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] fields = line.split(",");
            if(fields.length < 4)
                continue;

            try {
                acids.add(new AminoAcid(fields[0], fields[1], fields[2], fields[3]));
            } catch(NumberFormatException e) {
                System.out.println("Invalid format : " + line);
                continue;
            }
        }
        scanner.close();
        return acids;


    }
}
