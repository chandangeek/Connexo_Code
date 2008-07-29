/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

/**
 * @author gna
 *
 */
public class FirmwareVersion extends AbstractActarisObject{
	
	private String firmwareVersion = null;
	private int trackingID;

	/**
	 * empty constructor
	 */
	public FirmwareVersion() {
		this(null);
	}
	
	public FirmwareVersion(ObjectFactory objectFactory) {
		super(objectFactory);
	}

	public String getRequestString(){
		String msg = getObjectFactory().getXmlString().createRequest("qV", getTrackingID());
		return msg;
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

}
