//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.08 at 06:10:02 PM CET 
//


package edu.harvard.i2b2.pm.datavo.i2b2versionmessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for response_messageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="response_messageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message_header" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="message_body">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="i2b2_message_version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "response_messageType", propOrder = {
    "messageHeader",
    "messageBody"
})
public class ResponseMessageType {

    @XmlElement(name = "message_header", required = true)
    protected String messageHeader;
    @XmlElement(name = "message_body", required = true)
    protected ResponseMessageType.MessageBody messageBody;

    /**
     * Gets the value of the messageHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageHeader() {
        return messageHeader;
    }

    /**
     * Sets the value of the messageHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageHeader(String value) {
        this.messageHeader = value;
    }

    /**
     * Gets the value of the messageBody property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseMessageType.MessageBody }
     *     
     */
    public ResponseMessageType.MessageBody getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the value of the messageBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseMessageType.MessageBody }
     *     
     */
    public void setMessageBody(ResponseMessageType.MessageBody value) {
        this.messageBody = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="i2b2_message_version" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "i2B2MessageVersion"
    })
    public static class MessageBody {

        @XmlElement(name = "i2b2_message_version", required = true)
        protected String i2B2MessageVersion;

        /**
         * Gets the value of the i2B2MessageVersion property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getI2B2MessageVersion() {
            return i2B2MessageVersion;
        }

        /**
         * Sets the value of the i2B2MessageVersion property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setI2B2MessageVersion(String value) {
            this.i2B2MessageVersion = value;
        }

    }

}
