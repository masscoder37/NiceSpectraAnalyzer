import java.util.ArrayList;


/**
 * Created by Michael Stadlmeier on 6/15/2017.
 */
public class SumFormula {
    private String sumFormula;
    private ArrayList<Element> elements;
    private double exactMass;
    private double[] isotopicDistribution;

    public SumFormula(String formulaIn) {
        this.elements = new ArrayList<>();
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
            //check for numbers
            if (formulaIn.charAt(a) >= '0' && formulaIn.charAt(a) <= '9') {
                elementNumber += formulaIn.charAt(a);
            }
        }
        //flush remaining list to element Adder
        toElementAdder[0] = elementName;
        toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
        elementAdder(toElementAdder);

        for (Element element : this.elements) {
            this.exactMass += element.getElementMass();
        }
        this.sumFormula = Element.elementsToString(this.elements);
    }

    public static SumFormula sumFormulaJoiner(SumFormula a, SumFormula b){
        ArrayList<Element> joinedFormula = new ArrayList<>();
        joinedFormula.addAll(a.getElements());
        joinedFormula.addAll(b.getElements());
        String sumFormula = Element.elementsToString(joinedFormula);
        SumFormula newSumFormula = new SumFormula(sumFormula);
    return newSumFormula;
    }


    //to implement: error handling if element in b is not present anymore in a
    public static SumFormula sumFormulaSubstractor(SumFormula a, SumFormula b){
        if (a.getElements().size() < b.getElements().size())
        {
            throw new IllegalArgumentException("Sumformula "+b.getSumFormula()+" is bigger than Sumformula "+a.getSumFormula());
        }
        ArrayList<Element> joinedFormula = new ArrayList<>();
        joinedFormula.addAll(a.getElements());
        for (Element e : b.getElements()){
            for (int i = joinedFormula.size()-1; i>0;i--){
                if (e.getElementName().equals(joinedFormula.get(i).getElementName())){
                    joinedFormula.remove(i);
                    break;
                }
            }


        }
        String sumFormula = Element.elementsToString(joinedFormula);
        SumFormula newSumFormula = new SumFormula(sumFormula);
        return newSumFormula;
    }



    private void elementAdder(String[] elementQuantity) {
        if (elementQuantity.length != 2)
            throw new IllegalArgumentException("elementQuantity was formated incorrectly! Length: " + elementQuantity.length);
        int quantity = 0;
        String element = elementQuantity[0];
        if (elementQuantity[1].isEmpty())
            elementQuantity[1] = "1";
        try {
            quantity = Integer.parseInt(elementQuantity[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("This is not a number: " + elementQuantity[1]);
        }
        for (int i = 0; i < quantity; i++) {
            Element e = new Element("" + elementQuantity[0]);
            this.elements.add(e);
        }
    }

    public String getSumFormula() {
        return this.sumFormula;
    }

    public double getExactMass() {
        return this.exactMass;
    }

    public ArrayList<Element> getElements() {
        return this.elements;
    }

    public static SumFormula getWaterFormula(){
        SumFormula waterFormula = new SumFormula("H2O");
        return waterFormula;
    }
    public static SumFormula getProtonFormula(){
        SumFormula protonFormula = new SumFormula("H+");
        return protonFormula;
    }

    public static SumFormula getCliXlinkFormula (){
        SumFormula cliXlinkFormula = new SumFormula("C12H13NO4S");
        return cliXlinkFormula;
    }

    public static SumFormula getDSSOFormula(){
        SumFormula dssoFormula = new SumFormula("C6H6O3S");
        return dssoFormula;
    }

    public int getProtonNumber(){
        int protonNumber =0;

        for (Element e : this.elements){
            if (e.getElementName().equals("H+"))
                protonNumber++;
        }



        return protonNumber;
    }

    public int getCNumber(){
        int cNumber = 0;
        for (Element e : this.elements){
            if (e.getElementName().equals("C"))
                cNumber++;
        }
        return cNumber;
    }
    public int getHNumber(){
        int hNumber = 0;
        for (Element e : this.elements){
            if (e.getElementName().equals("H") || e.getElementName().equals("H+"))
                hNumber++;
        }
        return hNumber;
    }

    public int getNNumber(){
        int nNumber = 0;
        for (Element e : this.elements){
            if (e.getElementName().equals("N"))
                nNumber++;
        }
        return nNumber;
    }
    public int getONumber(){
        int oNumber = 0;
        for (Element e : this.elements){
            if (e.getElementName().equals("O"))
                oNumber++;
        }
        return oNumber;
    }
    public int getSNumber(){
        int sNumber = 0;
        for (Element e : this.elements){
            if (e.getElementName().equals("S"))
                sNumber++;
        }
        return sNumber;
    }

}
