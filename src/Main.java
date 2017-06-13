import java.text.DecimalFormat;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");


        String testFormula = "C10H1O8S3";
        SumFormula resultFormula = new SumFormula(testFormula);
        double formulaMass = resultFormula.getExactMass();
        System.out.println("Sum formula: "+resultFormula.getSumFormula());
        System.out.println("Exact mass: "+fiveDec.format(formulaMass));


    }
}
