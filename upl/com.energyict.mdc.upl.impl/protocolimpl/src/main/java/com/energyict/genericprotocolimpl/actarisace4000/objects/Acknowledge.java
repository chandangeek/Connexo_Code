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
public class Acknowledge extends AbstractActarisObject{

	private int trackingID;
	private String reqString = null;

	/**
	 *
	 */
	public Acknowledge() {
		this(null);
	}

	public Acknowledge(ObjectFactory objectFactory){
		super(objectFactory);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	protected int getTrackingID() {
		return trackingID;
	}

	protected void setTrackingID(int trackingID) {
		this.trackingID = trackingID;
	}

	protected String getReqString() {
		return reqString;
	}

	private void setReqString(String reqString){
		this.reqString = reqString;
	}

	protected void setElement(Element element) {
		// TODO Auto-generated method stub

	}

	public void prepareXML() {
		Document doc = createDomDocument();

		Element root = doc.createElement(XMLTags.MPULL);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.METERDATA);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.SERIALNUMBER);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
		md.appendChild(s);

		Element ak = doc.createElement(XMLTags.ACKNOWLEDGE);
		ak.setTextContent(Integer.toHexString(getTrackingID()));
		md.appendChild(ak);

		String msg = convertDocumentToString(doc);

		setReqString(msg.substring(msg.indexOf("?>")+2));

	}
}
