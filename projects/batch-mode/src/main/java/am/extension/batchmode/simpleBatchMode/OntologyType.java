//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.11.07 at 02:34:55 PM CST 
//


package am.extension.batchmode.simpleBatchMode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ontologyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ontologyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceOntology" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="targetOntology" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="matcherRegistryEntry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outputAlignmentFile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ontologyType", namespace = "http://www.example.org/SimpleBatchMode", propOrder = {
    "sourceOntology",
    "targetOntology",
    "matcherRegistryEntry",
    "outputAlignmentFile"
})
public class OntologyType {

    @XmlElement(namespace = "http://www.example.org/SimpleBatchMode", required = true)
    protected String sourceOntology;
    @XmlElement(namespace = "http://www.example.org/SimpleBatchMode", required = true)
    protected String targetOntology;
    @XmlElement(namespace = "http://www.example.org/SimpleBatchMode")
    protected String matcherRegistryEntry;
    @XmlElement(namespace = "http://www.example.org/SimpleBatchMode", required = true)
    protected String outputAlignmentFile;

    /**
     * Gets the value of the sourceOntology property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceOntology() {
        return sourceOntology;
    }

    /**
     * Sets the value of the sourceOntology property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceOntology(String value) {
        this.sourceOntology = value;
    }

    /**
     * Gets the value of the targetOntology property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetOntology() {
        return targetOntology;
    }

    /**
     * Sets the value of the targetOntology property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetOntology(String value) {
        this.targetOntology = value;
    }

    /**
     * Gets the value of the matcherRegistryEntry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatcherRegistryEntry() {
        return matcherRegistryEntry;
    }

    /**
     * Sets the value of the matcherRegistryEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatcherRegistryEntry(String value) {
        this.matcherRegistryEntry = value;
    }

    /**
     * Gets the value of the outputAlignmentFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputAlignmentFile() {
        return outputAlignmentFile;
    }

    /**
     * Sets the value of the outputAlignmentFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputAlignmentFile(String value) {
        this.outputAlignmentFile = value;
    }

}
