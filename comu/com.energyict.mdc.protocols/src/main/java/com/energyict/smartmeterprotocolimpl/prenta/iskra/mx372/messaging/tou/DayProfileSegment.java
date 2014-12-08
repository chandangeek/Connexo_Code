package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

public class DayProfileSegment {
	
	protected final static String ELEMENTNAME = "dayProfileSegment";
	protected final static String STARTNAME = "startTime";
	protected final static String ACTIONNAME = "action";
	
	private OctetString startTime = new OctetString();
	private ActionItem action = new ActionItem();

	public DayProfileSegment() {
		super();
	}
	
	public DayProfileSegment(String startTime,
			String scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}

	public DayProfileSegment(byte[] startTime,
			byte[] scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}
        
	public String getStartTime() {
		return startTime.convertOctetStringToString();
	}

	public void setStartTime(String startTime) {
		this.startTime = new OctetString(startTime);
	}

	public byte[] getStartTimeOctets() {
		return startTime.getOctets();
	}

	public void setStartTimeOctects(byte[] startTime) {
		this.startTime = new OctetString(startTime);
	}

	public ActionItem getAction() {
		return action;
	}

	public void setAction(ActionItem action) {
		this.action = action;
	}

}

