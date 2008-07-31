/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.ProtocolUtils;
/**
 * @author gna
 *
 */
public class LoadProfiel extends AbstractActarisObject {
	
	private String reqString = null;
	private int trackingID;

	/**
	 * @param of
	 */
	public LoadProfiel(ObjectFactory of) {
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
	
	protected void requestLoadProfile(Date from){
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getDeviceSerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.tracker);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);
		
		Element lp = doc.createElement(XMLTags.reqLP);
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int s = 1212105600;
		byte[] data = new byte[50];
		String str = Integer.toHexString(s);
		System.out.println(str);
	}

}
