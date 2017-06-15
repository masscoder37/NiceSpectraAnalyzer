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

        String testFormula = "C6H12O6";
        SumFormula a = new SumFormula(testFormula);
        SumFormula b = new SumFormula("H2O");

        SumFormula c = SumFormula.sumFormulaSubstractor(a,b);
        System.out.println("Sumformula: "+c.getSumFormula());
        System.out.println("Sumformula: "+c.getExactMass());
        for (Element e : c.getElements()){
            System.out.println(""+e.getElementName());
        }






    }
}
