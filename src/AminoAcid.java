import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by micha on 6/14/2017.
 */

//this class handels different AminoAcids
    //all the information is supplied by a .csv-File with all the AminiAcids
public class AminoAcid {
    private String name;
    private String threeLetter;
    private char oneLetter;
    private Double exactMass;
    private SumFormula SumFormula;
    private Double waterLossMass;
    private SumFormula waterLossFormula;
    private boolean hasModification;
    private Modification modification = null;


    public AminoAcid(String nameIn, String threeLetIn, String oneLetIn, String SumFormIn) {
        this.name = nameIn;
        this.threeLetter = threeLetIn;
        //Be sure that one letter code is always written in upper case
        String oneLetterUpperCase = oneLetIn.toUpperCase();
        if (oneLetterUpperCase.length() == 1) {
            this.oneLetter = oneLetterUpperCase.charAt(0);
        } else throw new IllegalArgumentException("Invalid One-letter Code formatting!");

        this.SumFormula = new SumFormula(SumFormIn);
        this.exactMass = this.SumFormula.getExactMass();
        this.waterLossFormula = SumFormula.sumFormulaSubstractor(this.SumFormula, SumFormula.getWaterFormula());
        this.waterLossMass = this.waterLossFormula.getExactMass();
        this.hasModification = false;
    }

    public void setHasModification(boolean modStatus) {
        this.hasModification = modStatus;
    }


    public void setModification(Modification modIn) {
        this.modification = modIn;
    }



    public String getName() {
        return this.name;
    }

    public String get3Let() {
        return this.threeLetter;
    }

    public char get1Let() {
        return this.oneLetter;
    }

    public double getMass() {
        return this.SumFormula.getExactMass();
    }

    public double getwaterLossMass() {
        return this.waterLossFormula.getExactMass();
    }

    public SumFormula getSumFormula() {
        return this.SumFormula;
    }

    public SumFormula getWaterLossFormula() {
        return this.waterLossFormula;
    }

    public boolean getModificationStatus() {
        return this.hasModification;
    }

    public Modification getModification(){return this.modification;}

    public static ArrayList<AminoAcid> getAminoAcidList() {
        ArrayList<AminoAcid> acids = new ArrayList<>();
        String[] copiedList = new String[20];

        copiedList[0] = "Alanine,Ala,A,C3H7NO2";
        copiedList[1] = "Arginine,Arg,R,C6H14N4O2";
        copiedList[2] = "Asparagine,Asn,N,C4H8N2O3";
        copiedList[3] = "Aspartic acid,Asp,D,C4H7NO4";
        copiedList[4] = "Cysteine,Cys,C,C3H7NO2S";
        copiedList[5] = "Glutamic acid,Glu,E,C5H9NO4";
        copiedList[6] = "Glutamine,Gln,Q,C5H10N2O3";
        copiedList[7] = "Glycine,Gly,G,C2H5NO2";
        copiedList[8] = "Histidine,His,H,C6H9N3O2";
        copiedList[9] = "Isoleucine,Ile,I,C6H13NO2";
        copiedList[10] = "Leucine,Leu,L,C6H13NO2";
        copiedList[11] = "Lysine,Lys,K,C6H14N2O2";
        copiedList[12] = "Methionine,Met,M,C5H11NO2S";
        copiedList[13] = "Phenylalanine,Phe,F,C9H11NO2";
        copiedList[14] = "Proline,Pro,P,C5H9NO2";
        copiedList[15] = "Serine,Ser,S,C3H7NO3";
        copiedList[16] = "Threonine,Thr,T,C4H9NO3";
        copiedList[17] = "Tryptophan,Trp,W,C11H12N2O2";
        copiedList[18] = "Tyrosine,Tyr,Y,C9H11NO3";
        copiedList[19] = "Valine,Val,V,C5H11NO2";


        for (int i = 0; i < copiedList.length; i++) {
            String line = copiedList[i];
            String[] fields = line.split(",");
            if (fields.length < 4)
                continue;
            try {
                acids.add(new AminoAcid(fields[0], fields[1], fields[2], fields[3]));
            } catch (NumberFormatException e) {
                System.out.println("Invalid format : " + line);
                continue;
            }
        }
        return acids;
    }

    public static AminoAcid createSpecificAA(String aaToCreate){
        AminoAcid output = null;
        if (!(aaToCreate.length() == 3)) {
            if (!(aaToCreate.length()==1))
                throw new IllegalArgumentException("Please enter 3- or 1-Letter code of the AA you want!");
        }

        ArrayList<AminoAcid> aaList = getAminoAcidList();
        if (aaToCreate.length() == 3){
            //3 letter is Xxx letter structure, e.g. Lys
            String correctStructure = aaToCreate.substring(0,1).toUpperCase() + aaToCreate.substring(1).toLowerCase();
            for (AminoAcid current : aaList){
                if (current.get3Let().equals(correctStructure)){
                    output = current;
                    break;
                }
            }
        }
        else {
            char toCheck = Character.toUpperCase(aaToCreate.charAt(0));
            for (AminoAcid current : aaList){
                if (current.get1Let() == toCheck){
                    output = current;
                    break;
                }
            }
        }
        return output;
    }




}
