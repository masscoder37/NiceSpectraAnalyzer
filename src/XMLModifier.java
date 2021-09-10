import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;



//class to modify XML files, especially mzXML files from allosaurus
public class XMLModifier {


    public static void mzXMLModifier(String filePathIn) throws JAXBException {
        File xmlFile = new File(filePathIn);

        JAXBContext jc = JAXBContext.newInstance(MzXMLSchema.class);
        Unmarshaller us = jc.createUnmarshaller();
        MzXMLSchema file = (MzXMLSchema) us.unmarshal(xmlFile);
        int numberOfScans = file.getMsRun().getScanCount();
        for(int i = 0; i < numberOfScans; i++){
           MzXMLSchema.MsRun.Scan current = file.getMsRun().getScan().get(i);
           MzXMLSchema.MsRun.Scan.PrecursorMz prec = null;
           prec = current.getPrecursorMz();
           if(prec == null)
               continue;
           prec.setPrecursorScanNum((short)1337);
        }

        String output = filePathIn.replace(".XML", "_mod.XML");
        File outputFile = new File(output);
        Marshaller ms = jc.createMarshaller();
        ms.marshal(file, outputFile);

    }



}
