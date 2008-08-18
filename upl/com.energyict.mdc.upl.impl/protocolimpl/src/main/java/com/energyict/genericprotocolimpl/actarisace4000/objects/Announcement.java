/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.genericprotocolimpl.actarisace4000.objects.tables.MeterTypeTable;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class Announcement extends AbstractActarisObject {
	
	private String reqString = null;
	private int trackingID;
	
	private String ICID 	= null;		// SIM card ICID number
	private String type		= null;		// meter type code 	
	private int sStrength 	= 0;		// GSM signal strength
	private int bStationID	= 0;		// GSM cell base station ID
	private String opName	= null;		// GSM opertor name
	private String cString	= null;		// meter codification string
	

	/**
	 * @param of
	 */
	public Announcement(ObjectFactory of) {
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
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	protected void setElement(Element mdElement) {
		NodeList list = mdElement.getElementsByTagName(XMLTags.announce);
		
		// TODO only create slave meters!!!
		getObjectFactory().getAace().findOrCreateMeter();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			
			if(element.getNodeName().equalsIgnoreCase(XMLTags.icid))
				setICID(element.getTextContent());
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.type))
				setType(MeterTypeTable.meterType[Integer.parseInt(element.getTextContent())]);
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.sStrength))
				setSStrength(Integer.parseInt(element.getTextContent()));
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.bStation))
				setBStationID(Integer.parseInt(element.getTextContent()));
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.operatorName))
				setOpName(element.getTextContent());
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.codString))
				setCString(element.getTextContent());
			else if(element.getNodeName().equalsIgnoreCase(XMLTags.curReading)){
				getObjectFactory().getCurrentReadings().setElement(element);
			}
			
		}
	}

	protected String getICID() {
		return ICID;
	}

	protected void setICID(String icid) {
		ICID = icid;
	}

	protected String getType() {
		return type;
	}

	protected void setType(String type) {
		this.type = type;
	}

	protected int getSStrength() {
		return sStrength;
	}

	protected void setSStrength(int strength) {
		sStrength = strength;
	}

	protected int getBStationID() {
		return bStationID;
	}

	protected void setBStationID(int stationID) {
		bStationID = stationID;
	}

	protected String getOpName() {
		return opName;
	}

	protected void setOpName(String opName) {
		this.opName = opName;
	}

	protected String getCString() {
		return cString;
	}

	protected void setCString(String string) {
		cString = string;
	}
}
