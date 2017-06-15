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
    private SumFormula waterFormula = new SumFormula("H2O");

    public Peptide(String sequenceIn, ArrayList<AminoAcid> acids){
        //set those parameters right away
        this.sequence = sequenceIn.toUpperCase();
        this.sequenceLength = sequenceIn.length();
        //determine amino acid composition and create sum formula
        SumFormula addFormula = new SumFormula("");
        boolean aaNotFound = true;
        //first for loop: go through the whole Sequence
        for (int a = 0; a<this.sequenceLength; a++){
            char current = this.sequence.charAt(a);
            //second for loop: go through all the amino acids until you find the right one
            //if the right one is found, aaNotFound = false and the loop exits prematurely
            for (int b = 0; b <acids.size()&&aaNotFound;b++){
                aaNotFound = true;
                if (current == acids.get(b).get1Let()){
                    //create elongated SumFormula
                    addFormula = SumFormula.sumFormulaJoiner(addFormula, acids.get(b).getWaterLossFormula());
                    aaNotFound = false;
                }
                //handling of unknown amino acids
                if (b == acids.size()-1 && aaNotFound){
                    throw new IllegalArgumentException("Amino Acid unknown: "+current);
                }
            }
        }
        //correct sum Formula is created by adding water to waterloss-Sum Formula
        this.sumFormula = SumFormula.sumFormulaJoiner(addFormula, waterFormula);
        //mass is calculated from SumFormula
        this.exactMass = this.sumFormula.getExactMass();
        this.hasModification = false;
        //invoke FragmentIon generation method
    }


    private ArrayList<FragmentIon> bIonBuilder(){
        ArrayList<FragmentIon> bIons = new ArrayList<>();







        return bIons;

    }

}
