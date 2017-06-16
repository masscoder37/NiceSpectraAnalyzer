import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        String filePath = "E:\\Anwendungen\\IntelliJ\\Project\\NiceSpectraAnalyzer\\Aminoacids_list.csv";
        File aminoAcids = new File(filePath);
        ArrayList<AminoAcid> aminoAcidsList = new ArrayList<>();
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);


        Peptide test = new Peptide("MICHI", aminoAcidsList);
        System.out.println("Peptide sequence: "+test.getSequence());
        System.out.println("Peptide exact mass: "+fiveDec.format(test.getExactMass()));
        System.out.println("Peptide sum formula: "+test.getSumFormula().getSumFormula());
        /*System.out.println("b-Ion series: ");

        for(FragmentIon ion : test.getbIons()){
            FragmentIon.fragmentIonPrinter(ion);
        }
        System.out.println();
        System.out.println("y-Ion series: ");
        for(FragmentIon ion : test.getyIons()){
            FragmentIon.fragmentIonPrinter(ion);
        }*/








    }
}
