//this class is used to detect features in MS-spectra
//MySpectrum object will have a list of features

import javax.naming.PartialResultException;
import java.util.ArrayList;

public class Feature {
    private ArrayList<Peak> peakList;
    private int scanNumber;
    private int chargeState;
    private double summedAbsIntensity = 0;
    private double summedRelIntensityPerTIC; //this value would only be important in comparison to spectra TIC? Not to base peak
    private int numberOfPeaks;
    private int featureNumber; //counts the Feature in the spectra

    public Feature(ArrayList<Peak> featurePeaksIn, double spectrumTICIn, int featureNumberIn) {
        this.peakList = new ArrayList<>();
        this.peakList.addAll(featurePeaksIn);
        this.numberOfPeaks = peakList.size();
        //this should never be the case, but error handling
        if (peakList.size() != 0) {
            //in MySpectrum, all peaks are certain to have the same scan number, so just pick the first element
            this.scanNumber = peakList.get(0).getScanNumber();
            //charge state should be set from the FeatureDetector function, so also is the same for all the peaks
            this.chargeState = peakList.get(0).getCharge();
            for (Peak peak : peakList) {
                this.summedAbsIntensity += peak.getIntensity();
            }
            this.summedRelIntensityPerTIC = this.summedAbsIntensity / spectrumTICIn * 100;
            this.featureNumber = featureNumberIn;
        }
    }


    //TODO: feature Assigner is implemented, but the Feature object is yet to be implemented in MySpectrum

    public static ArrayList<Feature> featureAssigner(MySpectrum spectrumIn, double deviationIn) {
        ArrayList<Feature> out = new ArrayList<>();
        //featureAssigner only makes sense if charge states were assigned
        if (!spectrumIn.areChargeStatesAssigned())
            return out;

        //reference peaklist, therefore be able to change things there
        ArrayList<Peak> peaklist = spectrumIn.getPeakList();


        //regarding the PPM offset to keep in mind, in the worst case, where M0 and M1 shift in opposite directions (unlikely), mass deviation is 2 * deviation...set as 2.1 to be sure
        double ppmDevToCheck = 2.1 * deviationIn;

        // 1) a feature is present if at least 2 peaks are present with same charge state
        // 2) detecting a feature always stars from the first peak of a feature
        // 3) a feature is only present if its continouus from M(0) to M(n)
        // 4) after a feature is detected, ignore all associated peaks from the peaklist
        // 4)cont. to do this, add the peak masses to a list of already detected peaks
        // 4)cont. the peak masses are unique

        ArrayList<Double> assignedPeakMasses = new ArrayList<>();
        int currentFeatureNumber = 1;

        //this for-loop loops through all peaks in the list
        for (int i = 0; i < peaklist.size(); i++) {
            //first, check if Peak was already assigned to a feature
            if (assignedPeakMasses.contains(peaklist.get(i).getMass())) {
                continue;
            }
            //if not, then set the current peak
            Peak current = peaklist.get(i);
            //if the current peak does not have a charge state, chargeStateAssigner already found that a feature is NOT present
            if (!current.isChargeStateKnown())
                continue;
            //assemble the next 15 peaks which potentially can be included in the Feature
            ArrayList<Peak> followingPeaks = new ArrayList<>();
            for (int m = i + 1; m < i + 15; m++) {
                //try-catch deals with not enough following peaks
                try {
                    //check again if peak is on the list of assigned peaks and skip that peak
                    //it is possible that a peak >i was removed if it was part of an feature before
                    if (assignedPeakMasses.contains(peaklist.get(m).getMass()))
                        continue;
                    followingPeaks.add(peaklist.get(m));
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
            }
            //now, we have the current peak and we have a list of peaks which are following
            //generate a new list with the Peaks which will be used to generate the feature
            ArrayList<Peak> newFeaturePeaks = new ArrayList<>();
            //current peak would always be part of feature
            newFeaturePeaks.add(current);
            int currentCharge = current.getCharge();
            double currentMass = current.getMass();
            //the isotope offset will guarantee that a complete feature is detected - see bullet point 3
            int isotopeOffset = 1;

            //check for every peak if it's part of a feature
            for (Peak neighbour : followingPeaks) {
                double massToCheck = currentMass + (AtomicMasses.getNEUTRON() * isotopeOffset) / currentCharge;
                if (DeviationCalc.ppmMatch(massToCheck, neighbour.getMass(), ppmDevToCheck)) {
                    newFeaturePeaks.add(neighbour);
                    //next, look for next peak of feature
                    isotopeOffset++;
                }
            }
            //check if a feature was detected
            //this is the case if newFeaturePeaks is larger than 1
            if (newFeaturePeaks.size() > 1) {
                //generate new feature
                out.add(new Feature(newFeaturePeaks, spectrumIn.getSpectrumTIC(),currentFeatureNumber));
                currentFeatureNumber++;
                //modification of the peaks is done in the assignFeature method of MySpectrum
                //add all the masses to the "exclusion list"
                for (Peak peak : newFeaturePeaks) {
                    assignedPeakMasses.add(peak.getMass());
                }
            }
            followingPeaks.clear();
            newFeaturePeaks.clear();
        }
        return out;
    }



    //getter

    public ArrayList<Peak> getPeakList() {
        return peakList;
    }

    public int getScanNumber() {
        return scanNumber;
    }

    public int getChargeState() {
        return chargeState;
    }

    public double getSummedAbsIntensity() {
        return summedAbsIntensity;
    }

    public double getSummedRelIntensityPerTIC() {
        return summedRelIntensityPerTIC;
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public int getFeatureNumber(){return this.featureNumber;}
}
