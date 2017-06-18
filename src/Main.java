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

        Peptide testPeptide = new Peptide("SANDRA", aminoAcidsList);
        System.out.println("Peptide sequence: "+testPeptide.getSequence());
        System.out.println("Peptide exact mass: "+fiveDec.format(testPeptide.getExactMass()));
        System.out.println("Peptide sum formula: "+testPeptide.getSumFormula().getSumFormula());
        System.out.println("b-Ion series: ");

        for(FragmentIon ion : testPeptide.getbIons()){
            FragmentIon.fragmentIonPrinter(ion);
            System.out.println("Is modified: "+ion.getModificationStatus());
        }
        System.out.println();
        System.out.println("y-Ion series: ");
        for(FragmentIon ion : testPeptide.getyIons()){
            FragmentIon.fragmentIonPrinter(ion);
            System.out.println("Is modified: "+ion.getModificationStatus());

        }
        System.out.println();System.out.println();
        ArrayList<Modification> modList = new ArrayList<>();
        Modification mod1 = new Modification("Test", "C10", 2);
        modList.add(mod1);
        Peptide modTest = testPeptide.peptideModifier(modList);

        System.out.println("Peptide sequence: "+modTest.getSequence());
        System.out.println("Peptide exact mass: "+fiveDec.format(modTest.getExactMass()));
        System.out.println("Peptide sum formula: "+modTest.getSumFormula().getSumFormula());
        System.out.println("b-Ion series: ");

        for(FragmentIon ion : modTest.getbIons()){
            FragmentIon.fragmentIonPrinter(ion);
            System.out.println("Is modified: "+ion.getModificationStatus());
        }
        System.out.println();
        System.out.println("y-Ion series: ");
        for(FragmentIon ion : modTest.getyIons()){
            FragmentIon.fragmentIonPrinter(ion);
            System.out.println("Is modified: "+ion.getModificationStatus());

        }










    }
}
