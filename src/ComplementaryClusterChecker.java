import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.util.ArrayList;
import java.util.Collections;

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
        ArrayList<IonMatch> successfulMatches = new ArrayList<>();
        //generate spectrum to look at:
        MySpectrum spectrumToCheck = MzXMLReadIn.mzXMLToMySpectrum(completeFileIn, spectrumID);
        String spectrumHeader = spectrumToCheck.getScanHeader();


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
        int countTag = lysCount+1;
        //create different mods for the different possibilities (cleaved/noncleaved)
        ArrayList<ArrayList<Modification>> allMods = new ArrayList<>();
        allMods = modCreatorEC(idPeptide, countTag, modsIn);

        //generate List of different modified peptides to check for every possibility (EC cleaved/EC not cleaved)
        ArrayList<Peptide> modifiedPeptides = new ArrayList<>();
        for (ArrayList<Modification> modList : allMods){
            Peptide modifiedPeptide = new Peptide(SequenceIn, acids);
            modifiedPeptides.add(modifiedPeptide.peptideModifier(modList));
        }
        //invoke PeakCompare function and store result matches in List of IonMatches
        for (Peptide modPeptide : modifiedPeptides){
            //System.out.println("Peptide: "+modPeptide.getSequence());
            successfulMatches.addAll(PeakCompare.peakCompare(spectrumToCheck, modPeptide, accuracy));
            //System.out.println();
        }



        //only use matches were there is a label present

        ArrayList<CompClusterIonMatch> relevantMatches = new ArrayList<>();
        relevantMatches = relevantMatchesPicker(successfulMatches, spectrumHeader);

        //relevantMatches still contains multiple instances of the same modification, since the fragment ions from different modified peptides can be the same

        ArrayList<CompClusterIonMatch> noDuplicateMatches = new ArrayList<>();
        noDuplicateMatches = matchesConsolidator(relevantMatches);

        return noDuplicateMatches;
    }


    //this function should create all the different possibilities for EC modifications
    //cleaved and uncleaved, EC179 and EC180 complementary ion-clusters
    public static ArrayList<ArrayList<Modification>> modCreatorEC(Peptide pepToModify, int countECIn, ArrayList<Modification> modsIn) {
        //create positions of modified lysines
        //lysPos is int[] with all the positions of EC-Modifications(N-Term + Lys) in the sequence
        int lysCount = countECIn - 1;
        int[] lysPos = new int[lysCount];
        int lysPosPointer = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(pepToModify.getAminoAcidsList());
        for (int a = 0; a < sequence.size(); a++) {
            if (sequence.get(a).get1Let() == 'K') {
                lysPos[lysPosPointer] = a + 1;
                lysPosPointer++;
            }
        }
        //create new list of modification lists
        ArrayList<ArrayList<Modification>> completeList = new ArrayList<>();
        //create the first 3 lists: 3 different possible modifications, all on NTerm
        for (int e = 0; e < 3; e++) {
            ArrayList<Modification> current = new ArrayList<>();
            current.addAll(modsIn);
            current.add(modChooserEC(e, 1));
            completeList.add(current);
        }
        //now loop through all the lysines and overwrite the existing list
        //do so by using the listMultiplierEC method
        //by this, create all possibilities...also those which aren't occuring
        for (int f = 0; f < lysCount; f++) {
            completeList = listMultiplierEC(completeList, lysPos[f]);
        }

        //completeList is now longer than it needs to be
        //one can remove all the cases where different cleaved modifications occur. e.g. cleaved-EC179, cleaved-EC180 is unnecessary
        //create new reduced list
        ArrayList<ArrayList<Modification>> reducedList = new ArrayList<>();

        //loop through all the modlists in the completeList(of lists)
        for (ArrayList<Modification> modList : completeList) {
            //check now what their modification names are
            boolean differentCleavedTags = false;
            String firstCleavedMod = "";
            //this loop searches for the first occurrence of a cleaved modification:EC179_cleaved or EC180_cleaved
            for (Modification mod : modList) {
                if (mod.getModificationName().contains("179") || mod.getModificationName().contains("180")) {
                    firstCleavedMod = mod.getModificationName();
                    break;
                }
            }
            //this loop now checks again: are there cleaved mods(containing 179 or 180)
            for (Modification mod : modList) {
                //is it a cleaved mod?
                if (mod.getModificationName().contains("179") || mod.getModificationName().contains("180")) {
                    //has it another name?
                    if (mod.getModificationName() != firstCleavedMod) {
                        //there are different cleaved mods! list should be sorted out
                        differentCleavedTags = true;
                        break;
                    }
                }
            }
            //only add the usefulf lists
            if (!differentCleavedTags) {
                reducedList.add(modList);
            }
        }

        return reducedList;
    }


    private static ArrayList<ArrayList<Modification>> listMultiplierEC(ArrayList<ArrayList<Modification>> listIn, int pos){
        ArrayList<ArrayList<Modification>> listOut = new ArrayList<>();
        for (ArrayList<Modification> modLists : listIn){
            for (int i = 0; i<3;i++){
                ArrayList<Modification> currentList = new ArrayList<>();
                currentList.addAll(modLists);
                currentList.add(modChooserEC(i, pos));
                listOut.add(currentList);
            }
        }
        return listOut;
    }



    private static Modification modChooserEC(int modChooser, int pos){
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

    private static ArrayList<CompClusterIonMatch> relevantMatchesPicker(ArrayList<IonMatch> completeListIn, String scanHeaderIn){
        ArrayList<CompClusterIonMatch> relevantList = new ArrayList<>();
        //loop through every match in the complete IonMatch list
        for (IonMatch match : completeListIn){
            //check if the fragment Ion contains the label
            if (match.getFragmentIon().getLabelStatus())
            {
                //in case the label is detected, determine if it is uncleaved, cleaved or mixed and get the modification name
                int labelQuantity = match.getFragmentIon().getLabelQuantity();
                ArrayList<AminoAcid> labeledAAs = new ArrayList<>();
                labeledAAs = match.getFragmentIon().getLabelAAs();
                if (match.getFragmentIon().getIonSeries() == 'y')
                Collections.reverse(labeledAAs);
                String labelName = "";
                boolean isCleaved = false;
                boolean isCleavedAtAll = false;
                boolean isMixed=false;
                for (int a = 0; a < labelQuantity; a++){
                    //determine label name
                    //if there are multiple Labels present, then insert ; in between names
                    if (!labelName.isEmpty())
                        labelName += ";";
                    labelName +=  labeledAAs.get(a).getModification().getModificationName();
                    //determine if cleavage status is all uncleaved (isCleaved = false, isMixed = false), all cleaved (isCleaved= true, isMixed = false)
                    //in case of mixing: isCleaved=true, isMixed=true
                    //if only one variable is present, then exit here
                    if (a==0) {
                        isCleaved = labeledAAs.get(a).getModification().getCleavedStatus(); //cleaved: true; intact=false
                        if (isCleaved)
                            isCleavedAtAll = true;
                        continue;
                    }
                    if (isCleaved!=labeledAAs.get(a).getModification().getCleavedStatus())
                        isMixed = true;
                    isCleaved = labeledAAs.get(a).getModification().getCleavedStatus();
                    if (isCleaved)
                        isCleavedAtAll = true;

                }
                CompClusterIonMatch importantMatch = new CompClusterIonMatch(match.getFragmentIon(), match.getPeak(),
                                                                                match.getPpmDeviation(),
                                                                                labelName,isCleavedAtAll, isMixed, scanHeaderIn);
                relevantList.add(importantMatch);
            }
        }

        return relevantList;
    }


    //this function removes the multiple entries (same fragment ions from different theoretical precursors) from the relevantMatchesList
    private static ArrayList<CompClusterIonMatch> matchesConsolidator(ArrayList<CompClusterIonMatch> listIn){
        ArrayList<CompClusterIonMatch> reducedList = new ArrayList<>();
        //int duplicateNumber = 0;

        for (CompClusterIonMatch oldMatch : listIn){
            boolean duplicate = false;
            if (!reducedList.isEmpty()) {

                for (CompClusterIonMatch newMatch: reducedList){
                    //check if match is already in the list
                    //things to check: ion series + number, modifications
                    if (oldMatch.getFragmentIon().getCompleteIon().equals(newMatch.getFragmentIon().getCompleteIon())
                            &&oldMatch.getMixedLabels() == newMatch.getMixedLabels()
                            &&oldMatch.getIsCleaved() == newMatch.getIsCleaved()
                            &&oldMatch.getLabelName().equals(newMatch.getLabelName())) {
                        duplicate = true;
                       // duplicateNumber++;
                    }
                    if (oldMatch.getFragmentIon().getLabelQuantity()>1){
                        if (oldMatch.getFragmentIon().getExactMass() == newMatch.getFragmentIon().getExactMass()){
                            duplicate = true;
                           // duplicateNumber++;
                        }
                    }
                }
            }
            if (!duplicate) {
                reducedList.add(oldMatch);
            }
        }
       // System.out.println(""+ duplicateNumber+" Matches removed (duplicates)!");
        return reducedList;
    }



    //handle TMTDuplex ComplementaryIons

    public static ArrayList<CompClusterIonMatch> compClusterCheckerTMT (ArrayList<AminoAcid> acids,
                                                                       String SequenceIn,
                                                                       ArrayList<Modification> modsIn,
                                                                       String spectrumID, MzXMLFile completeFileIn,
                                                                       double accuracy) throws JMzReaderException {
        ArrayList<IonMatch> successfulMatches = new ArrayList<>();
        //generate spectrum to look at:
        MySpectrum spectrumToCheck = MzXMLReadIn.mzXMLToMySpectrum(completeFileIn, spectrumID);
        String spectrumHeader = spectrumToCheck.getScanHeader();


        //create unmodified peptide
        Peptide idPeptide = new Peptide(SequenceIn, acids);
        //determine how many TMT modifications are present
        int lysCount = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(idPeptide.getAminoAcidsList());
        for (AminoAcid acid : sequence){
            if (acid.get1Let() == 'K')
                lysCount++;
        }
        int countTag = lysCount+1;
        //create different mods for the different possibilities (cleaved/noncleaved)
        ArrayList<ArrayList<Modification>> allMods = new ArrayList<>();
        allMods = modCreatorTMT(idPeptide, countTag, modsIn);

        //generate List of different modified peptides to check for every possibility (TMT cleaved/TMT not cleaved)
        ArrayList<Peptide> modifiedPeptides = new ArrayList<>();
        for (ArrayList<Modification> modList : allMods){
            Peptide modifiedPeptide = new Peptide(SequenceIn, acids);
            modifiedPeptides.add(modifiedPeptide.peptideModifier(modList));
        }
        //invoke PeakCompare function and store result matches in List of IonMatches
        for (Peptide modPeptide : modifiedPeptides){
            //System.out.println("Peptide: "+modPeptide.getSequence());
            successfulMatches.addAll(PeakCompare.peakCompare(spectrumToCheck, modPeptide, accuracy));
            //System.out.println();
        }

        //relevant Matches Picker doesn't change in regard to EC
        ArrayList<CompClusterIonMatch> relevantMatches = new ArrayList<>();
        relevantMatches = relevantMatchesPicker(successfulMatches, spectrumHeader);

        //relevantMatches still contains multiple instances of the same modification, since the fragment ions from different modified peptides can be the same
        //same matches Consolidator Method can be utilized
        ArrayList<CompClusterIonMatch> noDuplicateMatches = new ArrayList<>();
        noDuplicateMatches = matchesConsolidator(relevantMatches);

        return noDuplicateMatches;
    }

    //this function should create all the different possibilities for TMTduplex modifications
    //uncleaved and cleaved, only 2 possibilities
    public static ArrayList<ArrayList<Modification>> modCreatorTMT(Peptide pepToModify, int countTMTIn, ArrayList<Modification> modsIn) {
        //create positions of modified lysines
        //lysPos is int[] with all the positions of TMT-Modifications(N-Term + Lys) in the sequence
        int lysCount = countTMTIn - 1;
        int[] lysPos = new int[lysCount];
        int lysPosPointer = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(pepToModify.getAminoAcidsList());
        for (int a = 0; a < sequence.size(); a++) {
            if (sequence.get(a).get1Let() == 'K') {
                lysPos[lysPosPointer] = a + 1;
                lysPosPointer++;
            }
        }
        //create new list of modification lists
        ArrayList<ArrayList<Modification>> completeList = new ArrayList<>();
        //create the first 2 lists: 2 different possible modifications, all on NTerm
        for (int e = 0; e < 2; e++) {
            ArrayList<Modification> current = new ArrayList<>();
            current.addAll(modsIn);
            current.add(modChooserTMT(e, 1));
            completeList.add(current);
        }
        //now loop through all the lysines and overwrite the existing list
        //do so by using the listMultiplierEC method
        for (int f = 0; f < lysCount; f++) {
            completeList = listMultiplierTMT(completeList, lysPos[f]);
        }

        return completeList;
    }

    private static Modification modChooserTMT(int modChooser, int pos){
        Modification mod = null;
        switch(modChooser){
            case 0:
                mod = Modification.uncleavedTMTDuplex(pos);
                break;
            case 1:
                mod = Modification.cleavedTMTduplex(pos);
                break;
        }
        return mod;
    }

    private static ArrayList<ArrayList<Modification>> listMultiplierTMT(ArrayList<ArrayList<Modification>> listIn, int pos){
        ArrayList<ArrayList<Modification>> listOut = new ArrayList<>();
        for (ArrayList<Modification> modLists : listIn){
            for (int i = 0; i<2;i++){
                ArrayList<Modification> currentList = new ArrayList<>();
                currentList.addAll(modLists);
                currentList.add(modChooserTMT(i, pos));
                listOut.add(currentList);
            }
        }
        return listOut;
    }




}
