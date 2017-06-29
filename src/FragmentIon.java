import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by micha on 6/14/2017.
 */

//this class is a subclass of Ion
    //fragment ions of peptides are an object of this class
public class FragmentIon extends Ion {
    private Peptide precursor;
    private char ionSeries;
    private int ionNumber;
    private boolean isModified;
    //implement new variables for modifications
    private ArrayList<AminoAcid> aminoAcidsList;
    private ArrayList<Modification> modList;



    private static DecimalFormat fiveDec = new DecimalFormat("0.00000");


    public FragmentIon(SumFormula sumFormulaIn, int chargeIn, Peptide peptideIn, char ionSeriesIn, int ionNumberIn, boolean modificationStatus, ArrayList<AminoAcid> aaCompositionIn){
        super(sumFormulaIn, chargeIn);
        this.precursor = peptideIn;
        this.ionSeries = ionSeriesIn;
        this.ionNumber = ionNumberIn;
        this.isModified = modificationStatus;
        this.aminoAcidsList = aaCompositionIn;
    }

    public Peptide getPrecursor(){return this.precursor;}
    public String getPrecursorSequence(){return this.precursor.getSequence();}
    public char getIonSeries(){return this.ionSeries;}
    public int getIonNumber(){return this.ionNumber;}
    public String getCompleteIon(){return ""+this.ionSeries+this.ionNumber;}
    public boolean getModificationStatus(){return this.isModified;}

    public static void fragmentIonPrinter(FragmentIon queriedIon){

        System.out.print(""+queriedIon.ionSeries+queriedIon.ionNumber+" "
                +queriedIon.getCharge()+"+"
                +":    "+fiveDec.format(queriedIon.getMToZ())+" m/z"
        +"   is modified: "+queriedIon.getModificationStatus());
        System.out.print("    Sequence: ");
        String sequence = "";
        for (AminoAcid acid : queriedIon.aminoAcidsList){
           sequence += acid.get1Let();
        }
        System.out.println(""+sequence);

    }
    public static void fragmentIonFormulaPrinter(FragmentIon queriedIon){

        System.out.println(""+queriedIon.ionSeries+queriedIon.ionNumber+" "+queriedIon.getCharge()+"+"+":    "+queriedIon.getFormula().getSumFormula());
    }




}
