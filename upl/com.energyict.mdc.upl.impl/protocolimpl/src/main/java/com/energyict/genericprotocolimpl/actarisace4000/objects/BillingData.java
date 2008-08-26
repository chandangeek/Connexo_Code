/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

/**
 * @author gna
 *
 */
public class BillingData extends AbstractActarisObject {
	
	private int DEBUG = 0;
	
	private int trackingID;
	private String reqString = null;
	
	private String subSet = null;
	
	private Date timeStamp = null;
	
	private int enabled = -1;
	private int interval = -1;
	private int numOfRecs = -1;
	
	private ProfileData billingProfile;
	private ChannelInfo channelInfo;
	

	/**
	 * @param of
	 */
	public BillingData(ObjectFactory of) {
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
	
	public static void main(String[] args){
		String time;
		
		if(true){
		    Date date = new Date();
			time = "1219449600000";
			long t = Long.valueOf(time);
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(t);
			System.out.println(cal.getTime());
			Calendar cal2 = Calendar.getInstance();
			cal2.setTimeInMillis(t);
			System.out.println(cal2.getTime());
		}
		
		if(false){
			String str = "SHxmVgAAA9UAAAAAAAAA2AAAAv0AAAAAAAAAAAEB";
			String str2 = "SJJSAAAAA9UAAAAAAAAA2AAAAv0AAAAAAAAAAAER";
			String str3 = "1bDUQQAABucAAAAAAAACmAAABE8AAAAAAAAAAAEB";
			String str4 = "SKywAAAACE8AAAAAAAADBAAABUsAAAAAAAAAAAEA";
			
			
			ActarisACE4000 aace = new ActarisACE4000();
			ObjectFactory of = new ObjectFactory(aace);
			BillingData bd = new BillingData(of);
			
			bd.subSet = "TR1234";
			bd.setTrackingID(-1);
//		bd.setRegisterData(str);
//		bd.setRegisterData(str2);
			try {
				bd.setRegisterData(str4);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(bd.getProfileData());
		}
	}
	
	/**
	 * Request all billing data
	 */
	public void prepareXML(){
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
		
		Element bd = doc.createElement(XMLTags.reqAllBD);
		md.appendChild(bd);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	/**
	 * Request billing data from requested date
	 * @param from
	 */
	public void prepareXML(Date from){
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
		
		Element bd = doc.createElement(XMLTags.reqBDrange);
		bd.setTextContent(Long.toHexString(from.getTime()/1000)+Long.toHexString(System.currentTimeMillis()/1000));
		md.appendChild(bd);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	
	public void prepareXMLConfig(int enabled, int intervals, int numb){
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
		Element ps = doc.createElement(XMLTags.billingConf);
		cf.appendChild(ps);
		Element enable = doc.createElement(XMLTags.billEnable);
		enable.setTextContent(Integer.toString(enabled, 16));
		ps.appendChild(enable);
		Element bi = doc.createElement(XMLTags.billInt);
		bi.setTextContent(Integer.toString(intervals, 16));
		ps.appendChild(bi);
		Element bn = doc.createElement(XMLTags.billNumb);
		bn.setTextContent(Integer.toString(numb, 16));
		ps.appendChild(bn);
			
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}

	public void setElement(Element mdElement) throws DOMException, IOException {
		subSet = mdElement.getAttribute(XMLTags.bdAttr);
		
		NodeList list = mdElement.getChildNodes();
		if(DEBUG >= 2 )System.out.println("Billing:");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.regData))
				setRegisterData(element.getTextContent());
		}
	}
	
	public void setConfig(Element mdElement) {
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billEnable))
				setEnabled(Integer.parseInt(element.getTextContent()));
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billInt))
				setInterval(Integer.parseInt(element.getTextContent(),16));
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billNumb))
				setNumOfRecs(Integer.parseInt(element.getTextContent(),16));
		}
	}

	private void setRegisterData(String textContent) throws IOException {
		
		int offset = 0;
		byte[] decoded = Base64.decode(textContent);
		if(DEBUG >=1)System.out.println(new String(decoded));
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		if(DEBUG >= 2){
			System.out.print(timeStamp);
			System.out.print(" - " + new Date(timeStamp) + " - ");
		}
		setTimeStamp(new Date(timeStamp));
		offset+=4;
		
		IntervalData id = null;
		
		if(subSet != null){
			MeterReadingData mrd = new MeterReadingData();
			RegisterValue rv = null;
			RtuRegister register = null;
			ObisCode oc = null;
			Quantity q = null;
			id = new IntervalData(getTimeStamp());
			if(subSet.indexOf("T") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.0.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			if(subSet.indexOf("R") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.2.8.0.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			if(subSet.indexOf("1") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.1.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			if(subSet.indexOf("2") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.2.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			if(subSet.indexOf("3") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.3.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			if(subSet.indexOf("4") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.4.255");
				id.addValue(q.getAmount());
			} else id.addValue(0);
			
			getProfileData().addInterval(id);
		}
		
		if(getTrackingID() != -1){
			getObjectFactory().sendAcknowledge(getTrackingID());
			getObjectFactory().getAace().getLogger().log(Level.INFO, "Sent billingdata ACK for tracknr: " + getTrackingID());
		}
	}
	
	private void setTimeStamp(Date date) {
		this.timeStamp = date;
	}
	
	private Date getTimeStamp(){
		return timeStamp;
	}

	public int getEnabled() {
		return enabled;
	}

	protected void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public int getInterval() {
		return interval;
	}

	protected void setInterval(int interval) {
		this.interval = interval;
	}

	protected int getNumOfRecs() {
		return numOfRecs;
	}

	protected void setNumOfRecs(int numOfRecs) {
		this.numOfRecs = numOfRecs;
	}

	public ProfileData getProfileData(){
		if(billingProfile == null){
			billingProfile = new ProfileData();
			createChannelInfo();
		}
		return billingProfile;
	}
	
	public void createChannelInfo(){
		ArrayList result = new ArrayList();
		result.add(new ChannelInfo(0, 1, "Total Forward Register", Unit.get(BaseUnit.WATTHOUR)));
		result.add(new ChannelInfo(1, 2, "Total Reverse Register", Unit.get(BaseUnit.WATTHOUR)));
		result.add(new ChannelInfo(2, 3, "Rate 1 Register", Unit.get(BaseUnit.WATTHOUR)));
		result.add(new ChannelInfo(3, 4, "Rate 2 Register", Unit.get(BaseUnit.WATTHOUR)));
		result.add(new ChannelInfo(4, 5, "Rate 3 Register", Unit.get(BaseUnit.WATTHOUR)));
		result.add(new ChannelInfo(5, 6, "Rate 4 Register", Unit.get(BaseUnit.WATTHOUR)));
		getProfileData().setChannelInfos(result);
	}

}
