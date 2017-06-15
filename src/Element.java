import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Created by Michael Stadlmeier on 6/15/2017.
 */

//Element class
public class Element {
    private String elementName;
    private double elementMass;

    public Element(String elementName){
        this.elementName = elementName;
        elementChooser(this.elementName);
    }

    private void elementChooser(String elementName) {

            switch (elementName) {
                case "H":
                    this.elementMass = AtomicMasses.getHMASS();
                    break;
                case "C":
                    this.elementMass = AtomicMasses.getCMASS();
                    break;
                case "O":
                    this.elementMass = AtomicMasses.getOMASS();
                    break;
                case "N":
                    this.elementMass = AtomicMasses.getNMASS();
                    break;
                case "S":
                    this.elementMass = AtomicMasses.getSMASS();
                    break;
                case "H+":
                    this.elementMass = AtomicMasses.getPROTON();
                    break;

                default:
                    throw new NoSuchElementException("Element unknown: " + elementName);

            }
}
//implement this method here to have only one class if more supported elements are added
    public static String elementsToString(ArrayList<Element> elements) {
        String formula = "";
        int quantH = 0;
        int quantC = 0;
        int quantO = 0;
        int quantN = 0;
        int quantS = 0;
        int quantHPlus = 0;
        //this loop gets the element counts for each element
        for (Element e : elements) {
            String currentName = e.getElementName();
            switch (currentName) {
                case "H":
                    quantH++;
                    break;
                case "C":
                    quantC++;
                    break;
                case "O":
                    quantO++;
                    break;
                case "N":
                    quantN++;
                    break;
                case "S":
                    quantS++;
                    break;
                case "H+":
                    quantHPlus++;
                    break;
            }
        }
            //Strings for the elements, to filter out quantities with 0
            String H = "H" + quantH;
            String C = "C" + quantC;
            String N = "N" + quantN;
            String O = "O" + quantO;
            String S = "S" + quantS;
            String HPlus = "H+" + quantHPlus;

            if (quantH == 0) {
                H = "";
            }
            if (quantC == 0) {
                C = "";
            }
            if (quantN == 0) {
                N = "";
            }
            if (quantO == 0) {
                O = "";
            }
            if (quantS == 0) {
                S = "";
            }
            if (quantHPlus == 0) {
                HPlus = "";
            }

            formula = "" + H + C + N + O + S + HPlus;




        return formula;
    }

public double getElementMass(){return this.elementMass;}
public String getElementName(){return this.elementName;}
}
