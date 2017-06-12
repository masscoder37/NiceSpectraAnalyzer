import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */

//this class handles SumFormula to exact mass and to isotopic distribution conversion
public class SumFormula {
    private String sumFormula;
    private int quantC;
    private int quantN;
    private int quantO;
    private int quantS;
    private int quantH;
    private int quantProton;
    private double exactMass;
    private double[] isoDistribution;

    public SumFormula sumFormula(String formulaIn){
        this.sumFormula = formulaIn;
        //this sets the SumFormula String to a useful format
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
                    elementNumber[0] = ""+element;
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
        elementNumber[0] = ""+element;
        elementNumber[1] = number.isEmpty() ? "1" : number;
        elementList.add(elementNumber);


    }

    private void elementChooser(ArrayList<String[]> elementList){
        for (String[] elementNumber : elementList) {

            char element = elementNumber[0].charAt(0);

            switch (element) {
                case 'H':
                    this.quantH = Integer.parseInt(elementNumber[1]);
                    break;
                case 'C':
                    this.quantC = Integer.parseInt(elementNumber[1]);
                    break;
                case 'O':
                    this.quantO = Integer.parseInt(elementNumber[1]);
                    break;
                case 'N':
                    this.quantN = Integer.parseInt(elementNumber[1]);
                    break;
                case 'S':
                    this.quantS = Integer.parseInt(elementNumber[1]);
                    break;
                default:
                    throw new NoSuchElementException("Element unknown!");
            }
        }
    }

}
