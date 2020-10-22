import java.util.ArrayList;


/**
 * Created by Michael Stadlmeier on 6/15/2017.
 */
public class SumFormula {
    //helper class to avoid element lists
    private static class Composition {
        int amountC = 0;
        int amountH = 0;
        int amountN = 0;
        int amountO = 0;
        int amountS = 0;
        int amountP = 0;
        int amountF = 0;
        int amountCx = 0;
        int amountNx = 0;
        int amountNa = 0;
        int amountHPlus = 0;
        int amounteMinus = 0;
        double exactMass = 0;

        Composition() {
        }

        private void amountIncreaser(String elementName, int amount) {
            switch (elementName) {
                case "C":
                    amountC += amount;
                    break;
                case "H":
                    amountH += amount;
                    break;
                case "N":
                    amountN += amount;
                    break;
                case "O":
                    amountO += amount;
                    break;
                case "S":
                    amountS += amount;
                    break;
                case "P":
                    amountP += amount;
                    break;
                case "F":
                    amountF += amount;
                    break;
                case "Cx":
                    amountCx += amount;
                    break;
                case "Nx":
                    amountNx += amount;
                    break;
                case "H+":
                    amountHPlus += amount;
                    break;
                case "-":
                    amounteMinus += amount;
                    break;
                case "Na":
                    amountNa += amount;
                    break;
                default:
                    throw new IllegalArgumentException("Element unknown! Input element: " + elementName);

            }
        }

        private void setExactMass() {
            exactMass = amountC * AtomicMasses.getCMASS() + amountH * AtomicMasses.getHMASS() + amountN * AtomicMasses.getNMASS() + amountO * AtomicMasses.getOMASS()
                    + amountS * AtomicMasses.getSMASS() + amountP * AtomicMasses.getPMASS() + amountF * AtomicMasses.getFMASS() + amountCx * AtomicMasses.getCxMASS()
                    + amountNx * AtomicMasses.getNxMASS() + amountNa * AtomicMasses.getNaMASS() + amountHPlus * AtomicMasses.getPROTON() + amounteMinus * AtomicMasses.getELECTRON();
        }

        private double getExactMass() {
            if (exactMass == 0)
                this.setExactMass();
            return exactMass;
        }

        private String compositionToString() {
            String c = "C" + amountC;
            String h = "H" + amountH;
            String n = "N" + amountN;
            String o = "O" + amountO;
            String s = "S" + amountS;
            String hPlus = "H+" + amountHPlus;
            String cx = "Cx" + amountCx;
            String nx = "Nx" + amountNx;
            String f = "F" + amountF;
            String p = "P" + amountP;
            String eMinus = "-" + amounteMinus;
            String na = "Na" + amountNa;

            if (amountH == 0) {
                h = "";
            }
            if (amountC == 0) {
                c = "";
            }
            if (amountN == 0) {
                n = "";
            }
            if (amountO == 0) {
                o = "";
            }
            if (amountS == 0) {
                s = "";
            }
            if (amountHPlus == 0) {
                hPlus = "";
            }
            if (amountCx == 0) {
                cx = "";
            }
            if (amountF == 0) {
                f = "";
            }
            if (amountP == 0) {
                p = "";
            }
            if (amountNx == 0) {
                nx = "";
            }
            if (amounteMinus == 0) {
                eMinus = "";
            }
            if (amountNa == 0) {
                na = "";
            }

            String formula = "" + c + h + n + o + s + cx + nx + f + p + hPlus + na + eMinus;

            return formula;
        }

        private static Composition compositionCombiner(Composition a, Composition b) {
            Composition out = new Composition();
            out.amountH = a.amountH + b.amountH;
            out.amountC = a.amountC + b.amountC;
            out.amountN = a.amountN + b.amountN;
            out.amountO = a.amountO + b.amountO;
            out.amountS = a.amountS + b.amountS;
            out.amountP = a.amountP + b.amountP;
            out.amountF = a.amountF + b.amountF;
            out.amountCx = a.amountCx + b.amountCx;
            out.amountNx = a.amountNx + b.amountNx;
            out.amountNa = a.amountNa + b.amountNa;
            out.amountHPlus = a.amountHPlus + b.amountHPlus;
            out.amounteMinus = a.amounteMinus + b.amounteMinus;
            return out;
        }

        private static Composition compositionSubstractor(Composition a, Composition b) {
            Composition out = new Composition();
            out.amountH = a.amountH - b.amountH;
            out.amountC = a.amountC - b.amountC;
            out.amountN = a.amountN - b.amountN;
            out.amountO = a.amountO - b.amountO;
            out.amountS = a.amountS - b.amountS;
            out.amountP = a.amountP - b.amountP;
            out.amountF = a.amountF - b.amountF;
            out.amountCx = a.amountCx - b.amountCx;
            out.amountNx = a.amountNx - b.amountNx;
            out.amountNa = a.amountNa - b.amountNa;
            out.amountHPlus = a.amountHPlus - b.amountHPlus;
            out.amounteMinus = a.amounteMinus - b.amounteMinus;
            //error handling: if any amount is negative, throw error
            if (out.amountH < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountC < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountN < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountO < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountS < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountP < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountF < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountCx < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountNx < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountNa < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amountHPlus < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");
            if (out.amounteMinus < 0)
                throw new IllegalArgumentException("Error with SumFormula Substraction! Of one or more elements, a higher quantity was removed than present!");

            return out;
        }
    }

    private String sumFormula;
    private double exactMass;
    private Composition elements;
    //default charge state: H+ increases charge by 1, e- decreases charge by 1
    private int defaultChargeState;

    public SumFormula(String formulaIn) {
        //Composition replaces the older list of elements
        this.elements = new Composition();
        int length = formulaIn.length();
        //create String[] with length = 2 and elementName in [0] and elementQuantity in [1]
        String[] toElementAdder = new String[2];
        String elementName = "";
        String elementNumber = "";
        //loop through complete string
        for (int a = 0; a < length; a++) {
            //check if char is UpperCaseLetter and begins new element Name
            if (formulaIn.charAt(a) >= 'A' && formulaIn.charAt(a) <= 'Z') {
                //if there already is a upper case letter in elementName, then parse previous ElementName to elementAdder
                if (!elementName.isEmpty()) {
                    toElementAdder[0] = elementName;
                    toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
                    elementAdder(toElementAdder);
                    elementName = "";
                    elementNumber = "";
                    toElementAdder[0] = "";
                    toElementAdder[1] = "";
                }
                elementName += formulaIn.charAt(a);
            }
            //check for lower case letters
            if (formulaIn.charAt(a) >= 'a' && formulaIn.charAt(a) <= 'z') {
                elementName += formulaIn.charAt(a);
            }
            if (formulaIn.charAt(a) == '+') {
                elementName += formulaIn.charAt(a);
            }
            //electrons are also possible, i.e. negative charges added with the tmt comp ions
            //Sum Formula would look something like this: C3H5N-2O3...- is sign for electron
            if (formulaIn.charAt(a) == '-') {
                //flush if not empty
                if (!elementName.isEmpty()) {
                    toElementAdder[0] = elementName;
                    toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
                    elementAdder(toElementAdder);
                    elementName = "";
                    elementNumber = "";
                    toElementAdder[0] = "";
                    toElementAdder[1] = "";
                }
                elementName += formulaIn.charAt(a);
            }

            //check for numbers
            if (formulaIn.charAt(a) >= '0' && formulaIn.charAt(a) <= '9') {
                elementNumber += formulaIn.charAt(a);
            }
        }
        //flush remaining list to element Adder
        toElementAdder[0] = elementName;
        toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
        elementAdder(toElementAdder);

        this.exactMass = elements.getExactMass();
        this.sumFormula = elements.compositionToString();
        this.defaultChargeState = elements.amountHPlus - elements.amounteMinus;
    }

    //simplified constructor for SumFormula created with composition known
    public SumFormula(Composition compIn) {
        this.elements = compIn;
        this.exactMass = elements.getExactMass();
        this.sumFormula = elements.compositionToString();
        this.defaultChargeState = elements.amountHPlus - elements.amounteMinus;
    }

    //function to assist with constructing SumFormula
    private void elementAdder(String[] elementQuantity) {
        if (elementQuantity.length != 2)
            throw new IllegalArgumentException("elementQuantity was formated incorrectly! Length: " + elementQuantity.length);
        int quantity = 0;
        String elementName = elementQuantity[0];
        if (elementQuantity[1].isEmpty())
            elementQuantity[1] = "1";
        try {
            quantity = Integer.parseInt(elementQuantity[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("This is not a number: " + elementQuantity[1]);
        }
        elements.amountIncreaser(elementName, quantity);
    }

    //combines two SumFormulas
    public static SumFormula sumFormulaJoiner(SumFormula a, SumFormula b) {
        Composition aComp = a.elements;
        Composition bComp = b.elements;
        Composition combineComp = Composition.compositionCombiner(aComp, bComp);
        return new SumFormula(combineComp);
    }

    //substracts two SumFormulas. If more is substracted than present, error is thrown
    public static SumFormula sumFormulaSubstractor(SumFormula a, SumFormula b) {
        Composition aComp = a.elements;
        Composition bComp = b.elements;
        Composition substractedComp = Composition.compositionSubstractor(aComp, bComp);
        return new SumFormula(substractedComp);
    }

    //basic getters
    public String getSumFormula() {
        return this.sumFormula;
    }

    public double getExactMass() {
        return this.exactMass;
    }

    public int getDefaultChargeState(){
        return this.defaultChargeState;
    }

    //getters for specific formulas
    public static SumFormula getWaterFormula() {
        return new SumFormula("H2O");
    }

    public static SumFormula getProtonFormula() {
        return new SumFormula("H+");
    }

    public static SumFormula getCliXlinkFormula() {
        return new SumFormula("C12H13NO4S");
    }

    public static SumFormula getDSSOFormula() {
        return new SumFormula("C6H6O3S");
    }

    //getter for specific atom numbers
    public int getCNumber() {
        return this.elements.amountC;
    }
    public int getProtonNumber() {
        return this.elements.amountHPlus;
    }

    public int getElectronNumber(){
        return this.elements.amounteMinus;
    }

    public int getHNumber() {
        return this.elements.amountH;
    }

    public int getNNumber() {
        return this.elements.amountN;
    }

    public int getONumber() {
        return this.elements.amountO;
    }

    public int getSNumber() {
        return this.elements.amountS;
    }
}
