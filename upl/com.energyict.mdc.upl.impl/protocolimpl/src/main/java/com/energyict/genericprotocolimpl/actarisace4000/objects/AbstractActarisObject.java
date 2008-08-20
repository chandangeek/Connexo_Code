/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;

/**
 * @author gna
 *
 */
abstract public class AbstractActarisObject {
	
	private ObjectFactory objectFactory;
	private String serialNumber;
	
	/**
	 * Gets the tracking ID of this object
	 * @return trackingID
	 */
	abstract protected int getTrackingID();
	/**
	 * Sets the tracking ID of this object, the tracking ID can be retrieved from the Actaris class
	 * @param trackingID
	 */
	abstract protected void setTrackingID(int trackingID);
	/**
	 * Gets the request string to encapsulate in the request message
	 * @return reqString
	 */
	abstract protected String getReqString();
//	/**
//	 * Returns the element from the DatagramPacket.
//	 * Enables you to fill in the individual parameters of the object 
//	 * @param element
//	 */
//	abstract protected void setElement(Element mdElement);

	/**
	 * @param ObjectFactory of
	 */
	public AbstractActarisObject(ObjectFactory of) {
		this.objectFactory = of;
		this.serialNumber = getObjectFactory().getAace().getPushedSerialNumber();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
	
	/**
	 * Sends the actual request with the object request string
	 * @throws IOException 
	 */
	public void request() throws IOException{
//		getObjectFactory().getAace().getPacket().sendMessage(getReqString());
		getObjectFactory().getAace().getOutputStream().write(getReqString().getBytes());
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
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
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
        }
        return null;
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
	 * Checks whether the given Obiscode is configured on the device
	 * @param obisCode
	 * @return true or false
	 */
	protected boolean isAllowed(ObisCode obisCode) {
		Rtu rtu = getObjectFactory().getAace().getMeter();
		Iterator it = rtu.getRtuType().getRtuRegisterSpecs().iterator();
		while(it.hasNext()){
            RtuRegisterSpec spec = (RtuRegisterSpec) it.next();
            ObisCode oc = spec.getObisCode();
            if(oc.equals(obisCode))
            	return true;
		}
		return false;
	}
}
