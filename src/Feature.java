//this class is used to detect features in MS-spectra

import java.util.ArrayList;

public class Feature {
    private ArrayList<Peak> peakList;
    private int scanNumber;
    private int chargeState;
    private double summedAbsIntensity;
    private double summedRelIntensity;
    private int numberOfFeatures;

    public Feature (ArrayList<Peak> featurePeaksIn){
    this.peakList = featurePeaksIn;
    this.numberOfFeatures = peakList.size();
    //this should never be the case, but error handling
    if (peakList.size() != 0){
        //in MySpectrum, all peaks are certain to have the same scan number, so just pick the first element
        this.scanNumber = peakList.get(0).getScanNumber();
        //charge state should be set from the FeatureDetecter function, so also is the same for all the peaks
        this.chargeState = peakList.get(0).getCharge();
    }


    }




    //TODO: function which is fed a set of peaks or a spectrum and determines all the features in the spectrum
}
