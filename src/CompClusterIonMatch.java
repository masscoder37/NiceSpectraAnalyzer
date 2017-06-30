/**
 * Created by micha on 6/27/2017.
 */
public class CompClusterIonMatch extends IonMatch {
    //three cases:
    //case1: FragmentIon only contains cleaved Label
    //case2: FragmentIon only contains intact Label
    //case3: FragmentIon contains both cleaved and intact Label

    private boolean isCleavedOnly;
    private boolean isMixed;
    private String labelName;
    private String scanHeader;
    private int labelQuantity;

    public CompClusterIonMatch(FragmentIon fragmentIonIn, Peak peakIn, double ppmDeviationIn, String labelNameIn, boolean isCleavedOnlyIn, boolean isMixedIn, String scanHeaderIn){
        super(fragmentIonIn, peakIn, ppmDeviationIn);
        this.labelName = labelNameIn;
        this.isCleavedOnly = isCleavedOnlyIn;
        this.isMixed = isMixedIn;
        this.scanHeader = scanHeaderIn;
        this.labelQuantity = fragmentIonIn.getLabelQuantity();

    }

    public String getLabelName(){return this.labelName;}
    public boolean onlyCleaved(){return this.isCleavedOnly;}
    public boolean mixedLabels(){return this.isMixed;}
    public String getScanHeader(){return this.scanHeader;}


}
