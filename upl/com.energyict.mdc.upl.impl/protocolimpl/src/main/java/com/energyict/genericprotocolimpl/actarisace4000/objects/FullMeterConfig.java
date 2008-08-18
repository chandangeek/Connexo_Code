/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class FullMeterConfig extends AbstractActarisObject {
	
	private String reqString = null;
	private int trackingID;

	/**
	 * @param of
	 */
	public FullMeterConfig(ObjectFactory of) {
		super(of);
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getReqString()
	 */
	protected String getReqString() {
		return reqString;
	}
	
	private void setReqString(String reqString){
		this.reqString = reqString;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getTrackingID()
	 */
	protected int getTrackingID() {
		return trackingID;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#setTrackingID(int)
	 */
	protected void setTrackingID(int trackingID) {
		this.trackingID = trackingID;
	}
	
	protected void prepareXML(){
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getPushedSerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.tracker);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);
		
		Element cf = doc.createElement(XMLTags.fullConfig);
		md.appendChild(cf);

		String msg = convertDocumentToString(doc);
		
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}

	protected void setElement(Element element) {
		// TODO Auto-generated method stub
		
	}

}
