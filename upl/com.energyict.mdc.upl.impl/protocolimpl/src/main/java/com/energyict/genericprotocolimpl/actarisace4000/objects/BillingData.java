/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

/**
 * @author gna
 *
 */
public class BillingData extends AbstractActarisObject {
	
	private int DEBUG = 1;
	
	private int trackingID;
	private String reqString = null;
	
	private String subSet = null;
	
	private Date timeStamp = null;
	
	private int enabled = 0;
	private int interval = 0;
	private int numOfRecs = 0;
	
	private HashMap map = new HashMap();
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
		s.setTextContent(getObjectFactory().getAace().getPushedSerialnumber());
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
		s.setTextContent(getObjectFactory().getAace().getPushedSerialnumber());
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

	public void setElement(Element mdElement) {
		subSet = mdElement.getAttribute(XMLTags.bdAttr);
		
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.regData))
				setRegisterData(element.getTextContent());
			
			if(DEBUG >= 1)System.out.println(map);
		}
	}
	
	public void setConfig(Element mdElement) {
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billEnable))
				setEnabled(Integer.parseInt(element.getTextContent()));
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billInt))
				setInterval(Integer.parseInt(element.getTextContent()));
			if(element.getNodeName().equalsIgnoreCase(XMLTags.billNumb))
				setNumOfRecs(Integer.parseInt(element.getTextContent()));
		}
	}

	private void setRegisterData(String textContent) {
		int offset = 0;
		byte[] decoded = Base64.decode(textContent);
		if(DEBUG >=1)System.out.println(new String(decoded));
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		if(DEBUG >= 1)System.out.println(new Date(timeStamp));
		setTimeStamp(new Date(timeStamp));
		offset+=4;
		
		if(subSet != null){
			MeterReadingData mrd = new MeterReadingData();
			RegisterValue rv = null;
			RtuRegister register = null;
			ObisCode oc = null;
			Quantity q = null;
			if(subSet.indexOf("T") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.0.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
			
			if(subSet.indexOf("R") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.2.8.0.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
			
			if(subSet.indexOf("1") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.1.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
			
			if(subSet.indexOf("2") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.2.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
			
			if(subSet.indexOf("3") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.3.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
			
			if(subSet.indexOf("4") != -1){
				q = new Quantity(getNumberFromB64(decoded, offset, 4), Unit.get(BaseUnit.WATTHOUR));
				offset+=4;
				oc = ObisCode.fromString("1.0.1.8.4.255");
				map.put((String)oc.toString()+":"+Long.toString(getTimeStamp().getTime()), (Quantity)q);
			}
		}
	}

	private void setTimeStamp(Date date) {
		this.timeStamp = date;
	}
	
	private Date getTimeStamp(){
		return timeStamp;
	}

	public HashMap getMap() {
		return map;
	}

	public int getEnabled() {
		return enabled;
	}

	protected void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	protected int getInterval() {
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
		}
		return billingProfile;
	}
	
	public void constructProfile() {
		Iterator it = getMap().entrySet().iterator();
		while(it.hasNext()){
		    Map.Entry entry = (Map.Entry)it.next();
		    String key = (String)entry.getKey();
		    Long timeStamp = Long.valueOf(key.split(":")[1]);
		    ObisCode oc = ObisCode.fromString(key.split(":")[0]);
		    Quantity value = (Quantity)entry.getValue();
		}
	}
	
	
	public void createChannelInfo(){
		// TODO create the channelInfos from the configuration of the system
		getObjectFactory().getAace().getMeter().getChannels();
	}

}
