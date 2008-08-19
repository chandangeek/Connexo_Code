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
public class NetworkSettings extends AbstractActarisObject {
	
	private int trackingID;
	private String reqString = null;

	/**
	 * @param of
	 */
	public NetworkSettings(ObjectFactory of) {
		super(of);
		// TODO Auto-generated constructor stub
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

	protected void prepareXML(String dnsIPAddress, String username, String password, String apn, String port, String ip){
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
		
		Element cf = doc.createElement(XMLTags.configHandling);
		md.appendChild(cf);
		Element ipdef = doc.createElement(XMLTags.systemIPAddress);
		ipdef.setTextContent(ip);
		cf.appendChild(ipdef);
		Element ns = doc.createElement(XMLTags.networkSettings);
		cf.appendChild(ns);
		Element dnsip = doc.createElement(XMLTags.dnsIPAddress);
		dnsip.setTextContent(dnsIPAddress);
		ns.appendChild(dnsip);
		Element gun = doc.createElement(XMLTags.gprsUsername);
		gun.setTextContent(username);
		ns.appendChild(gun);
		Element gpw = doc.createElement(XMLTags.gprsPassword);
		gpw.setTextContent(password);
		ns.appendChild(gpw);
		Element gapn = doc.createElement(XMLTags.gprsAccessPoint);
		gapn.setTextContent(apn);
		ns.appendChild(gapn);
		Element csprt = doc.createElement(XMLTags.systemIPPortnr);
		csprt.setTextContent(port);
		ns.appendChild(csprt);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	protected void setElement(Element element) {
		// TODO Auto-generated method stub
		
	}

}
