package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author gna
 *
 */
public class Acknowledge extends AbstractActarisObject{

	public Acknowledge(ObjectFactory objectFactory){
		super(objectFactory);
	}

	protected void parse(Element element) {
        //No parsing here
	}

	public String prepareXML() {
		Document doc = createDomDocument();

		Element root = doc.createElement(XMLTags.MPULL);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.METERDATA);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.SERIALNUMBER);
		s.setTextContent(getObjectFactory().getAce4000().getNecessarySerialNumber());
		md.appendChild(s);

		Element ak = doc.createElement(XMLTags.ACKNOWLEDGE);
		ak.setTextContent(Integer.toHexString(getTrackingID()));
		md.appendChild(ak);

		String msg = convertDocumentToString(doc);

		return (msg.substring(msg.indexOf("?>")+2));

	}
}
