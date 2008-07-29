/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;

/**
 * @author gna
 *
 */
public class ObjectFactory {
	
	private ActarisACE4000 aace;
	private CreateXMLString xmlString;
	private FirmwareVersion firmwareVersion = null;

	/**
	 * 
	 */
	public ObjectFactory(ActarisACE4000 aace) {
		this.aace = aace;
		this.xmlString = new CreateXMLString(this);
	}
	
	public FirmwareVersion requestFirmwareVersion(){
		if(firmwareVersion == null){
			firmwareVersion = new FirmwareVersion(this);
			firmwareVersion.setTrackingID(getAace().getTracker());
			firmwareVersion.request(firmwareVersion.getRequestString());
		}
		return firmwareVersion;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public CreateXMLString getXmlString() {
		return xmlString;
	}

	public void setXmlString(CreateXMLString xmlString) {
		this.xmlString = xmlString;
	}

	public ActarisACE4000 getAace() {
		return aace;
	}

	public void setAace(ActarisACE4000 aace) {
		this.aace = aace;
	}

}
