/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

/**
 * @author gna
 *
 */
public class Serialnumber extends AbstractActarisObject{
	
	private int trackingID;
	private String serialnumber;
	private String reqString;

	/**
	 * 
	 */
	public Serialnumber() {
		this(null);
	}
	
	public Serialnumber(ObjectFactory objectFactory){
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

	public String getSerialnumber() {
		return serialnumber;
	}

	public void setSerialnumber(String serialnumber) {
		this.serialnumber = serialnumber;
	}

	protected String getReqString() {
		return reqString;
	}
	
	private void setReqString(String reqString){
		this.reqString = reqString;
	}

}
