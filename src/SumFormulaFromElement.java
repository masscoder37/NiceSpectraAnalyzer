import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/15/2017.
 */
public class SumFormulaFromElement {
    private String sumFormula;
    private ArrayList<Element> elements;
    private double exactMass;
    private double[] isotopicDistribution;

    public SumFormulaFromElement(String formulaIn){
        this.elements = new ArrayList<>();
        this.sumFormula = formulaIn;
        int length = formulaIn.length();
        //create String[] with length = 2 and elementName in [0] and elementQuantity in [1]
        String[] toElementAdder = new String[2];
        String elementName = "";
        String elementNumber = "";
        //loop through complete string
        for (int a = 0; a<length;a++){
            //check if char is UpperCaseLetter and begins new element Name
            if (formulaIn.charAt(a)>= 'A' && formulaIn.charAt(a)<='Z'){
                //if there already is a upper case letter in elementName, then parse previous ElementName to elementAdder
                if (!elementName.isEmpty())
                {
                    toElementAdder[0] = elementName;
                    toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
                    elementAdder(toElementAdder);
                    elementName= "";
                    elementNumber="";
                    toElementAdder[0]= "";
                    toElementAdder[1]="";

                }
                elementName += formulaIn.charAt(a);

            }
            //check for lower case letters
            if (formulaIn.charAt(a)>='a'&& formulaIn.charAt(a)<='z'){
                elementName += formulaIn.charAt(a);
            }
            if (formulaIn.charAt(a) == '+'){
                elementName += formulaIn.charAt(a);
            }
            //check for numbers
            if (formulaIn.charAt(a)>='0'&&formulaIn.charAt(a)<='9'){
                elementNumber += formulaIn.charAt(a);
            }
        }
        //flush remaining list to element Adder
        toElementAdder[0] = elementName;
        toElementAdder[1] = elementNumber; //if elementNumber is empty, e.g. in C H4, then elementAdder will take care of that
        elementAdder(toElementAdder);

        for (Element element : this.elements){
            this.exactMass += element.getElementMass();
        }


    }

private void elementAdder(String[] elementQuantity){
        if (elementQuantity.length != 2)
            throw new IllegalArgumentException("elementQuantity was formated incorrectly! Length: "+elementQuantity.length);
        int quantity = 0;
        String element = elementQuantity[0];
        if (elementQuantity[1].isEmpty())
            elementQuantity[1] = "1";
        try {
            quantity = Integer.parseInt(elementQuantity[1]);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("This is not a number: "+elementQuantity[1]);
        }
        for (int i = 0; i<quantity;i++){
            Element e = new Element(""+elementQuantity[0]);
            this.elements.add(e);
        }
}

public String getSumFormula(){return this.sumFormula;}
public double getExactMass(){return this.exactMass;}
public ArrayList<Element> getElements(){return this.elements;}

}
