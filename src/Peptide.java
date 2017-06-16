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
            //second for loop: go through all the amino acids until you find the right one
            //if the right one is found, aaNotFound = false and the loop exits prematurely
            for (int b = 0; b < acids.size() && aaNotFound; b++) {
                aaNotFound = true;
                if (current == acids.get(b).get1Let()) {
                    //create elongated SumFormula
                    this.aminoAcidsList.add(acids.get(b));
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
        for (AminoAcid aa : this.aminoAcidsList){
            addFormula = SumFormula.sumFormulaJoiner(addFormula, aa.getWaterLossFormula());
            if (aa.getModificationStatus()){
                this.hasModification = true;
            }
        }
        this.sumFormula = addFormula;
        this.exactMass = this.sumFormula.getExactMass();
        //create Fragment ions
        //bIonBuilder(1);
        //yIonBuilder(1);

    }

        private ArrayList<FragmentIon> bIonBuilder(int chargeState) {
        this.bIons = new ArrayList<>();
        //one Proton is added to not create empty sum formula
        SumFormula runningFormula = new SumFormula("H+");
            //this loop determines charge state; starts at 1 because one proton has already been added
            for (int a = 1; a<chargeState;a++){
                runningFormula = SumFormula.sumFormulaJoiner(runningFormula, SumFormula.getProtonFormula());
            }
        //this loop goes through all the amino acids
        for (int i = 0; i<this.sequenceLength;i++ ) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            this.bIons.add(new FragmentIon(runningFormula, chargeState, this, 'b', i+1));
        }
            return bIons;
        }

    private ArrayList<FragmentIon> yIonBuilder(int chargeState) {
        this.yIons = new ArrayList<>();
        SumFormula runningFormula = SumFormula.getWaterFormula();
        //this loop determines charge state; can start from 0, no proton in yet
        for (int a = 0; a<chargeState;a++){
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, SumFormula.getProtonFormula());
        }
        //this loop goes through all the amino acids
        for (int i = this.sequenceLength-1; i>-1;i-- ) {
            runningFormula = SumFormula.sumFormulaJoiner(runningFormula, this.aminoAcidsList.get(i).getWaterLossFormula());
            this.yIons.add(new FragmentIon(runningFormula, chargeState, this, 'y', this.sequenceLength-1));
        }
        return yIons;
    }



        public String getSequence(){return this.sequence;}
        public int getSequenceLength() {return this.sequenceLength;}
        public double  getExactMass(){return this.exactMass;}
        public SumFormula getSumFormula(){return this.sumFormula;}
        public boolean getModificationStatus() {return this.hasModification;}
        public ArrayList<AminoAcid> getAminoAcidsList() {return this.aminoAcidsList;}
        public ArrayList<FragmentIon> getbIons(){return this.bIons;}
        public ArrayList<FragmentIon> getyIons(){return this.yIons;}

    }