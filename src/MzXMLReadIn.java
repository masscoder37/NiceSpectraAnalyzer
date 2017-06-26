import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

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
    public static MySpectrum mzXMLToMySpectrum(MzXMLFile completeMzXML, String scanNumberIn) throws JMzReaderException {
        //MySpectrum requires PeakList, scan number as int and scan header
        //Peak requires: mass, intensity, scan number affiliation; charge is optional
        Spectrum currentSpectrum = completeMzXML.getSpectrumById(scanNumberIn);
        //scan Header shows precursor M/z and charge of precursor
        String scanHeader = "";
        try {
            scanHeader += fourDec.format(currentSpectrum.getPrecursorMZ()) + " " + currentSpectrum.getPrecursorCharge();
        }
        catch (IllegalArgumentException e){
            scanHeader = "NA";
        }
        scanHeader+= "+";
        //scan Number is set
        int scanNumber = Integer.parseInt(scanNumberIn);
        //get the peakList for creation of the Peak Objects
        ArrayList<Peak> peakList = new ArrayList<>();
        Map<Double, Double> mzXMLPeakList = currentSpectrum.getPeakList();
        for (Double mass : mzXMLPeakList.keySet()){
            Double intensity = mzXMLPeakList.get(mass);
            peakList.add(new Peak(mass, intensity, scanNumber));
        }
        MySpectrum spectrumOut = new MySpectrum(peakList, scanNumber, scanHeader);

        return spectrumOut;
    }
}
