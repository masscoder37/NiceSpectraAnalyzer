import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        /*String filePath = "C:\\Anwendungen\\IntelliJProjects\\NiceSpectraAnalyzer\\NiceSpectraAnalyzer\\Aminoacids_list.csv";
        File aminoAcids = new File(filePath);
        ArrayList<AminoAcid> aminoAcidsList = new ArrayList<>();
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);*/

        String testFormula = "HH+2ON3";
        SumFormula test = new SumFormula(testFormula);
        System.out.println("Formula: "+test.getSumFormula());
        System.out.println("Exact mass: "+fiveDec.format(test.getExactMass()));
        ArrayList<Element> elements = test.getElements();
        System.out.println();
        for(Element e : elements){
            System.out.println(""+e.getElementName());
        }





    }
}
