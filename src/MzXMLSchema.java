//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2021.09.08 um 12:41:56 AM EDTDiese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2021.09.08 um 12:41:56 AM EDT
//



import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.xml.util.AnySimpleTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="msRun"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="parentFile"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                           &lt;attribute name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="fileType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="fileSha1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="dataProcessing"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="software"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;simpleContent&gt;
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                                     &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                   &lt;/extension&gt;
 *                                 &lt;/simpleContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="scan" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="precursorMz" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;simpleContent&gt;
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;float"&gt;
 *                                     &lt;attribute name="precursorScanNum" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                                     &lt;attribute name="precursorIntensity" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *                                     &lt;attribute name="precursorCharge" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                     &lt;attribute name="activationMethod" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="isolationWidth" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                                     &lt;attribute name="isolationMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                                   &lt;/extension&gt;
 *                                 &lt;/simpleContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="peaks"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;simpleContent&gt;
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                                     &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                     &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                   &lt;/extension&gt;
 *                                 &lt;/simpleContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="labelData"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;simpleContent&gt;
 *                                   &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                                     &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                     &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                     &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                                   &lt;/extension&gt;
 *                                 &lt;/simpleContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="num" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                           &lt;attribute name="msLevel" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                           &lt;attribute name="scanEvent" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                           &lt;attribute name="masterIndex" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                           &lt;attribute name="peaksCount" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                           &lt;attribute name="ionInjectionTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="elapsedScanTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="polarity" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                           &lt;attribute name="scanType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="filterLine" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="retentionTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
 *                           &lt;attribute name="startMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="endMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="lowMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="highMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="basePeakMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="basePeakIntensity" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="totIonCurrent" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *                           &lt;attribute name="faimsState" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="compensationVoltage" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                           &lt;attribute name="collisionEnergy" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="scanCount" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                 &lt;attribute name="startTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
 *                 &lt;attribute name="endTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="index"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="offset" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;int"&gt;
 *                           &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="indexOffset" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "msRun",
        "index",
        "indexOffset"
})
@XmlRootElement(name = "mzXML")
public class MzXMLSchema {

    @XmlElement(required = true)
    protected MzXMLSchema.MsRun msRun;
    @XmlElement(required = true)
    protected MzXMLSchema.Index index;
    protected int indexOffset;

    /**
     * Ruft den Wert der msRun-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link MzXMLSchema.MsRun }
     *
     */
    public MzXMLSchema.MsRun getMsRun() {
        return msRun;
    }

    /**
     * Legt den Wert der msRun-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link MzXMLSchema.MsRun }
     *
     */
    public void setMsRun(MzXMLSchema.MsRun value) {
        this.msRun = value;
    }

    /**
     * Ruft den Wert der index-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link MzXMLSchema.Index }
     *
     */
    public MzXMLSchema.Index getIndex() {
        return index;
    }

    /**
     * Legt den Wert der index-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link MzXMLSchema.Index }
     *
     */
    public void setIndex(MzXMLSchema.Index value) {
        this.index = value;
    }

    /**
     * Ruft den Wert der indexOffset-Eigenschaft ab.
     *
     */
    public int getIndexOffset() {
        return indexOffset;
    }

    /**
     * Legt den Wert der indexOffset-Eigenschaft fest.
     *
     */
    public void setIndexOffset(int value) {
        this.indexOffset = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     *
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="offset" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;int"&gt;
     *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "offset"
    })
    public static class Index {

        protected List<MzXMLSchema.Index.Offset> offset;
        @XmlAttribute(name = "name")
        protected String name;

        /**
         * Gets the value of the offset property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the offset property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOffset().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MzXMLSchema.Index.Offset }
         *
         *
         */
        public List<MzXMLSchema.Index.Offset> getOffset() {
            if (offset == null) {
                offset = new ArrayList<MzXMLSchema.Index.Offset>();
            }
            return this.offset;
        }

        /**
         * Ruft den Wert der name-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getName() {
            return name;
        }

        /**
         * Legt den Wert der name-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setName(String value) {
            this.name = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         *
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;int"&gt;
         *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "value"
        })
        public static class Offset {

            @XmlValue
            protected int value;
            @XmlAttribute(name = "id")
            protected Short id;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             *
             */
            public int getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             *
             */
            public void setValue(int value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Short }
             *
             */
            public Short getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Short }
             *
             */
            public void setId(Short value) {
                this.id = value;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     *
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="parentFile"&gt;
     *           &lt;complexType&gt;
     *             &lt;simpleContent&gt;
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                 &lt;attribute name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="fileType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="fileSha1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *               &lt;/extension&gt;
     *             &lt;/simpleContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="dataProcessing"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="software"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;simpleContent&gt;
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                           &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                         &lt;/extension&gt;
     *                       &lt;/simpleContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="scan" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="precursorMz" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;simpleContent&gt;
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;float"&gt;
     *                           &lt;attribute name="precursorScanNum" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
     *                           &lt;attribute name="precursorIntensity" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
     *                           &lt;attribute name="precursorCharge" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                           &lt;attribute name="activationMethod" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="isolationWidth" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                           &lt;attribute name="isolationMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                         &lt;/extension&gt;
     *                       &lt;/simpleContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="peaks"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;simpleContent&gt;
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                           &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                           &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                         &lt;/extension&gt;
     *                       &lt;/simpleContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="labelData"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;simpleContent&gt;
     *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
     *                           &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                           &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                           &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                         &lt;/extension&gt;
     *                       &lt;/simpleContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="num" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
     *                 &lt;attribute name="msLevel" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                 &lt;attribute name="scanEvent" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                 &lt;attribute name="masterIndex" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                 &lt;attribute name="peaksCount" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
     *                 &lt;attribute name="ionInjectionTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="elapsedScanTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="polarity" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                 &lt;attribute name="scanType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="filterLine" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="retentionTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
     *                 &lt;attribute name="startMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="endMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="lowMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="highMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="basePeakMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="basePeakIntensity" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="totIonCurrent" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
     *                 &lt;attribute name="faimsState" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="compensationVoltage" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *                 &lt;attribute name="collisionEnergy" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="scanCount" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
     *       &lt;attribute name="startTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
     *       &lt;attribute name="endTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "parentFile",
            "dataProcessing",
            "scan"
    })
    public static class MsRun {

        @XmlElement(required = true)
        protected MzXMLSchema.MsRun.ParentFile parentFile;
        @XmlElement(required = true)
        protected MzXMLSchema.MsRun.DataProcessing dataProcessing;
        protected List<MzXMLSchema.MsRun.Scan> scan;
        @XmlAttribute(name = "scanCount")
        protected Short scanCount;
        @XmlAttribute(name = "startTime")
        protected Duration startTime;
        @XmlAttribute(name = "endTime")
        protected Duration endTime;

        /**
         * Ruft den Wert der parentFile-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link MzXMLSchema.MsRun.ParentFile }
         *
         */
        public MzXMLSchema.MsRun.ParentFile getParentFile() {
            return parentFile;
        }

        /**
         * Legt den Wert der parentFile-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link MzXMLSchema.MsRun.ParentFile }
         *
         */
        public void setParentFile(MzXMLSchema.MsRun.ParentFile value) {
            this.parentFile = value;
        }

        /**
         * Ruft den Wert der dataProcessing-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link MzXMLSchema.MsRun.DataProcessing }
         *
         */
        public MzXMLSchema.MsRun.DataProcessing getDataProcessing() {
            return dataProcessing;
        }

        /**
         * Legt den Wert der dataProcessing-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link MzXMLSchema.MsRun.DataProcessing }
         *
         */
        public void setDataProcessing(MzXMLSchema.MsRun.DataProcessing value) {
            this.dataProcessing = value;
        }

        /**
         * Gets the value of the scan property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the scan property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getScan().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MzXMLSchema.MsRun.Scan }
         *
         *
         */
        public List<MzXMLSchema.MsRun.Scan> getScan() {
            if (scan == null) {
                scan = new ArrayList<MzXMLSchema.MsRun.Scan>();
            }
            return this.scan;
        }

        /**
         * Ruft den Wert der scanCount-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link Short }
         *
         */
        public Short getScanCount() {
            return scanCount;
        }

        /**
         * Legt den Wert der scanCount-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link Short }
         *
         */
        public void setScanCount(Short value) {
            this.scanCount = value;
        }

        /**
         * Ruft den Wert der startTime-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link Duration }
         *
         */
        public Duration getStartTime() {
            return startTime;
        }

        /**
         * Legt den Wert der startTime-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link Duration }
         *
         */
        public void setStartTime(Duration value) {
            this.startTime = value;
        }

        /**
         * Ruft den Wert der endTime-Eigenschaft ab.
         *
         * @return
         *     possible object is
         *     {@link Duration }
         *
         */
        public Duration getEndTime() {
            return endTime;
        }

        /**
         * Legt den Wert der endTime-Eigenschaft fest.
         *
         * @param value
         *     allowed object is
         *     {@link Duration }
         *
         */
        public void setEndTime(Duration value) {
            this.endTime = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         *
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="software"&gt;
         *           &lt;complexType&gt;
         *             &lt;simpleContent&gt;
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *                 &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *               &lt;/extension&gt;
         *             &lt;/simpleContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "software"
        })
        public static class DataProcessing {

            @XmlElement(required = true)
            protected MzXMLSchema.MsRun.DataProcessing.Software software;

            /**
             * Ruft den Wert der software-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link MzXMLSchema.MsRun.DataProcessing.Software }
             *
             */
            public MzXMLSchema.MsRun.DataProcessing.Software getSoftware() {
                return software;
            }

            /**
             * Legt den Wert der software-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link MzXMLSchema.MsRun.DataProcessing.Software }
             *
             */
            public void setSoftware(MzXMLSchema.MsRun.DataProcessing.Software value) {
                this.software = value;
            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             *
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType&gt;
             *   &lt;simpleContent&gt;
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
             *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *     &lt;/extension&gt;
             *   &lt;/simpleContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "value"
            })
            public static class Software {

                @XmlValue
                protected String value;
                @XmlAttribute(name = "type")
                protected String type;
                @XmlAttribute(name = "name")
                protected String name;
                @XmlAttribute(name = "version")
                protected Byte version;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der type-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getType() {
                    return type;
                }

                /**
                 * Legt den Wert der type-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setType(String value) {
                    this.type = value;
                }

                /**
                 * Ruft den Wert der name-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getName() {
                    return name;
                }

                /**
                 * Legt den Wert der name-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setName(String value) {
                    this.name = value;
                }

                /**
                 * Ruft den Wert der version-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getVersion() {
                    return version;
                }

                /**
                 * Legt den Wert der version-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setVersion(Byte value) {
                    this.version = value;
                }

            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         *
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *       &lt;attribute name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="fileType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="fileSha1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "value"
        })
        public static class ParentFile {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "fileName")
            protected String fileName;
            @XmlAttribute(name = "fileType")
            protected String fileType;
            @XmlAttribute(name = "fileSha1")
            protected String fileSha1;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der fileName-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getFileName() {
                return fileName;
            }

            /**
             * Legt den Wert der fileName-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setFileName(String value) {
                this.fileName = value;
            }

            /**
             * Ruft den Wert der fileType-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getFileType() {
                return fileType;
            }

            /**
             * Legt den Wert der fileType-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setFileType(String value) {
                this.fileType = value;
            }

            /**
             * Ruft den Wert der fileSha1-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getFileSha1() {
                return fileSha1;
            }

            /**
             * Legt den Wert der fileSha1-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setFileSha1(String value) {
                this.fileSha1 = value;
            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         *
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="precursorMz" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;simpleContent&gt;
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;float"&gt;
         *                 &lt;attribute name="precursorScanNum" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
         *                 &lt;attribute name="precursorIntensity" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
         *                 &lt;attribute name="precursorCharge" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *                 &lt;attribute name="activationMethod" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="isolationWidth" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *                 &lt;attribute name="isolationMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *               &lt;/extension&gt;
         *             &lt;/simpleContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="peaks"&gt;
         *           &lt;complexType&gt;
         *             &lt;simpleContent&gt;
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *                 &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *                 &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *               &lt;/extension&gt;
         *             &lt;/simpleContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="labelData"&gt;
         *           &lt;complexType&gt;
         *             &lt;simpleContent&gt;
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
         *                 &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *                 &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *                 &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *               &lt;/extension&gt;
         *             &lt;/simpleContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="num" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
         *       &lt;attribute name="msLevel" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *       &lt;attribute name="scanEvent" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *       &lt;attribute name="masterIndex" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *       &lt;attribute name="peaksCount" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
         *       &lt;attribute name="ionInjectionTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="elapsedScanTime" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="polarity" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *       &lt;attribute name="scanType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="filterLine" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="retentionTime" type="{http://www.w3.org/2001/XMLSchema}duration" /&gt;
         *       &lt;attribute name="startMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="endMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="lowMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="highMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="basePeakMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="basePeakIntensity" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="totIonCurrent" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
         *       &lt;attribute name="faimsState" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="compensationVoltage" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *       &lt;attribute name="collisionEnergy" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "precursorMz",
                "peaks",
                "labelData"
        })
        public static class Scan {

            protected MzXMLSchema.MsRun.Scan.PrecursorMz precursorMz;
            @XmlElement(required = true)
            protected MzXMLSchema.MsRun.Scan.Peaks peaks;
            @XmlElement(required = true)
            protected MzXMLSchema.MsRun.Scan.LabelData labelData;
            @XmlAttribute(name = "num")
            protected Short num;
            @XmlAttribute(name = "msLevel")
            protected Byte msLevel;
            @XmlAttribute(name = "scanEvent")
            protected Byte scanEvent;
            @XmlAttribute(name = "masterIndex")
            protected Byte masterIndex;
            @XmlAttribute(name = "peaksCount")
            protected Short peaksCount;
            @XmlAttribute(name = "ionInjectionTime")
            protected Float ionInjectionTime;
            @XmlAttribute(name = "elapsedScanTime")
            protected Float elapsedScanTime;
            @XmlAttribute(name = "polarity")
            protected Byte polarity;
            @XmlAttribute(name = "scanType")
            protected String scanType;
            @XmlAttribute(name = "filterLine")
            protected String filterLine;
            @XmlAttribute(name = "description")
            protected String description;
            @XmlAttribute(name = "retentionTime")
            protected Duration retentionTime;
            @XmlAttribute(name = "startMz")
            protected Float startMz;
            @XmlAttribute(name = "endMz")
            protected Float endMz;
            @XmlAttribute(name = "lowMz")
            protected Float lowMz;
            @XmlAttribute(name = "highMz")
            protected Float highMz;
            @XmlAttribute(name = "basePeakMz")
            protected Float basePeakMz;
            @XmlAttribute(name = "basePeakIntensity")
            protected Float basePeakIntensity;
            @XmlAttribute(name = "totIonCurrent")
            protected Float totIonCurrent;
            @XmlAttribute(name = "faimsState")
            protected String faimsState;
            @XmlAttribute(name = "compensationVoltage")
            protected Byte compensationVoltage;
            @XmlAttribute(name = "collisionEnergy")
            protected Byte collisionEnergy;

            /**
             * Ruft den Wert der precursorMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link MzXMLSchema.MsRun.Scan.PrecursorMz }
             *
             */
            public MzXMLSchema.MsRun.Scan.PrecursorMz getPrecursorMz() {
                return precursorMz;
            }

            /**
             * Legt den Wert der precursorMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link MzXMLSchema.MsRun.Scan.PrecursorMz }
             *
             */
            public void setPrecursorMz(MzXMLSchema.MsRun.Scan.PrecursorMz value) {
                this.precursorMz = value;
            }

            /**
             * Ruft den Wert der peaks-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link MzXMLSchema.MsRun.Scan.Peaks }
             *
             */
            public MzXMLSchema.MsRun.Scan.Peaks getPeaks() {
                return peaks;
            }

            /**
             * Legt den Wert der peaks-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link MzXMLSchema.MsRun.Scan.Peaks }
             *
             */
            public void setPeaks(MzXMLSchema.MsRun.Scan.Peaks value) {
                this.peaks = value;
            }

            /**
             * Ruft den Wert der labelData-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link MzXMLSchema.MsRun.Scan.LabelData }
             *
             */
            public MzXMLSchema.MsRun.Scan.LabelData getLabelData() {
                return labelData;
            }

            /**
             * Legt den Wert der labelData-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link MzXMLSchema.MsRun.Scan.LabelData }
             *
             */
            public void setLabelData(MzXMLSchema.MsRun.Scan.LabelData value) {
                this.labelData = value;
            }

            /**
             * Ruft den Wert der num-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Short }
             *
             */
            public Short getNum() {
                return num;
            }

            /**
             * Legt den Wert der num-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Short }
             *
             */
            public void setNum(Short value) {
                this.num = value;
            }

            /**
             * Ruft den Wert der msLevel-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getMsLevel() {
                return msLevel;
            }

            /**
             * Legt den Wert der msLevel-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setMsLevel(Byte value) {
                this.msLevel = value;
            }

            /**
             * Ruft den Wert der scanEvent-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getScanEvent() {
                return scanEvent;
            }

            /**
             * Legt den Wert der scanEvent-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setScanEvent(Byte value) {
                this.scanEvent = value;
            }

            /**
             * Ruft den Wert der masterIndex-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getMasterIndex() {
                return masterIndex;
            }

            /**
             * Legt den Wert der masterIndex-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setMasterIndex(Byte value) {
                this.masterIndex = value;
            }

            /**
             * Ruft den Wert der peaksCount-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Short }
             *
             */
            public Short getPeaksCount() {
                return peaksCount;
            }

            /**
             * Legt den Wert der peaksCount-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Short }
             *
             */
            public void setPeaksCount(Short value) {
                this.peaksCount = value;
            }

            /**
             * Ruft den Wert der ionInjectionTime-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getIonInjectionTime() {
                return ionInjectionTime;
            }

            /**
             * Legt den Wert der ionInjectionTime-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setIonInjectionTime(Float value) {
                this.ionInjectionTime = value;
            }

            /**
             * Ruft den Wert der elapsedScanTime-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getElapsedScanTime() {
                return elapsedScanTime;
            }

            /**
             * Legt den Wert der elapsedScanTime-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setElapsedScanTime(Float value) {
                this.elapsedScanTime = value;
            }

            /**
             * Ruft den Wert der polarity-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getPolarity() {
                return polarity;
            }

            /**
             * Legt den Wert der polarity-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setPolarity(Byte value) {
                this.polarity = value;
            }

            /**
             * Ruft den Wert der scanType-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getScanType() {
                return scanType;
            }

            /**
             * Legt den Wert der scanType-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setScanType(String value) {
                this.scanType = value;
            }

            /**
             * Ruft den Wert der filterLine-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getFilterLine() {
                return filterLine;
            }

            /**
             * Legt den Wert der filterLine-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setFilterLine(String value) {
                this.filterLine = value;
            }

            /**
             * Ruft den Wert der description-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getDescription() {
                return description;
            }

            /**
             * Legt den Wert der description-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setDescription(String value) {
                this.description = value;
            }

            /**
             * Ruft den Wert der retentionTime-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Duration }
             *
             */
            public Duration getRetentionTime() {
                return retentionTime;
            }

            /**
             * Legt den Wert der retentionTime-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Duration }
             *
             */
            public void setRetentionTime(Duration value) {
                this.retentionTime = value;
            }

            /**
             * Ruft den Wert der startMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getStartMz() {
                return startMz;
            }

            /**
             * Legt den Wert der startMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setStartMz(Float value) {
                this.startMz = value;
            }

            /**
             * Ruft den Wert der endMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getEndMz() {
                return endMz;
            }

            /**
             * Legt den Wert der endMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setEndMz(Float value) {
                this.endMz = value;
            }

            /**
             * Ruft den Wert der lowMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getLowMz() {
                return lowMz;
            }

            /**
             * Legt den Wert der lowMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setLowMz(Float value) {
                this.lowMz = value;
            }

            /**
             * Ruft den Wert der highMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getHighMz() {
                return highMz;
            }

            /**
             * Legt den Wert der highMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setHighMz(Float value) {
                this.highMz = value;
            }

            /**
             * Ruft den Wert der basePeakMz-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getBasePeakMz() {
                return basePeakMz;
            }

            /**
             * Legt den Wert der basePeakMz-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setBasePeakMz(Float value) {
                this.basePeakMz = value;
            }

            /**
             * Ruft den Wert der basePeakIntensity-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getBasePeakIntensity() {
                return basePeakIntensity;
            }

            /**
             * Legt den Wert der basePeakIntensity-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setBasePeakIntensity(Float value) {
                this.basePeakIntensity = value;
            }

            /**
             * Ruft den Wert der totIonCurrent-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Float }
             *
             */
            public Float getTotIonCurrent() {
                return totIonCurrent;
            }

            /**
             * Legt den Wert der totIonCurrent-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Float }
             *
             */
            public void setTotIonCurrent(Float value) {
                this.totIonCurrent = value;
            }

            /**
             * Ruft den Wert der faimsState-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getFaimsState() {
                return faimsState;
            }

            /**
             * Legt den Wert der faimsState-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setFaimsState(String value) {
                this.faimsState = value;
            }

            /**
             * Ruft den Wert der compensationVoltage-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getCompensationVoltage() {
                return compensationVoltage;
            }

            /**
             * Legt den Wert der compensationVoltage-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setCompensationVoltage(Byte value) {
                this.compensationVoltage = value;
            }

            /**
             * Ruft den Wert der collisionEnergy-Eigenschaft ab.
             *
             * @return
             *     possible object is
             *     {@link Byte }
             *
             */
            public Byte getCollisionEnergy() {
                return collisionEnergy;
            }

            /**
             * Legt den Wert der collisionEnergy-Eigenschaft fest.
             *
             * @param value
             *     allowed object is
             *     {@link Byte }
             *
             */
            public void setCollisionEnergy(Byte value) {
                this.collisionEnergy = value;
            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             *
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType&gt;
             *   &lt;simpleContent&gt;
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
             *       &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *       &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *     &lt;/extension&gt;
             *   &lt;/simpleContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "value"
            })
            public static class LabelData {

                @XmlValue
                protected String value;
                @XmlAttribute(name = "precision")
                protected Byte precision;
                @XmlAttribute(name = "byteOrder")
                protected String byteOrder;
                @XmlAttribute(name = "contentType")
                protected String contentType;
                @XmlAttribute(name = "compressionType")
                protected String compressionType;
                @XmlAttribute(name = "compressedLen")
                protected Byte compressedLen;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der precision-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getPrecision() {
                    return precision;
                }

                /**
                 * Legt den Wert der precision-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setPrecision(Byte value) {
                    this.precision = value;
                }

                /**
                 * Ruft den Wert der byteOrder-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getByteOrder() {
                    return byteOrder;
                }

                /**
                 * Legt den Wert der byteOrder-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setByteOrder(String value) {
                    this.byteOrder = value;
                }

                /**
                 * Ruft den Wert der contentType-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getContentType() {
                    return contentType;
                }

                /**
                 * Legt den Wert der contentType-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setContentType(String value) {
                    this.contentType = value;
                }

                /**
                 * Ruft den Wert der compressionType-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getCompressionType() {
                    return compressionType;
                }

                /**
                 * Legt den Wert der compressionType-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setCompressionType(String value) {
                    this.compressionType = value;
                }

                /**
                 * Ruft den Wert der compressedLen-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getCompressedLen() {
                    return compressedLen;
                }

                /**
                 * Legt den Wert der compressedLen-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setCompressedLen(Byte value) {
                    this.compressedLen = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             *
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType&gt;
             *   &lt;simpleContent&gt;
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
             *       &lt;attribute name="precision" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *       &lt;attribute name="byteOrder" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="compressionType" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="compressedLen" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *     &lt;/extension&gt;
             *   &lt;/simpleContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "value"
            })
            public static class Peaks {

                @XmlValue
                protected String value;
                @XmlAttribute(name = "precision")
                protected Byte precision;
                @XmlAttribute(name = "byteOrder")
                protected String byteOrder;
                @XmlAttribute(name = "contentType")
                protected String contentType;
                @XmlAttribute(name = "compressionType")
                protected String compressionType;
                @XmlAttribute(name = "compressedLen")
                protected Byte compressedLen;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der precision-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getPrecision() {
                    return precision;
                }

                /**
                 * Legt den Wert der precision-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setPrecision(Byte value) {
                    this.precision = value;
                }

                /**
                 * Ruft den Wert der byteOrder-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getByteOrder() {
                    return byteOrder;
                }

                /**
                 * Legt den Wert der byteOrder-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setByteOrder(String value) {
                    this.byteOrder = value;
                }

                /**
                 * Ruft den Wert der contentType-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getContentType() {
                    return contentType;
                }

                /**
                 * Legt den Wert der contentType-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setContentType(String value) {
                    this.contentType = value;
                }

                /**
                 * Ruft den Wert der compressionType-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getCompressionType() {
                    return compressionType;
                }

                /**
                 * Legt den Wert der compressionType-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setCompressionType(String value) {
                    this.compressionType = value;
                }

                /**
                 * Ruft den Wert der compressedLen-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getCompressedLen() {
                    return compressedLen;
                }

                /**
                 * Legt den Wert der compressedLen-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setCompressedLen(Byte value) {
                    this.compressedLen = value;
                }

            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             *
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             *
             * <pre>
             * &lt;complexType&gt;
             *   &lt;simpleContent&gt;
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;float"&gt;
             *       &lt;attribute name="precursorScanNum" type="{http://www.w3.org/2001/XMLSchema}short" /&gt;
             *       &lt;attribute name="precursorIntensity" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
             *       &lt;attribute name="precursorCharge" type="{http://www.w3.org/2001/XMLSchema}byte" /&gt;
             *       &lt;attribute name="activationMethod" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
             *       &lt;attribute name="isolationWidth" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
             *       &lt;attribute name="isolationMz" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
             *     &lt;/extension&gt;
             *   &lt;/simpleContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             *
             *
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "value"
            })
            public static class PrecursorMz {

                @XmlValue
                protected float value;
                @XmlAttribute(name = "precursorScanNum")
                protected Short precursorScanNum;
                @XmlAttribute(name = "precursorIntensity")
                protected Integer precursorIntensity;
                @XmlAttribute(name = "precursorCharge")
                protected Byte precursorCharge;
                @XmlAttribute(name = "activationMethod")
                protected String activationMethod;
                @XmlAttribute(name = "isolationWidth")
                protected Float isolationWidth;
                @XmlAttribute(name = "isolationMz")
                protected Float isolationMz;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 *
                 */
                public float getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 *
                 */
                public void setValue(float value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der precursorScanNum-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Short }
                 *
                 */
                public Short getPrecursorScanNum() {
                    return precursorScanNum;
                }

                /**
                 * Legt den Wert der precursorScanNum-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Short }
                 *
                 */
                public void setPrecursorScanNum(Short value) {
                    this.precursorScanNum = value;
                }

                /**
                 * Ruft den Wert der precursorIntensity-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Integer }
                 *
                 */
                public Integer getPrecursorIntensity() {
                    return precursorIntensity;
                }

                /**
                 * Legt den Wert der precursorIntensity-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Integer }
                 *
                 */
                public void setPrecursorIntensity(Integer value) {
                    this.precursorIntensity = value;
                }

                /**
                 * Ruft den Wert der precursorCharge-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Byte }
                 *
                 */
                public Byte getPrecursorCharge() {
                    return precursorCharge;
                }

                /**
                 * Legt den Wert der precursorCharge-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Byte }
                 *
                 */
                public void setPrecursorCharge(Byte value) {
                    this.precursorCharge = value;
                }

                /**
                 * Ruft den Wert der activationMethod-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link String }
                 *
                 */
                public String getActivationMethod() {
                    return activationMethod;
                }

                /**
                 * Legt den Wert der activationMethod-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *
                 */
                public void setActivationMethod(String value) {
                    this.activationMethod = value;
                }

                /**
                 * Ruft den Wert der isolationWidth-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Float }
                 *
                 */
                public Float getIsolationWidth() {
                    return isolationWidth;
                }

                /**
                 * Legt den Wert der isolationWidth-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Float }
                 *
                 */
                public void setIsolationWidth(Float value) {
                    this.isolationWidth = value;
                }

                /**
                 * Ruft den Wert der isolationMz-Eigenschaft ab.
                 *
                 * @return
                 *     possible object is
                 *     {@link Float }
                 *
                 */
                public Float getIsolationMz() {
                    return isolationMz;
                }

                /**
                 * Legt den Wert der isolationMz-Eigenschaft fest.
                 *
                 * @param value
                 *     allowed object is
                 *     {@link Float }
                 *
                 */
                public void setIsolationMz(Float value) {
                    this.isolationMz = value;
                }

            }

        }

    }

}
