/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

/**
 * @author gna
 *
 */
public class Acknowledge extends AbstractActarisObject{

	private int trackingID;
	private String reqString = null;
	
	/**
	 * 
	 */
	public Acknowledge() {
		this(null);
	}
	
	public Acknowledge(ObjectFactory objectFactory){
		super(objectFactory);
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

	protected String getReqString() {
		return reqString;
	}

	private void setReqString(String reqString){
		this.reqString = reqString;
	}
}
