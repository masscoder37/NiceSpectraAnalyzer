import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Michael Stadlmeier on 6/26/2017.
 */

//reads in MzXML spectra via the jmzreader and mzXML-parser java classes
    //provided by Griss J, Reisinger F, Hermjakob H, Vizcaíno JA. jmzReader: A Java parser library to process and visualize multiple text and XML-based mass spectrometry data formats. Proteomics. 2012 Mar;12(6):795-8.
    //converts one read out mzXML spectrum into Peak- and MySpectrum objects
public class MzXMLReadIn {
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");

    //use of this class: get a mzXML spectrum with the specified Index
    //get the peak List as a map
    //parse the peak list into an new MySpectrum were additional parameters are assigned (rel Int, charge State)
    //note: the noise band is only available with MzXML files exported from triceratops
    public static MySpectrum mzXMLToMySpectrum(MzXMLFile completeMzXML, String scanNumberIn) throws JMzReaderException, MzXMLParsingException {
        //MySpectrum requires PeakList, scan number as int and scan header
        //Peak requires: mass, intensity, scan number affiliation; charge is optional
        Spectrum currentSpectrum = completeMzXML.getSpectrumById(scanNumberIn);
        //scan Header shows precursor M/z and charge of precursor
        String scanHeader = "";
        try {
            scanHeader += "MS"+currentSpectrum.getMsLevel()+" of m/z "+ fourDec.format(currentSpectrum.getPrecursorMZ()) + " (z = " + currentSpectrum.getPrecursorCharge()+"+)";
        }
        catch (IllegalArgumentException e){
            scanHeader = "MS1";
        }
        //scan Number is set
        int scanNumber = Integer.parseInt(scanNumberIn);
        //get the peakList for creation of the Peak Objects
        ArrayList<Peak> peakList = new ArrayList<>();
        Map<Double, Double> mzXMLPeakList = currentSpectrum.getPeakList();
        //also get noise information if present
        //if not, null is returned
        Map<Double, Double> mzXMLNoiseList = currentSpectrum.getNoiseList();
        boolean noisePresent = false;
        if (mzXMLNoiseList != null)
            noisePresent = true;


        for (Double mass : mzXMLPeakList.keySet()){
            //distinguish in the loop if noise is present or not
            double intensity = mzXMLPeakList.get(mass);
            if(!noisePresent) {
                peakList.add(new Peak(mass, intensity, scanNumber));
            }
            else {
                double noise;
                try {
                    noise = mzXMLNoiseList.get(mass);
                }
                catch (NullPointerException e){
                    noise = 0;
                }
                peakList.add(new Peak(mass,intensity, noise, scanNumber));
            }
        }

        //information about fragmentation method is good to know by the MySpectrum itself
        //unfortunately, only accessible with Scan instead of Spectrum
        Scan currentScan = completeMzXML.getScanByNum((long) Integer.parseInt(scanNumberIn));
        String fragmentationMethod;
        if (currentScan.getMsLevel() == 1){
            fragmentationMethod = "NA";
        }
        else
        fragmentationMethod = currentScan.getPrecursorMz().get(0).getActivationMethod();

        MySpectrum spectrumOut = new MySpectrum(peakList,noisePresent, scanNumber, scanHeader, fragmentationMethod);

        return spectrumOut;
    }



    public static MySpectrum spectrumConvert(MzXMLSpectrum spectrumIn){
        String scanHeader = "";
        try {
            scanHeader += fourDec.format(spectrumIn.getPrecursorMZ()) + ";" + spectrumIn.getPrecursorCharge();
        }
        catch (IllegalArgumentException e){
            scanHeader = "NA;NA";
        }
        scanHeader+= "+";

        int scanNumber = Integer.parseInt(spectrumIn.getId());


        ArrayList<Peak> peakList = new ArrayList<>();
        Map<Double, Double> mzXMLPeakList = spectrumIn.getPeakList();
        for (Double mass : mzXMLPeakList.keySet()){
            Double intensity = mzXMLPeakList.get(mass);
            peakList.add(new Peak(mass, intensity, scanNumber));
        }
        //TODO: fragMethod is not properly implemented
        String fragMethod = "NA";
        MySpectrum spectrumOut = new MySpectrum(peakList, scanNumber, scanHeader, fragMethod);

        return spectrumOut;
    }

}
