/**
 * Created by Michael Stadlmeier on 7/15/2017.
 */
public class ReporterMatch {

    private Peak peak;
    private double reporterMass;
    private String labelName;
    private String repName;
    private double ppmDev;


    public ReporterMatch(Peak peakIn, double repMassIn, String labelNameIn, String repNameIn){
        this.peak = peakIn;
        this.reporterMass = repMassIn;
        this.labelName = labelNameIn;
        this.repName = repNameIn;
        this.ppmDev = DeviationCalc.ppmDeviationCalc(repMassIn, this.peak.getMass());
    }

    public Peak getPeak() {
        return this.peak;
    }

    public double getReporterMass() {
        return this.reporterMass;
    }

    public String getLabelName() {
        return this.labelName;
    }

    public String getRepName() {
        return this.repName;
    }

    public double getPPMDev(){
        return this.ppmDev;
    }



}
