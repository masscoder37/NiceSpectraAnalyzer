//this class will eventually replace the legacy SOT-analysis classes
//objects of this class will be utilized for TMTproC comp ion analysis

import java.util.ArrayList;

//encompass all THEORETICAL ions, without Matches associated yet
public class TMTproCCompIon {
    private boolean isFullLength; //true: full-peptide comp Ion, false: fragment ion
    private boolean isMixed; //true: contains both cleaved and intact TMTPro tags
    private int numberOfTags;
    private boolean tmtPro0; //false: TMTproC 8plex
    private Ion ion = null; // for full length peptide
    private FragmentIon fragIon = null; // for fragment ions
    private String peptideOrigin;
    //two constructors - one for full length ion, one for fragment ion

    //ion constructor
    public TMTproCCompIon(boolean isFullLengthIn, boolean isMixedIn, boolean tmtPro0In, Ion ionIn, String peptideOriginIn, int numberOfTagsIn) {
        this.isFullLength = isFullLengthIn;
        this.isMixed = isMixedIn;
        this.tmtPro0 = tmtPro0In;
        this.ion = ionIn;
        this.peptideOrigin = peptideOriginIn;
        this.numberOfTags = numberOfTagsIn;
    }

    public TMTproCCompIon(boolean isFullLengthIn, boolean isMixedIn, boolean tmtPro0In, FragmentIon ionIn, String peptideOriginIn, int numberOfTagsIn) {
        this.isFullLength = isFullLengthIn;
        this.isMixed = isMixedIn;
        this.tmtPro0 = tmtPro0In;
        this.fragIon = ionIn;
        this.peptideOrigin = peptideOriginIn;
        this.numberOfTags = numberOfTagsIn;
    }

    public boolean isFullLength() {
        return isFullLength;
    }

    public boolean isMixed() {
        return isMixed;
    }

    public boolean isTmtPro0() {
        return tmtPro0;
    }

    public String getPeptideOrigin() {
        return peptideOrigin;
    }

    //special getter for ion
    //fragmentIon extends Ion, so it can be returned with this method
    public Ion getIon() {
        if (this.ion == null)
            return this.fragIon;
        else
            return this.ion;
    }

    //this function takes a peptide and provides all non redundant complementary ions, i.e. ions with at least 1 cleaved TMTpro tag
    //modify this code from the SOT legacy code
    //TODO: create switch between TMTpro0 and TMTproC-8plex
    public static ArrayList<TMTproCCompIon> compIonCreator(Peptide unmodPeptideIn, ArrayList<Modification> modListIn, boolean tmtpro0, int precursorZIn) {
        ArrayList<TMTproCCompIon> out = new ArrayList<>();
        //find all positions which can be modified
        ArrayList<Integer> modPos = new ArrayList<>();
        //add pos 1 (peptide counting starts from 1) to handle N-Terminus
        modPos.add(1);
        //Search from unmodified peptide sequence for lysines
        //TODO: handling of K at pos 1 is not done, not sure how Modification deals with that...
        for (int i = 0; i < unmodPeptideIn.getSequence().length(); i++) {
            if (unmodPeptideIn.getSequence().charAt(i) == 'K')
                //add the position +1, because peptide counting starts at 1 and not 0
                modPos.add(i + 1);
        }
        //create new startingModList with the present modifications
        ArrayList<Modification> startPoint = new ArrayList<>(modListIn);
        //also, declare an list of Modification lists and add the startPoint
        ArrayList<ArrayList<Modification>> allPossibleModifications = new ArrayList<>();
        allPossibleModifications.add(startPoint);
        //loop through all the positions
        for (int pos : modPos){
            //for every position, list has to be duplicated: intact or cleaved modification
            //also, do this for every list in the list of modifications
            ArrayList<ArrayList<Modification>> multipliedList = new ArrayList<>();
            for (ArrayList<Modification> singleModList : allPossibleModifications){
                //loop through 2 possibilities - this multiplies the list
                for (int m = 0; m < 2; m++){
                    //copy List
                    ArrayList<Modification> current = new ArrayList<>(singleModList);
                    current.add(modChooserTMTPro(m, pos));
                    multipliedList.add(current);
                }
            }
            //at this point, the lists have to be set as the new starting point
            allPossibleModifications = multipliedList;
        }
        //create all possible peptides from the modification lists
        ArrayList<Peptide> possiblePeptides = new ArrayList<>();
        for (ArrayList<Modification> mods : allPossibleModifications){
            possiblePeptides.add(unmodPeptideIn.peptideModifier(mods));
        }
        //for each peptide, generate complementary ion and complementary fragment ions
        for (Peptide pep : possiblePeptides){
            //check if pep has cleaved label at all
            //also determine if it's mixed
            ArrayList<String> labelStatus = new ArrayList<>();
            ArrayList<AminoAcid> aaList = pep.getAminoAcidsList();
            boolean containsCleavedTag = false;
            for (AminoAcid aa : aaList){
                //only of aa has mod
                if (aa.getModificationStatus()){
                    //and Modification is a label
                    if(aa.getModification().getLabelStatus()){
                        if(aa.getModification().getCleavedStatus()){
                            containsCleavedTag = true;
                            labelStatus.add("cleaved");
                        }
                        else
                            labelStatus.add("intact");
                    }
                }
            }
            if(!containsCleavedTag)
                //skip that peptide
                continue;
            //determine mixed status
            boolean isMixed = false;
            if(labelStatus.contains("intact")&&labelStatus.contains("cleaved"))
                isMixed = true;
            //only useful peptides remain
            //handle intact comp. ion
            SumFormula compIonFormula = new SumFormula(pep.getSumFormula().getSumFormula());
            //add protons according to precursor
            for(int i = 0; i<precursorZIn; i++){
                compIonFormula = SumFormula.sumFormulaJoiner(compIonFormula, SumFormula.getProtonFormula());
            }
            //now, Ion can be created. charge state inferred by formula
            Ion fullLengthCompIon = new Ion(compIonFormula);
            out.add(new TMTproCCompIon(true, isMixed, tmtpro0, fullLengthCompIon, pep.getUnmodifiedSequence(), modPos.size()));




        }






        return out;
    }


    private static Modification modChooserTMTPro(int modChooser, int pos) {
        Modification mod = null;
        switch (modChooser) {
            //intact
            case 0:
                mod = Modification.tmtPro0(pos);
                break;
            //cleaved
            case 1:
                mod = Modification.tmtPro0Comp(pos);
                break;
        }
        return mod;
    }

}
