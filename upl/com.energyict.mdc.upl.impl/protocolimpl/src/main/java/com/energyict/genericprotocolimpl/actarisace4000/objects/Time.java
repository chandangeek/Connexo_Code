/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 *
 */
public class Time extends AbstractActarisObject {
	
	private int DEBUG = 1;
	
	private String reqString = null;
	private int trackingID;
	
	private long meterTime;
	private long receiveTime;
	

	/**
	 * @param of
	 */
	public Time(ObjectFactory of) {
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
	
	/**
	 * Force the Meter time to the System time
	 */
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
		
		Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
		String hexString = Long.toHexString(cal.getTimeInMillis()/1000);
		
		Element ft = doc.createElement(XMLTags.forceTime);
		md.appendChild(ft);
		Element t1 = doc.createElement(XMLTags.time1);
		t1.setTextContent("00000000");
		ft.appendChild(t1);
		Element t2 = doc.createElement(XMLTags.time2);
		t2.setTextContent(hexString);
		ft.appendChild(t2);
		Element t3 = doc.createElement(XMLTags.time3);
		t3.setTextContent(hexString);
		ft.appendChild(t3);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	protected void prepareSyncXML(){
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
		
		Element ft = doc.createElement(XMLTags.forceTime);
		md.appendChild(ft);
		Element t1 = doc.createElement(XMLTags.time1);
		t1.setTextContent(Long.toHexString(getMeterTime()));
		ft.appendChild(t1);
		Element t2 = doc.createElement(XMLTags.time2);
		t2.setTextContent(Long.toHexString(getReceiveTime()/1000));
		ft.appendChild(t2);
		Element t3 = doc.createElement(XMLTags.time3);
		t3.setTextContent(Long.toHexString(ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis()/1000));
		ft.appendChild(t3);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	protected void setElement(Element mdElement) throws IOException{
		setReceiveTime(ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis());
		setMeterTime(Long.valueOf(mdElement.getTextContent(), 16));
		getObjectFactory().sendSyncTime();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
//		System.out.println(cal.getTime());
		String hexString = "48a97d9d";
		Long timeLong = (Long.valueOf(hexString, 16))*1000;
		Calendar cal2 = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
		cal2.setTimeInMillis(timeLong);
		System.out.println(cal2.getTime());
		
		String hex2 = "11bd5b0d434";
		Long long2 = Long.valueOf(hex2, 16);
		System.out.println(new Date(long2));
		
		Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
		String hexString2 = Long.toHexString(cal.getTimeInMillis()/1000);
		Long long3 = Long.valueOf(hexString2, 16);
		System.out.println(new Date(long3*1000));
	}

	protected long getMeterTime() {
		return meterTime;
	}

	protected void setMeterTime(long meterTime) {
		this.meterTime = meterTime;
	}

	protected long getReceiveTime() {
		return receiveTime;
	}

	protected void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

}
