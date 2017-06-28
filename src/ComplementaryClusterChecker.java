import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.util.ArrayList;

/**
 * Created by micha on 6/27/2017.
 */

//give this class a Sequence, a list of modifications and a spectrum and it will check which ions are part of the EC complementary ion cluster
    //also check if fragmentation of EC occurred
    //return an ArrayList of compClusterIonMatches
    //ArrayList of Mods can be empty, only filled if other modification than EC-modification is present
public class ComplementaryClusterChecker {
    public static ArrayList<CompClusterIonMatch> compClusterCheckerEC (ArrayList<AminoAcid> acids,
                                                                       String SequenceIn,
                                                                       ArrayList<Modification> modsIn,
                                                                       String spectrumID, MzXMLFile completeFileIn,
                                                                       double accuracy) throws JMzReaderException {
        ArrayList<CompClusterIonMatch> successfulMatches = new ArrayList<>();
        //generate spectrum to look at:
        MySpectrum spectrumToCheck = MzXMLReadIn.mzXMLToMySpectrum(completeFileIn, spectrumID);

        //generate List of different modified peptides to check for every possibility (EC cleaved/EC not cleaved)
        ArrayList<Peptide> modifiedPeptides = new ArrayList<>();
        //create unmodified peptide
        Peptide idPeptide = new Peptide(SequenceIn, acids);
        //determine how many EC modifications are present
        int lysCount = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(idPeptide.getAminoAcidsList());
        for (AminoAcid acid : sequence){
            if (acid.get1Let() == 'K')
                lysCount++;
        }
        int countEC = lysCount+1;
        //create different mods for the different possibilities (cleaved/noncleaved)



        return successfulMatches;
    }


    //this function should create all the different possibilities for EC modifications
    //cleaved and uncleaved, EC179 and EC180 complementary ion-clusters
    private static ArrayList<ArrayList<Modification>> modCreator(Peptide pepToModify, int countECIn, ArrayList<Modification>modsIn){
        //create new ArrayList of Modification-arraylists
        ArrayList<ArrayList<Modification>> nTermModList = new ArrayList<>();
        //create loop to exit as soon as all the modifications have been done
        int handledMods = 0;
            //create positions of modified lysines
            //lysPos is int[] with all the positions of lysines in the sequence
            int lysCount = countECIn - 1;
            int[] lysPos = new int[lysCount];
            int lysPosPointer = 0;
            ArrayList<AminoAcid> sequence = new ArrayList<>();
            sequence.addAll(pepToModify.getAminoAcidsList());
            for (int a = 0; a < sequence.size(); a++) {
                if (sequence.get(a).get1Let() == 'K') {
                    lysPos[lysPosPointer] = a;
                    lysPosPointer++;
                }
            }



        //modify N-terminus
        //after this, there are 3 Lists of Modifications with all the non-EC mods already included
        //list 0: uncleaved
        //list 1: cleaved EC179
        //list 2: cleaved EC180
        for (int b = 0; b < 3; b++){
                ArrayList<Modification> currentModList = new ArrayList<>();
                currentModList.addAll(modsIn);
                currentModList.add(nTermChooser(b));
                nTermModList.add(currentModList);
        }
        //as long as there are other modifications to handle, do this
        for (int c = 0; c<lysCount; c++){
            //next loop: go through the created lists
            for (int d = 0; d <3;d++){
                //and iterate the different modifications
                for (int e = 0; e <3;e++){
                    ArrayList<Modification> lysMods = new ArrayList<>();
                    lysMods.addAll(nTermModList.get(d));
                    lysMods.add(lysChooser(e, lysPos[c]));
                }
            }

        }






    }

    private static Modification nTermChooser(int nTermChooser){
        Modification mod = null;
        switch(nTermChooser){
            case 0:
                mod = Modification.uncleavedECDuplexNTerm();
                break;
            case 1:
                mod = Modification.cleavedEC179NTerm();
                break;
            case 2:
                mod = Modification.cleavedEC180NTerm();
                break;
        }
        return mod;
    }
    private static Modification lysChooser(int lysChooser, int pos){
        Modification mod = null;
        switch(lysChooser){
            case 0:
                mod = Modification.uncleavedECDuplexLys(pos);
                break;
            case 1:
                mod = Modification.cleavedEC179Lys(pos);
                break;
            case 2:
                mod = Modification.cleavedEC179Lys(pos);
                break;
        }
        return mod;
    }



}
