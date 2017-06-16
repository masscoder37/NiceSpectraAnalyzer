import java.text.DecimalFormat;

/**
 * Created by micha on 6/14/2017.
 */

//this class is a subclass of Ion
    //fragment ions of peptides are an object of this class
public class FragmentIon extends Ion {
    private Peptide precursor;
    private char ionSeries;
    private int ionNumber;
    public static DecimalFormat fiveDec = new DecimalFormat("0.00000");


    public FragmentIon(SumFormula sumFormulaIn, int chargeIn, Peptide peptideIn, char ionSeriesIn, int ionNumberIn){
        super(sumFormulaIn, chargeIn);
        this.precursor = peptideIn;
        this.ionSeries = ionSeriesIn;
        this.ionNumber = ionNumberIn;
    }

    public Peptide getPrecursor(){return this.precursor;}
    public String getPrecursorSequence(){return this.precursor.getSequence();}
    public char getIonSeries(){return this.ionSeries;}
    public int getIonNumber(){return this.ionNumber;}

    public static void fragmentIonPrinter(FragmentIon queriedIon){

        System.out.println(""+queriedIon.ionSeries+queriedIon.ionNumber+":    "+fiveDec.format(queriedIon.getExactMass()));
    }




}
