import java.util.NoSuchElementException;

/**
 * Created by Michael Stadlmeier on 6/15/2017.
 */

//Element class
public class Element {
    private String element;
    private double elementMass;

    public Element(String elementName){
        this.element = elementName;
        elementChooser(this.element);
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
                default:
                    throw new NoSuchElementException("Element unknown: " + elementName);

            }


}
