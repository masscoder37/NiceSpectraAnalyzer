import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by michael on 6/14/2017.
 */
public class Peptide {
    private String sequence;
    private int sequenceLength;
    private double exactMass;
    private SumFormula sumFormula;
    private ArrayList<FragmentIon> bIons;
    private ArrayList<FragmentIon> yIons;
    private boolean hasModification;
    private ArrayList<AminoAcid> aminoAcidsList;
    private String unmodifiedSequence;

    private DecimalFormat fiveDec = new DecimalFormat("0.00000");

    //ArrayList<AminoAcid> acids usually contains all 20 natural amino acids
    //ArrayList<AminoAcid> aminoAcidsList contains only aminoacids in the peptide, in order of the sequence
    public Peptide(String sequenceIn, ArrayList<AminoAcid> acids) {
        this.sequence = sequenceIn.toUpperCase();
        this.unmodifiedSequence = sequenceIn.toUpperCase();
        this.sequenceLength = sequenceIn.length();
        this.aminoAcidsList = new ArrayList<>();
        boolean aaNotFound = true;
        //first for loop: go through the whole Sequence
        for (int a = 0; a < this.sequenceLength; a++) {
            char current = this.sequence.charAt(a);
            aaNotFound = true;
            //second for loop: go through all the amino acids until you find the right one
            //if the right one is found, aaNotFound = false and the loop exits prematurely
            for (int b = 0; b < acids.size() && aaNotFound; b++) {
                aaNotFound = true;
                if (current == acids.get(b).get1Let()) {
                    //create elongated SumFormula
                    AminoAcid currentAcid = acids.get(b);
                    this.aminoAcidsList.add(currentAcid);
                    aaNotFound = false;
                }
                //handling of unknown amino acids
                if (b == acids.size() - 1 && aaNotFound) {
                    throw new IllegalArgumentException("Amino Acid unknown: " + current);
                }
            }
        }
        //now, amino acids are elements in a list, creating the peptide
        //SumFormula of Peptide is created and exact mass is calculated
        //to do so, Water is added to waterloss_sumformulas
        //at the same time, AminoAcids are checked for modification
        SumFormula addFormula = SumFormula.getWaterFormula();
        this.hasModification = false;
        for (AminoAcid aa : this.aminoAcidsList) {
            addFormula = SumFormula.sumFormulaJoiner(addFormula, aa.getWaterLossFormula());
        }
        this.sumFormula = addFormula;
        this.exactMass = this.sumFormula.getExactMass();
        //create Fragment ions
        this.bIons = new ArrayList<>();
        this.yIons = new ArrayList<>();
        bIonBuilder(1);
        yIonBuilder(1);

    }
    //implement new constructor, specifically for modified peptides
    //in this case, only ArrayList<AminoAcid> contains only AAs which already are known to be part of the sequence, since all peptides go through the normal constructor first
    private Peptide (String sequenceIn, ArrayList<AminoAcid> aminoAcidsListIn, boolean modStatus) {
        this.hasModification = modStatus;
        this.sequenceLength = sequenceIn.length();
        this.unmodifiedSequence = sequenceIn;
        //construct new Sequence which annotates modified AminoAcid with *
        String[] modPos = new String[sequenceIn.length()];
        for (int a = 0; a < this.sequenceLength; a++) {
            modPos[a] = "" + aminoAcidsListIn.get(a).get1Let();
            //write Modification name in brackets
            //write EC in brackets if you have isobaric Label modification
            if (aminoAcidsListIn.get(a).getModificationStatus()) {

                if (aminoAcidsListIn.get(a).getModification().getLabelStatus()) {
                    if (aminoAcidsListIn.get(a).getModification().getModificationName().contains("EC"))
                        modPos[a] += "(EC)";
                    if (aminoAcidsListIn.get(a).getModification().getModificationName().contains("TMT"))
                        modPos[a] += "(TMT)";
                } else {
                    modPos[a] += "(" + aminoAcidsListIn.get(a).getModification().getModificationName() + ")";

                }
            }
            this.sequence = "";
            for (String aa : modPos) {
                this.sequence += aa;
            }
            //aminoAcids List is just the list supplied by modification method
            this.aminoAcidsList = aminoAcidsListIn;
            //calculate SumFormula and exact masses
            //for peptide, sum up waterloss masses and waterFormula
            SumFormula addFormula = SumFormula.getWaterFormula();
            for (AminoAcid aa : this.aminoAcidsList) {
                addFormula = SumFormula.sumFormulaJoiner(addFormula, aa.getWaterLossFormula());
            }
            this.sumFormula = addFormula;
            this.exactMass = this.sumFormula.getExactMass();
            //create b- and y-ions
            this.bIons = new ArrayList<>();
            this.yIons = new ArrayList<>();
            bIonBuilder(1);
            yIonBuilder(1);
            bIonBuilder(2);
            yIonBuilder(2);

        }
    }



    private ArrayList<FragmentIon> bIonBuilder(int chargeState) {
        if (chargeState == 0)
            throw new IllegalArgumentException("Charge state of Fragment Ions can't be 0!");

        //one Proton is added to not create empty sum formula
        SumFormula runningFormula = new SumFormula("H+");
        //this loop determines charge state; starts at 1 because one proton has already been added
        for (int a = 1; a < chargeState; a++) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, SumFormula.getProtonFormula());
        }
        //implement knowledge about modification status
        boolean isModified = false;

        //this loop goes through all the amino acids
        ArrayList<AminoAcid> aminoAcidsComposition = new ArrayList<>();
        for (int i = 0; i < this.sequenceLength; i++) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            aminoAcidsComposition.add(this.aminoAcidsList.get(i));
            if (this.aminoAcidsList.get(i).getModificationStatus())
                isModified = true;
            ArrayList<AminoAcid> completeAAComp = new ArrayList<>();
            completeAAComp.addAll(aminoAcidsComposition);
            this.bIons.add(new FragmentIon(runningFormula, chargeState, this, 'b', i + 1, isModified, completeAAComp));
        }
        return bIons;
    }

    private ArrayList<FragmentIon> yIonBuilder(int chargeState) {
        if (chargeState == 0)
            throw new IllegalArgumentException("Charge state of Fragment Ions can't be 0!");

        SumFormula runningFormula = SumFormula.getWaterFormula();
        //this loop determines charge state; can start from 0, no proton in yet
        for (int a = 0; a < chargeState; a++) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, SumFormula.getProtonFormula());
        }
        //implement knowledge about modification status
        boolean isModified = false;
        int modPosition = 0;
        boolean hasModAtAll = false;
        for (int b = 0; b < this.aminoAcidsList.size(); b++) {
            if (aminoAcidsList.get(b).getModificationStatus()) {
                modPosition = b+1; //+1 because converting of index in real position
                hasModAtAll = true;
            }
        }
        //this loop goes through all the amino acids
        ArrayList<AminoAcid> aaComposition = new ArrayList<>();
        for (int i = this.sequenceLength - 1; i > -1; i--) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            aaComposition.add(this.aminoAcidsList.get(i));
            //at this point, all the other fragment ions are modified
            if (i <modPosition && hasModAtAll)
                isModified = true;
            if (i>=modPosition)
                isModified = false;
            ArrayList<AminoAcid> completeAAComp = new ArrayList<>();
            completeAAComp.addAll(aaComposition);
            this.yIons.add(new FragmentIon(runningFormula, chargeState, this, 'y', this.sequenceLength - i, isModified, completeAAComp));
        }
        return yIons;
    }


    //this method uses an ArrayList of Modifications to attach modifications to existing peptides
    public Peptide peptideModifier(ArrayList<Modification> modListIn) {
        ArrayList<AminoAcid> modAAList = this.getAminoAcidsList();
        boolean modStatus = false;
        for (Modification mod : modListIn) {
            boolean fixedModPosition = mod.getPositionType();
            if (fixedModPosition) {
                if (mod.getPositionNumber() > this.aminoAcidsList.size()) {
                    break;
                }
                AminoAcid currentAcid = this.aminoAcidsList.get(mod.getPositionNumber());
                SumFormula newFormula = SumFormula.sumFormulaJoiner(mod.getModificationFormula(), currentAcid.getSumFormula());
                AminoAcid modAcid = new AminoAcid("" + currentAcid.getName() + mod.getModificationName(), currentAcid.get3Let(), "" + currentAcid.get1Let(), newFormula.getSumFormula());
                modAAList.set(mod.getPositionNumber(), modAcid);
                modAAList.get(mod.getPositionNumber()).setHasModification(true);
                modAAList.get(mod.getPositionNumber()).setModification(mod);
                modStatus = true;
            }
            if (!fixedModPosition) {
                for (int i = 0; i < modAAList.size(); i++) {
                    if (modAAList.get(i).get1Let() == mod.getAminoAcidName()) {
                        AminoAcid currentAcid = modAAList.get(i);
                        SumFormula newFormula = SumFormula.sumFormulaJoiner(mod.getModificationFormula(), currentAcid.getSumFormula());
                        AminoAcid modAcid = new AminoAcid("" + currentAcid.getName() + mod.getModificationName(), currentAcid.get3Let(), "" + currentAcid.get1Let(), newFormula.getSumFormula());
                        modAAList.set(i, modAcid);
                        modAAList.get(i).setHasModification(true);
                        modAAList.get(i).setModification(mod);
                        modStatus = true;
                    }
                }
            }
        }
        Peptide modPeptide = new Peptide(this.getSequence(), modAAList, modStatus);
        return modPeptide;
    }


    public String getSequence() {
        return this.sequence;
    }

    public int getSequenceLength() {
        return this.sequenceLength;
    }

    public double getExactMass() {
        return this.exactMass;
    }

    public SumFormula getSumFormula() {
        return this.sumFormula;
    }

    public boolean getModificationStatus() {
        return this.hasModification;
    }

    public ArrayList<AminoAcid> getAminoAcidsList() {
        return this.aminoAcidsList;
    }

    public ArrayList<FragmentIon> getbIons() {
        return this.bIons;
    }

    public ArrayList<FragmentIon> getyIons() {
        return this.yIons;
    }

    public String getUnmodifiedSequence() {return this.unmodifiedSequence;}

    public void createAddFragmentIonChargestate(int chargeIn) {
        //check if additional charge states were already created
        boolean containsChargeStateB = false;
        boolean containsChargeStateY = false;

        for (FragmentIon fragmentIon : this.bIons) {
            if (chargeIn == fragmentIon.getCharge())
                containsChargeStateB = true;
        }
        for (FragmentIon fragmentIon : this.yIons) {
            if (chargeIn == fragmentIon.getCharge())
                containsChargeStateY = true;
        }

        if (!containsChargeStateB) {
            this.bIonBuilder(chargeIn);
        }
        if (!containsChargeStateY) {
            this.yIonBuilder(chargeIn);
        }

    }

    public void peptidePrinter(){
        System.out.println("");
        System.out.println("Peptide: "+this.sequence);
        System.out.println("is modified: "+this.hasModification);
        System.out.println("Sum Formula: "+this.sumFormula.getSumFormula());
        System.out.println("Length: "+this.sequenceLength);
        System.out.println("Mass: "+fiveDec.format(this.exactMass));
        System.out.println("");
    }

}