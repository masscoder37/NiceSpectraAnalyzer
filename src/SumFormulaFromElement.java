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
        ArrayList<Element> elements = new ArrayList<>();

        char element = 0;
        String number = "";
        //this arrayList will be used to set the parameters
        ArrayList<String[]> elementList = new ArrayList<>();
        //go through every character in the formula
        for (int i = 0; i < formulaIn.length(); i++) {
            //if char is a upper case letter
            if (formulaIn.charAt(i) >= 'A' && formulaIn.charAt(i) < 'a') {
                //if element isn't zero, that means second iteration
                if (element != 0) {
                    String[] elementNumber = new String[2];
                    elementNumber[0] = "" + element;
                    //if number is empty then 1, else number; only happens if element is letter and
                    elementNumber[1] = number.isEmpty() ? "1" : number;
                    elementList.add(elementNumber);
                }
                //reset the number and set the element to new value;
                number = "";
                element = formulaIn.charAt(i);
            } else //if the char isn't an element, add the number here to the other number or the empty string
                number += formulaIn.charAt(i);
        }
        //at the end of the loop, flush last element into array list
        String[] elementNumber = new String[2];
        elementNumber[0] = "" + element;
        elementNumber[1] = number.isEmpty() ? "1" : number;
        elementList.add(elementNumber);


        for(String[] elementQuantity : elementList) {
            int quantity = 0;
            try {quantity = Integer.parseInt(elementQuantity[1]);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("This is not a number: " + elementQuantity[1]);
            }
            for (int i = 0; i<quantity;i++)
            elements.add(new Element(elementQuantity[0]));

        }


    }
}
