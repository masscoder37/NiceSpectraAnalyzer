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


    //constructor for SumFormula
    //only gets String
    //sets the respective parameters (quantity of elements, exactMass, isotopic distribution)
    public SumFormula(String formulaIn){
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

        //element list is complete, set the element quantities
        elementChooser(elementList);
        //after element quantities are set, exact mass is calculated
        this.exactMass = AtomicMasses.getHMASS() * this.quantH +
                AtomicMasses.getCMASS() * this.quantC +
                AtomicMasses.getNMASS() * this.quantN +
                AtomicMasses.getOMASS() * this.quantO +
                AtomicMasses.getSMASS() * this.quantS;
                // still to do: implement isotopic distribution
    }

    //gives the sum of 2 sum formulas
    public static SumFormula sumFormulaJoiner (SumFormula a, SumFormula b){
        int newH = a.quantH + b.quantH;
        int newC = a.quantC + b.quantC;
        int newN = a.quantN + b.quantN;
        int newO = a.quantO + b.quantO;
        int newS = a.quantS + b.quantS;
        String strFormula = "H"+newH+"C"+newC+"N"+newN+"O"+newO+"S"+newS;
        SumFormula joinedFormula = new SumFormula(strFormula);
        return joinedFormula;
    }

    public static SumFormula sumFormulaSubstractor (SumFormula a, SumFormula b){
        int newH = a.quantH - b.quantH;
        int newC = a.quantC - b.quantC;
        int newN = a.quantN - b.quantN;
        int newO = a.quantO - b.quantO;
        int newS = a.quantS - b.quantS;
        String strFormula = "H"+newH+"C"+newC+"N"+newN+"O"+newO+"S"+newS;
        SumFormula joinedFormula = new SumFormula(strFormula);
        return joinedFormula;

    }


    //getters
    public String getSumFormula(){
        return this.sumFormula;
    }

    public double getExactMass(){
        return this.exactMass;
    }
    public int[] getElementalComposition(){
        //returns an int[] with quantities of H, C, N, O, S
        int[] comp = new int[5];
        comp[0] = this.quantH;
        comp[1] = this.quantC;
        comp[2] = this.quantN;
        comp[3] = this.quantO;
        comp[4] = this.quantS;

        return comp;
    }



    //helper method to set quantities during element parse out
    private void elementChooser(ArrayList<String[]> elementList){
        for (String[] elementNumber : elementList) {

            char element = elementNumber[0].charAt(0);

            switch (element) {
                case 'H':
                    try {
                        this.quantH = Integer.parseInt(elementNumber[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("This is not a number: "+elementNumber[1]);
                    }
                    break;
                case 'C':
                    try {
                        this.quantC = Integer.parseInt(elementNumber[1]);
                    }
                    catch (NumberFormatException e) {
                    throw new IllegalArgumentException("This is not a number: "+elementNumber[1]);
                }
                    break;
                case 'O':
                    try {
                        this.quantO = Integer.parseInt(elementNumber[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("This is not a number: "+elementNumber[1]);
                    }
                    break;
                case 'N':
                    try {
                        this.quantN = Integer.parseInt(elementNumber[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("This is not a number: "+elementNumber[1]);
                    }
                    break;
                case 'S':
                    try {
                        this.quantS = Integer.parseInt(elementNumber[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("This is not a number: "+elementNumber[1]);
                    }
                    break;
                default:
                    throw new NoSuchElementException("Element unknown: "+element);
            }
        }
    }

}
