/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class AutoPushConfig extends AbstractActarisObject {
	
	private String reqString = null;
	private int trackingID;

	/**
	 * @param of
	 */
	public AutoPushConfig(ObjectFactory of) {
		super(of);
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
	
	protected void prepareXML(String enableState, String open, String close, boolean random){
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
		
		Element cf = doc.createElement(XMLTags.configHandling);
		md.appendChild(cf);
		Element ps = doc.createElement(XMLTags.puschSchedule);
		cf.appendChild(ps);
		Element enable = doc.createElement(XMLTags.enableState);
		enable.setTextContent(enableState);
		ps.appendChild(enable);
		Element tmo = doc.createElement(XMLTags.timeOpen);
		tmo.setTextContent(open);
		ps.appendChild(tmo);
		Element tmc = doc.createElement(XMLTags.timeClose);
		tmc.setTextContent(close);
		ps.appendChild(tmc);
		if(random){
			Element rand = doc.createElement(XMLTags.random);
			ps.appendChild(rand);
		}
			
		String msg = convertDocumentToString(doc);
		
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		AutoPushConfig apc = new AutoPushConfig(null);

        DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement(XMLTags.mPull);
			doc.appendChild(root);
			Element md = doc.createElement(XMLTags.meterData);
			root.appendChild(md);
			Element s = doc.createElement(XMLTags.serialNumber);
			s.setTextContent("123456789");
			md.appendChild(s);
			Element t = doc.createElement(XMLTags.tracker);
			t.setTextContent(String.valueOf(1));
			md.appendChild(t);
			
			System.out.println(apc.convertDocumentToString(doc));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
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
	
}
