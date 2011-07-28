package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.cbo.ApplicationException;
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
 *
 */
abstract public class AbstractActarisObject {
	
	private ObjectFactory objectFactory;
	private String serialNumber;
    private int trackingID;

    /**
     * Parses the received content
     * @param element the received content
     * @throws Exception when SAX error, ...
     */
    abstract protected void parse(Element element) throws Exception;

    /**
     * This generates the necessary meterXML to send the request to the meter
     * @return the meterXML string
     */
    abstract protected String prepareXML();

	/**
	 * @param of
	 */
	public AbstractActarisObject(ObjectFactory of) {
		this.objectFactory = of;
		this.serialNumber = getObjectFactory().getAce4000().getPushedSerialNumber();
	}

    /**
     * Gets the tracking ID of this object
     * @return trackingID the tracking ID
     */
    protected int getTrackingID() {
        return trackingID;
    }

    /**
     * Sets the tracking ID of this object, the tracking ID can be retrieved from the Actaris class
     * @param trackingID the tracking ID
     */
    protected void setTrackingID(int trackingID) {
        this.trackingID = trackingID;
    }

    /**
	 * Sends the actual request with the object request string
	 * @throws IOException 
	 */
	public void request() throws IOException{
		getObjectFactory().getAce4000().getOutputStream().write(prepareXML().getBytes());
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public String getSerialNumber(){
		return serialNumber;
	}
	
	/**
	 * Creates a standard document to start making an XML DOM object
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
     * @param doc
     * @return converted string
     */
	public static String convertDocumentToString( Document doc ) {
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
	 * @param decoded, the decoded byte array
	 * @param offset, where to start summing
	 * @param length, the number of bytes to sum
	 * @return
	 */
	protected int getNumberFromB64(byte[] decoded, int offset, int length){
		int sum = 0;
		int shift = 0;
		for(int i = length-1; i >= 0; i--){
			sum += (decoded[offset+i]&0xFF)<<(8*shift++);
		}
		return sum;
	}

    /**
     * Converts a given date to the meter time zone and returns the hex representation
     * Also pads the length so its always 8 characters long
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
}