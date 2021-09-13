import MymzXMLScheme.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;


//class to modify XML files, especially mzXML files from allosaurus
public class XMLModifier {


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
           prec.setPrecursorScanNum((short)21373);

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
