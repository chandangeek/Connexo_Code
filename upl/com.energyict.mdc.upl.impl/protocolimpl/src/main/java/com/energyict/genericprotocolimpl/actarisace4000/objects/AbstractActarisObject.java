/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.StringWriter;

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

import com.energyict.cbo.ApplicationException;

/**
 * @author gna
 *
 */
abstract public class AbstractActarisObject {
	
	private ObjectFactory objectFactory;
	
	abstract protected int getTrackingID();
	abstract protected void setTrackingID(int trackingID);
	abstract protected String getReqString();

	/**
	 * @param ObjectFactory of
	 */
	public AbstractActarisObject(ObjectFactory of) {
		this.objectFactory = of;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
	
	public void request(){
		getObjectFactory().getAace().getPacket().sendMessage(getReqString());
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}
	
    public static Document createDomDocument() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
        }
        return null;
    }
    
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

}
