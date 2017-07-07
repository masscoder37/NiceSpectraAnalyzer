import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by micha on 7/7/2017.
 */
public class CSVAnalyzer {
    //analyzes created comp-Cluster Matches list
    public static void cicStatistics(String filePath) throws FileNotFoundException {
        try{
            File cicAnalysis = new File(filePath);
        }
        catch (FileNotFoundException e){
            throw new FileNotFoundException("Couldn't open specified file: " +filePath);
        }




    }
}
