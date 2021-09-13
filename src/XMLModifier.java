import MymzXMLScheme.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


//class to modify XML files, especially mzXML files from allosaurus
public class XMLModifier {

    //helper container to deal with finding the corresponding MS2 scan
    private static class ScanContainer{
        private int scanNumber;
        private int msLevel;
        private String scanDescription;
        private double precursorMZ;

        private ScanContainer(int scanNumberIn, int msLevelIn, String descriptionIn, double precursorMZIn){
        this.scanNumber = scanNumberIn;
        this.msLevel = msLevelIn;
        this.scanDescription = descriptionIn;
        this.precursorMZ = precursorMZIn;
        }

        public int getScanNumber() {
            return scanNumber;
        }

        public int getMsLevel() {
            return msLevel;
        }

        public String getScanDescription() {
            return scanDescription;
        }

        public double getPrecursorMZ() {
            return precursorMZ;
        }
    }

    //class to match a slow, higher-resolution MS2 scan to the corresponding fast MS2-scan
    public static void mzXMLPrecursorMatcher(String filePathIn) throws JAXBException {
        //setup of the xml File and opening with the Unmarshaller
        File xmlFile = new File(filePathIn);
        JAXBContext jc = JAXBContext.newInstance(MzXMLSchema.class);
        Unmarshaller us = jc.createUnmarshaller();
        MzXMLSchema file = (MzXMLSchema) us.unmarshal(xmlFile);
        //File is open, fields are accessible now
        int numberOfScans = Integer.valueOf(file.getMsRun().getScanCount());
        //Progressbar to show progress, not required
        ProgressBar.progressBar("Scanning .xml File", numberOfScans);
        //prepare HashMap, key is ScanNumber, value is the corresponding scan information (scanContainer object)
        HashMap<Integer, ScanContainer> orderedMap = new HashMap<>();
        //loop through all scans
        for(int i = 0; i < numberOfScans; i++){
            //get the necessary info and put an entry into the orderedMap
            int scanNumber = i + 1;
            //i is offset to scannumber by 1
            MzXMLSchema.MsRun.Scan currentScan = file.getMsRun().getScan().get(i);
            int msLevel = (int) currentScan.getMsLevel();
            String scanDescription = currentScan.getDescription();
            double precMZ = 0;
            try {
                 precMZ = (double) currentScan.getPrecursorMz().getValue();
            }
            catch (NullPointerException e){
            }
            orderedMap.put(scanNumber, new ScanContainer(scanNumber, msLevel, scanDescription, precMZ));
            //advance progress bar
            ProgressBar.progress();
        }
        //now, loop through the orderedMap dictionary to get to the custom scans we are interested in
        for(Map.Entry<Integer, ScanContainer> current : orderedMap.entrySet()){
            String desc = current.getValue().getScanDescription();
            if(desc.equals("API Event!")){
                double currentPrecursor = current.getValue().getPrecursorMZ();
                int currentScanNumber = current.getValue().getScanNumber();
                ArrayList<ScanContainer> previousScanList = new ArrayList<>();
                for(int i = 1; i < 25; i++){
                    try{
                        previousScanList.add(orderedMap.get(currentScanNumber-i));
                    }
                    catch (IllegalArgumentException e)
                    {
                        continue;
                    }
                }
                for(ScanContainer sc : previousScanList){
                    if(DeviationCalc.ppmMatch(currentPrecursor, sc.getPrecursorMZ(), 10)){
                        file.getMsRun().getScan().get(currentScanNumber-1).getPrecursorMz().setPrecursorScanNum((short) sc.getScanNumber());
                        break;
                    }
                }
            }
        }

            ProgressBar.close();
            System.out.println("Marshalling of file");
        String output = filePathIn.replace(".mzXML", "_mod.mzXML");
        File outputFile = new File(output);
        Marshaller ms = jc.createMarshaller();
        ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ms.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1 http://sashimi.sourceforge.net/schema_revision/mzXML_3.1/mzXML_idx_3.1.xsd");
        //ms.setProperty(Marshaller.JAXB_FRAGMENT, true);
        ms.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
        ms.marshal(file, outputFile);

    }


    public static void mzXMLModifierTest(String filePathIn) throws JAXBException {
        File xmlFile = new File(filePathIn);

        JAXBContext jc = JAXBContext.newInstance(MzXMLSchema.class);
        Unmarshaller us = jc.createUnmarshaller();
        MzXMLSchema file = (MzXMLSchema) us.unmarshal(xmlFile);
        int numberOfScans = Integer.valueOf(file.getMsRun().getScanCount());
        ProgressBar.progressBar("Modifying .xml File", numberOfScans);
        for(int i = 0; i < numberOfScans; i++){
           MzXMLSchema.MsRun.Scan current = file.getMsRun().getScan().get(i);
           String desc = current.getDescription();
            System.out.println(desc);
           MzXMLSchema.MsRun.Scan.PrecursorMz prec = null;
           prec = current.getPrecursorMz();
           if(prec == null)
               continue;
           prec.setPrecursorScanNum(prec.getPrecursorScanNum());

           ProgressBar.progress();
        }

        ProgressBar.close();
        System.out.println("Marshalling of file");
        String output = filePathIn.replace(".mzXML", "_mod.mzXML");
        File outputFile = new File(output);
        Marshaller ms = jc.createMarshaller();
        ms.marshal(file, outputFile);

    }



}
