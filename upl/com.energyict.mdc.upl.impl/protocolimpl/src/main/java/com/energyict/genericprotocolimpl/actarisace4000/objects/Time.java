/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

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
	
	private int DEBUG = 0;
	
	private String reqString = null;
	private int trackingID;
	
	private long meterTime;
	private long receiveTime;
	private long minDiff = 180000;
	private long maxDiff = 600000;

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
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
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
	
	/**
	 * Sync the meter time to the system time
	 */
	protected void prepareSyncXML(){
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
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
	
	/**
	 * Sends a new time configuration to the meter
	 * @param diff Maximum time difference allowed for clock synchronization in seconds
	 * @param trip Maximum SNTP message trip allowed in seconds
	 * @param retry Number of clock sync retries allowed
	 */
	protected void prepareXMLConfig(int diff, int trip, int retry){
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.tracker);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);
		
		Element cf = doc.createElement(XMLTags.configHandling);
		md.appendChild(cf);
		Element ps = doc.createElement(XMLTags.timeSync);
		cf.appendChild(ps);
		Element tDiff = doc.createElement(XMLTags.diff);
		tDiff.setTextContent(Integer.toString(diff, 16));
		ps.appendChild(tDiff);
		Element tTrip = doc.createElement(XMLTags.trip);
		tTrip.setTextContent(Integer.toString(trip, 16));
		ps.appendChild(tTrip);
		Element tRetry = doc.createElement(XMLTags.retry);
		tRetry.setTextContent(Integer.toString(retry, 16));
		ps.appendChild(tRetry);
			
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	protected void setElement(Element mdElement) throws IOException{
		setReceiveTime(ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT")).getTimeInMillis());
		setMeterTime(Long.valueOf(mdElement.getTextContent(), 16));
		long diff = Math.abs(getMeterTime()*1000 - getReceiveTime()); 
		getObjectFactory().getAace().getLogger().log(Level.INFO, "MeterTime: " + new Date(getMeterTime()*1000) + " - SystemTime: " + new Date(getReceiveTime()) + " - Difference = " + diff/1000 + "s.");
		if(diff > minDiff){
			if(diff < maxDiff){
				getObjectFactory().getAace().getLogger().log(Level.INFO, "TimeDifference between boundry; Sending meter synchronization");
				getObjectFactory().sendSyncTime();
			}
			else{
				getObjectFactory().getAace().getLogger().severe("No clock sync, time difference exceeds allow maximum: " + diff/1000 + "s.");
				getObjectFactory().getAace().getLogger().severe("MeterTime: " + new Date(getMeterTime()*1000) + " - SystemTime: " + new Date(getReceiveTime()));
			}
			
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
//		System.out.println(cal.getTime());
		String hexString = "48ac1e0f";
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
