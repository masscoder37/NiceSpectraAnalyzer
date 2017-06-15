import java.io.File;
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
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        SumFormula a = new SumFormula(aminoAcidsList.get(0).getSumFormula().getSumFormula());
        SumFormula b = new SumFormula(aminoAcidsList.get(5).getSumFormula().getSumFormula());
        System.out.println("a: "+a.getSumFormula());
        System.out.println("b: "+b.getSumFormula());
        System.out.println("a+b: "+SumFormula.sumFormulaJoiner(a, b).getSumFormula());
        System.out.println("a+b mass: "+SumFormula.sumFormulaJoiner(a, b).getExactMass());
        System.out.println("a+b: "+SumFormula.sumFormulaSubstractor(a, b).getSumFormula());
        System.out.println("a+b mass: "+SumFormula.sumFormulaSubstractor(a, b).getExactMass());*/

        String testFormula = "HH+2ON3";
        SumFormulaFromElement test = new SumFormulaFromElement(testFormula);
        System.out.println("Formula: "+test.getSumFormula());
        System.out.println("Exact mass: "+fiveDec.format(test.getExactMass()));
        ArrayList<Element> elements = test.getElements();
        System.out.println();
        for(Element e : elements){
            System.out.println(""+e.getElementName());
        }





    }
}
