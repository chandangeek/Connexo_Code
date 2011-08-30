package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.cbo.ApplicationException;
import com.energyict.protocol.InvalidPropertyException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

/**
 * @author gna
 */
abstract public class AbstractActarisObject {

    private ObjectFactory objectFactory;
    private String serialNumber;
    protected int reason = 0; //Only used in case of NACK or Reject messages
    private int trackingId = -1;

    /**
     * Parses the received content
     *
     * @param element the received content
     * @throws Exception when SAX error, ...
     */
    abstract protected void parse(Element element) throws Exception;

    /**
     * This generates the necessary meterXML to send the request to the meter
     *
     * @return the meterXML string
     * @throws InvalidPropertyException when a message contains invalid arguments
     */
    abstract protected String prepareXML() throws InvalidPropertyException;

    /**
     * @param of
     */
    public AbstractActarisObject(ObjectFactory of) {
        this.objectFactory = of;
        this.serialNumber = getObjectFactory().getAce4000().getPushedSerialNumber();
    }

    /**
     * Gets the tracking ID of this object. It's incremented by 1 for every new request.
     * It can also be a specified ID, in case of ACK's.
     *
     * @return trackingID the tracking ID
     */
    protected int getTrackingID() {
        if (trackingId == -1) {
            return getObjectFactory().getIncreasedTrackingID();
        }
        return trackingId;
    }

    /**
     * Set the trackingId necessary for the message. Only needs to be set in the case of ACK messages.
     * If left empty, the message uses the default incremented tracking ID.
     *
     * @param trackingId
     */
    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
    }

    /**
     * Sends the actual request with the object request string
     *
     * @throws IOException
     */
    public void request() throws IOException {
        if (getObjectFactory().getAce4000().getOutputStream() == null) {
            return;
        }
        getObjectFactory().getAce4000().getOutputStream().write(prepareXML().getBytes());
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Creates a standard document to start making an XML DOM object
     *
     * @return document
     */
    public static Document createDomDocument() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    /**
     * Converts a given Document to a readable string
     *
     * @param doc
     * @return converted string
     */
    public static String convertDocumentToString(Document doc) {
        try {
            Source domSource = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(domSource, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new ApplicationException("Could not transform current document into String.");
        }
    }

    /**
     * Returns a fully summed integer from a base64 decoded byte array
     *
     * @param decoded, the decoded byte array
     * @param offset,  where to start summing
     * @param length,  the number of bytes to sum
     * @return
     */
    protected int getNumberFromB64(byte[] decoded, int offset, int length) {
        int sum = 0;
        int shift = 0;
        for (int i = length - 1; i >= 0; i--) {
            sum += (decoded[offset + i] & 0xFF) << (8 * shift++);
        }
        return sum;
    }

    /**
     * Converts a given date to the meter time zone and returns the hex representation
     * Also pads the length so its always 8 characters long
     *
     * @param date the timestamp that needs to be converted
     * @return hex representation
     */
    protected String getHexDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        String hex = Long.toHexString(getObjectFactory().getMeterTime(date).getTime() / 1000);
        while (hex.length() < 8) {
            hex = "0" + hex;
        }
        return hex;
    }

    protected boolean isBitSet(int flag, int bitNumber) {
        int bit = 0x01 << bitNumber;
        return (flag & bit) == bit;
    }

    public String getReasonDescription() {
        String description = "";
        if (isBitSet(reason, 0)) {
            description += getDescriptionSeparator(description) + "Cannot synchronise the time as drift too much";
        }
        if (isBitSet(reason, 1)) {
            description += getDescriptionSeparator(description) + "Over the air firmware upgrade failed";
        }
        if (isBitSet(reason, 2)) {
            description += getDescriptionSeparator(description) + "Invalid output setting";
        }
        if (isBitSet(reason, 3)) {
            description += getDescriptionSeparator(description) + "Invalid daily send schedule setting";
        }
        if (isBitSet(reason, 4)) {
            description += getDescriptionSeparator(description) + "Self test failed";
        }
        if (isBitSet(reason, 5)) {
            description += getDescriptionSeparator(description) + "Cannot return requested BD data";
        }
        if (isBitSet(reason, 6)) {
            description += getDescriptionSeparator(description) + "Cannot return requested LP data";
        }
        if (isBitSet(reason, 7)) {
            description += getDescriptionSeparator(description) + "Cannot understand tag";
        }
        if (isBitSet(reason, 8)) {
            description += getDescriptionSeparator(description) + "Invalid DINSO configuration data";
        }
        if (isBitSet(reason, 9)) {
            description += getDescriptionSeparator(description) + "Invalid tariff configuration data";
        }
        if (isBitSet(reason, 10)) {
            description += getDescriptionSeparator(description) + "Configuration not applied";
        }
        if (isBitSet(reason, 11)) {
            description += getDescriptionSeparator(description) + "Configuration partially applied";
        }
        if (isBitSet(reason, 12)) {
            description += getDescriptionSeparator(description) + "Contactor command not allowed in current mode";
        }
        if (isBitSet(reason, 13)) {
            description += getDescriptionSeparator(description) + "Password error";
        }
        if (isBitSet(reason, 14)) {
            description += getDescriptionSeparator(description) + "Consumption limitation configuration error";
        }

        return "".equals(description) ? "Unknown reason" : description;
    }

    private String getDescriptionSeparator(String description) {
        if ("".equals(description)) {
            return "";
        }
        return ", ";
    }

    public boolean isFirmwareFailed() {
        return isBitSet(reason, 1);
    }

    public boolean isConnectCommandFailed() {
        return isBitSet(reason, 12);
    }
}