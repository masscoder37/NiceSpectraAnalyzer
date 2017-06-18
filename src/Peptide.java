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

    public Peptide(String sequenceIn, ArrayList<AminoAcid> acids) {
        this.sequence = sequenceIn.toUpperCase();
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
            if (aa.getModificationStatus()) {
                this.hasModification = true;
            }
        }
        this.sumFormula = addFormula;
        this.exactMass = this.sumFormula.getExactMass();
        //create Fragment ions
        this.bIons = new ArrayList<>();
        this.yIons = new ArrayList<>();
        bIonBuilder(1);
        yIonBuilder(1);

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
        for (int i = 0; i < this.sequenceLength; i++) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            if (this.aminoAcidsList.get(i).getModificationStatus())
                isModified = true;
            this.bIons.add(new FragmentIon(runningFormula, chargeState, this, 'b', i + 1, isModified));
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
                modPosition = b;
                hasModAtAll = true;
                break;
            }
        }
        //this loop goes through all the amino acids
        for (int i = this.sequenceLength - 1; i > -1; i--) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            //at this point, all the other fragment ions are modified
            if (i == this.sequenceLength - modPosition && hasModAtAll)
                isModified = true;
            this.yIons.add(new FragmentIon(runningFormula, chargeState, this, 'y', this.sequenceLength - i, isModified));
        }
        return yIons;
    }


    //this method uses an ArrayList of Modifications to attach modifications to existing peptides
    public Peptide peptideModifier(ArrayList<Modification> modListIn) {
        ArrayList<AminoAcid> modAAList = this.getAminoAcidsList();
        for (Modification mod : modListIn) {
            boolean fixedModPosition = mod.getPositionType();
            if (fixedModPosition) {
                AminoAcid currentAcid = this.aminoAcidsList.get(mod.getPositionNumber());
                SumFormula newFormula = SumFormula.sumFormulaJoiner(mod.getModificationFormula(), currentAcid.getSumFormula());
                AminoAcid modAcid = new AminoAcid("" + currentAcid.getName() + mod.getModificationName(), currentAcid.get3Let(), "" + currentAcid.get1Let(), newFormula.getSumFormula());
                modAAList.set(mod.getPositionNumber(), modAcid);
                modAAList.get(mod.getPositionNumber()).setHasModification(true);
            }
            if (!fixedModPosition) {
                for (int i = 0; i < modAAList.size(); i++) {
                    if (modAAList.get(i).get1Let() == mod.getAminoAcidName()) {
                        AminoAcid currentAcid = modAAList.get(i);
                        SumFormula newFormula = SumFormula.sumFormulaJoiner(mod.getModificationFormula(), currentAcid.getSumFormula());
                        AminoAcid modAcid = new AminoAcid("" + currentAcid.getName() + mod.getModificationName(), currentAcid.get3Let(), "" + currentAcid.get1Let(), newFormula.getSumFormula());
                        modAAList.set(i, modAcid);
                        modAAList.get(i).setHasModification(true);
                    }
                }
            }
        }
        Peptide modPeptide = new Peptide(this.getSequence(), modAAList);
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

    public void createAddFragmentIonChargestate(int chargeIn) {
        this.bIonBuilder(chargeIn);
        this.yIonBuilder(chargeIn);
    }

}