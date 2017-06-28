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
    public static ArrayList<ArrayList<Modification>> modCreator(Peptide pepToModify, int countECIn, ArrayList<Modification>modsIn){
            //create positions of modified lysines
            //lysPos is int[] with all the positions of EC-Modifications(N-Term + Lys) in the sequence
            int lysCount = countECIn - 1;
            int[] lysPos = new int[lysCount];
            int lysPosPointer = 0;
            ArrayList<AminoAcid> sequence = new ArrayList<>();
            sequence.addAll(pepToModify.getAminoAcidsList());
            for (int a = 0; a < sequence.size(); a++) {
                if (sequence.get(a).get1Let() == 'K') {
                    lysPos[lysPosPointer] = a+1;
                    lysPosPointer++;
                }
            }
        ArrayList<ArrayList<Modification>> completeList= new ArrayList<>();
        for (int e = 0; e < 3; e++){
            ArrayList<Modification> current = new ArrayList<>();
            current.addAll(modsIn);
            current.add(modChooser(e, 1));
            completeList.add(current);
        }
        for (int f = 0; f < lysCount; f++) {
            completeList = listMultiplier(completeList, lysPos[f]);
        }

        System.out.println("Size: "+completeList.size());

return completeList;
    }


    private static ArrayList<ArrayList<Modification>> listMultiplier(ArrayList<ArrayList<Modification>> listIn, int pos){
        int sizeOfList = listIn.size();
        ArrayList<ArrayList<Modification>> listOut = new ArrayList<>();
        for (ArrayList<Modification> modLists : listIn){
            for (int i = 0; i<3;i++){
                ArrayList<Modification> currentList = new ArrayList<>();
                currentList.addAll(modLists);
                currentList.add(modChooser(i, pos));
                listOut.add(currentList);
            }
        }
        return listOut;
    }



    private static Modification modChooser(int modChooser, int pos){
        Modification mod = null;
        switch(modChooser){
            case 0:
                mod = Modification.uncleavedECDuplex(pos);
                break;
            case 1:
                mod = Modification.cleavedEC179(pos);
                break;
            case 2:
                mod = Modification.cleavedEC180(pos);
                break;
        }
        return mod;
    }



}
