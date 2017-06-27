import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.util.ArrayList;

/**
 * Created by micha on 6/27/2017.
 */

//give this class a Sequence, a list of modifications and a spectrum and it will check which ions are part of the EC complementary ion cluster
    //also check if fragmentation of EC occurred
    //return an ArrayList of compClusterIonMatches
public class ComplementaryClusterChecker {
    public static ArrayList<CompClusterIonMatch> compClusterCheckerEC (String SequenceIn, ArrayList<Modification> modsIn, String spectrumID, MzXMLFile completeFileIn, double accuracy){
        ArrayList<CompClusterIonMatch> successfulMatches = new ArrayList<>();









        return successfulMatches;
    }
}
