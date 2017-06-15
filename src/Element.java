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
                case "Na":
                    this.elementMass = AtomicMasses.getNaMASS();
break;
                default:
                    throw new NoSuchElementException("Element unknown: " + elementName);

            }
}

public double getElementMass(){return this.elementMass;}
public String getElementName(){return this.elementName;}
}
