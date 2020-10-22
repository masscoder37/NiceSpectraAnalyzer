/**
 * Created by micha on 6/27/2017.
 */
public class CompClusterIonMatch extends IonMatch {
    //three cases:
    //case1: FragmentIon only contains cleaved Label: is cleaved = true, is mixed = false
    //case2: FragmentIon only contains intact Label: is cleaved = false, is mixed = false
    //case3: FragmentIon contains both cleaved and intact Label: is cleaved = true, is mixed = true;

    private boolean isCleaved;
    private boolean isMixed;
    private String labelName;
    private String scanHeader;
    private String leadingProteins;
    private int labelQuantity;

    public CompClusterIonMatch(FragmentIon fragmentIonIn, Peak peakIn, double ppmDeviationIn, String labelNameIn, boolean isCleavedOnlyIn, boolean isMixedIn, String scanHeaderIn, String leadProteinsIn){
        super(fragmentIonIn, peakIn, ppmDeviationIn);
        this.labelName = labelNameIn;
        this.isCleaved = isCleavedOnlyIn;
        this.isMixed = isMixedIn;
        this.scanHeader = scanHeaderIn;
        this.labelQuantity = fragmentIonIn.getLabelQuantity();
        this.leadingProteins = leadProteinsIn;
    }

    public String getLabelName(){return this.labelName;}
    public boolean getIsCleaved(){return this.isCleaved;}
    public boolean getMixedLabels(){return this.isMixed;}
    public String getScanHeader(){return this.scanHeader;}
    public String getLeadingProteins(){return this.leadingProteins;}
    public int getLabelQuantity() {return labelQuantity;}
}
