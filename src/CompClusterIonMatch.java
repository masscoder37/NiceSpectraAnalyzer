/**
 * Created by micha on 6/27/2017.
 */
public class CompClusterIonMatch extends IonMatch {
    private boolean isComplementaryIon;
    private String labelName;

    public CompClusterIonMatch(FragmentIon fragmentIonIn, Peak peakIn, double ppmDeviationIn, String labelNameIn, boolean isCleaved){
        super(fragmentIonIn, peakIn, ppmDeviationIn);
        this.labelName = labelNameIn;
        if (fragmentIonIn.getModificationStatus() && isCleaved){
            this.isComplementaryIon = true;
        }
        else {
            this.isComplementaryIon = false;
        }
    }

    public String getLabelName(){return this.labelName;}
    public boolean getComplementaryIonStatus(){return this.isComplementaryIon;}


}
